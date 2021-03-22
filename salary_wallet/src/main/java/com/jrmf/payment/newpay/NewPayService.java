package com.jrmf.payment.newpay;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.payment.newpay.constants.Constants;
import com.jrmf.payment.newpay.process.PayProcess;
import com.jrmf.payment.newpay.process.PayQueryProcess;
import com.jrmf.payment.newpay.util.CommonUtil;
import com.jrmf.payment.newpay.util.DateUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewPayService {

  private final static Logger logger = LoggerFactory.getLogger(NewPayService.class);

  private String merId;
  private String payPublicKey;
  private String payPrivateKey;
  private String notifyUrl;
  private String merKeyStrorepwd;
  private String merKeyKeyAlias;
  private String merKeyPrikeypwd;
  private Map<String, String> params;

  public NewPayService(String merId, String payPublicKey, String payPrivateKey,
      String notifyUrl, String merKeyStrorepwd, String merKeyKeyAlias,
      String merKeyPrikeypwd, Map<String, String> params) {
    this.merId = merId;
    this.payPublicKey = payPublicKey;
    this.payPrivateKey = payPrivateKey;
    this.notifyUrl = notifyUrl;
    this.params = params;
    this.merKeyStrorepwd = merKeyStrorepwd;
    this.merKeyKeyAlias = merKeyKeyAlias;
    this.merKeyPrikeypwd = merKeyPrikeypwd;
  }

  public Map<String, String> paymentTransfer() {

    String orderNo = params.get("merOrderId");

    try {

      Map<String, String> reqParams = new HashMap<>();
      // 版本号
      reqParams.put("version", Constants.TRAN_VERSION);
      // 交易代码
      reqParams.put("tranCode", Constants.TRAN_CODE_PAY);
      // 商户ID
      reqParams.put("merId", this.merId); //Constants.MER_ID
      // 商户订单号
      reqParams.put("merOrderId", orderNo);
      // 请求提交时间
      reqParams.put("submitTime", DateUtils.getCurrTime());
      // 签名类型
      reqParams.put("signType", Constants.SIGN_TYPE_RSA);
      // 编码方式
      reqParams.put("charset", Constants.CHARSET_UTF8);

      Map<String, String> jsonMap = new HashMap<String, String>();
      jsonMap.put("tranAmt", params.get("tranAmt"));
      jsonMap.put("payType", Constants.PAY_TYPE_TO_BANK);
      jsonMap.put("auditFlag", Constants.AUDIT_FLAG_NO);
      jsonMap.put("payeeName", params.get("payeeName"));
      jsonMap.put("payeeAccount", params.get("payeeAccount"));
      jsonMap.put("remark", params.get("remark"));
      jsonMap.put("payeeType", Constants.PAYEET_YPE_PERSON);
      jsonMap.put("notifyUrl", this.notifyUrl);
      jsonMap.put("paymentTerminalInfo", "01|10001");
      jsonMap.put("deviceInfo",
          "127.0.0.1|0123456789AB|012345678912345|543219876543210|01234567890123456789|BA9876543210|006");
      // 转换为JSON字符串
      String jsonStr = JSONObject.toJSONString(jsonMap);
      // 报文密文
      reqParams.put("msgCiphertext", CommonUtil.doEncryt(jsonStr, this.payPublicKey));
      // 签名数据
      reqParams.put("signValue", PayProcess
          .buildSignParam(reqParams, this.payPrivateKey, this.merKeyStrorepwd, this.merKeyKeyAlias,
              this.merKeyPrikeypwd));

      logger.info("新生支付单笔付款请求报文:{}", reqParams);

      // 向新生发起请求
      String respJsonStr = CommonUtil.doSubmit(Constants.URL_PAY, reqParams);

      logger.info("新生支付单笔付款请求响应报文:{}", respJsonStr);

      // 对新生响应报文进行验签
      if (PayProcess.doVerifySign(respJsonStr, this.payPublicKey)) {
        HashMap<String, String> respMap = (HashMap<String, String>) JSONObject
            .parseObject(respJsonStr, Map.class);
        return respMap;
      }

      logger.error("新生支付单笔付款请求响应报文验签失败,订单号：{}", orderNo);

    } catch (Exception e) {
      logger.error("新生支付单笔付款请求异常,订单号:{}", orderNo);
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public Map<String, String> queryTransferResult(String orderNo) {

    try {

      Map<String, String> reqParams = new HashMap<>();
      // 版本号
      reqParams.put("version", Constants.QUERY_VERSION);
      // 交易代码
      reqParams.put("tranCode", Constants.TRAN_CODE_PAY_QUERY);
      // 商户ID
      reqParams.put("merId", this.merId);
      // 商户订单号
      reqParams.put("merOrderId", orderNo);
      // 请求提交时间
      reqParams.put("submitTime", DateUtils.getCurrTimeDay());
      // 签名类型
      reqParams.put("signType", Constants.SIGN_TYPE_RSA);
      // 编码方式
      reqParams.put("charset", Constants.CHARSET_UTF8);
      // 签名数据
      reqParams.put("signValue", PayQueryProcess
          .buildSignParam(reqParams, this.payPrivateKey, this.merKeyStrorepwd, this.merKeyKeyAlias,
              this.merKeyPrikeypwd));

      logger.info("新生支付单笔付款结果查询请求报文:{}", reqParams);

      String respJsonStr = CommonUtil.doSubmit(Constants.URL_PAY_QUERY, reqParams);

      logger.info("新生支付单笔付款结果查询响应报文:{}", respJsonStr);

      // 对新生响应报文进行验签
      if (PayQueryProcess.doVerifySign(respJsonStr, this.payPublicKey)) {
        HashMap<String, String> respMap = (HashMap<String, String>) JSONObject
            .parseObject(respJsonStr, Map.class);

        return respMap;
      }

      logger.error("新生支付单笔付款结果查询请求响应报文验签失败,订单号：{}", orderNo);

    } catch (Exception e) {
      logger.error("新生支付单笔付款结果查询请求异常,订单号:{}", orderNo);
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  public static class Builder {

    private String merId;
    private String payPublicKey;
    private String payPrivateKey;
    private String notifyUrl;
    private String merKeyStrorepwd;
    private String merKeyKeyAlias;
    private String merKeyPrikeypwd;
    private Map<String, String> params;

    public NewPayService.Builder merId(String merId) {
      this.merId = merId;
      return this;
    }

    public NewPayService.Builder payPublicKey(String payPublicKey) {
      this.payPublicKey = payPublicKey;
      return this;
    }


    public NewPayService.Builder payPrivateKey(String payPrivateKey) {
      this.payPrivateKey = payPrivateKey;
      return this;
    }

    public NewPayService.Builder notifyUrl(String notifyUrl) {
      this.notifyUrl = notifyUrl;
      return this;
    }

    public NewPayService.Builder merKeyStrorepwd(String merKeyStrorepwd) {
      this.merKeyStrorepwd = merKeyStrorepwd;
      return this;
    }


    public NewPayService.Builder merKeyKeyAlias(String merKeyKeyAlias) {
      this.merKeyKeyAlias = merKeyKeyAlias;
      return this;
    }

    public NewPayService.Builder merKeyPrikeypwd(String merKeyPrikeypwd) {
      this.merKeyPrikeypwd = merKeyPrikeypwd;
      return this;
    }

    public NewPayService.Builder params(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public NewPayService build() {
      return new NewPayService(merId, payPublicKey, payPrivateKey,
          notifyUrl, merKeyStrorepwd, merKeyKeyAlias, merKeyPrikeypwd, params);
    }
  }


}
