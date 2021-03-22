package com.jrmf.payment.zxpay.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * MD5加密算法
 * @author xumum
 *
 */
public class Md5EncryptUtils {
	/**
	 * Used building output as Hex
	 */
	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 对字符串进行MD5加密
	 * @param text 明文
	 * @return 密文
	 */
	public static String md5(String text) {
		MessageDigest msgDigest = null;
		try {
			msgDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"System doesn't support MD5 algorithm.");
		}
		try {
			msgDigest.update(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(
					"System doesn't support your  EncodingException.");
		}
		byte[] bytes = msgDigest.digest();
		String md5Str = new String(encodeHex(bytes));
		return md5Str;
	}

	public static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS[0x0F & data[i]];
		}
		return out;
	}
	
	
	/** 
     * 方法描述:签名字符串 
     * @author xumum
     * @param params 需要签名的参数 
     * @param key 签名密钥 
     * @return 
     */  
    public static String sign(Map<String, Object> params, String key) {  
        StringBuilder valueSb = new StringBuilder();  
       
        // 将参数以参数名的字典升序排序  
        Map<String, Object> sortParams = new TreeMap<String, Object>(params);  
        Set<Entry<String, Object>> entrys = sortParams.entrySet();  
        // 参数以参数名的字典升序排序 ,并拼接param1=value1&param2=value2...&key=value格式  
        for (Entry<String, Object> entry : entrys) {  
        	valueSb.append(entry.getKey());
        	valueSb.append("=");
            valueSb.append(entry.getValue());  
            valueSb.append("&");  
        }  
        String paramStr = valueSb.toString();
        paramStr = paramStr.substring(0, paramStr.length()-1);
        return MD5sign(paramStr,key);  
    }  
    
    /** 
     * MD5生成签名字符串 
     * @param map 需签名参数 
     * @param key MD5key 
     * @return 
     */  
    public static String MD5sign(String parmas, String key) {  
        String sign = "";
        try {  
        	parmas += "&key=" + key;  
        	sign = md5(parmas);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return sign;  
    }  
    
    public static void main(String[] args) {  
        Map<String ,Object> map = new HashMap<String,Object>();  
        map.put("companyNo", "1504a229bb034f19b6bf21738976981d");  
        map.put("salaryOrderCode", "2018040353359360");  
        map.put("predictNumber", 1);  
        map.put("predictTotalSum", "1.00");  
        /***MD5签名与验签**/  
        String key="7d50522f57df480c80c16703493bf259";  
        String sign= sign(map,key);  
        System.out.println("生成的MD5签名："+sign);  
    }  

}