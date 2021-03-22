package com.jrmf.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.aip.util.Base64Util;
import com.jrmf.utils.baiduai.FileUtil;
import com.jrmf.utils.baiduai.HttpUtil;

public class OcrTest {
    //设置APPID/AK/SK
    private static final String APP_ID = "16895381";
    private static final String API_KEY = "YOS8NeW4xRutBPjcaYrBB6Zw";
    private static final String SECRET_KEY = "ZnAt76822VBEzoeFWgN3wN0vTdrhFMiG";
    
    public static void main(String[] args) {
    	getInvoice();
	}

    /**
     * 获取发票信息
     * @return
     */
    public static String getInvoice() {
    	String access_token = getAuth(API_KEY,SECRET_KEY);
    	String info = vatInvoice(access_token);
    	try {
    		JSONObject jsonObject = new JSONObject(info);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return info;
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
            System.err.println("result:" + result);
            JSONObject jsonObject = new JSONObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    
    public static String vatInvoice(String access_token) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";
        try {
            // 本地文件路径
            String filePath = "C:/Users/孙春辉/Desktop/微信图片_20200330184208.jpg";
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = access_token;

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
