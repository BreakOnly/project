/**
 *
 */
package com.jrmf.payment.mybankapi.service;

import com.jrmf.payment.mybankapi.common.util.MD5;
import com.jrmf.payment.mybankapi.common.util.MagCore;
import com.jrmf.payment.mybankapi.common.util.RSA;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * <p>注释</p>
 * @author fjl
 * @version $Id: SecurityService.java, v 0.1 2013-12-20 下午3:03:30 fjl Exp $
 */
@Component
public class SecurityService {

  private static Logger logger = LoggerFactory
      .getLogger(SecurityService.class);

  @Value("mybankMd5Key")
  private String mybankMd5Key;

  @Value("PublicKey")
  private String mybankRsaPublicKey;

  /**
   * 验证签名
   * @param map
   * @param charset
   * @param signType
   * @return
   */
  public boolean verify(Map<String, String> map, String charset, String sign, String signType) {
    Map<String, String> tmp = MagCore.paraFilter(map);
    String str = MagCore.createLinkString(tmp, false);
    if (signType.equalsIgnoreCase("MD5")) {
      return verifyMd5(str, sign, charset);
    } else if (signType.equalsIgnoreCase("RSA")) {
      return verifyRSA(str, sign, charset);
    }
    return false;
  }

  private boolean verifyMd5(String src, String sign, String charset) {
    boolean result = false;
    if (logger.isInfoEnabled()) {
      logger.info("verify sign with MD5:src ={},sign={}", new Object[]{src, sign});
    }
    try {
      result = MD5.verify(src, sign, mybankMd5Key, charset);
    } catch (Exception e) {
      logger.error("MD5 verify failure:src ={},sign={}", new Object[]{src, sign});
      logger.error("MD5 verify failure", e);
    }
    return result;
  }

  private boolean verifyRSA(String src, String sign, String charset) {
    boolean result = false;
    if (logger.isInfoEnabled()) {
      logger.info("verify sign with RSA:src ={},sign={}", new Object[]{src, sign});
    }
    try {
      result = RSA.verify(src, sign, mybankRsaPublicKey, charset);
    } catch (Exception e) {
      logger.error("RSA verify failure:src ={},sign={}", new Object[]{src, sign});
      logger.error("RSA verify failure", e);
    }
    return result;
  }

}
