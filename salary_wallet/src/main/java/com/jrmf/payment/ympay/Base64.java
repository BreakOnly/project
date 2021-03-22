package com.jrmf.payment.ympay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/***
 *
 *
 * 描    述：Base64加解密
 *
 * 创 建 者： @author wl
 * 创建时间： 2019/1/23 14:47
 * 创建描述：
 *
 * 修 改 者：
 * 修改时间：
 * 修改描述：
 *
 * 审 核 者：
 * 审核时间：
 * 审核描述：
 *
 */
public class Base64 {
	/**
	 * Base64编码 加密
	 *
	 * @param data 待加密字节数组
	 * @return 加密后字符串
	 */
	public static String encode(byte[] data) {
		if (data == null) {
			return null;
		}
		return new String(org.bouncycastle.util.encoders.Base64.encode(data));
	}

	/**
	 * Base64编码 加密
	 *
	 * @param data 待加密字节数组
	 * @return 加密后字符串
	 */
	public static String encode(String data) {
		if (data == null) {
			return null;
		}
		return new String(org.bouncycastle.util.encoders.Base64.encode(data.getBytes()));
	}


	/**
	 * Base64编码解密
	 *
	 * @param data 待解密字符串
	 * @return 解密后字节数组
	 * @throws Exception 异常
	 */
	public static byte[] decode(String data) throws Exception {
		if (data == null) {
			return null;
		}
		try {
			return org.bouncycastle.util.encoders.Base64.decode(data.getBytes());
		} catch (RuntimeException e) {
			throw new Exception(e.getMessage(), e);
		}
	}


	/**
	 * Base64编码解密
	 *
	 * @param data 待解密字符串
	 * @return 解密后字节数组
	 * @throws CodecException 异常
	 */
	public static byte[] decode(byte[] data){
		if (data == null) {
			return null;
		}
		return org.bouncycastle.util.encoders.Base64.decode(data);
	}
	
    /**
     * Base64编码 加密
     *
     * @param data 待加密字节数组
     * @return 加密后字符串
     */
    public static String encodeGbk(String data) {
        if (data==null||data.equals("")) {
            return null;
        }
        String tranData = new BASE64Encoder().encode(data.getBytes(Charset.forName("GBK")));
        return tranData;
    }
    
    /**
     * Base64编码解密
     *
     * @param data 待解密字符串
     * @return 解密后字节数组
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     * @throws CodecException 异常
     */
    public static String decodeGbk(String data) throws Exception{
        if (data==null||data.equals("")) {
            return null;
        }
        //对消息进行Base64解码
        String tranData =new String(new BASE64Decoder().decodeBuffer(data),"GBK");
        return tranData;
    }
    
    /**
     * Base64编码解密
     *
     * @param data        参数
     * @param charsetName 字符编码
     * @return
     * @throws Exception
     */
    public static String decodeCharSet(String data, String charsetName) throws Exception {
        if (data==null||data.equals("")) {
            return null;
        }
        //对消息进行Base64解码
        String sourceData = new String(new BASE64Decoder().decodeBuffer(data), charsetName);
        return sourceData;
    }
}