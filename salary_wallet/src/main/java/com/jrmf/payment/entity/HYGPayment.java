package com.jrmf.payment.entity;

import com.alibaba.fastjson.JSON;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.hygpay.HYGPayService;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.AmountConvertUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HYGPayment implements Payment<Map<String, String>, Map<String, String>, String> {

  private Logger logger = LoggerFactory.getLogger(NewPayPayment.class);

  public PaymentConfig payment;

  public HYGPayment(PaymentConfig payment) {
    this.payment = payment;
  }

  @Override
  public Map<String, String> getTransferTemple(UserCommission userCommission) {
    Map<String, String> params = new HashMap<>();

    params.put("workerName", userCommission.getUserName());
    params.put("workerAccount", userCommission.getAccount());
    params.put("idNumber", userCommission.getCertId());
    params.put("workerMobile", userCommission.getPhoneNo());
    params.put("distributeAmount", AmountConvertUtil.changeY2F(userCommission.getAmount()));
    params.put("remark", "服务费");
    params.put("requestNo", userCommission.getOrderNo());

    return params;
  }

  @Override
  public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
    logger.info("订单号：{} 付款通道为慧用工", userCommission.getOrderNo());
    PaymentReturn<String> transferReturn;
    try {
      //请求参数封装
      Map<String, String> params = getTransferTemple(userCommission);
      //调用服务
      HYGPayService hygPayService = new HYGPayService.Builder().reqUrl(this.payment.getPreHost())
          .merId(this.payment.getAppIdAyg()).payPublicKey(this.payment.getPayPublicKey())
          .payPrivateKey(this.payment.getPayPrivateKey()).params(params)
          .build();

      Map<String, String> result = hygPayService.paymentTransfer();
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
    String code;
    String message;
    try {
      //处理响应
      if (null != result.get("statusCode") && "000000".equals(result.get("statusCode"))) {
        //受理成功
        logger.info("付款受理成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
      } else {
        //付款受理失败
        logger.info("付款受理失败");
        logger.info("失败原因：" + result.get("statusCode") + "，失败描述：" + result.get("statusText"));
        code = PayRespCode.RESP_CHECK_FAIL;
        message = String.valueOf(result.get("statusText"));
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
      HYGPayService hygPayService = new HYGPayService.Builder().reqUrl(this.payment.getPreHost())
          .merId(this.payment.getAppIdAyg()).payPublicKey(this.payment.getPayPublicKey())
          .payPrivateKey(this.payment.getPayPrivateKey())
          .build();

      Map<String, String> result = hygPayService.queryTransferResult(orderNo);
      if (null != result.get("statusCode") && "000000".equals(result.get("statusCode"))) {
        //查询成功
        logger.info("订单号：" + orderNo + "查询成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

        Map<String, Object> data = null;

        if (result.containsKey("data")) {
          data = JSON.parseObject(String.valueOf(result.get("data")), Map.class);
        }

        String tradeStatus = String.valueOf(data.get("distributeStatus"));

        if ("60".equals(tradeStatus)) {
          //交易成功
          logger.info("订单号：{} 交易成功,交易流水号为：{}", orderNo, data.get("distributeId"));
          transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
        } else if ("75".equals(tradeStatus)) {
          //交易失败
          logger.info("订单号：{} 交易失败：{}", orderNo, data.get("remark"));
          transCode = PayRespCode.RESP_TRANSFER_FAILURE;
          transMsg = String.valueOf(data.get("remark"));
        } else {
          //待付款/汇款处理中
          logger.info("订单号：{} 处理中：{}", orderNo, result);
          transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
        }
      } else {
        code = PayRespCode.RESP_FAILURE;
        message = result.get("statusText");
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
