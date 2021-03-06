package com.jrmf.api.task;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.LdOrderStatusEnum;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ExecuteSplitOrderLdQuery implements Runnable {

  private Logger logger = LoggerFactory.getLogger(ExecuteSplitOrderLdQuery.class);
  public static final String PROCESS = "process";

  private String processId;
  private List<UserCommission> batchData;
  private UserCommissionService commissionService;
  private CompanyService companyService;
  private UtilCacheManager utilCacheManager;
  private CustomLimitConfService customLimitConfService;
  private LdOrderStepService ldOrderStepService;
  private CustomBalanceService customBalanceService;
  private BaseInfo baseInfo;
  private TransferDealStatusNotifier transferDealStatusNotifier;
  private UserSerivce userSerivce;
  private ForwardCompanyAccountService forwardCompanyAccountService;

  public ExecuteSplitOrderLdQuery(String processId,
      List<UserCommission> batchData,
      UserCommissionService commissionService,
      CompanyService companyService,
      UtilCacheManager utilCacheManager,
      CustomLimitConfService customLimitConfService,
      LdOrderStepService ldOrderStepService, CustomBalanceService customBalanceService,
      BaseInfo baseInfo,
      TransferDealStatusNotifier transferDealStatusNotifier,
      UserSerivce userSerivce,ForwardCompanyAccountService forwardCompanyAccountService) {
    super();
    this.processId = processId;
    this.batchData = batchData;
    this.commissionService = commissionService;
    this.companyService = companyService;
    this.utilCacheManager = utilCacheManager;
    this.customLimitConfService = customLimitConfService;
    this.ldOrderStepService = ldOrderStepService;
    this.customBalanceService = customBalanceService;
    this.baseInfo = baseInfo;
    this.transferDealStatusNotifier = transferDealStatusNotifier;
    this.userSerivce = userSerivce;
    this.forwardCompanyAccountService = forwardCompanyAccountService;
  }

  @Override
  public void run() {

    MDC.put(PROCESS, processId);
    for (UserCommission commission : batchData) {
      String orderNo = commission.getOrderNo();
      logger.info("????????????????????????" + commission.getOrderNo() + "????????????????????????????????????");
      if (StringUtil.isEmpty(orderNo)) {
        continue;
      }

      List<LdOrderStep> ldOrderSteps = ldOrderStepService.getList(orderNo);
      for (LdOrderStep ldOrderStep : ldOrderSteps) {
        if (LdOrderStatusEnum.SUBMITTED.getCode() == ldOrderStep.getStatus()) {
          logger.info("????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo()
                  + "??????????????????????????????");

          PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(String.valueOf(commission.getPayType()),
                  commission.getOriginalId(), ldOrderStep.getIssuedCompanyid(),
                  ldOrderStep.getIssuedRealCompanyId(), ldOrderStep.getPathno());

          logger.info(
              "????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo()
                  + "??????????????????????????????----??????????????????------paymentConfig.toString()???" + paymentConfig
                  .toString());
          if (StringUtil.isEmpty(paymentConfig.getPathNo())) {
            logger.info("????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo()
                    + "????????????????????????????????????----?????????????????????????????????-----customKey:" + commission.getOriginalId()
                    + "--????????????ID???" + commission.getCompanyId());
            continue;
          }

          try {
            PaymentReturn<TransStatus> paymentReturn;
            if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(commission.getOriginalId())) {//????????????
              TransStatus transStatus1 = new TransStatus(ldOrderStep.getStepOrderNo(), PayRespCode.RESP_TRANSFER_SUCCESS, "????????????");
              paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS, "????????????", transStatus1);
            } else {
              //??????????????????????????????
              Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
              PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
              Payment proxy = paymentProxy.getProxy();
              paymentReturn = proxy.queryTransferResult(ldOrderStep.getStepOrderNo());
            }

            logger.info("????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo()
                    + "??????????????????????????????----------???" + paymentReturn.toString());
            if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
              TransStatus transStatus = paymentReturn.getAttachment();
              logger.error("????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo() + "????????????---------resultCode:"
                      + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
              if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
                ldOrderStep.setStatus(CommissionStatus.SUCCESS.getCode());
                ldOrderStep.setStatusDesc(CommissionStatus.SUCCESS.getDesc());
                ldOrderStepService.update(ldOrderStep);
              } else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
                ldOrderStep.setStatus(CommissionStatus.FAILURE.getCode());
                String errorMsg = transStatus.getResultMsg();
                if (errorMsg != null) {
                  if (errorMsg.contains("??????")) {
                    errorMsg = "?????????????????????????????????";
                  } else {
                    String statusDesc = transStatus.getResultMsg();
                    if (statusDesc.length() > 200) {
                      statusDesc = statusDesc.substring(0, 200);
                    }
                    String s = statusDesc.replaceAll(",", "-");
                    errorMsg = s;
                  }
                }
                ldOrderStep.setStatusDesc(errorMsg);
                ldOrderStepService.update(ldOrderStep);

              } else {
                logger.error("???????????????---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
              }
            } else {
              logger.error(
                  "????????????????????????" + commission.getOrderNo() + ",??????????????????" + ldOrderStep.getStepOrderNo()
                      + "????????????---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
            }
          } catch (Exception e) {
            logger.error("????????????????????????????????????" + commission.getOrderNo(), e);
          }
        }
      }

      //???????????????????????????????????????
      int totalCount = ldOrderStepService.getCountByOrderNo(orderNo);
      int successCount = ldOrderStepService.getCountSuccessByOrderNo(orderNo);
      int failCount = ldOrderStepService.getCountFailByOrderNo(orderNo);
      if (successCount == totalCount) {
        logger.info("????????????????????????" + commission.getOrderNo() + "?????????????????????????????????????????????????????????????????????");
        commission.setPaymentTime(DateUtils.getNowDate());
        commission.setStatus(CommissionStatus.SUCCESS.getCode());
        commission.setStatusDesc(CommissionStatus.SUCCESS.getDesc());

        Map<String, Object> stringObjectMap = userSerivce.addUserInfo(
            commission.getUserName(),
            commission.getDocumentType(),
            commission.getCertId(),
            commission.getUserNo(),
            commission.getPhoneNo(),
            commission.getOriginalId(),
            commission.getMerchantId(), "");
        commission.setUserId(stringObjectMap.get("userId") + "");

        int updateCount = commissionService.updateUserCommissionById(commission);
        //api??????????????????
        if (StringUtil.isEmpty(commission.getBatchId()) && updateCount == 1) {
          transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_DONE,
              CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());
          logger.info("????????????????????????" + commission.getOrderNo() + "??????????????????????????????????????????");
        }
      } else if (failCount == totalCount) {
        logger.info("????????????????????????" + commission.getOrderNo() + "?????????????????????????????????????????????????????????????????????");
        commission.setPaymentTime(DateUtils.getNowDate());
        commission.setStatus(CommissionStatus.FAILURE.getCode());
        commission.setStatusDesc(CommissionStatus.FAILURE.getDesc());
        int updateCount = commissionService.updateUserCommissionById(commission);
        //api??????????????????
        if (StringUtil.isEmpty(commission.getBatchId()) && updateCount == 1) {

          try {
            customBalanceService.updateCustomBalance(CommonString.ADDITION,
                new CustomBalanceHistory(commission.getOriginalId(),
                    commission.getCompanyId(), commission.getPayType(),
                    ArithmeticUtil
                        .addStr(commission.getAmount(), commission.getSumFee(),
                            2),
                    1, TradeType.PAYMENTREFUND.getCode(),
                    commission.getOrderNo(),"apiTaskJob"));

            logger.info("????????????????????????{}", commission.getOrderNo());

            customLimitConfService.updateCustomPaymentTotalAmount(commission.getCompanyId(),
                commission.getOriginalId(),
                commission.getCertId(),
                commission.getSourceAmount(),
                false);
            logger.info("???????????????????????????????????????{}???", commission.getSourceAmount());

            String realCompanyId = commission.getRealCompanyId();
            if (!StringUtil.isEmpty(realCompanyId) && !commission.getCompanyId().equals(realCompanyId)){//??????????????????
              //????????????????????????????????????????????????????????????
              CompanyAccountVo companyAccountVo = new CompanyAccountVo();
              //String changeAmount = ArithmeticUtil.addStr(commission.getAmount(), commission.getSumFee(), 2);
              companyAccountVo.setBalance(commission.getAmount());
              companyAccountVo.setCustomKey(commission.getOriginalId());
              companyAccountVo.setCompanyId(commission.getCompanyId());
              companyAccountVo.setRealCompanyId(commission.getRealCompanyId());
              companyAccountVo.setTradeType(TradeType.PAYMENTREFUND.getCode());
              companyAccountVo.setRelateOrderNo(commission.getOrderNo());
              companyAccountVo.setOperator("apiTaskJob");
              companyAccountVo.setAmount(1);
              companyAccountVo.setOperating(CommonString.ADDITION);
              forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);

              //???????????????????????????????????????????????????
             try{
               CustomBalanceHistory queryBalanceHistory = new CustomBalanceHistory();
               queryBalanceHistory.setTradeType(TradeType.APIPAYMENT.getCode());
               queryBalanceHistory.setRelateOrderNo(commission.getOrderNo());
               queryBalanceHistory.setCustomKey(commission.getCompanyId());
               queryBalanceHistory.setCompanyId(commission.getRealCompanyId());
               List<CustomBalanceHistory> customBalanceHistories = customBalanceService.listCustomBalanceHistory(queryBalanceHistory);
               if (!CollectionUtils.isEmpty(customBalanceHistories)){
                 String tradeAmount = customBalanceHistories.get(0).getTradeAmount();
                 CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(commission.getCompanyId(),
                         commission.getRealCompanyId(), commission.getPayType(), tradeAmount,
                         1, TradeType.PAYMENTREFUND.getCode(),commission.getOrderNo(),
                     "apiTaskJob");
                 customBalanceService.updateCustomBalance(CommonString.ADDITION, customBalanceHistory);
               }
             }catch (Exception e){
               logger.error("?????????????????????????????????????????????????????????"+e.getMessage(), e);
             }
            }
            transferDealStatusNotifier
                .notify(commission.getOrderNo(), TransferStatus.TRANSFER_FAILED,
                    CommonRetCodes.UNCATCH_ERROR.getCode(), commission.getStatusDesc());
            logger.info("????????????????????????" + commission.getOrderNo() + "??????????????????????????????????????????");
          } catch (Exception e) {
            logger.error("api??????????????????????????????");
            logger.error(e.getMessage(), e);
          }
        }
      }
    }
    MDC.remove(PROCESS);
  }

}
