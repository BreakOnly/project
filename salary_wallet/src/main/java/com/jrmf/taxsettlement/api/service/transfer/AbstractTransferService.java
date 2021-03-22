
package com.jrmf.taxsettlement.api.service.transfer;

import com.alibaba.fastjson.JSON;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.domain.*;
import com.jrmf.domain.dto.SubcontractRouterQueryDTO;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.SendSmsHistoryRecordDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.*;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.util.ChannelRelatedCache;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.SalaryConfigUtil;
import com.jrmf.utils.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractTransferService<P extends AbstractTransferServiceParams, T extends AbstractTransferServiceAttachment>
        implements Action<P, T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransferService.class);
    @Autowired
    private MerchantAPITransferRecordDao transferRecordDao;
    @Autowired
    private APIDockingManager apiDockingManager;
    @Autowired
    private TaxSettlementInertnessDataCache dataCache;
    @Autowired
    private TransferBankDao transferBankDao;
    @Autowired
    private AgreementTemplateService agreementTemplateService;
    @Autowired
    private SalaryConfigUtil conf;
    @Autowired
    private ChannelRelatedCache channelRelatedCache;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private CalculationFeeService calculationFeeService;
    @Autowired
    private CustomLimitConfService customLimitConfService;
    @Autowired
    private UserCommission2Dao userCommissionDao2;
    @Autowired
    private CustomCompanyRateConfService customCompanyRateConfService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private UsersAgreementService usersAgreementService;
    @Autowired
    private CustomLdConfigService customLdConfigService;
    @Autowired
    private QbBlackUsersService blackUsersService;
    @Autowired
    private WhiteUserService whiteUserService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private UtilCacheManager utilCacheManager;
    @Autowired
    private SendSmsHistoryRecordDao sendSmsHistoryRecordDao;
    @Autowired
    ChannelInterimBatchService2 channelInterimBatchService2;
    @Autowired
    private ChannelCustomService channelCustomService;

    @Value("${jrmfMonthAmountLimit}")
    private String jrmfMonthAmountLimit;
    @Value("${jrmfLimitState}")
    private Integer jrmfLimitState;

    @Override
    public ActionResult<T> execute(P actionParams) {

        logger.debug("transfer service[{}] start with params[{}]", this.getActionType(),
                JSON.toJSONString(actionParams));

        String merchantId = actionParams.getMerchantId();
        String requestNo = actionParams.getRequestNo();

        MerchantAPIDockingConfig dockingConfig = apiDockingManager.getMerchantAPIDockingConfig(merchantId);

        logger.debug("get related merchant api config[{}]", JSON.toJSONString(dockingConfig));

        APIDockingMode apiDockingMode = dockingConfig.getAPIDockingMode();
        if (APIDockingMode.TEST.equals(apiDockingMode) && new BigDecimal(actionParams.getAmount())
                .compareTo(new BigDecimal(conf.getTestModeAmountLimit())) > 0) {
            logger.error("request beyond permitted is:{}", JSON.toJSONString(actionParams));
            throw new APIDockingException(APIDockingRetCodes.AMOUNT_BEYOND_LIMIT.getCode(),
                    new StringBuilder(merchantId).append("-").append(apiDockingMode.name()).toString());
        }

        ChannelRelated channelWithContract = channelRelatedCache.getChannelRelated(merchantId, actionParams.getTransferCorpId());

        //修正feeRuleType显示
        CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfService.getConfByCustomKeyAndCompanyId(merchantId, actionParams.getTransferCorpId());
        if (customCompanyRateConf == null || customCompanyRateConf.getServiceFeeType() == null) {
            logger.error("request--T012--无对应费率档位配置信息");
            throw new APIDockingException(APIDockingRetCodes.RATECONF_NO_NOT_EXISTED.getCode(),
                    new StringBuilder(APIDockingRetCodes.RATECONF_NO_NOT_EXISTED.getDesc()).toString());
        } else {
            channelWithContract.setFeeRuleType(customCompanyRateConf.getFeeRuleType() + "");
        }

        boolean lockFlag = false;
        Map<String, Object> params = new HashMap<String, Object>();
        String dealNo = orderNoUtil.getChannelSerialno();
        ActionResult<T> result = null;

        try {

            lockFlag = utilCacheManager.lockWithTimeout(actionParams.getCertificateNo(), 180000, 60000);
            if (lockFlag) {

                //增加一个原交易金额，用于后续如果存在用户承担手续费时记录原交易金额
                actionParams.setSourceAmount(actionParams.getAmount());
                Map<String, String> feeInfo = checkAndCountFeeBeforeTransfer(channelWithContract, actionParams);
                feeInfo.put("merchantId", channelWithContract.getMerchantId());

                logger.debug("count and get fee info:{}", JSON.toJSONString(feeInfo));

                params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
                params.put(APIDockingRepositoryConstants.BATCH_NO, actionParams.getBatchNo());
                params.put(APIDockingRepositoryConstants.REQUEST_NO, requestNo);
                params.put(APIDockingRepositoryConstants.TRANSFER_CORP_ID, actionParams.getTransferCorpId());
                params.put(APIDockingRepositoryConstants.PARTNER_ID, actionParams.getPartnerId());
                params.put(APIDockingRepositoryConstants.STATUS, TransferStatus.ACCEPTED.getCode());
                String notifyUrl = actionParams.getNotifyUrl();
                if (notifyUrl == null || "".equals(notifyUrl)) {
                    notifyUrl = dockingConfig.getNotifyUrl();
                }
                params.put(APIDockingRepositoryConstants.NOTIFY_URL, notifyUrl);

                params.put(APIDockingRepositoryConstants.DEAL_NO, dealNo);

                try {
                    transferRecordDao.addNewTransferRequest(params);
                } catch (Exception e) {
                    logger.error("error occured add new transfer request", e);
                    throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_EXISTED.getCode(),
                            new StringBuilder(merchantId).append("-").append(requestNo).toString());
                }

                logger.debug("new transfer request added");

                try {
                    SubcontractRouterQueryDTO subcontractRouterQueryDTO =new SubcontractRouterQueryDTO();
                    subcontractRouterQueryDTO.setCustomKey(actionParams.getMerchantId());
                    subcontractRouterQueryDTO.setCompanyId(Integer.valueOf(actionParams.getTransferCorpId()));
                    List<SubcontractRouter> subcontractRouterList =channelInterimBatchService2.listSubcontractRouter(subcontractRouterQueryDTO);
                    if (subcontractRouterList!=null && subcontractRouterList.size()>0){
                        throw new APIDockingException(APIDockingRetCodes.UNSUPPORT_MERCHANT_MUTIL_SUBCONTRACT_COMPANY.getCode(),
                            APIDockingRetCodes.UNSUPPORT_MERCHANT_MUTIL_SUBCONTRACT_COMPANY.getDesc());
                    }
                } catch (NumberFormatException e) {
                    logger.error("查询商户完善服务公司列表异常",e);
                }

                result = transfer(dealNo, actionParams, feeInfo, channelWithContract);

                logger.debug("get result:{}", JSON.toJSONString(result));

                params.put(APIDockingRepositoryConstants.RET_CODE, result.getRetCode());
                params.put(APIDockingRepositoryConstants.RET_MSG, result.getRetMsg());
                transferRecordDao.updateTransferRequest(params);

            } else {
                throw new APIDockingException(APIDockingRetCodes.SAME_CERTID_PAY.getCode(),
                        APIDockingRetCodes.SAME_CERTID_PAY.getDesc() + ":" + actionParams.getCertificateNo());
            }

        } catch (Exception e) {
            params.put(APIDockingRepositoryConstants.STATUS, TransferStatus.FAIL_TO_ACCEPT.getCode());
            if (e instanceof APIDockingException) {
                APIDockingException apiDockingError = (APIDockingException) e;
                params.put(APIDockingRepositoryConstants.RET_CODE, apiDockingError.getErrorCode());
                params.put(APIDockingRepositoryConstants.RET_MSG, apiDockingError.getErrorMsg());
            } else {
                params.put(APIDockingRepositoryConstants.RET_CODE, CommonRetCodes.UNEXPECT_ERROR.getCode());
                params.put(APIDockingRepositoryConstants.RET_MSG, e.getMessage());
            }
            transferRecordDao.updateTransferRequest(params);
            throw e;
        } finally {
            utilCacheManager.releaseLock(actionParams.getCertificateNo(), lockFlag);
            logger.debug("update request status and return");
        }

        return result;
    }

    protected Map<String, String> checkAndCountFeeBeforeTransfer(ChannelRelated channelWithContract, P actionParams) {
        if (actionParams.getFeeInfo() != null) {
            return actionParams.getFeeInfo();
        }

        String merchantId = actionParams.getMerchantId();

        if (channelWithContract == null) {
            throw new APIDockingException(APIDockingRetCodes.NO_CONTRACT_WITH_AGENT.getCode(), merchantId);
        }

        String certificateNo = actionParams.getCertificateNo();
        String companyId = channelWithContract.getCompanyId();
        String customkey = channelWithContract.getOriginalId();

        QbBlackUsers blackUsers = new QbBlackUsers();
        blackUsers.setUserName(actionParams.getName());
        blackUsers.setCustomkey(customkey);
        blackUsers.setCertId(certificateNo);
        blackUsers.setDocumentType(CertType.ID_CARD.getCode());

        //黑名单校验
        int isBlack = blackUsersService.countExistByCertIdAndName(blackUsers);
        if (isBlack > 0) {
            throw new APIDockingException(APIDockingRetCodes.BLACK_USER.getCode(), APIDockingRetCodes.BLACK_USER.getDesc() + ":" + certificateNo);
        }

        ChannelCustom custom = channelCustomService.getCustomByCustomkey(customkey);
        if (custom.getBusinessPlatformId() != null
            && CommonString.JRMF_PLATFORM_LIMIT_OPEN == jrmfLimitState
            && CommonString.JRMF_PLATFORM_ID == custom.getBusinessPlatformId()) {
            String monthSumAmount = userCommissionDao2
                .getSignleCommissionMounthSumAmonutByPlatformId(CommonString.JRMF_PLATFORM_ID,
                    certificateNo);
            monthSumAmount = ArithmeticUtil.addStr(monthSumAmount, actionParams.getAmount());
            if (ArithmeticUtil.compareTod(monthSumAmount, jrmfMonthAmountLimit) > 0){
                throw new APIDockingException(APIDockingRetCodes.PLATFORM_MONTH_AMOUNT_BEYOND_LIMIT.getCode(), APIDockingRetCodes.PLATFORM_MONTH_AMOUNT_BEYOND_LIMIT.getDesc());
            }
        }

        //白名单校验
        Integer isWhiteUser = 0;
        WhiteUser whiteUser = new WhiteUser();
        whiteUser.setCertId(certificateNo);
        whiteUser.setDocumentType(1);
        whiteUser.setCustomkey(customkey);
        whiteUser.setCompanyId(companyId);
        isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);

        if (isWhiteUser < 1) {
            /**
             * @Description 检测用户名
             **/
            Integer count = sendSmsHistoryRecordDao.checkByUserName(actionParams.getName(),customkey);
            if(null!=count && count>0){
                throw new APIDockingException(APIDockingRetCodes.INDUSTRIAL_COMMERCIAL_PUBLICITY_PERSONNEL_SAME_NAME.getCode(), APIDockingRetCodes.INDUSTRIAL_COMMERCIAL_PUBLICITY_PERSONNEL_SAME_NAME.getDesc());
            }
            Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
            //格式校验之后加上下发年龄校验
            String msg = StringUtil.checkAge(certificateNo, company.getMinAge(), company.getMaxAge());
            if (!StringUtil.isEmpty(msg)) {
                throw new APIDockingException(APIDockingRetCodes.ID_ERROR_OR_AGE_MAX.getCode(), msg);
            }

            String userName = actionParams.getName();

            List<User> userData = transferBankDao.getUserByCertId(certificateNo);
            if (userData != null && userData.size() > 0) {
                User user = userData.get(0);
                int checkTruth = user.getCheckTruth();
                String name = user.getUserName();
                if(checkTruth == 1 && (!userName.equals(name))){
                    throw new APIDockingException(APIDockingRetCodes.REAL_NAME_VERIFY_FAILED.getCode(), userName + "-" + certificateNo);
                }
            }

            Map<String, Object> paramMap = new HashMap<>(12);
            paramMap.put("companyId", companyId);
            paramMap.put("originalId", customkey);
            paramMap.put("agreementPayment", "1");
            List<AgreementTemplate> agreementTemplateList = agreementTemplateService
                    .getAgreementTemplateByParam(paramMap);
            int templateCount = agreementTemplateList == null ? 0 : agreementTemplateList.size();

            int whiteListCount = usersAgreementService.getWhiteListCount(customkey, companyId, certificateNo);
            //签约白名单用户
            if (templateCount > 0 && templateCount != whiteListCount) {
                paramMap.clear();
                paramMap.put("certId", certificateNo);
                paramMap.put("userName", userName);
                paramMap.put("signStatus", "5");
                paramMap.put("customKey", customkey);
                paramMap.put("companyId", companyId);
                String agreementTemplateId = "";
                if (agreementTemplateList != null && agreementTemplateList.size() > 0) {
                    for (int ai = 0; ai < agreementTemplateList.size(); ai++) {
                        agreementTemplateId = agreementTemplateList.get(ai).getId() + "," + agreementTemplateId;
                    }
                    agreementTemplateId = agreementTemplateId.substring(0, agreementTemplateId.lastIndexOf(","));
                }

                paramMap.put("agreementTemplateId", agreementTemplateId);
                int userAgreeCount = agreementTemplateService.getUserAgreementCountByParam(paramMap);
                if (userAgreeCount != templateCount) {
                    throw new APIDockingException(APIDockingRetCodes.NO_CONTRACT_WITH_USER.getCode(), userName);
                }
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("customkey", customkey);
            params.put("companyId", companyId);
            CustomLimitConf customLimitConf = customLimitConfService.getCustomLimitConf(params);
            //单日下发累计金额
            CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId,
                    customkey,
                    certificateNo);
            CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
            logger.info("------下发限额判断--联动--- customkey:{},companyId:{},conLdConfig:{}", customkey, companyId, conLdConfig);
            if (conLdConfig == null) {
                String companySingleMonthLimit = company.getSingleMonthLimit();
                String companySingleQuarterLimit = company.getSingleQuarterLimit();
                CustomPaymentTotalAmount companyPaymentTotalAmount = customLimitConfService.queryCompanyPaymentTotalAmount(companyId, certificateNo);

                //增加服务公司当月及当季度限额校验
                if (!StringUtil.isEmpty(companySingleMonthLimit)) {
                    String companyMonthTotal = companyPaymentTotalAmount.getCurrentMonthTotalStr();
                    logger.info("----------服务公司月限额判断--服务公司当月累计monthTotal:" + companyMonthTotal);
                    String companyMonthAllAmount = ArithmeticUtil.addStr(companyMonthTotal, actionParams.getAmount());
                    logger.info("----------服务公司月限额判断--服务公司当月companySingleMonthLimit:" + companySingleMonthLimit + "---累计当月总金额+本次金额companyMonthAllAmount：" + companyMonthAllAmount);
                    if (ArithmeticUtil.compareTod(companyMonthAllAmount, companySingleMonthLimit) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.COMPANY_MONTH_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(companyMonthAllAmount).append(">").append(companySingleMonthLimit).toString());
                    }
                }

                //获取商户下发公司关联关系
                ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);

                //实际下发公司id
                String realCompanyId = company.getRealCompanyId();
                //配置实际下发公司id，并且未开启下发公司白名单功能则进入校验
                if (!StringUtil.isEmpty(realCompanyId) && (channelRelated.getRealCompanyOperate() == null || channelRelated.getRealCompanyOperate() != 1)) {
                    logger.info("进入实际下发公司限额校验");
                    //进入实际下发公司限额校验
                    //获取实际下发公司累计额度
                    CustomPaymentTotalAmount realCompanyPaymentTotalAmount = customLimitConfService.queryCompanyPaymentTotalAmount(realCompanyId, certificateNo);
                    if (realCompanyPaymentTotalAmount == null) {
                        customLimitConfService.initCustomPaymentTotalAmount(realCompanyId, customkey, certificateNo);
//                        realCompanyPaymentTotalAmount = customLimitConfService.queryCompanyPaymentTotalAmount(realCompanyId, certificateNo);
                    }
                    //获取使用实际下发公司的累计额度
//                    Map<String, Object> userRealCompanyPaymentTotalAmount = customLimitConfService.queryCompanyPaymentTotalAmountByRealCompany(realCompanyId, certificateNo);
//                    String realCompanyMonthTotal = userCommissionDao2.getRealCommissionMounthSumAmonutByCertId(realCompanyId, certificateNo);
//                    String useRealCompanyMonthTotal = String.valueOf(userRealCompanyPaymentTotalAmount.get("currentMonthTotalStr"));
                    String realAlreadyCompanyMonthTotal = userCommissionDao2.getRealCommissionMounthSumAmonutByCertId(realCompanyId, certificateNo);
                    String realAllCompanyMonthTotal = ArithmeticUtil.addStr(realAlreadyCompanyMonthTotal, actionParams.getAmount());
                    Company realCompany = companyService.getCompanyByUserId(Integer.parseInt(realCompanyId));
                    String realCompanySingleMonthLimit = realCompany.getSingleMonthLimit();
                    logger.info("----------实际下发公司月限额判断--服务公司月累计monthTotal:" + realAlreadyCompanyMonthTotal);
                    logger.info("----------服务公司月限额判断--实际下发公司当月companySingleMonthLimit:" + realCompanySingleMonthLimit + "---累计当月总金额+本次金额companyMonthAllAmount：" + realAllCompanyMonthTotal);
                    if (ArithmeticUtil.compareTod(realAllCompanyMonthTotal, realCompanySingleMonthLimit) > 0) {
                        logger.info("超出实际下发公司限额");
                        //抛出超出实际下发公司限额异常
                        throw new APIDockingException(APIDockingRetCodes.REAL_COMPANY_QUARTER_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(realAllCompanyMonthTotal).append(">").append(realCompanySingleMonthLimit).toString());
                    }
                }

                //增加服务公司当月及当季度限额校验
                if (!StringUtil.isEmpty(companySingleQuarterLimit)) {
                    String companyQuarterTotal = companyPaymentTotalAmount.getCurrentQuarterTotalStr();
                    logger.info("----------服务公司季度限额判断--服务公司季度累计quarterTotal:" + companyQuarterTotal);
                    String companyQuarterAllAmount = ArithmeticUtil.addStr(companyQuarterTotal, actionParams.getAmount());
                    logger.info("----------服务公司月限额判断--服务公司当月companySingleQuarterLimit:" + companySingleQuarterLimit + "---累计季度总金额+本次金额companyQuarterAllAmount：" + companyQuarterAllAmount);
                    if (ArithmeticUtil.compareTod(companyQuarterAllAmount, companySingleQuarterLimit) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.COMPANY_QUARTER_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(companyQuarterAllAmount).append(">").append(companySingleQuarterLimit).toString());
                    }
                }
            }
            if (customLimitConf != null) {
                String transAmount = actionParams.getAmount();
                BigDecimal transAmountBigDecimal = new BigDecimal(transAmount);
                String singleOrderLimit = customLimitConf.getSingleOrderLimit();
                String singleDayLimit = customLimitConf.getSingleDayLimit();
                String singleMonthLimit = customLimitConf.getSingleMonthLimit();
                String singleQuarterLimit = customLimitConf.getSingleQuarterLimit();

                //单笔金额
                logger.info("-----单笔限额判断----单笔限额singleOrderLimit：" + singleOrderLimit + "单笔下发金额transAmount：" + transAmount);
                if (!StringUtil.isEmpty(singleOrderLimit)) {
                    BigDecimal singleOrderLimitBigDecimal = new BigDecimal(singleOrderLimit);
                    if (transAmountBigDecimal.compareTo(singleOrderLimitBigDecimal) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.ORDER_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(transAmount).append(">").append(singleOrderLimit).toString());
                    }
                }


                BigDecimal todayTotal = new BigDecimal(customPaymentTotalAmount.getTodayTotal());
                String daySumAmonut = todayTotal.divide(new BigDecimal(100)).toString();

                BigDecimal daySumAmonutBigDecimal = new BigDecimal(daySumAmonut);
                BigDecimal dayTranSumAmonutBigDecimal = transAmountBigDecimal.add(daySumAmonutBigDecimal);
                String daySumAmount = dayTranSumAmonutBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                logger.info("-----单日限额判断----单日限额：" + singleDayLimit + "单日下发金额daySumAmonut + 本次金额transAmount：" + daySumAmount);
                if (!StringUtil.isEmpty(singleDayLimit)) {
                    BigDecimal singleDayLimitBigDecimal = new BigDecimal(singleDayLimit);
                    if (dayTranSumAmonutBigDecimal.compareTo(singleDayLimitBigDecimal) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.DAY_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(daySumAmount).append(">").append(singleDayLimitBigDecimal).toString());
                    }
                }

                //月下发累计金额
                BigDecimal currentMonthTotal = new BigDecimal(customPaymentTotalAmount.getCurrentMonthTotal());
                String mounthSumAmonut = currentMonthTotal.divide(new BigDecimal(100)).toString();

                BigDecimal amountTempDecimal = new BigDecimal(transAmount);
                BigDecimal mounthSumAmonutDecimal = new BigDecimal(mounthSumAmonut);
                BigDecimal mounthAndTempSumDecimal = mounthSumAmonutDecimal.add(amountTempDecimal);
                String mounthAndTempSum = mounthAndTempSumDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("-----单月限额判断--单月限额singleMonthLimit：" + singleMonthLimit + "单月下发金额mounthAndTempSum + 本次金额transAmount：" + mounthAndTempSum);
                if (!StringUtil.isEmpty(singleMonthLimit)) {
                    BigDecimal singleMonthLimitDecimal = new BigDecimal(singleMonthLimit);
                    if (mounthAndTempSumDecimal.compareTo(singleMonthLimitDecimal) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.MONTH_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(mounthAndTempSum).append(">").append(singleMonthLimitDecimal).toString());
                    }
                }

                //季度下发累计金额
                BigDecimal currentQuarterTotal = new BigDecimal(customPaymentTotalAmount.getCurrentQuarterTotal());
                String quarterSumAmonut = currentQuarterTotal.divide(new BigDecimal(100)).toString();

                BigDecimal quarterSumAmonutDecimal = new BigDecimal(quarterSumAmonut);
                BigDecimal quarterAndTempSumDecimal = quarterSumAmonutDecimal.add(amountTempDecimal);
                String quarterAndTempSum = quarterAndTempSumDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("-----季度限额判断--季度限额singleQuarterLimit：" + singleQuarterLimit + "季度下发金额quarterAndTempSum + 本次金额transAmount：" + quarterAndTempSum);
                if (!StringUtil.isEmpty(singleQuarterLimit)) {
                    BigDecimal singleQuarterLimitDecimal = new BigDecimal(singleQuarterLimit);
                    if (quarterAndTempSumDecimal.compareTo(singleQuarterLimitDecimal) > 0) {
                        throw new APIDockingException(APIDockingRetCodes.QUARTER_AMOUNT_BEYOND_LIMIT.getCode(),
                                new StringBuilder(quarterAndTempSum).append(">").append(singleQuarterLimitDecimal).toString());
                    }
                }
            }
        }

        boolean autoSupplement = customLimitConfService.autoSupplement(companyId, customkey);
        Map<String, String> feeInfo = countServiceFee(merchantId, channelWithContract.getCompanyId(), certificateNo,
                channelWithContract.getProfiltLower(), channelWithContract.getProfiltUpper(),
                channelWithContract.getServiceRates(), channelWithContract.getUpperServiceRates(),
                actionParams.getSourceAmount(), channelWithContract.getFeeRuleType(), autoSupplement);
        return feeInfo;
    }

    public Map<String, String> countServiceFee(String merchantId, String companyId, String certificateNo,
                                               String profiltLower, String profiltUpper, String serviceRates, String upperServiceRates, String transAmount,
                                               String ruleType, boolean autoSupplement) {


        String sumFee = "0.00";
        String profiltRates = "0.00";
        String profitAmount = "0.00";
        String supplementFee = "0.00";
        String supplementAmount = "0.00";
        String calculationRates = "0.00";
        String rateInterval = "";

        Map<String, String> commissionFeeInfoMap = new HashMap<String, String>();

        Map<String, Object> params = new HashMap<String, Object>();
        //		String mounthSumAmonut = userCommissionDao2.getSumAmountOfMonthByCertId(certificateNo, merchantId, companyId);
        CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId,
                merchantId,
                certificateNo);
        BigDecimal currentMonthTotal = new BigDecimal(customPaymentTotalAmount.getCurrentMonthTotal());
        String mounthSumAmonut = currentMonthTotal.divide(new BigDecimal(100)).toString();

        BigDecimal amountTempDecimal = new BigDecimal(transAmount);
        BigDecimal mounthSumAmonutDecimal = new BigDecimal(mounthSumAmonut);
        BigDecimal mounthAndTempSumDecimal = mounthSumAmonutDecimal.add(amountTempDecimal);
        String mounthAndTempSum = mounthAndTempSumDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        params.put("originalId", merchantId);
        params.put("companyId", companyId);
        params.put("sumAmount", mounthAndTempSum);
        CustomCompanyRateConf rateConfAll = customCompanyRateConfService.getCustomCompanyRateConf(params);
        if (rateConfAll == null) {
            throw new APIDockingException(APIDockingRetCodes.RATECONF_NO_NOT_EXISTED.getCode(),
                    new StringBuilder(APIDockingRetCodes.RATECONF_NO_NOT_EXISTED.getDesc()).toString());
        }

        if (rateConfAll.getServiceFeeType() != null) {
            Map<String, String> commissionFeeInfoRespMap = calculationFeeService.calculationFeeInfo("api", merchantId, companyId, null, null, certificateNo, transAmount, autoSupplement, null, rateConfAll.getServiceFeeType());
            if (rateConfAll.getServiceFeeType() == ServiceFeeType.ISSUE.getCode() || rateConfAll.getServiceFeeType() == ServiceFeeType.PERSON.getCode()) {
                sumFee = commissionFeeInfoRespMap.get("sumFee");
                profiltRates = commissionFeeInfoRespMap.get("profiltRates");
                profitAmount = commissionFeeInfoRespMap.get("profitAmount");
                supplementFee = commissionFeeInfoRespMap.get("supplementFee");
                supplementAmount = commissionFeeInfoRespMap.get("supplementAmount");

                if (rateConfAll.getServiceFeeType() == ServiceFeeType.PERSON.getCode()) {
                    transAmount = ArithmeticUtil.subStr(transAmount, sumFee, 2);
                }
            }

            calculationRates = commissionFeeInfoRespMap.get("calculationRates");
            rateInterval = commissionFeeInfoRespMap.get("rateInterval");

        }

        commissionFeeInfoMap.put(AbstractTransferServiceParams.ARRIVL_AMOUNT, transAmount);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.SUM_FEE, sumFee);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.PROFIT_AMOUNT, profitAmount);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.SUPPLEMENT_FEE, supplementFee);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.SUPPLEMENT_AMOUNT, supplementAmount);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.CALCULATION_RATES, calculationRates);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.PROFILT_RATES, profiltRates);
        commissionFeeInfoMap.put(AbstractTransferServiceParams.RATE_INTERVAL, rateInterval);

        return commissionFeeInfoMap;
    }

    protected String getNameFromCache(String id) {
        return dataCache.getNameOfId(id);
    }

    protected abstract ActionResult<T> transfer(String dealNo, P actionParams, Map<String, String> feeInfo,
                                                ChannelRelated channelWithContract);
}
