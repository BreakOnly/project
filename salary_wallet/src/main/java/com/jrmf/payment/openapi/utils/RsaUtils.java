package com.jrmf.payment.openapi.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.jrmf.payment.openapi.exception.AygOpenApiException;

public class RsaUtils {

	private static final String KEY_ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHM = "SHA256withRSA";
	
    /** 貌似默认是RSA/NONE/PKCS1Padding，未验证 */
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /** RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024 */
    private static final int KEY_SIZE = 1024; 
    
    /** 
     * RSA最大加密明文大小:明文长度(bytes) <= 密钥长度(bytes)-11
     */  
    private static final int MAX_ENCRYPT_BLOCK = KEY_SIZE / 8 - 11 ;  
      
    /** 
     * RSA最大解密密文大小 
     */  
    private static final int MAX_DECRYPT_BLOCK = KEY_SIZE / 8; 

	public static PublicKey loadPublicKey(String pubKeyString) {
		byte[] keyBytes = Base64.getDecoder().decode(pubKeyString);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
			PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static PrivateKey loadPrivateKey(String privateKeyString) {
		try {
			byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			return keyFactory.generatePrivate(pkcs8KeySpec);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public static String encrypt(PublicKey key, String plainText) {
		byte[] encodeBytes = encrypt(key, plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encodeBytes);
	}

	public static byte[] encrypt(PublicKey key, byte[] plainBytes) {

		ByteArrayOutputStream out = null;
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);

			int inputLen = plainBytes.length;
			if (inputLen <= MAX_ENCRYPT_BLOCK) {
				return cipher.doFinal(plainBytes);
			}
			out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(plainBytes, offSet, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(plainBytes, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_ENCRYPT_BLOCK;
			}
			return out.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new AygOpenApiException("无此解密算法");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			throw new AygOpenApiException("解密私钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new AygOpenApiException("密文长度非法");
		} catch (BadPaddingException e) {
			throw new AygOpenApiException("密文数据已损坏");
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e2) {
			}
		}
	}
	
	public static String decrypt(PrivateKey key, String encodedText) {
    	byte[] bytes = Base64.getDecoder().decode(encodedText);
    	return decrypt(key, bytes);
    }
	
	
	 public static String decrypt(PrivateKey key, byte[] encodedText) {

	    	ByteArrayOutputStream out = null;
	        try {
	            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	            cipher.init(Cipher.DECRYPT_MODE, key);
	            int inputLen = encodedText.length; 
	            
	            if(inputLen <= MAX_DECRYPT_BLOCK){
	            	return new String(cipher.doFinal(encodedText),StandardCharsets.UTF_8);
	            }
	            
	            out = new ByteArrayOutputStream();  
	            int offSet = 0;  
	            byte[] cache;  
	            int i = 0;  
	            // 对数据分段解密  
	            while (inputLen - offSet > 0) {  
	                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {  
	                    cache = cipher.doFinal(encodedText, offSet, MAX_DECRYPT_BLOCK);  
	                } else {  
	                    cache = cipher.doFinal(encodedText, offSet, inputLen - offSet);  
	                }  
	                out.write(cache, 0, cache.length);  
	                i++;  
	                offSet = i * MAX_DECRYPT_BLOCK;  
	            }  
	            return new String(out.toByteArray(),StandardCharsets.UTF_8);
	        } catch (NoSuchAlgorithmException e) {  
	            throw new AygOpenApiException("无此解密算法");  
	        } catch (NoSuchPaddingException e) {  
	            e.printStackTrace();  
	            return null;  
	        } catch (InvalidKeyException e) {  
	            throw new AygOpenApiException("解密私钥非法,请检查");  
	        } catch (IllegalBlockSizeException e) {  
	            throw new AygOpenApiException("密文长度非法");  
	        } catch (BadPaddingException e) {  
	            throw new AygOpenApiException("密文数据已损坏");  
	        }finally{
	        	try {if(out != null)out.close(); } catch (Exception e2) {}
	        }
	    }
	    

	public static String signature(PrivateKey privateKey,String contents) {
		try {
			byte[] data = contents.getBytes(StandardCharsets.UTF_8.name());
			Signature signature = Signature.getInstance(SIGN_ALGORITHM);
			signature.initSign(privateKey);
			signature.update(data);

			return  Base64.getEncoder().encodeToString(signature.sign());
		} catch (NoSuchAlgorithmException e) {
			// TODO: handle exception
		} catch (InvalidKeyException e) {
			throw new AygOpenApiException("私钥格式错误");
		} catch (SignatureException e) {
			// TODO: handle exception
		} catch (UnsupportedEncodingException e) {
			// TODO: handle exception
		}

		return null;
	}

	public static boolean verifySignature(String contents, String sign, PublicKey publicKey) {
		try {
			byte[] data = contents.getBytes(StandardCharsets.UTF_8.name());
			byte[] dataSignature = Base64.getDecoder().decode(sign);

			Signature signature = Signature.getInstance(SIGN_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(data, 0, data.length);
			return signature.verify(dataSignature);
		} catch (Exception e) {
			return false;
		}

	}
	
}
