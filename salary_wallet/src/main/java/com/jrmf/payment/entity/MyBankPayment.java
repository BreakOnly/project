package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.request.PaymentTransferRequest;
import com.jrmf.payment.mybankapi.request.QueryAccountBalanceRequest;
import com.jrmf.payment.mybankapi.request.QueryTransferResultRequest;
import com.jrmf.payment.mybankapi.response.MyBankBaseResponse;
import com.jrmf.payment.mybankapi.response.QueryTransferResultResponse;
import com.jrmf.payment.mybankapi.service.MyBankService;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBankPayment implements
    Payment<PaymentTransferRequest, MyBankBaseResponse, String> {

  private Logger logger = LoggerFactory.getLogger(MyBankPayment.class);
  private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);

  public PaymentConfig payment;

  public MyBankPayment(PaymentConfig payment) {
    this.payment = payment;
  }

  @Override
  public PaymentTransferRequest getTransferTemple(UserCommission userCommission) {

    PaymentTransferRequest request = new PaymentTransferRequest();
    request.setOuter_trade_no(userCommission.getOrderNo());
    request.setOuter_inst_order_no(userCommission.getOrderNo());
    request.setUid(userCommission.getOriginalId());
    //开户后提供
    request.setWhite_channel_code(payment.getParameter3());
    request.setAccount_type("BASIC");
    request.setBank_account_no(userCommission.getAccount());
    request.setAccount_name(userCommission.getUserName());

    if (PayType.ALI_PAY.getCode() == userCommission.getPayType()) {
      request.setBank_code("ALIPAY");
    }

    request.setCard_type("DC");
    request.setCard_attribute("C");
    request.setAmount(userCommission.getAmount());
    if (ArithmeticUtil.compareTod(userCommission.getSumFee(), "0") > 0) {
      request.setFee_info("{\"buyerFee\":\"" + userCommission.getSumFee() + "\"}");
    }
    request.setNotify_url(baseInfo.getDomainName() + "/myBankNotify.do");

    return request;
  }

  @Override
  public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
    logger.info("订单号：{} 付款通道为网商支付", userCommission.getOrderNo());
    PaymentReturn<String> transferReturn;
    try {

      MyBankService myBankService = new MyBankService.Builder()
          .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
          .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
          .build();

      MyBankBaseResponse response = myBankService
          .paymentTransfer(getTransferTemple(userCommission));
      transferReturn = getTransferResult(response);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      transferReturn = new PaymentReturn<>(PayRespCode.RESP_UNKNOWN,
          PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN), userCommission.getOrderNo());
    }

    return transferReturn;
  }

  @Override
  public PaymentReturn<String> getTransferResult(MyBankBaseResponse response) {
    String code = null;
    String message = null;
    try {
      //处理响应
      if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
        //受理成功
        logger.info("付款受理成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
      } else if (BaseRequestConstant.FAIL.equals(response.getIs_success())) {
        //付款受理失败
        logger.info("付款受理失败");
        logger.info("失败原因：{}，失败描述：{}", response.getError_code(), response.getError_message());
        code = PayRespCode.RESP_UNKNOWN;
        message = response.getError_message();
      }
    } catch (Exception e) {
      logger.error("付款受理异常", e);
      code = PayRespCode.RESP_UNKNOWN;
      message = "付款受理异常";
    }
    return new PaymentReturn<>(code, message, "");
  }

  @Override
  public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {
    String code;
    String message;
    String transCode = "";
    String transMsg = "";
    PaymentReturn<TransStatus> paymentReturn;
    try {

      MyBankService myBankService = new MyBankService.Builder()
          .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
          .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
          .build();

      QueryTransferResultRequest request = new QueryTransferResultRequest();
      request.setOuter_trade_no(orderNo);

      QueryTransferResultResponse response = (QueryTransferResultResponse) myBankService
          .queryTransferResult(request);
      if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
        //查询成功
        logger.info("订单号：" + orderNo + "查询成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

        if (BaseRequestConstant.TRADE_FINISHED.equals(response.getTrade_status())) {
          //交易成功
          logger.info("订单号：{} 交易成功,交易流水号为：{}", orderNo, response.getInner_trade_no());
          transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
        } else if (BaseRequestConstant.TRADE_FAILED.equals(response.getTrade_status())) {
          //交易失败
          logger.info("订单号：{} 交易失败：{}", orderNo, response.getFail_reason());
          transCode = PayRespCode.RESP_TRANSFER_FAILURE;
          transMsg = response.getFail_reason();
        } else {
          //待付款/汇款处理中
          logger.info("订单号：{} 处理中：{}", orderNo, response);
          transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
        }
      } else {
        code = PayRespCode.RESP_FAILURE;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
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
