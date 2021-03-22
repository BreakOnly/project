package com.jrmf.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public class MapUtil {

	public static Map getParameterMap(HttpServletRequest request) {  
		// 参数Map  
		Map properties = request.getParameterMap();  
		// 返回值Map  
		Map returnMap = new HashMap();  
		Iterator entries = properties.entrySet().iterator();  
		Map.Entry entry;  
		String name = "";  
		String value = "";  
		while (entries.hasNext()) {  
			entry = (Map.Entry) entries.next();  
			name = (String) entry.getKey();  
			Object valueObj = entry.getValue();  
			if(null == valueObj){  
				value = "";  
			}else if(valueObj instanceof String[]){  
				String[] values = (String[])valueObj;  
				for(int i=0;i<values.length;i++){  
					value = values[i] + ",";  
				}  
				value = value.substring(0, value.length()-1);  
			}else{  
				value = valueObj.toString();  
			}  
			returnMap.put(name, value);  
		}  
		return returnMap;  
	}

	/**
	 * 拼接签名参数(key=value&key=value)
	 * @param map
	 * @return
	 */
	public static String mapToStr(Map<String, String> map) {
		Object[] keys =  map.keySet().toArray();
		Arrays.sort(keys);
		StringBuilder originStr = new StringBuilder();
		for(Object key:keys){
			if(!StringUtils.isEmpty(key)&&!StringUtils.isEmpty(String.valueOf(key)))
				originStr.append(key).append("=").append(String.valueOf(map.get(key))).append("&");
		}
		String str = originStr==null?"":originStr.toString().substring(0, originStr.length()-1);
		return str;			
	}
	
	/**
	 * 拼接签名参数(key=value&key=value)
	 * @param map
	 * @return
	 */
	public static String strArrayToStr(String []strs) {
		StringBuilder originStr = new StringBuilder();
		for (String str : strs) {
			originStr.append(str).append(",");
		}
		String str = originStr==null?"":originStr.toString().substring(0, originStr.length()-1);
		return str;			
	}
	

	/**
	 * 获取请求数据
	 * @param in
	 * @return
	 */
	public static String getContents(InputStream in ) {
		BufferedReader bre = null;
		StringBuffer sb = new StringBuffer();
		String contents = "";
		try {
			bre = new BufferedReader(new InputStreamReader(in));
			while ((contents = bre.readLine()) != null) {// 判断最后一行不存在，为空结束循环
				sb.append(contents);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(bre!=null){
					bre.close();
				}
			} catch (Exception e2) {
			}
			try {
				if(in!=null){
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return sb.toString();
	}

}
