package com.jrmf.payment.hddpay.util;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SHA1WithRSA {
	public static final String SIGN_TYPE_RSA = "RSA";
	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;
	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * RSA签名
     * 
     * @param content 待签名数据
     * @param privateKey 商户私钥
     * @param input_charset 编码格式
     * @return 签名值
     */
    public static String sign(String content, String privateKey, String input_charset) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64Util.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance(SIGN_TYPE_RSA);
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(input_charset));

            byte[] signed = signature.sign();

            return Base64Util.encode(signed);
        } catch (Exception e) {
        	
        }
        return null;
    }

    /**
     * RSA验签名检查
     * 
     * @param content 待签名数据
     * @param sign 签名值
     * @param ali_public_key 支付宝公钥
     * @param input_charset 编码格式
     * @return 布尔值
     */
    public static boolean verify(String content, String sign, String ali_public_key, String input_charset) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
            byte[] encodedKey = Base64Util.encodeByte(ali_public_key);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes(input_charset));

            boolean bverify = signature.verify(Base64Util.decode(sign));
            return bverify;

        } catch (Exception e) {
        }

        return false;
    }
    
    public static String encrypt(String content, String publickey) {
    	try {
			byte[] data = content.getBytes("UTF-8");
			byte[] keys = Base64Util.decode(publickey);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keys);
			KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
			Key publicK = keyFactory.generatePublic(x509KeySpec);
			// 对数据加密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicK);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0, i = 0;
			byte[] cache;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_ENCRYPT_BLOCK;
			}
			byte[] encryptedData = out.toByteArray();
			out.close();
			return Base64Util.encode(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "";
    }
    
    public static String decrypt(String content, String privatekey) {
    	try {
			byte[] data = Base64Util.decode(content);
			byte[] keyBytes = Base64Util.decode(privatekey);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
			Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateK);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0, i = 0;
			byte[] cache;
			// 对数据分段解密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
					cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_DECRYPT_BLOCK;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return new String(decryptedData, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "";
    }
    
    public static void main(String[] args) {
        //String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANWCsjJK8va1KQpKqn+A2H98nlt2DJc1krohxOOlr2M6lAZeNacMhMvgAxH6Af+VZBFoOOBIUyaSBlWcXtxdpBKSAe5sLESyhLMK0jRDZJst13Tfimq65X0G2yEFQQAD3wLlrp+EGnJIXTZwLRRPKCfR34bOvvDuVi6GFD3rjFtXAgMBAAECgYAtaiNlNzP+7gHQwpkJjep/E47LfYyoDSkslko+8dsgjDE4OYnIAo2sn/WHOVp7dtdWLIkMi9XD+oOGakBzNyfSt+Xmzc4NBJoBGp1YRjJy8vwwgMD9QLCeXcTLaIMbfoDQ6D410rjPecqdfrfqf8+OEsR13XJ3W7qavvFejT8eAQJBAO/s5sqRg7W0ASTA8nQE18xO+kmJWNpZOAawh1bdh9b5ZIqjtXOk46XenxQBP00PgLlPbTHwj7YqozG8ixnsMZcCQQDj0LzuIbecFuOeQRkyRYSMX/CKqg8g3MvmB+gcKV1Con007y7NthYK/ILEdIhll/5k/F1zz4pQvgv/t5tWetxBAkEAxcQsFUvPGobO7oHRGIspanKrYtRBFnK2eOUoGhUvNKeMFa+OEU4YjBAuZmoEyLt/qsBqzOBzRYBt1sCIlIyZ7wJAHOL88d0IoRwt5IUworU552rK32pM3MfietqJzfhYwPhFlA64CpGKHf6CvlJgcTFiqn9tgh/Q1AglEVnAsqiFgQJAcspCepgN1Ef9We1lLccFdf+7uBp0iFTv232pPoLV4OPwaacgbtRi/rJYuRsAtQ66d2PMPp6VhROzLpVCwQhwpA==";
    	String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANWCsjJK8va1KQpKqn+A2H98nlt2DJc1krohxOOlr2M6lAZeNacMhMvgAxH6Af+VZBFoOOBIUyaSBlWcXtxdpBKSAe5sLESyhLMK0jRDZJst13Tfimq65X0G2yEFQQAD3wLlrp+EGnJIXTZwLRRPKCfR34bOvvDuVi6GFD3rjFtXAgMBAAECgYAtaiNlNzP+7gHQwpkJjep/E47LfYyoDSkslko+8dsgjDE4OYnIAo2sn/WHOVp7dtdWLIkMi9XD+oOGakBzNyfSt+Xmzc4NBJoBGp1YRjJy8vwwgMD9QLCeXcTLaIMbfoDQ6D410rjPecqdfrfqf8+OEsR13XJ3W7qavvFejT8eAQJBAO/s5sqRg7W0ASTA8nQE18xO+kmJWNpZOAawh1bdh9b5ZIqjtXOk46XenxQBP00PgLlPbTHwj7YqozG8ixnsMZcCQQDj0LzuIbecFuOeQRkyRYSMX/CKqg8g3MvmB+gcKV1Con007y7NthYK/ILEdIhll/5k/F1zz4pQvgv/t5tWetxBAkEAxcQsFUvPGobO7oHRGIspanKrYtRBFnK2eOUoGhUvNKeMFa+OEU4YjBAuZmoEyLt/qsBqzOBzRYBt1sCIlIyZ7wJAHOL88d0IoRwt5IUworU552rK32pM3MfietqJzfhYwPhFlA64CpGKHf6CvlJgcTFiqn9tgh/Q1AglEVnAsqiFgQJAcspCepgN1Ef9We1lLccFdf+7uBp0iFTv232pPoLV4OPwaacgbtRi/rJYuRsAtQ66d2PMPp6VhROzLpVCwQhwpA==";
        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDVgrIySvL2tSkKSqp/gNh/fJ5bdgyXNZK6IcTjpa9jOpQGXjWnDITL4AMR+gH/lWQRaDjgSFMmkgZVnF7cXaQSkgHubCxEsoSzCtI0Q2SbLdd034pquuV9BtshBUEAA98C5a6fhBpySF02cC0UTygn0d+Gzr7w7lYuhhQ964xbVwIDAQAB";
        
        String ali_pub_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
        		              
        
        
        String content = "body=2015-01-11在天上人间消费&buyer_email=215262016@qq.com&buyer_id=2088902962305570&discount=0.00&gmt_create=2015-01-11 09:54:48&gmt_payment=2015-01-11 09:54:49&is_total_fee_adjust=N&notify_id=8261289e01d29d527d5de6802b90d99156&notify_time=2015-01-11 09:54:49&notify_type=trade_status_sync&out_trade_no=2015011100000002&payment_type=1&price=0.01&quantity=1&seller_email=ttl66868@163.com&seller_id=2088711660560521&subject=天上人间消费&total_fee=0.01&trade_no=2015011128211457&trade_status=TRADE_SUCCESS&use_coupon=N";
        
        String sign = sign(content, privateKey, "UTF-8");
        System.out.println(sign);
        boolean b = verify(content, sign, pubKey, "UTF-8");
        System.out.println(b);
        
        sign = "C9guMhDh1MSmE2O7O04oaPbCkKWf0WsokBR97W1BQidL/mQaQpvyHFSGYe+jnWo8iOryZCOirWrSrdfbwWePkCj9PGyPD9GeW0gUoK6VOdDxekUXPQvRmV2KznOhDXIQF+aw+aiMiTKsGEskdLorn9LaEP13TTYQHaiXzSh+G50=";
        boolean bl = verify(content, sign, ali_pub_key, "UTF-8");
        System.out.println(bl);
    }
    
    

    /**
     * 解密
     * 
     * @param content 密文
     * @param private_key 商户私钥
     * @param input_charset 编码格式
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String private_key, String input_charset) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);

        Cipher cipher = Cipher.getInstance(SIGN_TYPE_RSA);
        cipher.init(Cipher.DECRYPT_MODE, prikey);

        InputStream ins = new ByteArrayInputStream(Base64Util.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        // rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;

            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }

            writer.write(cipher.doFinal(block));
        }

        return new String(writer.toByteArray(), input_charset);
    }

    /**
     * 得到私钥
     * 
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64Util.decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        return privateKey;
    }
}
