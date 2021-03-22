package com.jrmf.payment.openapi.utils;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public final class TokenGenerator {

	private static final String LINE_THROUGH = "-";
	private static final String ORDER_FLAG = "1001";
	private static final String SERIAL_NUMBER = "0110";
	
	public static String generate(String...prefixs){
		String str = StringUtils.replace(UUID.randomUUID().toString(), LINE_THROUGH, StringUtils.EMPTY);
		if(prefixs != null && prefixs.length > 0 &&  StringUtils.isNotBlank(prefixs[0])){
			return prefixs[0].concat(str);
		}
		return str;
	}
	
	public static int nextRandom() {
		Random random = new Random();
		int result = Math.abs(random.nextInt(8));
		System.err.println(result);
		return result;
	}
	
	/**
	 * @param increment 用来保证自增顺序.
	 */
	public static String generateSerialNumber(long increment) {
        return String.valueOf(System.currentTimeMillis()) 
        		+ SERIAL_NUMBER + ORDER_FLAG 
        		+ String.format("%08d", increment == 0 ? nextRandom() : increment);
    }
	
}
