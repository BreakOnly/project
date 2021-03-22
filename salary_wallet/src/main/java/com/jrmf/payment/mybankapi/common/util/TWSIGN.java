package com.jrmf.payment.mybankapi.common.util;

import com.wxmlabs.aurora.CMSSigner;
import com.wxmlabs.aurora.SignatureService;
import com.wxmlabs.aurora.compatible.TCACompatibleSignatureService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>证书签名工具类</p>
 *
 * @author Fei Jiutong
 * @version $Id: TWSIGN.java, v 0.1 2018年9月7日 下午3:58:03 feijiutong Exp $
 */
public class TWSIGN {

  private static final Logger logger = LoggerFactory.getLogger(TWSIGN.class);
  private static SignatureService signatureService;

  public TWSIGN(String configFilePath) {
    if (signatureService == null) {
      // 加载证书配置
      String json = loadCertConfig(configFilePath);
      // 初始化证书
      signatureService = TCACompatibleSignatureService.getInstance(json);
    }
  }

  private static String loadCertConfig(String configFilePath) {
    FileInputStream fis = null;
    String json = "";
    try {
      // 读取证书配置
      fis = new FileInputStream(new File(configFilePath));
      byte[] buf = new byte[fis.available()];
      fis.read(buf);
      json = new String(buf, "UTF-8");
    } catch (FileNotFoundException e) {
      logger.error("配置文件不存在。文件路径：{}", configFilePath, e);
    } catch (IOException e) {
      logger.error("配置文件读取失败。文件路径：{}", configFilePath, e);
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        logger.error("文件流关闭失败。文件路径：{}", configFilePath, e);
      }
    }
    return json;
  }

  /**
   * 加签，不含原文
   *
   * @param plainData    原文
   * @param keyStoreName
   * @param charset
   * @return
   * @throws Exception
   */
  public static String sign(String plainData, String keyStoreName, String charset)
      throws Exception {
    logger.info("原文：{}", plainData);
    CMSSigner cmsSigner = getSigner(keyStoreName);

    // 不带原文
    byte[] signedData = cmsSigner.sign(plainData.getBytes(charset));
    String signedDataB64 = Base64.encodeBase64String(signedData);
//    logger.info("签名：{}", signedDataB64);
    return signedDataB64;
  }

  /**
   * 加密
   *
   * @param plainData    原文
   * @param keyStoreName
   * @return
   * @throws Exception
   */
  public static byte[] encode(byte[] plainData, String keyStoreName) throws Exception {
    CMSSigner cmsSigner = getSigner(keyStoreName);

    // 带原文
    byte[] signedData = cmsSigner.sign(plainData, true);
    byte[] signedDataB64 = Base64.encodeBase64(signedData);
    return signedDataB64;
  }

  private static CMSSigner getSigner(String keyStoreName) throws Exception {
    CMSSigner cmsSigner = null;
    for (String signerName : signatureService.listSigner()) {
      String[] signerNames = StringUtils.split(signerName, "_");
      if (StringUtils.equals(signerNames[0], keyStoreName)) {
        cmsSigner = (CMSSigner) signatureService.findSignerByName(signerName);
        break;
      }
    }

    if (cmsSigner == null) {
      throw new Exception("证书不存在。keyStoreName：" + keyStoreName);
    }
    return cmsSigner;
  }
}