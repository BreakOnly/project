package com.jrmf.payment.execute;

import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.sms.SmsSendEnum;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomBalanceService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.service.SmsService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.service.YmyfCommonService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jrmf.controller.constant.PayType;
import com.jrmf.utils.threadpool.ThreadUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;

public class ExecuteBatchGrantQuery implements Runnable {

  private Logger logger = LoggerFactory.getLogger(ExecuteBatchGrantQuery.class);
  public static final String PROCESS = "process";
  private UserSerivce userSerivce;

  private String processId;
  private BaseInfo baseInfo;
  private List<UserCommission> batchData;
  private UserCommissionService commissionService;
  private ChannelRelatedDao channelRelatedDao;
  private CompanyService companyService;
  private UtilCacheManager utilCacheManager;
  private CustomLimitConfService customLimitConfService;
  private CustomBalanceService customBalanceService;
  private YmyfCommonService ymyfCommonService;
  private ChannelHistoryService channelHistoryService;
  private ChannelInterimBatchService channelInterimBatchService;
  private ChannelCustomService channelCustomService;
  private SmsService smsService;
  private UsersAgreementService usersAgreementService;

  public ExecuteBatchGrantQuery(String processId,
      List<UserCommission> batchData,
      UserCommissionService commissionService, UserSerivce userSerivce,
      ChannelRelatedDao channelRelatedDao,
      CompanyService companyService,
      UtilCacheManager utilCacheManager,
      BaseInfo baseInfo,
      CustomLimitConfService customLimitConfService,
      CustomBalanceService customBalanceService,
      YmyfCommonService ymyfCommonService,
      ChannelHistoryService channelHistoryService,
      ChannelInterimBatchService channelInterimBatchService,
      SmsService smsService,
      ChannelCustomService channelCustomService,
      UsersAgreementService usersAgreementService) {
    super();
    this.baseInfo = baseInfo;
    this.processId = processId;
    this.batchData = batchData;
    this.commissionService = commissionService;
    this.userSerivce = userSerivce;
    this.channelRelatedDao = channelRelatedDao;
    this.companyService = companyService;
    this.utilCacheManager = utilCacheManager;
    this.customLimitConfService = customLimitConfService;
    this.customBalanceService = customBalanceService;
    this.ymyfCommonService = ymyfCommonService;
    this.channelInterimBatchService = channelInterimBatchService;
    this.channelHistoryService = channelHistoryService;
    this.smsService = smsService;
    this.channelCustomService = channelCustomService;
    this.usersAgreementService = usersAgreementService;
  }

