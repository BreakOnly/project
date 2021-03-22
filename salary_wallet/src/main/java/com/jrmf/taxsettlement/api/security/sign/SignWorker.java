package com.jrmf.taxsettlement.api.security.sign;

import java.util.Map;

public interface SignWorker {

	String generateSign(Map<String, Object> mapData, String generationKey) throws Exception;
	
	String generateSign(byte[] byteArrayData, String generationKey) throws Exception;
	
	boolean verifySign(Map<String, Object> mapData, String verificationKey, String sign) throws Exception;

	boolean verifySign(byte[] byteArrayData, String verificationKey, String sign)throws Exception;

}
