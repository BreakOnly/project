/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: HexUtil.java
 * Author:   Andy_Wang01
 * Date:     Aug 26, 2014 10:59:23 AM
 */
package com.jrmf.payment.zjpay;

/**
 * @author Andy_Wang01
 */
public class HexUtil {

    /**
     * 十六进制字符串转字节数组
     *
     * @param hexstr 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexstr2Bytes(String hexstr) {
        String upper = hexstr.toUpperCase();
        int length = upper.length() / 2;
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++) {
            byte high = (byte) ("0123456789ABCDEF".indexOf(upper.charAt(2 * i)));
            byte low = (byte) ("0123456789ABCDEF".indexOf(upper.charAt(2 * i + 1)));
            ret[i] = (byte) ((high << 4) + low);
        }
        return ret;
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytes2Hexstr(byte[] bytes) {
        StringBuilder ret = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret.append(hex.toUpperCase());
        }
        return ret.toString();
    }
}
