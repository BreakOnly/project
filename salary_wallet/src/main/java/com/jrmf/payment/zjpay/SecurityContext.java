package com.jrmf.payment.zjpay;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SecurityContext {

	public static SSLContext sslContext;
	public static SSLSocketFactory sslSocketFactory;

	public static void initSSLSocketFactory(String trustKeyStore, String trustKeyStorePassword) throws Exception {
		TrustManager[] tms = getTrustManagers(trustKeyStore, "JKS", trustKeyStorePassword);

		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tms, null);

		sslSocketFactory = sslContext.getSocketFactory();
	}
	
	public static TrustManager[] getTrustManagers(String trustStore, String trustStoreType, String trustStorePassword) throws Exception {
		String algorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
		KeyStore keyStore = KeyStore.getInstance(trustStoreType);
		FileInputStream fileInputStream = new FileInputStream(trustStore);
		keyStore.load(fileInputStream, trustStorePassword.toCharArray());
		fileInputStream.close();
		trustManagerFactory.init(keyStore);
		TrustManager[] tms = trustManagerFactory.getTrustManagers();
		return tms;
	}
}
