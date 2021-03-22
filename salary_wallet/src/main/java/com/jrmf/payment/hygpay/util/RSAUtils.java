package com.jrmf.payment.hygpay.util;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

public class RSAUtils {

  public static final String KEY_ALGORITHM = "RSA";
  public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
  private static final String PUBLIC_KEY = "RSAPublicKey";
  private static final String PRIVATE_KEY = "RSAPrivateKey";
  private static final int MAX_ENCRYPT_BLOCK = 117;
  private static final int MAX_DECRYPT_BLOCK = 128;

  public RSAUtils() {
  }

  public static Map<String, Key> genKeyPair() throws Exception {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    keyPairGen.initialize(1024);
    KeyPair keyPair = keyPairGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    Map<String, Key> keyMap = new HashMap(2);
    keyMap.put(PUBLIC_KEY, publicKey);
    keyMap.put(PRIVATE_KEY, privateKey);
    return keyMap;
  }

  /**
   * 数字签名
   *
   * @param data
   * @param privateKey
   * @return
   * @throws Exception
   */
  public static String sign(byte[] data, String privateKey) throws Exception {
    byte[] keyBytes = Base64.decodeBase64(privateKey);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initSign(privateK);
    signature.update(data);
    return new String(Base64.encodeBase64(signature.sign()));
  }

  /**
   * 验证签名
   *
   * @param data
   * @param publicKey
   * @param sign
   * @return
   * @throws Exception
   */
  public static boolean verify(byte[] data, String publicKey, String sign) throws
      Exception {
    byte[] keyBytes = Base64.decodeBase64(publicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    PublicKey publicK = keyFactory.generatePublic(keySpec);
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initVerify(publicK);
    signature.update(data);
    return signature.verify(Base64.decodeBase64(sign));
  }

  /**
   * 私钥解密
   *
   * @param encryptedData
   * @param privateKey
   * @return
   * @throws Exception
   */
  public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
      throws Exception {
    byte[] keyBytes = Base64.decodeBase64(privateKey);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(2, privateK);
    int inputLen = encryptedData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    for (int i = 0; inputLen - offSet > 0; offSet = i * MAX_DECRYPT_BLOCK) {
      byte[] cache;
      if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
        cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      ++i;
    }
    byte[] decryptedData = out.toByteArray();
    out.close();
    return decryptedData;
  }


  /**
   * 公钥解密
   *
   * @param encryptedData
   * @param publicKey
   * @return
   * @throws Exception
   */
  public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
      throws Exception {
    byte[] keyBytes = Base64.decodeBase64(publicKey);
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key publicK = keyFactory.generatePublic(x509KeySpec);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(2, publicK);
    int inputLen = encryptedData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    for (int i = 0; inputLen - offSet > 0; offSet = i * MAX_DECRYPT_BLOCK) {
      byte[] cache;
      if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
        cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      ++i;
    }
    byte[] decryptedData = out.toByteArray();
    out.close();
    return decryptedData;
  }

  /**
   * 公钥加密
   *
   * @param data
   * @param publicKey
   * @return
   * @throws Exception
   */
  public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws
      Exception {
    byte[] keyBytes = Base64.decodeBase64(publicKey);
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key publicK = keyFactory.generatePublic(x509KeySpec);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(1, publicK);
    int inputLen = data.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    for (int i = 0; inputLen - offSet > 0; offSet = i * MAX_ENCRYPT_BLOCK) {
      byte[] cache;
      if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
        cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(data, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      ++i;
    }
    byte[] encryptedData = out.toByteArray();
    out.close();
    return encryptedData;
  }

  /**
   * 私钥加密
   *
   * @param data
   * @param privateKey
   * @return
   * @throws Exception
   */
  public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws
      Exception {
    byte[] keyBytes = Base64.decodeBase64(privateKey);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(1, privateK);
    int inputLen = data.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    for (int i = 0; inputLen - offSet > 0; offSet = i * MAX_ENCRYPT_BLOCK) {
      byte[] cache;
      if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
        cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(data, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      ++i;
    }
    byte[] encryptedData = out.toByteArray();
    out.close();
    return encryptedData;
  }

  /**
   * 获取私钥
   *
   * @param keyMap
   * @return
   * @throws Exception
   */
  public static String getPrivateKey(Map<String, Key> keyMap) throws Exception {
    Key key = keyMap.get(PRIVATE_KEY);
    return new String(Base64.encodeBase64(key.getEncoded()));
  }

  /**
   * 获取公钥
   *
   * @param keyMap
   * @return
   * @throws Exception
   */
  public static String getPublicKey(Map<String, Key> keyMap) throws Exception {
    Key key = keyMap.get(PUBLIC_KEY);
    return new String(Base64.encodeBase64(key.getEncoded()));
  }

  /**
   * 将参数名称按照 ASCII 排序
   *
   * @param paramMap
   * @return
   */
  public static String sortParam(Map<String, Object> paramMap) {
    List<Entry<String, Object>> list = new ArrayList<Entry<String,
        Object>>(paramMap.entrySet());
    Collections.sort(list, new Comparator<Entry<String, Object>>() {
      @Override
      public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object>
          o2) {
        return (o1.getKey().compareTo(o2.getKey()));
      }
    });
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Object> item : list) {
      if (!StringUtils.isEmpty(item.getKey())
          && !StringUtils.isEmpty(item.getValue())) {
        sb.append(item.getKey()).append("=").append(item.getValue()).append("&");
      }
    }
    return sb.substring(0, sb.length() - 1);
  }

  public static void main(String[] args) throws Exception {
    Map<String, Key> map = genKeyPair();
    String privateKey = getPrivateKey(map);
    String publicKey = getPublicKey(map);
    System.out.println("pub=" + publicKey);
    System.out.println("pri=" + privateKey);
  }

}
