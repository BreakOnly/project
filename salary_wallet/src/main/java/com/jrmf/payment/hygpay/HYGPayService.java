package com.jrmf.payment.hygpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.payment.hygpay.util.AESUtils;
import com.jrmf.payment.hygpay.util.RSAUtils;
import com.jrmf.utils.StringUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HYGPayService {

  private final static Logger logger = LoggerFactory.getLogger(HYGPayService.class);

  private String merId;
  private String payPublicKey;
  private String payPrivateKey;
  private String notifyUrl;
  private String reqUrl;
  private Map<String, String> params;

  public HYGPayService(String reqUrl, String merId, String payPublicKey, String payPrivateKey,
      String notifyUrl, Map<String, String> params) {
    this.reqUrl = reqUrl;
    this.merId = merId;
    this.payPublicKey = payPublicKey;
    this.payPrivateKey = payPrivateKey;
    this.notifyUrl = notifyUrl;
    this.params = params;
  }

  public Map<String, String> paymentTransfer() {

    String orderNo = params.get("requestNo");

    try {

      Map<String, Object> reqParams = new HashMap<>();
      reqParams.put("cooperatorId", this.merId);
      reqParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
      reqParams.put("workerName", params.get("workerName"));
      reqParams.put("workerAccount", params.get("workerAccount"));
      reqParams.put("workerType", "1");//证件类型 1、身份证 2、港澳居民来往大陆通行证 3、护照 5、台湾居民来往大陆通行证
      reqParams.put("receiptChannel", "10");//收款渠道  10、银行卡  20、支付宝  30、微信
      reqParams.put("idNumber", params.get("idNumber"));
      reqParams.put("workerMobile", params.get("workerMobile"));
      reqParams.put("distributeAmount", params.get("distributeAmount"));
      reqParams.put("requestNo", params.get("requestNo"));
      reqParams.put("remark", params.get("remark"));
      String s1 = RSAUtils.sortParam(reqParams);
      String sign = RSAUtils.sign(s1.getBytes(), this.payPrivateKey);
      reqParams.put("sign", sign);
      String jsonString = JSON.toJSONString(reqParams);

      logger.info("慧用工单笔付款请求签名加密前请求明文:{}", jsonString);

      String businessBody = AESUtils.encrypt2Hex(jsonString, this.payPublicKey);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("cooperatorId", this.merId);
      jsonObject.put("businessBody", businessBody);

//      logger.info("慧用工单笔付款请求签名加密后请求密文:{}", jsonObject);

      String res = reqSevice(this.reqUrl + HYGPayConfig.PAYMENT_PAY,
          jsonObject.toJSONString());

      Map<String, String> resultMap = JSON.parseObject(res, Map.class);
//      logger.info("慧用工单笔付款响应 requestNo: {} 返回值解密之前: {}", reqParams.get("requestNo"), res);
      String dataBefore = resultMap.get("data");
      if (!StringUtil.isEmpty(dataBefore)) {
        String dataAfter = AESUtils.decryptByHex(dataBefore, this.payPublicKey);
        resultMap.put("data", dataAfter);
      }
      logger.info("慧用工单笔付款响应 requestNo: {} 返回值解密之后: {}", reqParams.get("requestNo"), resultMap);

      return resultMap;

    } catch (Exception e) {
      logger.error("慧用工单笔付款请求异常,订单号:{}", orderNo);
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public Map<String, String> queryTransferResult(String orderNo) {

    try {

      Map<String, Object> reqParams = new HashMap<>();
      reqParams.put("cooperatorId", this.merId);
      reqParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
      reqParams.put("requestNo", orderNo);
      String s1 = RSAUtils.sortParam(reqParams);
      String sign = RSAUtils.sign(s1.getBytes(), this.payPrivateKey);
      reqParams.put("sign", sign);
      String jsonString = JSON.toJSONString(reqParams);

      logger.info("慧用工单笔付款查询请求签名加密前请求明文:{}", jsonString);

      String s = AESUtils.encrypt2Hex(jsonString, this.payPublicKey);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("cooperatorId", this.merId);
      jsonObject.put("businessBody", s);

//      logger.info("慧用工单笔付款查询请求签名加密后请求密文:{}", jsonObject);

      String res = reqSevice(this.reqUrl + HYGPayConfig.PAYMENT_QUERY,
          jsonObject.toJSONString());
      Map<String, String> resultMap = JSON.parseObject(res, Map.class);
//      logger.info("慧用工单笔付款查询响应 requestNo: {} 返回值解密之前: {}", orderNo, res);
      String dataBefore = resultMap.get("data");
      if (!StringUtil.isEmpty(dataBefore)) {
        String dataAfter = AESUtils.decryptByHex(dataBefore, this.payPublicKey);
        resultMap.put("data", dataAfter);
      }
      logger.info("慧用工单笔付款查询响应 requestNo: {} 返回值解密之后: {}", orderNo, resultMap);

      return resultMap;

    } catch (Exception e) {
      logger.error("慧用工单笔付款结果查询请求异常,订单号:{}", orderNo);
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  public static String reqSevice(String url, String params) throws Exception {
    logger.info("慧用工通道 请求地址:{} 请求参数：{}", url, params);

    RestTemplate client = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = HttpMethod.POST;
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    //将请求头部和参数合成一个请求
    HttpEntity requestEntity = new HttpEntity(params, headers);
    //执行HTTP请求，将返回的结构使用Response类格式化
    ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);

    logger.info("慧用工通道 请求地址:{} 响应参数:{}", url, response.getBody());
    return response.getBody();
  }

  public static class Builder {

    private String merId;
    private String payPublicKey;
    private String payPrivateKey;
    private String notifyUrl;
    private String reqUrl;
    private Map<String, String> params;

    public HYGPayService.Builder merId(String merId) {
      this.merId = merId;
      return this;
    }

    public HYGPayService.Builder payPublicKey(String payPublicKey) {
      this.payPublicKey = payPublicKey;
      return this;
    }


    public HYGPayService.Builder payPrivateKey(String payPrivateKey) {
      this.payPrivateKey = payPrivateKey;
      return this;
    }

    public HYGPayService.Builder notifyUrl(String notifyUrl) {
      this.notifyUrl = notifyUrl;
      return this;
    }

    public HYGPayService.Builder reqUrl(String reqUrl) {
      this.reqUrl = reqUrl;
      return this;
    }

    public HYGPayService.Builder params(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public HYGPayService build() {
      return new HYGPayService(reqUrl, merId, payPublicKey, payPrivateKey,
          notifyUrl, params);
    }
  }


}
