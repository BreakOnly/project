package com.jrmf.payment.hddpay.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 
 * @author Admin
 */
public final class TtpayUtils {

    /** 
     * 除去数组中的空值和签名参数
     * 为了兼容健康商城签名问题，过滤sign2参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> filter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (StringUtils.isEmpty(value) || key.equalsIgnoreCase("sign")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
    	// 第一步：把字典按Key的字母顺序排序,参数使用TreeMap已经完成排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        
        // 第二步：把所有参数名和参数值串在一起
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (!StringUtils.isEmpty(value)) {
            	sb.append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }
    
    /**
	 * 方法说明：根据运单号返回还有校验位
	 * 
	 * @param no
	 * @return
	 */
    public static String createId(String mobile, int length) {
		int count = 0;
		String no = mobile.substring(0, length);//计算前6位校验码
		int len = no.length();
		for (int i=0;i<len;i++) {
			int p = (no.charAt(len - i -1)) * (i * 2 + 1);
			int q = divide(p, 10);
			int r = p - q * 10;
			count += (q + r);
		}
		return ((divide(count, 10) + 1) * 10 - count) % 10 + mobile.substring(mobile.length() - length + 1);
	}
	
	public static int divide(int x, int y) {
		if (y == 0) {
			return 0;
		}
		BigDecimal bigX = new BigDecimal(x);
		BigDecimal bigY = new BigDecimal(y);
		return bigX.divide(bigY, 0, RoundingMode.HALF_UP).intValue();
	}
	
	public static void main(String[] args) {
		System.out.println(TtpayUtils.createId("15000000013", 7));
	}
}
