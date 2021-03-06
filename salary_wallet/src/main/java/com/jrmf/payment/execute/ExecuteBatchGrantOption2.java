package com.jrmf.payment.execute;

import com.jrmf.controller.constant.PayType;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import com.jrmf.domain.*;
import com.jrmf.service.*;

import com.jrmf.utils.exception.CheckUserNameCertIdCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

/**
 * @author zhanghuan
 */
public class ExecuteBatchGrantOption2 implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ExecuteBatchGrantOption2.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static final String PROCESS = "process";

    private String processId;

    private String batchId;

    private OrderNoUtil orderNoUtil;

    private String operatorName;

    private String customName;

    private String companyName;

    private String fileName;

    private String batchDesc;

    private String batchName;

    private int menuId;

    private String originalId;

    private String menuName;

    private String subAcctNo;

    private CyclicBarrier cb;

    private List<CommissionTemporary> param;

    private UserSerivce userSerivce;

    private UserCommission2Dao userCommissionDao2;

    private ChannelRelated channelRelated;

    private BaseInfo baseInfo;

    private CustomLimitConfService customLimitConfService;

    private CompanyService companyService;

    private CustomLdConfigService customLdConfigService;

    private UserCommissionService userCommissionService;

    public ExecuteBatchGrantOption2(String processId,
                                    CyclicBarrier cb,
                                    OrderNoUtil orderNoUtil,
                                   List<CommissionTemporary> param,
                                    UserCommission2Dao userCommissionDao2,
                                    UserSerivce userSerivce,
                                    ChannelRelated channelRelated,
                                    Map<String, Object> batchData,
                                    BaseInfo baseInfo,
                                    CustomLimitConfService customLimitConfService,
                                    CompanyService companyService,
                                    CustomLdConfigService customLdConfigService,
                                    UserCommissionService userCommissionService
    ) {
        super();
        this.processId = processId;
        this.param = param;
        this.cb = cb;
        this.orderNoUtil = orderNoUtil;
        this.userCommissionDao2 = userCommissionDao2;
        this.userSerivce = userSerivce;
        this.channelRelated = channelRelated;
        this.customLimitConfService = customLimitConfService;
        batchId = (String) batchData.get("batchId");
        operatorName = (String) batchData.get("operatorName");
        fileName = (String) batchData.get("fileName");
        batchDesc = (String) batchData.get("batchDesc");
        batchName = (String) batchData.get("batchName");
        originalId = (String) batchData.get("originalId");
        menuName = (String) batchData.get("menuName");
        customName = (String) batchData.get("customName");
        companyName = (String) batchData.get("companyName");
        menuId = (Integer) batchData.get("menuId");
        subAcctNo = (String) batchData.get("subAcctNo");
        this.baseInfo = baseInfo;
        this.companyService=companyService;
        this.customLdConfigService=customLdConfigService;
        this.userCommissionService=userCommissionService;
    }

    @Override
    public void run() {
        try {
            MDC.put(PROCESS, processId);
            logger.info("-----------------??????????????????---????????????:"+param.size()+"---------");
            List<UserCommission> commissionBatch = new ArrayList<>();
            //??????????????????<-->??????????????????????????????
            for (CommissionTemporary commission: param) {
                try {
                    //????????????????????????????????????????????????
                    if (!commission.getBankCardNo().equals(commission.getIdCard())){
                        Map<String, Object> addRespMap = userSerivce.addUserInfo(commission.getUserName(),
                                commission.getDocumentType(),
                                commission.getIdCard(),
                                null,
                                null,
                                originalId,
                                null, "");
                        commission.setUserId((int)addRespMap.get("userId"));

                        logger.info("-----addRespMap:{}-----" , addRespMap);
                    }
                    //????????????????????????--????????????????????????
                    if(commission.getStatus()==2){
                        createErrorCommion(commission,commissionBatch);
                        continue;
                    }else if(commission.getStatus()==1){
                        createSuccessUserCommission(commission, commissionBatch);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    commission.setStatusDesc("??????????????????????????????");
                    createErrorCommion(commission,commissionBatch);
                }
            }

            //???????????????????????????
            if(commissionBatch.size() != 0){
                int result = userCommissionDao2.addUserCommissionBatch(commissionBatch);
                logger.info("----->??????????????????--????????????:" + commissionBatch.size() + "\r\n----->????????????--????????????:" + result);
            }

            //????????????
            for (UserCommission userCommission : commissionBatch) {
                //1.???????????????????????????
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customkey", originalId);
                params.put("companyId", userCommission.getCompanyId());
                CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
                logger.info("????????????"+userCommission.getOrderNo()+",??????????????????????????????????????????");
                //??????????????????
                orderLssued(userCommission);
            }
            logger.info("------------------??????????????????------------");
            cb.await();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }finally{
            MDC.remove(PROCESS);
        }
    }

    /**
     * ??????????????????
     * @param userCommission
     */
    private void orderLssued(UserCommission userCommission) {
        try {
            if (userCommission.getStatus() != 0) {
                logger.error("????????????:{}??????????????????????????????????????????", userCommission.getOrderNo());
                return;
            }
            //?????????????????????????????????
            PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(String.valueOf(userCommission.getPayType()), originalId, userCommission.getCompanyId(), userCommission.getRealCompanyId());
            if(paymentConfig != null){
                userCommission.setPathNo(paymentConfig.getPathNo());
                userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
                //??????????????????????????????appid???????????????????????????????????????????????????
                if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
                    paymentConfig.setAppIdAyg(channelRelated.getAppIdAyg());
                }
                Company realCompany = companyService.getCompanyByUserId(Integer.parseInt(userCommission.getRealCompanyId()));
                if (realCompany != null && realCompany.getServiceCompanyId()!=null){
                    paymentConfig.setServiceCompanyId(realCompany.getServiceCompanyId());
                }
            }else{
                logger.error("????????????????????????????????????????????????????????????");
                logger.error("????????????????????????????????????----?????????????????????????????????-----customKey:" + userCommission.getOriginalId() + "--????????????ID???"+ userCommission.getRealCompanyId() +  "-????????????" + userCommission.getOrderNo());

                userCommission.setStatus(2);
                userCommission.setStatusDesc("??????-?????????????????????????????????");
                userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
                return;
            }


            logger.info("??????????????????------??????????????????------paymentConfig.toString()???" + paymentConfig.toString());

            String failMessage ;
            PaymentReturn<String> paymentReturn ;
            String orderNo = userCommission.getOrderNo();
            //????????????
            String transferBaffleSwitch = "1";
            if(transferBaffleSwitch.equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals	(originalId)){
                String retCode ;

                String amount = userCommission.getAmount();
                amount = ArithmeticUtil.mulStr(amount, "100");
                int length = amount.length();
                //??????
                if(Integer.parseInt(amount.substring(length - 1))%2 == 0){
                    retCode = PayRespCode.RESP_SUCCESS;
                    failMessage = "????????????";
                }else{
                    retCode = PayRespCode.RESP_FAILURE;
                    failMessage = "????????????";
                }
                paymentReturn = new PaymentReturn(retCode, failMessage, orderNo);
            }else{
                userCommission.setPathNo(paymentConfig.getPathNo());
                //??????????????????????????????
                paymentReturn = userCommissionService.getPaymentReturn(userCommission, paymentConfig);

            }
            logger.info("1----?????????????????????????????????"+paymentReturn.toString()+"-------");

            if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())
                    || PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
                // ?????????????????????
                String bankOrderNo = paymentReturn.getAttachment();
                logger.info("2---?????????????????????????????????"+bankOrderNo+"-------");
                if(bankOrderNo.length()>100){
                    bankOrderNo = bankOrderNo.substring(0, 50);
                }
                userCommission.setStatus(3);
                userCommission.setAygOrderNo(bankOrderNo);
                userCommission.setStatusDesc("?????????");

                //????????????????????????
                customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(),
                        userCommission.getOriginalId(),
                        userCommission.getCertId(),
                        userCommission.getSourceAmount(),
                        true);
                logger.info("?????????????????????????????????{}???", userCommission.getSourceAmount());
            } else {
                userCommission.setStatus(2);
                failMessage = paymentReturn.getFailMessage();
                if (failMessage.length() > 200) {
                    failMessage = failMessage.substring(0, 200);
                } else if (failMessage.length() == 0) {
                    failMessage = RespCode.CONNECTION_ERROR;
                }
                userCommission.setStatusDesc(failMessage);
            }
            userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
        } catch (Exception e) {
            logger.error("??????????????????????????? ??????????????? ???"+userCommission.getOrderNo());
            logger.error(e.getMessage(),e);

            //????????????????????????
            customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(),
                    userCommission.getOriginalId(),
                    userCommission.getCertId(),
                    userCommission.getSourceAmount(),
                    true);
            logger.info("?????????????????????????????????{}???", userCommission.getSourceAmount());

            userCommission.setStatus(3);
            userCommission.setStatusDesc("?????????");
            userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
        }
    }


    private void createErrorCommion(CommissionTemporary temporary,List<UserCommission> commissionBatch){
        UserCommission commission = new UserCommission();
        commission.setAmount(temporary.getAmount());
        commission.setSourceAmount(temporary.getSourceAmount());
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId()+"");
        commission.setStatus(2);
        commission.setStatusDesc(temporary.getStatusDesc());
        commission.setRemark(temporary.getRemark());
        commission.setSourceRemark(temporary.getRemark());
        commission.setBatchId(batchId);
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setOrderNo(temporary.getOrderNo());

        commission.setSumFee("0");
        commission.setSupplementFee("0");
        commission.setSupplementAmount("0");
        commission.setCalculationRates("0");
        commission.setProfilt("0");
        commission.setProfiltFree("0");

        commission.setOriginalId(channelRelated.getOriginalId());
        commission.setMerchantId(channelRelated.getMerchantId());
        commission.setOperatorName(temporary.getOperatorName());
        commission.setPayUserName(operatorName);
        commission.setPayType(temporary.getPayType());
        commission.setAccount(temporary.getBankCardNo());
        //?????????
        commission.setInvoiceStatus(2);

        commission.setUserName(temporary.getUserName());
        commission.setAccount(temporary.getBankCardNo());
        commission.setBankNo(temporary.getBankNo());
        commission.setBankName(temporary.getBankName());
        commission.setContentName(menuName);
        commission.setMenuId(menuId);
        commission.setBatchFileName(fileName);
        commission.setBatchDesc(batchDesc);
        commission.setBatchName(batchName);
        commission.setDocumentType(temporary.getDocumentType());
        commission.setCertId(temporary.getIdCard());
        commission.setBankNo(temporary.getBankNo());
        commission.setCustomName(customName);
        commission.setCompanyName(companyName);
        commission.setFeeRuleType(temporary.getFeeRuleType());
        commission.setPhoneNo(temporary.getPhoneNo());

        commission.setBusinessManager(temporary.getBusinessManager());
        commission.setOperationsManager(temporary.getOperationsManager());
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commissionBatch.add(commission);
    }

    private void createSuccessUserCommission(CommissionTemporary temporary, List<UserCommission> commissionBatch){
        String amount  = temporary.getAmount();

        UserCommission commission = new UserCommission();

        commission.setAmount(amount);
        commission.setSourceAmount(temporary.getSourceAmount());
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId() + "");
        commission.setStatus(0);
        commission.setAccount(temporary.getBankCardNo());
        commission.setBatchId(batchId);
        commission.setOriginalId(originalId);
        commission.setMerchantId(channelRelated.getMerchantId());
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setOrderNo(temporary.getOrderNo());
        commission.setOperatorName(temporary.getOperatorName());
        commission.setPayUserName(operatorName);
        commission.setStatusDesc("????????????????????????");

        commission.setSumFee(temporary.getSumFee());
        commission.setSupplementFee(temporary.getSupplementFee());
        commission.setSupplementAmount(temporary.getSupplementAmount());
        commission.setCalculationRates(temporary.getCalculationRates());
        commission.setProfilt(temporary.getProfiltRates());
        commission.setProfiltFree(temporary.getProfilt());

        commission.setPayType(temporary.getPayType());
        //?????????
        commission.setInvoiceStatus(2);
        commission.setContentName(menuName);
        commission.setMenuId(menuId);
        commission.setBatchFileName(fileName);
        commission.setBatchDesc(batchDesc);
        commission.setBatchName(batchName);
        commission.setAccountDate(sdf.format(new Date()));

        commission.setCreatetime(DateUtils.getNowDate());
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setUserName(temporary.getUserName());
        commission.setBankNo(temporary.getBankNo());
        commission.setBankName(temporary.getBankName());
        commission.setDocumentType(temporary.getDocumentType());
        commission.setCertId(temporary.getIdCard());
        commission.setBankNo(temporary.getBankNo());
        commission.setReceiptNo(orderNoUtil.getReceiptNo());
        commission.setRemark(commission.getReceiptNo()+temporary.getRemark());
        commission.setSourceRemark(temporary.getRemark());
        commission.setCompanyName(companyName);
        commission.setCustomName(customName);
        commission.setRepeatcheck(temporary.getRepeatcheck());
        commission.setFeeRuleType(temporary.getFeeRuleType());
        commission.setPhoneNo(temporary.getPhoneNo());

        commission.setBusinessManager(temporary.getBusinessManager());
        commission.setOperationsManager(temporary.getOperationsManager());
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commissionBatch.add(commission);
    }

}
