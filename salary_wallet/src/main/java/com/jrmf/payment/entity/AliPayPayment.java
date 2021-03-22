package com.jrmf.payment.entity;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransOrderQueryModel;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliPayPayment implements
    Payment<AlipayFundTransToaccountTransferRequest, AlipayFundTransToaccountTransferResponse, String> {

  private Logger logger = LoggerFactory.getLogger(AliPayPayment.class);

  public PaymentConfig payment;

  public AliPayPayment(PaymentConfig payment) {
    super();
    this.payment = payment;
  }

  @Override
  public AlipayFundTransToaccountTransferRequest getTransferTemple(UserCommission userCommission) {

    AlipayFundTransToaccountTransferRequest transRequest = new AlipayFundTransToaccountTransferRequest();
    transRequest.setBizContent("{"
        + "\"out_biz_no\":\""
        + userCommission.getOrderNo() + "\","
        + "\"payee_type\":\""
        + "ALIPAY_LOGONID" + "\"," //支付宝登录账号
        + "\"payee_account\":\""
        + userCommission.getAccount() + "\","
        + "\"amount\":\""
        + userCommission.getAmount() + "\","
        + "\"payer_show_name\":\""
        + userCommission.getCompanyName() + "\","
        + "\"payee_real_name\":\""
        + userCommission.getUserName() + "\","
        + "\"remark\":\""
        + userCommission.getRemark() + "\""
        + "}");

    logger.info("支付宝上送下发参数：" + transRequest.getBizContent() + "-------");
    return transRequest;
  }

  @Override
  public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

    AlipayFundTransToaccountTransferRequest request = getTransferTemple(userCommission);
    AlipayFundTransToaccountTransferResponse response = null;
    PaymentReturn<String> paymentReturn = null;

    try {
      AlipayClient alipayClient = new DefaultAlipayClient(payment.getPreHost(),
          payment.getCorporationAccount(),
          payment.getPayPrivateKey(),
          payment.getParameter1(),
          payment.getParameter2(),
          payment.getPayPublicKey(),
          payment.getParameter3());
      response = alipayClient.execute(request);
      paymentReturn = getTransferResult(response);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      paymentReturn = new PaymentReturn<String>(PayRespCode.RESP_UNKNOWN,
          PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),
          "");
    }

    return paymentReturn;
  }

  @Override
  public PaymentReturn<String> getTransferResult(
      AlipayFundTransToaccountTransferResponse paramter) {

    String code = "";
    String message = "";

    if (PayRespCode.ALI_TRANS_CODE_SUCCESS.equals(paramter.getCode())) {
      code = PayRespCode.RESP_SUCCESS;
      message = paramter.getMsg();
    } else {
      code = PayRespCode.RESP_FAILURE;
      message = paramter.getSubMsg();
    }
    String orderNo = paramter.getOutBizNo();

    PaymentReturn<String> transferReturn = new PaymentReturn<String>(code,
        message,
        orderNo);

    logger.info("支付宝下发返回参数：" + transferReturn.toString());

    return transferReturn;
  }

  @Override
  public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

    String code = "";
    String massage = "";
    String transCode = "";
    String transMsg = "";
    String transOrderNo = "";

    AlipayFundTransOrderQueryResponse queryResult = null;
    AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
    model.setOutBizNo(orderNo);
    AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
    request.setBizModel(model);
    logger.info("支付宝查询上送参数：" + request.getBizModel());
    try {
      AlipayClient alipayClient = new DefaultAlipayClient(payment.getPreHost(),
          payment.getCorporationAccount(),
          payment.getPayPrivateKey(),
          payment.getParameter1(),
          payment.getParameter2(),
          payment.getPayPublicKey(),
          payment.getParameter3());
      queryResult = alipayClient.execute(request);
      logger.info("支付宝查询返回参数：" + queryResult.toString());
      if (PayRespCode.ALI_TRANS_CODE_SUCCESS.equals(queryResult.getCode())) {
        code = PayRespCode.RESP_SUCCESS;
        if (PayRespCode.ALI_ORDER_STATUS_SUCCESS.equals(queryResult.getStatus())) {
          transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
        } else if (PayRespCode.ALI_ORDER_STATUS_FAIL.equals(queryResult.getStatus())
            || PayRespCode.ALI_ORDER_STATUS_REFUND.equals(queryResult.getStatus())) {
          transCode = PayRespCode.RESP_TRANSFER_FAILURE;
        } else {
          transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
        }
      } else {
        code = PayRespCode.RESP_SUCCESS;
        transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
      }

    } catch (AlipayApiException e) {
      logger.error(e.getMessage(), e);
      code = PayRespCode.RESP_FAILURE;
    }

    massage = queryResult.getMsg();
    transMsg = queryResult.getSubMsg();

    TransStatus transStatus = new TransStatus(transOrderNo,
        transCode,
        transMsg);

    PaymentReturn<TransStatus> paymentReturn = new PaymentReturn<>(code,
        massage,
        transStatus);
    logger.info("支付宝查询返回参数paymentReturn：" + paymentReturn.toString());
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
