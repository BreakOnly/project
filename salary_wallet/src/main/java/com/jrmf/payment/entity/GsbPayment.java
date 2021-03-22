package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.gsbpay.GsbService;
import com.jrmf.payment.gsbpay.entity.TradeItemReq;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.AmountConvertUtil;
import com.jrmf.utils.SpringContextUtil;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsbPayment implements Payment<Map<String, Object>, Map<String, Object>, String> {

  private Logger logger = LoggerFactory.getLogger(GsbPayment.class);
  private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);

  public PaymentConfig payment;

  public GsbPayment(PaymentConfig payment) {
    this.payment = payment;
  }

  @Override
  public Map<String, Object> getTransferTemple(UserCommission userCommission) {
    // 封装请求参数
    Map<String, Object> tranDataMap = new TreeMap<>();

    TradeItemReq tradeItemReq = new TradeItemReq();
    tradeItemReq.setAccType("00");
    tradeItemReq.setAccNo(userCommission.getAccount());
    tradeItemReq.setAmt(AmountConvertUtil.changeY2F(userCommission.getAmount()));
    tradeItemReq.setIdType("00");
    tradeItemReq.setIdNo(userCommission.getCertId());
    tradeItemReq.setIdName(userCommission.getUserName());
    tradeItemReq.setMobile(userCommission.getPhoneNo());
    tradeItemReq.setNote(userCommission.getRemark());
    tradeItemReq.setSeqNo(userCommission.getOrderNo());

    tranDataMap.put("bizContent", Arrays.asList(tradeItemReq));
    tranDataMap.put("outTradeNo", userCommission.getOrderNo());

    return tranDataMap;
  }

  @Override
  public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
    logger.info("订单号：{} 付款通道为公司宝", userCommission.getOrderNo());
    PaymentReturn<String> transferReturn;
    try {
      //请求参数封装
      Map<String, Object> params = getTransferTemple(userCommission);
      //调用服务
      GsbService gsbService = new GsbService.Builder().reqUrl(payment.getPreHost())
          .appId(payment.getParameter1()).mchtId(payment.getParameter2())
          .secret(payment.getPayPrivateKey())
          .notifyUrl(baseInfo.getDomainName() + "/newPayNotify.do")
          .params(params)
          .build();
      Map<String, Object> result = gsbService.paymentTransfer();
      transferReturn = getTransferResult(result);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      transferReturn = new PaymentReturn<>(PayRespCode.RESP_UNKNOWN,
          PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN), userCommission.getOrderNo());
    }
    return transferReturn;
  }

  @Override
  public PaymentReturn<String> getTransferResult(Map<String, Object> result) {
    String code;
    String message;
    try {
      //处理响应
      if (null != result.get("code") && "0".equals(result.get("code"))) {
        //受理成功
        logger.info("付款受理成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
      } else {
        //付款受理失败
        logger.info("付款受理失败");
        logger.info("失败原因：" + result.get("code") + "，失败描述：" + result.get("msg"));
        code = PayRespCode.RESP_CHECK_FAIL;
        message = String.valueOf(result.get("msg"));
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
      GsbService gsbService = new GsbService.Builder().reqUrl(payment.getPreHost())
          .appId(payment.getParameter1()).mchtId(payment.getParameter2())
          .secret(payment.getPayPrivateKey())
          .build();

      Map<String, Object> result = gsbService.queryTransferResult(orderNo);
      if (null != result.get("code") && "0".equals(result.get("code"))) {
        //查询成功
        logger.info("订单号：" + orderNo + "查询成功");
        code = PayRespCode.RESP_SUCCESS;
        message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

        String tradeStatus = null;

        if (result.containsKey("tradeStatus")) {
          tradeStatus = (String) result.get("tradeStatus");
        }

        if ("00".equals(tradeStatus)) {
          //交易成功
          logger.info("订单号：{} 交易成功,交易流水号为：{}", orderNo, result.get("platformSeqNo"));
          transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
        } else if ("02".equals(tradeStatus) || "03".equals(tradeStatus)) {
          //交易失败
          logger.info("订单号：{} 交易失败：{}", orderNo, result.get("tradeDesc"));
          transCode = PayRespCode.RESP_TRANSFER_FAILURE;
          transMsg = (String) result.get("tradeDesc");
        } else {
          //待付款/汇款处理中
          logger.info("订单号：{} 处理中：{}", orderNo, result);
          transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
          transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
        }
      } else {
        code = PayRespCode.RESP_FAILURE;
        message = (String) result.get("msg");
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
