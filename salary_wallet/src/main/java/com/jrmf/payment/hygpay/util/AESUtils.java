package com.jrmf.payment.hygpay.util;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.util.Base64Utils;

public class AESUtils {

  private static String transformation = "AES/ECB/PKCS5Padding";
  private static String algorithm = "AES";

  /**
   * 解密
   *
   * @param content
   * @param key
   * @return
   * @throws Exception
   */
  public static String decryptByHex(String content, String key) throws Exception {
    return decrypt(hex2Byte(content), key);
  }

  /**
   * 解密过程
   *
   * @param encryptBytes
   * @param key
   * @return
   * @throws Exception
   */
  private static String decrypt(byte[] encryptBytes, String key) throws Exception {
    Key k = toKey(Base64Utils.decodeFromString(key));
    Cipher cipher = Cipher.getInstance(transformation);
    cipher.init(2, k);
    return new String(cipher.doFinal(encryptBytes));
  }

  /**
   * 加密, 结果为 16 进制
   *
   * @param content
   * @param key
   * @return
   * @throws Exception
   */
  public static String encrypt2Hex(String content, String key) throws Exception {
    return byte2Hex(encrypt(content, key));
  }

  /**
   * 加密过程
   *
   * @param content
   * @param key
   * @return
   * @throws Exception
   */
  private static byte[] encrypt(String content, String key) throws Exception {
    Key k = toKey(Base64Utils.decodeFromString(key));
    Cipher cipher = Cipher.getInstance(transformation);
    cipher.init(Cipher.ENCRYPT_MODE, k);
    return cipher.doFinal(content.getBytes());
  }

  /**
   * 2 进制转 16 进制
   *
   * @param buff
   * @return
   */
  public static String byte2Hex(byte buff[]) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < buff.length; i++) {
      String hex = Integer.toHexString(buff[i] & 0xFF);
      if (hex.length() == 1) {
        hex = '0' + hex;
      }
      sb.append(hex.toUpperCase());
    }
    return sb.toString();
  }

  /**
   * 16 进制转 2 进制
   *
   * @param hex
   * @return
   */
  public static byte[] hex2Byte(String hex) {
    if (hex != null && hex.length() >= 1 && hex.length() % 2 == 0) {
      byte[] result = new byte[hex.length() / 2];
      for (int i = 0; i < result.length; ++i) {
        int high = Integer.parseInt(hex.substring(i * 2, i * 2 + 1), 16);
        int low = Integer.parseInt(hex.substring(i * 2 + 1, i * 2 + 2), 16);
        result[i] = (byte) (high << 4 | low);
      }
      return result;
    } else {
      return null;
    }
  }

  public static Key toKey(byte[] key) throws Exception {
    return new SecretKeySpec(key, algorithm);
  }

  /**
   * 密钥生成
   *
   * @return
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  public static String getAutoCreateAESKey() throws NoSuchAlgorithmException,
      IOException {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
//要生成多少位，只需要修改这里即可 128, 192 或 256
    kg.init(128);
    SecretKey sk = kg.generateKey();
    byte[] b = sk.getEncoded();
    return Base64Utils.encodeToString(b);
  }

  public static void main(String[] args) throws IOException,
      NoSuchAlgorithmException {
    String autoCreateAESKey = getAutoCreateAESKey();
    System.out.println(autoCreateAESKey);
  }

}
