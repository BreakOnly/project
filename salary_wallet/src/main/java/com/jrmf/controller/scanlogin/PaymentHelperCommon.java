package com.jrmf.controller.scanlogin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class PaymentHelperCommon {
	/**
	 * 
	 * @param json 需要发送的字符串 已组装好
	 * @param address  发送的地址
	 * @return
	 */
	public static String send(String json,String address){
		String jsonString  = "";
		try{
			 URL url = new URL(address);
		     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		     conn.setRequestMethod("POST");// 提交模式
		     conn.setDoOutput(true);// 是否输入参数
		     byte[] bypes = json.getBytes(); 
		     conn.getOutputStream().write(bypes);// 输入参数
		     
		     // 获取输出参数
		     InputStream inStream=conn.getInputStream();
		     jsonString = new String(readInputStream(inStream), "utf-8");
		}catch(Exception e){
			System.out.println("调外围接口异常"+e);
		}
		return jsonString;
	}
	   public static byte[] readInputStream(InputStream inStream) throws Exception{
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int len = 0;
	        while( (len = inStream.read(buffer)) !=-1 ){
	            outStream.write(buffer, 0, len);
	        }
	        byte[] data = outStream.toByteArray();//网页的二进制数据
	        outStream.close();
	        inStream.close();
	        return data;
	    }
}
