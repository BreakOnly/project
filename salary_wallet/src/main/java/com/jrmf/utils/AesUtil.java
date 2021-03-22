package com.jrmf.utils;

import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * filename：com.jrmf.utils.AesUtil.java
 * 
 * @author: zhangyong
 * @time: 2015-8-11下午4:52:01
 */

public class AesUtil {

	// 1. KeyGenerator keyGen = KeyGenerator.getInstance("AES");
	// 2.
	// 3. String pwd = "passordgggggg";

	// 7. SecretKey skey = keyGen.generateKey();
	// 8. byte[] raw = skey.getEncoded();
	// 9.
	// 10. SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

	/**
	 * 加密
	 * 
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 */
	public static byte[] encryptToByte(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
            // 需要自己手动设置
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes());
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(byteContent);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
        return null;
	}

	/**
	 * 解密
	 * 
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 */
	public static byte[] decryptToByte(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes());
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
        return null;
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1){
			return null;
		}
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 解密
	 * 
	 * @param content
	 *            需要解密的内容
	 * @param secretKey
	 *            解密秘钥
	 * @return
	 */
	public static String decrypt(String content, String secretKey) {
		byte[] decryptFrom = parseHexStr2Byte(content);
		byte[] decryptResult = decryptToByte(decryptFrom, secretKey);
		return new String(decryptResult);
	}

	/**
	 * 加密
	 * 
	 * @param content
	 *            需要加密的内容
	 * @param secretKey
	 *            加密秘钥
	 * @return
	 */
	public static String encrypt(String content, String secretKey) {
		byte[] encryptResult = encryptToByte(content, secretKey);
        return parseByte2HexStr(encryptResult);
	}
	
	public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
	    return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
	}

	public static String aesEncryptString(String content, String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		byte[] contentBytes = content.getBytes(CHARSET);
		byte[] keyBytes = key.getBytes(CHARSET);
		byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
		System.out.println(encryptedBytes.length);
		BASE64Encoder encoder = new BASE64Encoder();
	    return encoder.encode(encryptedBytes);
	}
	
	private static final String IV_STRING = "0000000000000000";
	private static final String CHARSET = "UTF-8";
	
	private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
	    byte[] initParam = IV_STRING.getBytes(CHARSET);
	    IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(mode, secretKey, ivParameterSpec);
 	 	return cipher.doFinal(contentBytes);
	}
	
	public static void main(String[] aa) {
        String content = "798456465514154";

        String secretKey = "13E80F176EDCA60456220FE8EDCB5772";
        System.out.println(encrypt(content, secretKey));
        System.out.println(decrypt(encrypt(content, secretKey), secretKey));
    }
}
