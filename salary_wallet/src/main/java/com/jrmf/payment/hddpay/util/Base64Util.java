/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * www.hnapay.com
 */

package com.jrmf.payment.hddpay.util;

import java.util.Base64;

/**
 * base64编码工具类
 */
@SuppressWarnings({"all"})
public class Base64Util {

    /**
     * 将 s 进行 BASE64 编码
     *
     * @param s
     * @return
     */
    public static String encode(byte[] s) {
        if (s == null)
            return null;
        return Base64.getEncoder().encodeToString(s);
    }

    public static byte[] encodeByte(String s) {
        if (s == null)
            return null;
        return Base64.getEncoder().encode(s.getBytes());
    }

    /**
     * 将 s 进行 BASE64 编码
     *
     * @param s
     * @return
     */
    public static String encode(String s) {
        if (s == null)
            return null;
        return encode(s.getBytes());
    }

    /**
     * 将 BASE64 编码的字符串 s 进行解码
     *
     * @param s
     * @return
     */
    public static byte[] decode(String s) {
        if (s == null)
            return null;
        try {
            byte[] b = Base64.getDecoder().decode(s);
            return b;
        } catch (Exception e) {
            return null;
        }
    }


}


