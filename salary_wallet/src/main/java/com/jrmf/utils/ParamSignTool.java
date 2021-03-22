package com.jrmf.utils;

import net.sf.json.JSONObject;
import org.apache.http.NameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

/**
 * filename：com.jrmf.utils.ParamSignTool.java
 * 
 * @author: zhangyong
 * @time: 2015-9-18下午2:25:44
 */

public class ParamSignTool {
	public static boolean signVerify(String seckey, HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> map = request.getParameterMap();
		Map<String, String> data = new HashMap<String, String>();
		for (String key : map.keySet()) {
			data.put(key, map.get(key)[0]);
		}
		return signVerify(seckey, data);
	}

	private static boolean signVerify(String seckey, Map<String, String> params) {
		Map<String, String> map = new HashMap<String, String>();
		for (String key : params.keySet()) {
			if (!key.equals("sign")) {
				map.put(key, params.get(key));
			}
		}
		map.put("seckey", seckey);
		String sign = sign(map);
		if (sign.equals(params.get("sign"))) {
			return true;
		}
		return false;
	}

	private static String toHexValue(byte[] messageDigest) {
		if (messageDigest == null){
			return "";
		}
		StringBuilder hexValue = new StringBuilder();
		for (byte aMessageDigest : messageDigest) {
			int val = 0xFF & aMessageDigest;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	public static String sign(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		StringBuffer sb = new StringBuffer();
		int count = keys.size();
		for (String s : keys) {
			sb.append(s);
			sb.append(":");
			if (!StringUtil.hasNullStr(params.get(s))) {
				sb.append(params.get(s));
			}
			if (count > 1) {
				sb.append("|");
			}
			count--;
		}
		String sign = "";
		try {
			sign = toHexValue(encryptMD5(sb.toString().getBytes(
					Charset.forName("utf-8"))));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("md5 error");
		}
		return sign;
	}

	public static String sign(String seckey, List<NameValuePair> formparams) {
		Map<String, String> data = new HashMap<String, String>();
		for (NameValuePair pair : formparams) {
			if (!"sign".equals(pair.getName())
					&& !"seckey".equals(pair.getName())) {
				data.put(pair.getName(), pair.getValue());
			}
		}
		data.put("seckey", seckey);
		String sign = sign(data);
		return sign;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String sign(String seckey, HttpServletRequest request) {
		Map<String, String[]> map = request.getParameterMap();
		Map<String, String> data = new HashMap<String, String>();
		for (String key : map.keySet()) {
			if (!key.equals("sign") && !key.equals("seckey")) {
				data.put(key, map.get(key)[0]);
			}
		}
		data.put("seckey", seckey);
		String sign = sign(data);
		return sign;
	}

	private static byte[] encryptMD5(byte[] data) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(data);
		return md5.digest();
	}
	
	/**
	 * 生成签名字符串
	 * 
	 * @param seckey
	 * @return
	 */
	public static String sign(String seckey,Map<String, String> map) {
		Map<String, String> data = new HashMap<String, String>();
		for (String key : map.keySet()) {
			if (!key.equals("sign") && !key.equals("seckey")) {
				data.put(key, map.get(key));
			}
		}
		data.put("seckey", seckey);
		String sign = sign(data);
		return sign;
	}
	/*public static void main(String[] args) {
		List<CommissionDomain> BatchMoenyList = new ArrayList<CommissionDomain>();
		CommissionDomain moeny = new CommissionDomain();
		moeny.setUserNo("closedbate0001");
		moeny.setAmount("100");
		BatchMoenyList.add(moeny);
		String commissionsJson = com.alibaba.fastjson.JSONArray.toJSONString(BatchMoenyList);
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantId", "aiyuangong");
		map.put("originalId", "closedbate");
		map.put("timeStamp", "2018-05-23 22:25:11");
		map.put("seckey", "weadfgbhjyetrgs");
		map.put("commissionsJson", commissionsJson);
		map.put("originalBeachNo", "100001");
		System.out.println(commissionsJson);
		System.out.println(sign(map));
	}*/
	
	/*public static void main(String[] args) {
		List<UserDomain> BatchMoenyList = new ArrayList<UserDomain>();
		UserDomain moeny = new UserDomain();
		moeny.setUserNo("closedbate0001");
		moeny.setCardNo("371482197002210013");
		moeny.setBankcardNo("6228481818992299275");
		moeny.setMobile("13639491167");
		moeny.setUserName("王斌");
		BatchMoenyList.add(moeny);
		String commissionsJson = com.alibaba.fastjson.JSONArray.toJSONString(BatchMoenyList);
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantId", "aiyuangong");
		map.put("originalId", "closedbate");
		map.put("timeStamp", "2018-05-23 22:15:11");
		map.put("seckey", "weadfgbhjyetrgs");
		map.put("usersJson", commissionsJson);
		System.out.println(commissionsJson);
		System.out.println(sign(map));
	}*/
	
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantId", "aiyuangong");
		map.put("originalId", "closedbate");
		map.put("timeStamp", "2018-05-28 9:29:11");
		map.put("seckey", "c569a3f8-6645-4b26-b6e7-651de5ff0d91");
		map.put("userId", "610");
		map.put("startDate", "");
		map.put("endDate", "");
		map.put("tranType", "");
		map.put("typeFlag", "");
		System.out.println(sign(map));
	}
	
	/**
	 * 获得请求的参数及值
	 * 
	 * @param request
	 * @return
	 */
	public static String getReqParams2JsonStr(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> map = request.getParameterMap();  
		Map<String, String> data = new HashMap<String, String>();
		for (String key : map.keySet()) {
			data.put(key , map.get(key)[0]);
		}
		return JSONObject.fromObject(data).toString();
	}
}
