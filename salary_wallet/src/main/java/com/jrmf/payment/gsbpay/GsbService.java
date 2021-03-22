package com.jrmf.payment.gsbpay;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.gsbpay.entity.TradePayRsp;
import com.jrmf.payment.gsbpay.entity.TradeQueryItemRsp;
import com.jrmf.payment.util.PaymentReturn;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class GsbService {

  private final static Logger logger = LoggerFactory.getLogger(GsbService.class);

  private String appId;
  private String mchtId;
  private String secret;
  private String notifyUrl;
  private String reqUrl;
  private Map<String, Object> params;

  public GsbService(String appId, String mchtId, String secret, String notifyUrl, String reqUrl,
      Map<String, Object> params) {
    this.appId = appId;
    this.mchtId = mchtId;
    this.secret = secret;
    this.params = params;
    this.reqUrl = reqUrl;
    this.notifyUrl = notifyUrl;
  }

  public Map<String, Object> paymentTransfer() {

    try {

      // 封装请求参数
      Map<String, Object> reqParams = new TreeMap<>();
      reqParams.put("notityUrl", this.notifyUrl); //baseInfo.getDomainName() + "/gsbNotify.do"
      reqParams.put("tradeTime",
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
      reqParams.put("appId", this.appId);
      reqParams.put("outTradeNo", this.params.get("outTradeNo"));
      reqParams.put("mchtId", this.mchtId);
      reqParams.put("signType", "MD5");
      reqParams.put("currency", "CNY");
      reqParams.put("nonceStr", this.params.get("outTradeNo"));

      String sign = this.sign(reqParams);
      reqParams.put("sign", sign);

      logger.info("公司宝通道下发签名后字符串:{}", sign);

      reqParams.put("bizContent", this.params.get("bizContent"));

      logger.info("公司宝生成签名后放入转账人员信息:{}", this.params.get("bizContent"));

      TradePayRsp tradePayRsp = reqSevice(this.reqUrl + GsbConfig.PAYMENT_PAY,
          JSON.toJSONString(reqParams));

      Map<String, Object> result = new HashMap<>();
      result.put("code", tradePayRsp.getCode());
      result.put("msg", tradePayRsp.getMsg());

      logger.info("公司宝下发响应格式化参数：{}", tradePayRsp);

      return result;

    } catch (Exception e) {
      logger.error("公司宝下发异常订单:{}", this.params.get("outTradeNo"));
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public Map<String, Object> queryTransferResult(String orderNo) {

    // 封装请求参数
    Map<String, Object> data = new TreeMap<>();
    data.put("appId", this.appId);
    data.put("mchtId", this.mchtId);
    data.put("nonceStr", orderNo); // TODO 自己生成一个16位的随机码
    data.put("outTradeNo", orderNo);
    data.put("signType", "MD5");

    String sign = this.sign(data);
    data.put("sign", sign);

    logger.info("公司宝通道下发查询签名后字符串:{}", sign);

    try {
      TradePayRsp tradePayRsp = reqSevice(this.reqUrl + GsbConfig.PAYMENT_QUERY,
          JSON.toJSONString(data));

      Map<String, Object> result = new HashMap<>();
      result.put("code", tradePayRsp.getCode());
      result.put("msg", tradePayRsp.getMsg());

      if (null != tradePayRsp.getData() && null != tradePayRsp.getData().getBizContent()
          && tradePayRsp.getData().getBizContent().size() > 0) {
        TradeQueryItemRsp tradeQueryItemRsp = tradePayRsp.getData().getBizContent().get(0);
        result.put("tradeStatus", tradeQueryItemRsp.getTradeStatus());
        result.put("tradeDesc", tradeQueryItemRsp.getRespDesc());
        result.put("platformSeqNo", tradeQueryItemRsp.getPlatformSeqNo());
      }

      logger.info("公司宝下发响应格式化参数：{}", tradePayRsp);

      return result;

    } catch (Exception e) {

      logger.error("公司宝下发查询异常订单:{}", orderNo);
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public Map<String, Object> queryBalanceResult() throws Exception {

    // 封装请求参数
    Map<String, Object> data = new TreeMap<>();
    data.put("appId", this.appId);
    data.put("mchtId", this.mchtId);
    data.put("nonceStr", "1234567812345678"); // TODO 自己生成一个16位的随机码
    data.put("signType", "MD5");

    String sign = this.sign(data);
    data.put("sign", sign);

    logger.info("公司宝通道余额查询签名后字符串:{}", sign);

    TradePayRsp tradePayRsp = reqSevice(this.reqUrl + GsbConfig.BALANCE_QUERY,
        JSON.toJSONString(data));

    logger.info("公司宝余额查询响应格式化参数：{}", tradePayRsp);

    return null;

  }

  private static String byteArrayToHexString(byte b[]) {
    StringBuffer resultSb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      resultSb.append(byteToHexString(b[i]));
    }

    return resultSb.toString();
  }

  private static String byteToHexString(byte b) {
    int n = b;
    if (n < 0) {
      n += 256;
    }
    int d1 = n / 16;
    int d2 = n % 16;
    return hexDigits[d1] + hexDigits[d2];
  }

  public static String md5(String origin) {
    String resultString = null;
    try {
      resultString = new String(origin);
      MessageDigest md = MessageDigest.getInstance("MD5");
      resultString = byteArrayToHexString(md.digest(resultString
          .getBytes()));
    } catch (Exception exception) {
    }
    return resultString;
  }

  private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
      "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};


  public static TradePayRsp reqSevice(String url, String params) throws Exception {
    logger.info("公司宝 请求地址:{} 请求参数：{}", url, params);

    RestTemplate client = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = HttpMethod.POST;
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    //将请求头部和参数合成一个请求
    HttpEntity requestEntity = new HttpEntity(params, headers);
    //执行HTTP请求，将返回的结构使用Response类格式化
    ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);

    logger.info("公司宝 请求地址:{} 响应参数:{}", url, response.getBody());
    return JSON.parseObject(response.getBody(), TradePayRsp.class);
  }

  public String sign(Map<String, Object> data) {
    // 生成签名字符串
    List<String> list = new ArrayList<>();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      list.add(entry.getKey() + "=" + entry.getValue());
    }
    // 加入 发薪系统提供的 key
    list.add("key=" + this.secret);
    String signStr = StringUtils.join(list, "&");
    // 打印签名前字符串
    logger.info("公司宝通道签名前字符串:{}", signStr);

    // 获取签名，放到请求参数中
    String sign = md5(signStr).toUpperCase();

    return sign;
  }

  public static class Builder {

    private String appId;
    private String mchtId;
    private String secret;
    private String notifyUrl;
    private String reqUrl;
    private Map<String, Object> params;

    public GsbService.Builder appId(String appId) {
      this.appId = appId;
      return this;
    }

    public GsbService.Builder mchtId(String mchtId) {
      this.mchtId = mchtId;
      return this;
    }


    public GsbService.Builder secret(String secret) {
      this.secret = secret;
      return this;
    }

    public GsbService.Builder notifyUrl(String notifyUrl) {
      this.notifyUrl = notifyUrl;
      return this;
    }

    public GsbService.Builder reqUrl(String reqUrl) {
      this.reqUrl = reqUrl;
      return this;
    }

    public GsbService.Builder params(Map<String, Object> params) {
      this.params = params;
      return this;
    }

    public GsbService build() {
      return new GsbService(appId, mchtId, secret, notifyUrl, reqUrl, params);
    }
  }

}
