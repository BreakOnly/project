package com.jrmf.payment.newpay.process;

import com.jrmf.payment.newpay.constants.Constants;
import com.jrmf.payment.newpay.util.CommonUtil;
import com.jrmf.payment.newpay.util.DateUtils;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 单笔付款处理过程
 *
 * @author juny-zhang
 */
public class PayProcess {

  private static final Logger logger = LoggerFactory.getLogger(PayProcess.class);

  /**
   * 构建签名参数
   */
  public static String buildSignParam(Map<String, String> params, String rsaPath,String merKeyStrorepwd,String merKeyKeyAlias,String merKeyPrikeypwd) throws Exception {
    String signFormatStr = "version=[%s]tranCode=[%s]merId=[%s]merOrderId=[%s]submitTime=[%s]msgCiphertext=[%s]signType=[%s]";
    String version = params.get("version");
    String tranCode = params.get("tranCode");
    String merId = params.get("merId");
    String merOrderId = params.get("merOrderId");
    String submitTime = params.get("submitTime");
    String msgCiphertext = params.get("msgCiphertext");
    String signType = params.get("signType");

    // 签名明文串
    String signPlainStr = String
        .format(signFormatStr, version, tranCode, merId, merOrderId, submitTime, msgCiphertext,
            signType);

    // 执行签名操作
    return CommonUtil.doSign(signPlainStr, rsaPath, merKeyStrorepwd, merKeyKeyAlias, merKeyPrikeypwd);
  }

  /**
   * 对新生响应报文执行验签操作
   */
  @SuppressWarnings("unchecked")
  public static boolean doVerifySign(String respJsonStr, String payPublicKey) throws Exception {
    // 解析响应报文
    HashMap<String, String> respMap = (HashMap<String, String>) JSONObject
        .parseObject(respJsonStr, Map.class);
    String signMsg = respMap.get("signValue");

    // 构建验签明文串
    String signFormatStr = "version=[%s]tranCode=[%s]merOrderId=[%s]merId=[%s]charset=[%s]signType=[%s]resultCode=[%s]hnapayOrderId=[%s]";
    String version = respMap.get("version");
    String tranCode = respMap.get("tranCode");
    String merOrderId = respMap.get("merOrderId");
    String merId = respMap.get("merId");
    String charset = respMap.get("charset");
    String signType = respMap.get("signType");
    String resultCode = respMap.get("resultCode");
    String hnapayOrderId = respMap.get("hnapayOrderId");
    String signPlainStr = String
        .format(signFormatStr, version, tranCode, merOrderId, merId, charset, signType, resultCode,
            hnapayOrderId);

    // 执行验签操作
    return CommonUtil.doVerify(signPlainStr, signMsg, payPublicKey);
  }
}
