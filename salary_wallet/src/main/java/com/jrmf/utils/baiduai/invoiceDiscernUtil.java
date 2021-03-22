package com.jrmf.utils.baiduai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aip.util.Base64Util;

public class invoiceDiscernUtil {

	private static Logger logger = LoggerFactory.getLogger(invoiceDiscernUtil.class);

	//设置APPID/AK/SK
	private static final String APP_ID = "16895381";
	private static final String API_KEY = "YOS8NeW4xRutBPjcaYrBB6Zw";
	private static final String SECRET_KEY = "ZnAt76822VBEzoeFWgN3wN0vTdrhFMiG";


	/**
	 * 获取发票信息
	 * @return
	 */
	public static Map<String, String> getInvoice(byte[] imgData) {
		Map<String, String> invoiceInfo = new HashMap<String, String>();
		String access_token = getAuth(API_KEY,SECRET_KEY);
		if(access_token!=null){
			try {
				String info = vatInvoiceByInputStream(access_token,imgData);
				JSONObject jsonObject = new JSONObject(info);
				if(jsonObject.isNull("error_code")){
					JSONObject invoiceResult = jsonObject.getJSONObject("words_result");
					invoiceInfo.put("code", "0000");
					invoiceInfo.put("invoiceCode", invoiceResult.getString("InvoiceCode"));
					invoiceInfo.put("invoiceNum", invoiceResult.getString("InvoiceNum"));
					invoiceInfo.put("invoiceAmount", invoiceResult.getString("AmountInFiguers"));
				}else{
					invoiceInfo.put("code", jsonObject.getString("error_code"));
					invoiceInfo.put("msg", jsonObject.getString("error_msg"));
				}
			} catch (JSONException e) {
				logger.error(e.getMessage(),e);
				invoiceInfo.put("code", "1002");
				invoiceInfo.put("msg", "获取发票信息异常");
			}
		}else{
			invoiceInfo.put("code", "1001");
			invoiceInfo.put("msg", "获取token信息失败");
		}
		return invoiceInfo;
	}

	public static String vatInvoice(String access_token,String filePath) {
		// 请求url
		String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";
		try {
			// 本地文件路径
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			String param = "image=" + imgParam;

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			String accessToken = access_token;
			logger.info("获取发票信息请求地址:" + url);
			logger.info("获取发票信息请求参数:" + param);
			String result = HttpUtil.post(url, accessToken, param);
			logger.info("获取发票信息响应信息:" + result);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	public static String vatInvoiceByInputStream(String access_token,byte[] imgData) {
		// 请求url
		String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";
		try {
			// 本地文件路径
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			String param = "image=" + imgParam;

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			String accessToken = access_token;
			logger.info("获取发票信息请求地址:" + url);
			logger.info("获取发票信息请求参数:" + param);
			String result = HttpUtil.post(url, accessToken, param);
			logger.info("获取发票信息响应信息:" + result);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}	

	public static String getAuth(String ak, String sk) {
		// 获取token地址
		String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
		String getAccessTokenUrl = authHost
				// 1. grant_type为固定参数
				+ "grant_type=client_credentials"
				// 2. 官网获取的 API Key
				+ "&client_id=" + ak
				// 3. 官网获取的 Secret Key
				+ "&client_secret=" + sk;
		try {
			logger.info("获取token请求地址:" + getAccessTokenUrl);
			URL realUrl = new URL(getAccessTokenUrl);
			// 打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.err.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响应
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String result = "";
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			/**
			 * 返回结果示例
			 */
			logger.info("result:" + result);
			JSONObject jsonObject = new JSONObject(result);
			String access_token = jsonObject.getString("access_token");
			return access_token;
		} catch (Exception e) {
			logger.info("获取token失败！");
		}
		return null;
	}

}
