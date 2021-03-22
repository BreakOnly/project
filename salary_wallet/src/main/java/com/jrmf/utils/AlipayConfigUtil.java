package com.jrmf.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

public class AlipayConfigUtil {
	private static Logger logger = LoggerFactory.getLogger(AlipayConfigUtil.class);
	public static Properties props;
	static {
		props = new Properties();
		// 使用InPutStream流读取properties文件
		try {
			ClassLoader classLoader = AlipayConfigUtil.class.getClassLoader();
			URL resource = classLoader.getResource("Alipay.properties");
			String path = resource.getPath();
			InputStream resourceAsStream = classLoader.getResourceAsStream("Alipay.properties");
			props.load(resourceAsStream);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	public static final String APP_ID = "APP_ID";
	public static final String APP_PRIVATE_KEY = "APP_PRIVATE_KEY";
	public static final String CHARSET = "CHARSET";
	public static final String ALIPAY_PUBLIC_KEY = "ALIPAY_PUBLIC_KEY";
	public static final String URL = "URL";
	public static final String TYPE = "TYPE";
	public static final String SECREAT = "SECREAT";
	public static final String PAYER_SHOW_NAME = "PAYER_SHOW_NAME";
	public static final String ALIPAY_USERID = "ALIPAY_USERID";
	public static final String ALIPAY_LOGONID = "ALIPAY_LOGONID";
	public static final String ACCOUNTNO = "ACCOUNTNO";
	public static final String BATCHOUTCALLBACKURL = "BATCHOUTCALLBACKURL";

	public static String getAccountno() {
		return props.getProperty(ACCOUNTNO);
	}

	public static String getPrivateKey() {
		return props.getProperty(APP_PRIVATE_KEY);
	}
	public static String getAppID() {
		return props.getProperty(APP_ID);
	}

	public static String getCharset() {
		return props.getProperty(CHARSET);
	}

	public static String getPublicKey() {
		return props.getProperty(ALIPAY_PUBLIC_KEY);
	}

	public static String getURL() {
		return props.getProperty(URL);
	}

	public static String getTYPE() {
		return props.getProperty(TYPE);
	}

	public static String getSecreat() {
		return props.getProperty(SECREAT);
	}

	public static String getPayerShowName() {
		return props.getProperty(PAYER_SHOW_NAME);
	}

	public static String getUserID() {
		return props.getProperty(ALIPAY_USERID);
	}

	public static String getLoginID() {
		return props.getProperty(ALIPAY_LOGONID);
	}

	public static String getBackURL() {
		return props.getProperty(BATCHOUTCALLBACKURL);
	}

}
