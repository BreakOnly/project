package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.controller.constant.CertType;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.*;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.util.HttpPostUtil;
import com.jrmf.utils.*;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chonglulu
 */
@ActionConfig(name = "发放风控预授权")
public class PrepareUnifiedOrderService
		implements Action<PrepareUnifiedOrderServiceParams, PrepareUnifiedOrderServiceAttachment> {


    private static final Logger logger = LoggerFactory.getLogger(PrepareUnifiedOrderService.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private final UserSerivce userSerivce;
    private final CompanyService companyService;
    private final CustomLimitConfService customLimitConfService;
    private final UserCommissionService userCommissionService;
    private final OrderNoUtil orderNoUtil;
    private final ChannelCustomService channelCustomService;
    private final APIDockingManager apiDockingManager;
    private final UsersAgreementService usersAgreementService;

    @Autowired
    public PrepareUnifiedOrderService(UserSerivce userSerivce, CompanyService companyService, CustomLimitConfService customLimitConfService, UserCommissionService userCommissionService, OrderNoUtil orderNoUtil, ChannelCustomService channelCustomService, APIDockingManager apiDockingManager, UsersAgreementService usersAgreementService) {
        this.userSerivce = userSerivce;
        this.companyService = companyService;
        this.customLimitConfService = customLimitConfService;
        this.userCommissionService = userCommissionService;
        this.orderNoUtil = orderNoUtil;
        this.channelCustomService = channelCustomService;
        this.apiDockingManager = apiDockingManager;
        this.usersAgreementService = usersAgreementService;
    }

    @Override
	public String getActionType() {
		return APIDefinition.PREPARE_UNIFIED_ORDER.name();
	}

	@Override
	public ActionResult<PrepareUnifiedOrderServiceAttachment> execute(
            PrepareUnifiedOrderServiceParams actionParams) {
        Map<Object, Object> paramMap = new HashMap<>(2);
        paramMap.put("customOrderNo",actionParams.getCustomOrderNo());
        paramMap.put("originalId",actionParams.getMerchantId());
        int count = userCommissionService.getCommissionsCountByParams(paramMap);
        if(count>0){
            throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_EXISTED.getCode(), APIDockingRetCodes.REQUEST_NO_EXISTED.getDesc());
        }
        String certificateNo = actionParams.getCertificateNo();
        String phoneNo = actionParams.getPhoneNo();
        String bankCardNo = actionParams.getBankCardNo();
        String secretKey = "13E80F176EDCA60456220FE8EDCB5772";
        try {
            certificateNo = AesUtil.decrypt(certificateNo, secretKey);
            phoneNo = AesUtil.decrypt(phoneNo, secretKey);
            bankCardNo = AesUtil.decrypt(bankCardNo, secretKey);
        } catch (Exception e) {
            throw new APIDockingException(APIDockingRetCodes.PARAMETER_ANALYSIS_ERROR.getCode(), APIDockingRetCodes.PARAMETER_ANALYSIS_ERROR.getDesc());
        }
        Map<String, Object> objectMap = userSerivce.addUserInfo(actionParams.getName(), CertType.ID_CARD.getCode(), certificateNo, null, phoneNo, actionParams.getMerchantId(), null, "");
        int userId = (int) objectMap.get("userId");
        ChannelCustom custom = channelCustomService.getCustomByCustomkey(actionParams.getMerchantId());

        Company company = companyService.getCompanyByUserId(Integer.parseInt(actionParams.getTransferCorpId()));
        int calculateType = company.getCalculateType();
        if(calculateType == 0){
            logger.info("本地计算");
            UserCommission userCommission = new UserCommission();
            userCommission.setCustomOrderNo(actionParams.getCustomOrderNo());
            userCommission.setAccountDate(SDF.format(new Date()));
            userCommission.setPhoneNo(phoneNo);
            userCommission.setPayType(4);
            userCommission.setOriginalId(actionParams.getMerchantId());
            userCommission.setStatusDesc("预下发");
            userCommission.setUserName(actionParams.getName());
            userCommission.setOrderNo(orderNoUtil.getChannelSerialno());
            userCommission.setAmount(actionParams.getAmount());
            userCommission.setCreatetime(DateUtils.getNowDate());
            userCommission.setUserId(userId+"");
            userCommission.setStatus(0);
            userCommission.setCompanyId(actionParams.getTransferCorpId());
            userCommission.setCompanyName(company.getCompanyName());
            userCommission.setCertId(certificateNo);
            userCommission.setCustomName(custom.getCompanyName());
            userCommission.setAccount(bankCardNo);
            userCommission.setDocumentType(CertType.ID_CARD.getCode());
            userCommission.setBankName(actionParams.getAccountName());
            userCommission.setDescription(actionParams.getMemo());
            userCommission.setServiceType(actionParams.getServiceType());
            userCommission.setRegType("02");
            userCommission.setNotifyUrl(actionParams.getNotifyUrl());
            userCommission.setCalculateType(calculateType+"");

            userCommissionService.addUserCommission(userCommission);
            String finalCertificateNo = certificateNo;
            ThreadUtil.pdfThreadPool.execute(() -> {
                //商户限额
                String amount;
                String amountZero = "0.00";
                String payAmount = actionParams.getAmount();
                Map<String, Object> map = new HashMap<>(4);
                map.put("customkey", actionParams.getMerchantId());
                map.put("companyId", actionParams.getTransferCorpId());
                CustomLimitConf customLimitConf = customLimitConfService.getCustomLimitConf(map);
                QueryOrderQuotaServiceAttachmentDetail detail = new QueryOrderQuotaServiceAttachmentDetail();
                detail.setTransferCorpId(actionParams.getTransferCorpId());
                if (customLimitConf == null) {
                    amount = amountZero;
                } else {
                    String customSingleMonthLimit = customLimitConf.getSingleMonthLimit();
                    if (StringUtil.isEmpty(customSingleMonthLimit)) {
                        amount = amountZero;
                    } else {
                        CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(actionParams.getTransferCorpId(), actionParams.getMerchantId(), finalCertificateNo);
                        String currentMonthTotal = customPaymentTotalAmount.getCurrentMonthTotalStr();
                        String customAmount = ArithmeticUtil.subStr(customSingleMonthLimit, currentMonthTotal);
                        logger.info("商户个人剩余下发额：" + customAmount);
                        amount = customAmount;
                    }
                }
                if (ArithmeticUtil.compareTod(amount, amountZero) < 0) {
                    amount = amountZero;
                }
                int i = ArithmeticUtil.compareTod(amount, payAmount);
                String retCode = CommonRetCodes.ACTION_DONE.getCode();
                String message = "可以下发";
                if(i<0){
                    retCode = CommonRetCodes.UNSUPPORTED_ACTION.getCode();
                    message = "额度超限，下发失败";
                    logger.info(message);
                    userCommission.setStatus(2);
                    userCommission.setStatusDesc("预授权发放失败，额度超限");
                }else{
                    logger.info("额度没有超限，"+message);
                    customLimitConfService.updateCustomPaymentTotalAmount(actionParams.getTransferCorpId(), actionParams.getMerchantId(),finalCertificateNo,payAmount,true);
                    userCommission.setStatus(5);
                    userCommission.setStatusDesc("预授权发放，等待下发结果");
                }
                userCommissionService.updateUserCommission(userCommission);
                //回调
                MerchantAPIDockingConfig dockingConfig = apiDockingManager.getMerchantAPIDockingConfig(userCommission.getOriginalId());
                String signType = dockingConfig.getSignType();
                Map<String, Object> outData = new HashMap<>(16);
                outData.put("customOrderNo", userCommission.getCustomOrderNo());
                outData.put("deal_no", userCommission.getOrderNo());
                outData = usersAgreementService.getOutData(retCode, message, dockingConfig, signType, outData);
                Map<String, Object> result = HttpPostUtil.httpPost(userCommission.getNotifyUrl(), outData);
                logger.info("通知结果：{}",result);
            });


        }else{
            throw new APIDockingException(APIDockingRetCodes.CALCULATE_TYPE_NO_EXISTED.getCode(), APIDockingRetCodes.CALCULATE_TYPE_NO_EXISTED.getDesc());
        }
        return new ActionResult<>();
    }

}
