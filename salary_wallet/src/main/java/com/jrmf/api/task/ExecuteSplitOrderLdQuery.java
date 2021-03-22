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
      logger.info("交易明细订单号：" + commission.getOrderNo() + "查询下发操作执行结果明细");
      if (StringUtil.isEmpty(orderNo)) {
        continue;
      }

      List<LdOrderStep> ldOrderSteps = ldOrderStepService.getList(orderNo);
      for (LdOrderStep ldOrderStep : ldOrderSteps) {
        if (LdOrderStatusEnum.SUBMITTED.getCode() == ldOrderStep.getStatus()) {
          logger.info("交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
                  + "查询下发操作执行结果");

          PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(String.valueOf(commission.getPayType()),
                  commission.getOriginalId(), ldOrderStep.getIssuedCompanyid(),
                  ldOrderStep.getIssuedRealCompanyId(), ldOrderStep.getPathno());

          logger.info(
              "交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
                  + "查询下发操作执行结果----通道配置信息------paymentConfig.toString()：" + paymentConfig
                  .toString());
          if (StringUtil.isEmpty(paymentConfig.getPathNo())) {
            logger.info("交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
                    + "查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + commission.getOriginalId()
                    + "--服务公司ID：" + commission.getCompanyId());
            continue;
          }

          try {
            PaymentReturn<TransStatus> paymentReturn;
            if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(commission.getOriginalId())) {//挡板有效
              TransStatus transStatus1 = new TransStatus(ldOrderStep.getStepOrderNo(), PayRespCode.RESP_TRANSFER_SUCCESS, "付款成功");
              paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS, "查询成功", transStatus1);
            } else {
              //调用支付通道工厂模式
              Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
              PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
              Payment proxy = paymentProxy.getProxy();
              paymentReturn = proxy.queryTransferResult(ldOrderStep.getStepOrderNo());
            }

            logger.info("交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
                    + "查询下发操作执行结果----------：" + paymentReturn.toString());
            if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
              TransStatus transStatus = paymentReturn.getAttachment();
              logger.error("交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo() + "查询结果---------resultCode:"
                      + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
              if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
                ldOrderStep.setStatus(CommissionStatus.SUCCESS.getCode());
                ldOrderStep.setStatusDesc(CommissionStatus.SUCCESS.getDesc());
                ldOrderStepService.update(ldOrderStep);
              } else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
                ldOrderStep.setStatus(CommissionStatus.FAILURE.getCode());
                String errorMsg = transStatus.getResultMsg();
                if (errorMsg != null) {
                  if (errorMsg.contains("余额")) {
                    errorMsg = "网络异常，请联系管理员";
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
                logger.error("交易未完成---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
              }
            } else {
              logger.error(
                  "交易明细订单号：" + commission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
                      + "查询失败---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
            }
          } catch (Exception e) {
            logger.error("交易明细订单号查询异常：" + commission.getOrderNo(), e);
          }
        }
      }

      //根据拆单订单判断主账户状态
      int totalCount = ldOrderStepService.getCountByOrderNo(orderNo);
      int successCount = ldOrderStepService.getCountSuccessByOrderNo(orderNo);
      int failCount = ldOrderStepService.getCountFailByOrderNo(orderNo);
      if (successCount == totalCount) {
        logger.info("交易明细订单号：" + commission.getOrderNo() + "联动明细步骤全部为成功，更新交易明细订单为成功");
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
        //api下发进行回调
        if (StringUtil.isEmpty(commission.getBatchId()) && updateCount == 1) {
          transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_DONE,
              CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());
          logger.info("交易明细订单号：" + commission.getOrderNo() + "交易成功！系统内部回调成功！");
        }
      } else if (failCount == totalCount) {
        logger.info("交易明细订单号：" + commission.getOrderNo() + "联动明细步骤全部为失败，更新交易明细订单为失败");
        commission.setPaymentTime(DateUtils.getNowDate());
        commission.setStatus(CommissionStatus.FAILURE.getCode());
        commission.setStatusDesc(CommissionStatus.FAILURE.getDesc());
        int updateCount = commissionService.updateUserCommissionById(commission);
        //api下发进行回调
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

            logger.info("退款成功！订单号{}", commission.getOrderNo());

            customLimitConfService.updateCustomPaymentTotalAmount(commission.getCompanyId(),
                commission.getOriginalId(),
                commission.getCertId(),
                commission.getSourceAmount(),
                false);
            logger.info("更新累计金额：减去失败下发{}元", commission.getSourceAmount());

            String realCompanyId = commission.getRealCompanyId();
            if (!StringUtil.isEmpty(realCompanyId) && !commission.getCompanyId().equals(realCompanyId)){//转包服务公司
              //退转包服务公司在实际服务公司的记账户余额
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

              //退转包服务公司在实际服务公司的余额
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
               logger.error("退回转包服务公司在实际下发公司余额异常"+e.getMessage(), e);
             }
            }
            transferDealStatusNotifier
                .notify(commission.getOrderNo(), TransferStatus.TRANSFER_FAILED,
                    CommonRetCodes.UNCATCH_ERROR.getCode(), commission.getStatusDesc());
            logger.info("交易明细订单号：" + commission.getOrderNo() + "交易失败！系统内部回调失败！");
          } catch (Exception e) {
            logger.error("api拆单失败退还余额异常");
            logger.error(e.getMessage(), e);
          }
        }
      }
    }
    MDC.remove(PROCESS);
  }

}
