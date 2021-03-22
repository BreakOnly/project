package com.jrmf.payment.hddpay.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class WebUtils {
	private static final int CONNECT_TIMEOUT = Config.getHttpConnectTimeout();// 设置连接建立的超时时间为10s
	private static final int SOCKET_TIMEOUT = Config.getHttpSocketTimeout();
	private static final int MAX_CONN = Config.getHttpMaxPoolSize(); // 最大连接数
	private static final int MAX_PRE_ROUTE = Config.getHttpMaxPoolSize();
	private static final int MAX_ROUTE = Config.getHttpMaxPoolSize();

	private static CloseableHttpClient httpClient; // 发送请求的客户端单例
	private static PoolingHttpClientConnectionManager manager; // 连接池管理类
	private static ScheduledExecutorService monitorExecutor;

	private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全

	/**
	 * http 请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String post(String url, Map<String, String> params) throws Exception {
		CloseableHttpClient client = getHttpClient(url);
		String responseText = "";
		CloseableHttpResponse response = null;
		try {
			HttpPost method = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
					.setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
			method.setConfig(requestConfig);
			if (params != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Entry<String, String> param : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
			}
			response = client.execute(method, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (null != response) {
					response.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
		return responseText;
	}

	public static String post(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
		CloseableHttpClient client = getHttpClient(url);
		String responseText = "";
		CloseableHttpResponse response = null;
		try {
			HttpPost method = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
					.setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
			method.setConfig(requestConfig);
			if (null != headers && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					method.setHeader(entry.getKey(), entry.getValue());
				}
			}
			if (params != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Entry<String, String> param : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
			}
			response = client.execute(method, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (null != response) {
					response.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
		return responseText;
	}

	/**
	 * 通过流方式传数据
	 *
	 * @param uri
	 * @param data
	 * @return
	 */
	public static String post(String uri, String data) {
		log.info("向银行发起参数：{}", data);
		CloseableHttpClient httpClient = getHttpClient(uri);
		HttpPost method = new HttpPost(uri);
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
		method.setConfig(requestConfig);

		String readContent = null;
		try {
			method.addHeader("Content-type", "application/xml; charset=ISO-8859-1");
			method.setHeader("Accept", "application/xml");
			method.setEntity(new StringEntity(data, Charset.forName("utf-8")));
			HttpResponse response = httpClient.execute(method, HttpClientContext.create());
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				return "failed";
			}
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			int count = 0;
			while (count == 0) {
				count = Integer.parseInt("" + entity.getContentLength());// in.available();
			}
			if (count <= 0) {
				return EntityUtils.toString(entity);
			}
			byte[] bytes = new byte[count];
			int readCount = 0; // 已经成功读取的字节的个数
			while (readCount <= count) {
				if (readCount == count) {
					break;
				}
				readCount += in.read(bytes, readCount, count - readCount);
			}
			readContent = new String(bytes, 0, readCount, "UTF-8");
			log.info("Get Response Content():\n {}", readContent);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}
		return readContent;
	}

	/**
	 * 关闭连接池
	 */
	public static void closeConnectionPool() {
		try {
			httpClient.close();
			manager.close();
			monitorExecutor.shutdown();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * 把map转为xml
	 *
	 * @param parameters
	 * @return
	 */
	public static String parseXML(Map<String, String> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Iterator<Entry<String, String>> itor = parameters.entrySet().iterator();
		while (itor.hasNext()) {
			Entry<String, String> entry = itor.next();
			String k = entry.getKey();
			String v = entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				sb.append("<" + k + ">" + parameters.get(k) + "</" + k + ">\n");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * 把map转为xml
	 * 
	 * @param parameters
	 * @return
	 */
	public static String parseJSON(Map<String, String> parameters) {
		return new JSONObject().toJSONString(parameters);
	}

	/**
	 * 从URL中提取所有的参数。
	 * 
	 * @param query URL地址
	 * @return 参数映射
	 */
	public static Map<String, String> splitUrlQuery(String query) {
		Map<String, String> result = new HashMap<String, String>();

		String[] pairs = query.split("&");
		if (pairs != null && pairs.length > 0) {
			for (String pair : pairs) {
				String[] param = pair.split("=", 2);
				if (param != null && param.length == 2) {
					result.put(param[0], param[1]);
				}
			}
		}
		return result;
	}

	public static CloseableHttpClient getHttpClient(String url) {
		int port = 80;
		String hostName = url.split("/")[2];
        if (hostName.contains(":")){
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }

		if (httpClient == null) {
			// 多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
			synchronized (syncLock) {
				if (httpClient == null) {
					httpClient = createHttpClient(hostName, port);
					// 开启监控线程,对异常和空闲线程进行关闭
					monitorExecutor = Executors.newScheduledThreadPool(1);
					monitorExecutor.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							// 关闭异常连接
							manager.closeExpiredConnections();
							// 关闭5s空闲的连接
							manager.closeIdleConnections(Config.getHttpIdelTimeout(), TimeUnit.MILLISECONDS);
						}
					}, Config.getHttpMonitorInterval(), Config.getHttpMonitorInterval(), TimeUnit.MILLISECONDS);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 根据host和port构建httpclient实例
	 * 
	 * @param host 要访问的域名
	 * @param port 要访问的端口
	 * @return
	 */
	public static CloseableHttpClient createHttpClient(String host, int port) {
		ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", plainSocketFactory).register("https", sslSocketFactory).build();

		manager = new PoolingHttpClientConnectionManager(registry);
		// 设置连接参数
		manager.setMaxTotal(MAX_CONN); // 最大连接数
		manager.setDefaultMaxPerRoute(MAX_PRE_ROUTE); // 路由最大连接数

		HttpHost httpHost = new HttpHost(host, port);
		manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);

		// 请求失败时,进行请求重试
		HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
				if (i > 3) {
					// 重试超过3次,放弃请求
					log.error("retry has more than 3 time, give up request");
					return false;
				}
				if (e instanceof NoHttpResponseException) {
					// 服务器没有响应,可能是服务器断开了连接,应该重试
					log.error("receive no response from server, retry");
					return true;
				}
				if (e instanceof SSLHandshakeException) {
					// SSL握手异常
					log.error("SSL hand shake exception");
					return false;
				}
				if (e instanceof InterruptedIOException) {
					// 超时
					log.error("InterruptedIOException");
					return false;
				}
				if (e instanceof UnknownHostException) {
					// 服务器不可达
					log.error("server host unknown");
					return false;
				}
				if (e instanceof ConnectTimeoutException) {
					// 连接超时
					log.error("Connection Time out");
					return false;
				}
				if (e instanceof SSLException) {
					log.error("SSLException");
					return false;
				}

				HttpClientContext context = HttpClientContext.adapt(httpContext);
				HttpRequest request = context.getRequest();
				if (!(request instanceof HttpEntityEnclosingRequest)) {
					// 如果请求不是关闭连接的请求
					return true;
				}
				return false;
			}
		};
		return HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
	}
}

class Config {
	static int httpConnectTimeout = 3000;
	static int httpSocketTimeout = 60000;
	static int httpMaxPoolSize = 2000;
	static int httpMonitorInterval = 3000;
	static int httpIdelTimeout = 2000;

	public static int getHttpIdelTimeout() {
		return httpIdelTimeout;
	}

	public static int getHttpSocketTimeout() {
		return httpSocketTimeout;
	}

	public static int getHttpMaxPoolSize() {
		return httpMaxPoolSize;
	}

	public static int getHttpMonitorInterval() {
		return httpMonitorInterval;
	}

	public static int getHttpConnectTimeout() {
		return httpConnectTimeout;
	}
}
