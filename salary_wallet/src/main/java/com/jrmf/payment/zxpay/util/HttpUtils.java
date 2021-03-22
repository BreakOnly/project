package com.jrmf.payment.zxpay.util;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.scanlogin.MyX509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: he
 * @Description:
 *
 */
public class HttpUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);




	/**
	 * post请求-json参数
	 */
	public static String postHttp(String url, JSONObject jsonParm) {
		return postHttp(url, new StringEntity(jsonParm.toString(), "UTF-8"));
	}


	/**
	 * post请求
	 */
	private static String postHttp(String url, HttpEntity formEntity) {
		CloseableHttpClient client = null;
		HttpPost post = null;
		String result = "";
		try {
			client = HttpClients.createDefault();
			post = new HttpPost(url);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			post.setEntity(formEntity);
			HttpResponse resp = client.execute(post);
			HttpEntity entity = resp.getEntity();
			result = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
		return result;
	}

	/**
	 * 发起post请求
	 *
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doHttpClientPost(String url, Map<String, String> params) {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String rst = null;
		try {
			// 创建一个post对象
			List<NameValuePair> ps = buildPostParams(params);
			HttpPost post = new HttpPost(url);
			logger.info("請求信息地址：{}，打印：{}", url, ps);
			post.setEntity(new UrlEncodedFormEntity(ps, "UTF-8"));
			// 执行post请求
			response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {// 网关调用成功
				rst = inputStreamToStr(response.getEntity().getContent(), "UTF-8");
				logger.info("=======================================");
				logger.info(String.format("httpClient Post调用结果：%s", rst));
				logger.info("=======================================");
			}
		} catch (Exception e) {
			logger.info("=======================================");
			logger.info(String.format("httpClient Post 请求失败：{}", e));
			logger.info("=======================================");
		} finally {
			try {
				if (null != response)
					response.close();
				if (null != httpClient)
					httpClient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		return rst;
	}

	/**
	 * json request
	 *
	 * @param url
	 * @param json
	 * @return
	 */
	public static String doPost(String url, String json) {
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		HttpGet get = new HttpGet(url);

		String response = null;
		try {
			StringEntity s = new StringEntity(json,"UTF-8");
//			s.setContentEncoding("UTF-8");
			// 发送json数据需要设置contentType
			s.setContentType("application/json");
			post.setEntity(s);
			HttpResponse res = httpclient.execute(post);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				response = EntityUtils.toString(res.getEntity());// 返回json格式：
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	/**
	 * 组合map参数
	 *
	 * @param params
	 * @return
	 */
	private static List<NameValuePair> buildPostParams(Map<String, String> params) {
		if (params == null || params.size() == 0)
			return null;
		List<NameValuePair> results = new ArrayList<NameValuePair>();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			results.add(new BasicNameValuePair(key, value));
		}

		return results;
	}

	/**
	 * 根据请求的输入流获取请求参数
	 *
	 * @param is
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	private static String inputStreamToStr(InputStream is, String charset) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		return new String(buffer.toString().getBytes("ISO-8859-1"), charset);
	}

	/**
	 * 根据请求获取请求IP
	 *
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}
	
    /*
     * 处理https GET/POST请求
     * 请求地址、请求方法、参数
     * */
    public static String httpsRequest(String requestUrl,String requestMethod,String outputStr){
        StringBuffer buffer=null;
        try{
        //创建SSLContext
        SSLContext sslContext=SSLContext.getInstance("SSL");
        TrustManager[] tm={new MyX509TrustManager()};
        //初始化
        sslContext.init(null, tm, new java.security.SecureRandom());;
        //获取SSLSocketFactory对象
        SSLSocketFactory ssf=sslContext.getSocketFactory();
        URL url=new URL(requestUrl);
        HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod(requestMethod);
        //设置当前实例使用的SSLSoctetFactory
        conn.setSSLSocketFactory(ssf);
        conn.connect();
        //往服务器端写内容
        if(null!=outputStr){
            OutputStream os=conn.getOutputStream();
            os.write(outputStr.getBytes("utf-8"));
            os.close();
        }
        
        //读取服务器端返回的内容
        InputStream is=conn.getInputStream();
        InputStreamReader isr=new InputStreamReader(is,"utf-8");
        BufferedReader br=new BufferedReader(isr);
        buffer=new StringBuffer();
        String line=null;
        while((line=br.readLine())!=null){
            buffer.append(line);
        }
        }catch(Exception e){
					logger.error(e.getMessage(),e);
        }
        return buffer.toString();
    }
}
