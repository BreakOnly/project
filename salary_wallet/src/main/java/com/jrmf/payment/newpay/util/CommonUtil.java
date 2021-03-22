package com.jrmf.payment.newpay.util;

import com.jrmf.payment.newpay.constants.Constants;
import com.jrmf.payment.newpay.process.PayProcess;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommonUtil {

  private static final Logger logger = LoggerFactory.getLogger(PayProcess.class);

  /**
   * 向新生发起单笔付款交易请求
   */
  public static String doSubmit(String url, Map<String, String> reqParams) throws Exception {
    HttpTransport httpTransport = new HttpTransport();
    HttpPost method = new HttpPost();
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)
        .setConnectTimeout(30000).setConnectionRequestTimeout(30000).build();
    method.setHeader(HTTP.CONTENT_ENCODING, "UTF-8");
    method.setHeader(HTTP.USER_AGENT, "Rich Powered/1.0");
    method.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
    method.setConfig(requestConfig);
    method.setURI(new URI(url + "?v=" + UUID.randomUUID()));
    httpTransport.setMethod(method);
    String response;

    if (url.startsWith("https")) {
      response = httpTransport.submit_https(reqParams);
    } else {
      response = httpTransport.submit(reqParams);
    }
    return response;
  }

  /**
   * 执行签名操作
   */
  public static String doSign(String signPlainStr, String rsaPath,String merKeyStrorepwd,String merKeyKeyAlias,String merKeyPrikeypwd) throws Exception {
    try {
      logger.info("商户私钥签名. 明文串={}", signPlainStr);

      // 从jks文件中加载rsa商户私钥(此处请填入商户收款秘钥JKS文件路径)
//      String jksPath = CommonUtil.class.getClassLoader()
//          .getResource(rsaPath).getPath();
      logger.info("商户收款秘钥JKS文件路径={}", rsaPath);
      PrivateKey prikey = getPrivateKeyByJks(rsaPath, merKeyStrorepwd, merKeyKeyAlias, merKeyPrikeypwd);

      // 执行签名操作
      byte[] b = RSAAlgorithms.getSignByte(prikey, signPlainStr);
      // 进行Base64编码
      String base64 = Base64Util.encode(b);
      base64 = base64.replace("\n", "").replace("\r", "");

      logger.info("商户私钥签名. 签名结果串={}", base64);
      return base64;

    } catch (Exception e) {
      logger.error("签名失败", e);
      throw e;
    }
  }

  /**
   * 加载商户私钥
   */
  private static PrivateKey getPrivateKeyByJks(String jksPath,String merKeyStrorepwd,String merKeyKeyAlias,String merKeyPrikeypwd)
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException,
      UnrecoverableKeyException {
    KeyStore recvKeyStore = null;
    PrivateKey recvPriKey = null;
    recvKeyStore = KeyStore.getInstance("JKS");
    recvKeyStore.load(new FileInputStream(jksPath), merKeyStrorepwd.toCharArray());
    recvPriKey = (PrivateKey) recvKeyStore
        .getKey(merKeyKeyAlias, merKeyPrikeypwd.toCharArray());

    return recvPriKey;
  }

  /**
   * 执行加密操作(使用新生公钥进行rsa加密)
   */
  public static String doEncryt(String plainStr, String payPublicKey) throws Exception {
    logger.info("新生公钥加密. 明文串={}", plainStr);
    // 进行加密处理
    byte[] cipherBytes = RSAAlgorithms.encryptByPublicKey(plainStr.getBytes("UTF-8"), payPublicKey);

    // 将加密结果进行Base64编码
    String cipherStr = Base64Util.encode(cipherBytes);
    logger.info("新生公钥加密. 密文串={}", cipherStr);

    return cipherStr;
  }

  /**
   * 执行验签操作(使用新生公钥进行验签)
   *
   * @param signPlainStr 待验签明文串
   * @param signMsg      签名串
   * @return 是否验签通过
   * @throws Exception
   */
  public static boolean doVerify(String signPlainStr, String signMsg, String payPublicKey)
      throws Exception {
    logger.info("新生公钥验签. 待验签明文串={}, 签名串={}", signPlainStr, signMsg);

    // 将签名串进行Base64解码
    byte[] signMsgBytes = Base64Util.decode(signMsg);
    // 执行验签操作
    boolean flag = RSAAlgorithms
        .verify(RSAAlgorithms.getPublicKey(payPublicKey), signPlainStr, signMsgBytes);

    logger.info("新生公钥验签. 验签结果={}", flag);
    return flag;
  }
}