  @Override
  public void run() {

    MDC.put(PROCESS, processId);
    for (UserCommission commission : batchData) {
      String orderNo = commission.getOrderNo();
      logger.info("??????????????????????????????----------orderNo???" + orderNo);
      if (StringUtil.isEmpty(orderNo)) {
        continue;
      }
      ChannelRelated channelRelated = channelRelatedDao
          .getRelatedByCompAndOrig(commission.getOriginalId(), commission.getCompanyId());
      PaymentConfig paymentConfig = companyService
          .getPaymentConfigInfoPlus(String.valueOf(commission.getPayType()),
              commission.getOriginalId(), commission.getCompanyId(), commission.getRealCompanyId(),
              commission.getPathNo());
      if (paymentConfig != null) {
        logger
            .info("??????????????????????????????----??????????????????------paymentConfig.toString()???" + paymentConfig.toString());

        //????????????appid????????????
        if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
          paymentConfig.setAppIdAyg(channelRelated.getAppIdAyg());
        }
      } else {
        logger.error(
            "????????????????????????????????????----?????????????????????????????????-----customKey:" + commission.getOriginalId() + "--????????????ID???"
                + commission.getCompanyId() + "-????????????" + commission.getOrderNo());
        continue;
      }

      PaymentReturn<TransStatus> paymentReturn = null;
      if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K"
          .equals(commission.getOriginalId())) {//????????????
        String retCode = "";
        String failMessage = "";
        String resultCode = "";
        String resultMsg = "";
        retCode = PayRespCode.RESP_SUCCESS;

        String amount = commission.getAmount();
        amount = ArithmeticUtil.mulStr(amount, "100");
        int length = amount.length();
        //??????
        if (Integer.valueOf(amount.substring(length - 1)) % 2 == 0) {
          resultCode = PayRespCode.RESP_TRANSFER_SUCCESS;
          failMessage = "????????????";
          resultMsg = "????????????";
        } else {
          resultCode = PayRespCode.RESP_TRANSFER_FAILURE;
          failMessage = "????????????";
          resultMsg = "????????????";
        }

        TransStatus transStatusSandBox = new TransStatus(orderNo, resultCode, resultMsg);
        paymentReturn = new PaymentReturn(retCode, failMessage, transStatusSandBox);
      } else {
        if (PaymentFactory.YMFWSPAY.equals(commission.getPathNo())) {
          logger.info("????????????" + commission.getOrderNo() + "??????????????????????????????");
          //??????????????????????????????????????????
          ChannelHistory channelHistory = channelHistoryService
              .getChannelHistoryById(commission.getBatchId());
          ChannelInterimBatch interimBatch = channelInterimBatchService
              .getChannelInterimBatchBySuccess(channelHistory.getOriginalBeachNo(),
                  commission.getOriginalId());
          interimBatch.setRealCompanyId(commission.getRealCompanyId());
          Map<String, Object> respInfo = ymyfCommonService
              .smsPayResultQuery(interimBatch, commission.getOrderNo());
          if (respInfo.get("state").equals("1")) {
            //????????????
            logger.info("????????????" + commission.getOrderNo() + "????????????");
            commission.setStatus(1);
            commission.setStatusDesc("??????");
            commission.setPaymentTime(DateUtils.getNowDate());
            customLimitConfService.updateCustomPaymentTotalAmount(commission.getCompanyId(),
                commission.getOriginalId(),
                commission.getCertId(),
                commission.getSourceAmount(),
                true);
            logger
                .info("????????????" + commission.getOrderNo() + "?????????????????????????????????{}???", commission.getAmount());
          } else if (respInfo.get("state").equals("2")) {
            logger.info("????????????" + commission.getOrderNo() + "????????????");
            //????????????
            commission.setStatus(2);
            commission.setStatusDesc(String.valueOf(respInfo.get("msg")));
            if ("??????????????????????????????".equals(respInfo.get("msg"))) {
              String idNo = commission.getCertId();
              String companyId = commission.getRealCompanyId();
              String customKey = commission.getOriginalId();
              Map<String, Object> params = new HashMap<>();
              params.put("originalId", customKey);
              params.put("companyId", companyId);
              params.put("certId", idNo);
              params.put("signStatus", 4);
              List<UsersAgreement> usersAgreements = usersAgreementService
                  .getUsersAgreementsByParams(params);
              if (usersAgreements != null && usersAgreements.size() == 1) {
                commission.setStatusDesc(usersAgreements.get(0).getSignStatusDes());
              } else {
                commission.setStatusDesc("??????????????????");
              }
            }
          }
          commissionService.updateUserCommissionById(commission);
          logger.info("?????????????????????????????? orderNo[{}]", commission.getOrderNo());
        } else {
          paymentConfig.setSubAcctNo(commission.getSubAcctNo());
          //??????????????????????????????
          Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
          PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME,
              utilCacheManager);
          Payment proxy = paymentProxy.getProxy();
          paymentReturn = proxy.queryTransferResult(orderNo);
        }

      }
      UserCommission userCommission = new UserCommission();

      if (!PaymentFactory.YMFWSPAY.equals(commission.getPathNo())) {
        logger.info("??????????????????????????????----------???" + paymentReturn.toString());

        if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
          userCommission.setId(commission.getId());
          TransStatus transStatus = paymentReturn.getAttachment();
          logger.error(
              "????????????---------orderNO:" + transStatus.getOrderNo() + "--resultCode:" + transStatus
                  .getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
          if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
            userCommission.setStatus(1);
            userCommission.setStatusDesc("??????");

            if (commission.getPayType() == PayType.PINGAN_BANK.getCode() && PaymentFactory.PAKHKF
                .equals(paymentConfig.getPathNo()) && !StringUtil.isEmpty(commission.getSubAcctNo())
                && ArithmeticUtil.compareTod(commission.getSumFee(), "0") > 0) {
              logger.info("------------web?????????????????????????????????????????????,?????????{}---------------",
                  commission.getOrderNo());

              //?????????????????????????????????,??????????????????????????????
              ThreadUtil.subAccountThreadPool.execute(() -> customBalanceService
                  .updateSubAccountBalance(commission.getOriginalId(), commission.getCompanyId(),
                      commission.getPayType(), commission.getSumFee(), CommonString.DEDUCTION,
                      TradeType.SERVICEFEE, commission.getOperatorName()));
            }
          } else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
            userCommission.setStatus(2);
            String errorMsg = transStatus.getResultMsg();
            if (errorMsg != null) {
              if (errorMsg.contains("??????")) {
                errorMsg = "?????????????????????????????????";
              } else if ("??????????????????????????????".equals(errorMsg)) {
                String idNo = commission.getCertId();
                String companyId = commission.getRealCompanyId();
                String customKey = commission.getOriginalId();
                Map<String, Object> params = new HashMap<>();
                params.put("originalId", customKey);
                params.put("companyId", companyId);
                params.put("certId", idNo);
                params.put("signStatus", 4);
                List<UsersAgreement> usersAgreements = usersAgreementService
                    .getUsersAgreementsByParams(params);
                if (usersAgreements != null && usersAgreements.size() == 1) {
                  errorMsg = usersAgreements.get(0).getSignStatusDes();
                } else {
                  errorMsg = "??????????????????";
                }
              } else {
                String statusDesc = transStatus.getResultMsg();
                if (statusDesc.length() > 200) {
                  statusDesc = statusDesc.substring(0, 200);
                }
                String s = statusDesc.replaceAll(",", "-");
                errorMsg = s;
              }
            }
            userCommission.setStatusDesc(errorMsg);

            customLimitConfService.updateCustomPaymentTotalAmount(commission.getCompanyId(),
                commission.getOriginalId(),
                commission.getCertId(),
                commission.getSourceAmount(),
                false);
            logger.info("?????????????????????????????????{}???", commission.getSourceAmount());
          } else {
            logger.error("????????????---------" + paymentReturn.getRetCode() + "------" + transStatus
                .getResultMsg());
            userCommission.setStatus(3);
            userCommission
                .setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());
          }

          userCommission.setPaymentTime(DateUtils.getNowDate());
          commissionService.updateUserCommissionById(userCommission);
          logger.info("?????????????????????????????? orderNo[{}]", commission.getOrderNo());

          if (CommissionStatus.SUCCESS.getCode() == userCommission.getStatus()) {
            //??????userrelated??????????????????????????????????????????
            userSerivce.updateUserRelated(commission.getUserId(), commission.getPhoneNo(),
                commission.getOriginalId());
          }
        } else {
          logger
              .error("????????????---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
        }
      }
    }
    MDC.remove(PROCESS);
  }
}
