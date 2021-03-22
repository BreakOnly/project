package com.jrmf.payment.mybankapi.common.util;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.constant.GatewayConstant;
import com.jrmf.payment.mybankapi.request.MyBankBaseRequest;
import com.jrmf.payment.mybankapi.response.MyBankBaseResponse;
import com.jrmf.payment.newpay.process.PayProcess;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Log4j
public class MyBankHttpClientUtil {

  private static final Logger logger = LoggerFactory.getLogger(MyBankHttpClientUtil.class);

  public static String doHttpClientPost(String reqUrl, String keyStoreName,
      String configFilePath,
      MyBankBaseRequest params) {

    CloseableHttpClient httpClient = HttpClients.createDefault();
    CloseableHttpResponse response = null;
    String rst = null;

    try {

      Map<String, String> send = MagCore
          .paraFilter2(JSON.parseObject(JSON.toJSONString(params), Map.class));
      String sign = MagCore
          .buildRequestByTWSIGN(send, params.getCharset(), keyStoreName, configFilePath);

      send.put("sign", sign);
      send.put("sign_type", params.getSign_type());

      List<NameValuePair> ps = buildPostParams(send);
      HttpPost post = new HttpPost(reqUrl);
      post.setEntity(new UrlEncodedFormEntity(ps, "UTF-8"));

      logger.info("网商银行 请求参数：{}", params.toString());

      response = httpClient.execute(post);
      if (200 == response.getStatusLine().getStatusCode()) {// 网关调用成功
        rst = inputStreamToStr(response.getEntity().getContent(), "UTF-8");
        logger.info("网商银行 请求响应：{}", rst);
      }

    } catch (Exception e) {
      logger.error("网商银行 请求异常：{}", e);
    } finally {
      try {
        if (null != response) {
          response.close();
        }
        if (null != httpClient) {
          httpClient.close();
        }
      } catch (IOException e) {
        logger.error("网商银行 关闭流异常：{}", e);
      }
    }
    return rst;
  }

  private static List<NameValuePair> buildPostParams(Map<String, String> params) {
    if (params == null || params.size() == 0) {
      return null;
    }
    List<NameValuePair> results = new ArrayList<NameValuePair>();

    for (Map.Entry<String, String> entry : params.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      results.add(new BasicNameValuePair(key, value));
    }

    return results;
  }

  private static String inputStreamToStr(InputStream is, String charset) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
    StringBuffer buffer = new StringBuffer();
    String line = "";
    while ((line = in.readLine()) != null) {
      buffer.append(line);
    }
    return new String(buffer.toString().getBytes("ISO-8859-1"), charset);
  }

}
