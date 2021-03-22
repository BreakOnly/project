package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.newpay.NewPayService;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.SpringContextUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewPayPayment implements Payment<Map<String, String>, Map<String, String>, String> {

  private Logger logger = LoggerFactory.getLogger(NewPayPayment.class);
  private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);

  public PaymentConfig payment;

  public NewPayPayment(PaymentConfig payment) {
    this.payment = payment;
  }

  @Override
  public Map<String, String> getTransferTemple(UserCommission userCommission) {
    Map<String, String> params = new HashMap<>();

    params.put("merOrderId", userCommission.getOrderNo());
    params.put("tranAmt", userCommission.getAmount());
    params.put("payeeName", userCommission.getUserName());
    params.put("payeeAccount", userCommission.getAccount());
    params.put("remark", userCommission.getRemark());

    return params;
  }

  @Override
  public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
    logger.info("订单号：{} 付款通道为新生支付", userCommission.getOrderNo());
    PaymentReturn<String> transferReturn;
    try {
      //请求参数封装
      Map<String, String> params = getTransferTemple(userCommission);
      //调用服务
      NewPayService newPayService = new NewPayService.Builder()
          .merId(payment.getApiKey()).payPublicKey(payment.getPayPublicKey())
          .payPrivateKey(payment.getPayPrivateKey()).merKeyStrorepwd(payment.getParameter1())
          .merKeyKeyAlias(payment.getParameter2()).merKeyPrikeypwd(payment.getParameter3())
          .notifyUrl(baseInfo.getDomainName() + "/newPayNotify.do").params(params)
          .build();

      Map<String, String> result = newPayService.paymentTransfer();
      transferReturn = getTransferResult(result);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      transferReturn = new PaymentReturn<>(PayRespCode.RESP_UNKNOWN,
          PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN), userCommission.getOrderNo());
    }
    return transferReturn;
  }

  @Override
  public PaymentReturn<String> getTransferResult(Map<String, String> result) {
    String code = null;
    String message = null;
    try {
      //处理响应
      if (null != result.get("resultCode") && ("0000".equals(result.get("resultCode")) || "9999"
          .equals(result.get("resultCode")))) {
        //受理成功
        logger.info("付款受理成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
      } else if ("4444".equals(result.get("resultCode"))) {
        //付款受理失败
        logger.info("付款受理失败");
        logger.info("失败原因：" + result.get("errorCode") + "，失败描述：" + result.get("errorMsg"));
        code = PayRespCode.RESP_UNKNOWN;
        message = String.valueOf(result.get("errorMsg"));
      }
    } catch (Exception e) {
      logger.error("付款受理异常", e);
      code = PayRespCode.RESP_UNKNOWN;
      message = "付款受理异常";
    }
    PaymentReturn<String> transferReturn = new PaymentReturn<String>(code, message, "");
    return transferReturn;
  }

  @Override
  public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {
    String code;
    String message;
    String transCode = "";
    String transMsg = "";
    PaymentReturn<TransStatus> paymentReturn;
    try {

      //调用服务
      NewPayService newPayService = new NewPayService.Builder()
          .merId(payment.getApiKey()).payPublicKey(payment.getPayPublicKey())
          .payPrivateKey(payment.getPayPrivateKey()).merKeyStrorepwd(payment.getParameter1())
          .merKeyKeyAlias(payment.getParameter2()).merKeyPrikeypwd(payment.getParameter3())
          .build();

      Map<String, String> result = newPayService.queryTransferResult(orderNo);
      if (null != result.get("resultCode") && "0000".equals(result.get("resultCode"))) {
        //查询成功
        logger.info("订单号：" + orderNo + "查询成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

        String tradeStatus = null;

        if (result.containsKey("orderStatus")) {
          tradeStatus = result.get("orderStatus");
        }

        if ("1".equals(tradeStatus)) {
          //交易成功
          logger.info("订单号：{} 交易成功,交易流水号为：{}", orderNo, result.get("hnapayOrderId"));
          transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
        } else if ("2".equals(tradeStatus)) {
          //交易失败
          logger.info("订单号：{} 交易失败：{}", orderNo, result.get("orderFailedMsg"));
          transCode = PayRespCode.RESP_TRANSFER_FAILURE;
          transMsg = result.get("orderFailedMsg");
        } else {
          //待付款/汇款处理中
          logger.info("订单号：{} 处理中：{}", orderNo, result);
          transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
        }
      } else {
        code = PayRespCode.RESP_FAILURE;
        message = result.get("errorMsg");
      }
      TransStatus transStatus = new TransStatus(orderNo, transCode, transMsg);
      paymentReturn = new PaymentReturn<>(code, message, transStatus);
    } catch (Exception e) {
      code = PayRespCode.RESP_FAILURE;
      message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
      paymentReturn = new PaymentReturn<>(code, message, null);
    }
    return paymentReturn;
  }

  @Override
  public PaymentReturn<String> queryBalanceResult(String type) {
    return null;
  }

  @Override
  public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
    return null;
  }

  @Override
  public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(
      LinkageQueryTranHistory queryParams) {
    return null;
  }
}
