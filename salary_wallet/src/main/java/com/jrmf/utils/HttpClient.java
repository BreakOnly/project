package com.jrmf.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.reflect.TypeToken;
import com.jrmf.utils.MapUtil;
import com.jrmf.utils.sms.channel.ym.response.ResponseData;
import com.jrmf.utils.sms.channel.ym.response.SmsResponse;

/**
 * http、https 请求工具类， 微信为https的请求
 * @author yehx
 *
 */
public class HttpClient {

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String _GET = "GET"; // GET
	private static final String _POST = "POST";// POST
	public static final int DEF_CONN_TIMEOUT = 30000;
	public static final int DEF_READ_TIMEOUT = 30000;

	/**
	 * 初始化http请求参数
	 * 
	 * @param url
	 * @param method
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private static HttpURLConnection initHttp(String url, String method,
			Map<String, String> headers,Integer connTimeout,Integer readTimeout) throws Exception {
		URL _url = new URL(url);
		HttpURLConnection http = (HttpURLConnection) _url.openConnection();
		// 连接超时
		if(connTimeout==null){
			http.setConnectTimeout(DEF_CONN_TIMEOUT);
		}else{
			http.setConnectTimeout(connTimeout);
		}
		if(readTimeout==null){
			// 读取超时 --服务器响应比较慢，增大时间
			http.setReadTimeout(DEF_READ_TIMEOUT);
		}else{
			http.setReadTimeout(readTimeout);
		}
		http.setUseCaches(false);
		http.setRequestMethod(method);
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		http.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
		if (null != headers && !headers.isEmpty()) {
			for (Entry<String, String> entry : headers.entrySet()) {
				http.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		http.setDoOutput(true);
		http.setDoInput(true);
		http.connect();
		return http;
	}


	private static HttpsURLConnection initHttps(String url, String method,
			Map<String, String> headers,HttpsParams httpsParams,Integer connTimeout,Integer readTimeout) throws Exception {
		URL _url = new URL(url);
		SSLContext ctx = getSSLContext(httpsParams);
		HttpsURLConnection http = (HttpsURLConnection) _url.openConnection();
		if(connTimeout==null){
			http.setConnectTimeout(DEF_CONN_TIMEOUT);
		}else{
			http.setConnectTimeout(connTimeout);
		}
		if(readTimeout==null){
			// 读取超时 --服务器响应比较慢，增大时间
			http.setReadTimeout(DEF_READ_TIMEOUT);
		}else{
			http.setReadTimeout(readTimeout);
		}
		http.setSSLSocketFactory(ctx.getSocketFactory());
		http.setHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		// 连接超时
		http.setConnectTimeout(DEF_CONN_TIMEOUT);
		// 读取超时 --服务器响应比较慢，增大时间
		http.setReadTimeout(DEF_READ_TIMEOUT);
		http.setUseCaches(false);
		http.setRequestMethod(method);
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		http.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
		if (null != headers && !headers.isEmpty()) {
			for (Entry<String, String> entry : headers.entrySet()) {
				http.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		http.setDoOutput(true);
		http.setDoInput(true);
		http.connect();
		return http;
	}

	/**
	 * 获得SSLSocketFactory.
	 * 
	 * @param password
	 *            密码
	 * @param keyStorePath
	 *            密钥库路径
	 * @param trustStorePath
	 *            信任库路径
	 * @return SSLSocketFactory
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws Exception
	 */
	private static SSLContext getSSLContext(HttpsParams params) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		// 实例化SSL上下文
		SSLContext ctx = SSLContext.getInstance("TLS");
		if (params != null) {
			// 实例化密钥库 KeyManager选择证书证明自己的身份
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			// 实例化信任库 TrustManager决定是否信任对方的证书
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			// 获得密钥库
			KeyStore keyStore = getKeyStore(params);
			// 初始化密钥工厂
			keyManagerFactory.init(keyStore, params.getPassword().toCharArray());
			// 获得信任库
			KeyStore trustStore = getKeyStore(params);
			// 初始化信任库
			trustManagerFactory.init(trustStore);
			// 初始化SSL上下文
			ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
		} else {
			ctx.init(null, new TrustManager[] { myX509TrustManager }, new java.security.SecureRandom());
		}
		return ctx;
	}

	/**
	 * 获得KeyStore.
	 * 
	 * @param keyStorePath
	 *            密钥库路径
	 * @param password
	 *            密码
	 * @return 密钥库
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	private static KeyStore getKeyStore(HttpsParams params) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		// 实例化密钥库 KeyStore用于存放证书，创建对象时 指定交换数字证书的加密标准
		// 指定交换数字证书的加密标准
		KeyStore ks = KeyStore.getInstance(params.getAlgorithm());
		// 获得密钥库文件流
		FileInputStream is = new FileInputStream(params.getKeyStorePath());
		// 加载密钥库
		ks.load(is, params.getPassword().toCharArray());
		// 关闭密钥库文件流
		is.close();
		return ks;
	}

	private static TrustManager myX509TrustManager = new X509TrustManager() {

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	};

	/**
	 * 
	 * @description 功能描述: get 请求
	 * @return 返回类型:
	 * @throws Exception
	 */
	public static String get(String url, Map<String, String> params,Integer connTimeout,Integer readTimeout,
			Map<String, String> headers,HttpsParams config) throws Exception {
		HttpURLConnection http = null;
		if (isHttps(url)) {
			http = initHttps(initParams(url, params), _GET, headers,config,connTimeout,readTimeout);
		} else {
			http = initHttp(initParams(url, params), _GET, headers,connTimeout,readTimeout);
		}
		InputStream in = http.getInputStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(in,
				DEFAULT_CHARSET));
		String valueString = null;
		StringBuffer bufferRes = new StringBuffer();
		while ((valueString = read.readLine()) != null) {
			bufferRes.append(valueString);
		}
		in.close();
		if (http != null) {
			http.disconnect();// 关闭连接
		}
		return bufferRes.toString();
	}

	public static String get(String url,Integer connTimeout,Integer readTimeout) throws Exception {
		return get(url, null,connTimeout,readTimeout,null,null);
	}

	public static String get(String url, Map<String, String> params,Integer connTimeout,Integer readTimeout)
			throws Exception {
		return get(url, params,connTimeout,readTimeout,null,null);
	}

	public static String post(String url, String params,Integer connTimeout,Integer readTimeout,Map<String, String> headers,HttpsParams config)
			throws Exception {
		HttpURLConnection http = null;
		if (isHttps(url)) {
			http = initHttps(url, _POST, headers,config,connTimeout,readTimeout);
		} else {
			http = initHttp(url, _POST, null,connTimeout,readTimeout);
		}
		OutputStream out = http.getOutputStream();
		out.write(params.getBytes(DEFAULT_CHARSET));
		out.flush();
		out.close();

		InputStream in = http.getInputStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(in,
				DEFAULT_CHARSET));
		String valueString = null;
		StringBuffer bufferRes = new StringBuffer();
		while ((valueString = read.readLine()) != null) {
			bufferRes.append(valueString);
		}
		in.close();
		if (http != null) {
			http.disconnect();// 关闭连接
		}
		return bufferRes.toString();
	}

	/**
	 * 功能描述: 构造请求参数
	 * 
	 * @return 返回类型:
	 * @throws Exception
	 */
	public static String initParams(String url, Map<String, String> params)
			throws Exception {
		if (null == params || params.isEmpty()) {
			return url;
		}
		StringBuilder sb = new StringBuilder(url);
		if (url.indexOf("?") == -1) {
			sb.append("?");
		}
		sb.append(map2Url(params));
		return sb.toString();
	}

	/**
	 * map构造url
	 * 
	 * @return 返回类型:
	 * @throws Exception
	 */
	public static String map2Url(Map<String, String> paramToMap)
			throws Exception {
		if (null == paramToMap || paramToMap.isEmpty()) {
			return null;
		}
		StringBuffer url = new StringBuffer();
		boolean isfist = true;
		for (Entry<String, String> entry : paramToMap.entrySet()) {
			if (isfist) {
				isfist = false;
			} else {
				url.append("&");
			}
			url.append(entry.getKey()).append("=");
			String value = entry.getValue();
			if (!StringUtils.isEmpty(value)) {
				url.append(URLEncoder.encode(value, DEFAULT_CHARSET));
			}
		}
		return url.toString();
	}

	/**
	 * 检测是否https
	 * 
	 * @param url
	 */
	private static boolean isHttps(String url) {
		return url.startsWith("https");
	}

	/**
	 * https 域名校验
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;// 直接返回true
		}
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> params = new HashMap<String,String>();
		params.put("appId", "8SDK-EMY-6699-SEZOK");
		params.put("sign", "2d773c55bcbb21be02548b273ad7e844");
		params.put("timestamp", "20191225163141");
		params.put("mobiles", "15210679032");
		params.put("content", URLEncoder.encode("【金融魔方】验证码 765571，为了您的帐号安全，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务"));
		String reqStr = MapUtil.mapToStr(params);
		String a = HttpClient.post("http://www.btom.cn:8080/simpleinter/sendSMS",reqStr,null,null,null,null);
		ResponseData<SmsResponse[]> data = JsonHelper.fromJson(new TypeToken<ResponseData<SmsResponse[]>>() {
		}, a);
		
		System.out.println(a);
	}
	
	
}