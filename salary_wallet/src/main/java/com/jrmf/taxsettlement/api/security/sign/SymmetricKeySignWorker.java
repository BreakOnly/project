package com.jrmf.taxsettlement.api.security.sign;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SymmetricKeySignWorker extends AbstractSignWorker {

	private static final Logger logger = LoggerFactory.getLogger(SymmetricKeySignWorker.class);
	
	private String algorithm;

	public SymmetricKeySignWorker(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	* @Description 将商户加密的数据和系统自己效验的加密数据进行比对
	**/
	@Override
	public boolean verifySign(String sortKVStr, String verificationKey, String sign) throws Exception {
		String signGet = generateSign(sortKVStr, verificationKey);
		logger.debug("sign generate is:{}", signGet);
		return sign.equals(signGet);
		// return true;
	}

	/**
	* @Description 将排序好的 商户请求数据 根据配置文件的加密方式进行加密
	**/
	@Override
	protected String generateSign(String sortKVStr, String signGenerationKey) throws Exception {
		StringBuilder sb = new StringBuilder(sortKVStr);
		sb.append("&key=").append(signGenerationKey);
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		byte[] signBytes = digest.digest(sb.toString().getBytes(Charset.forName("utf-8")));
		return Base64.getEncoder().encodeToString(signBytes);
	}

	@Override
	public boolean verifySign(byte[] byteArrayData, String verificationKey, String sign) throws Exception {
		String signGet = generateSign(byteArrayData, verificationKey);
		logger.debug("sign generate is:{}", signGet);
		return sign.equals(signGet);	
	}

	@Override
	public String generateSign(byte[] byteArrayData, String generationKey) throws Exception {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.update(byteArrayData);
		digest.update(generationKey.getBytes());
		byte[] signBytes = digest.digest();
		return Base64.getEncoder().encodeToString(signBytes);
	}
	
}
