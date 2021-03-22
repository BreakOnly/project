package com.jrmf.payment.zjpay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HttpsConnection {

	protected String method = "POST";

	protected String outputCharset = "UTF-8";

	protected String inputCharset = "UTF-8";

	protected boolean useDefaultSSLSocketFactory = true;

	protected boolean ignoreHostname = false;

	protected boolean useHttpProxy = false;

	protected String contentType = "application/x-www-form-urlencoded";

	private int connectTimeoutLimit = 50000;

	private int readTimeoutLimit = 50000;

	private HttpsURLConnection httpsURLConnection;

	public HttpsConnection(String spec,String ssl_path,String keystore_pass)throws Exception
	{
		URL url = new URL(spec);
		SecurityContext.initSSLSocketFactory(ssl_path,keystore_pass);
		this.httpsURLConnection = ((HttpsURLConnection)url.openConnection());
	}

	public String send(List<NameValuePair> list)
			throws IOException
	{
		HttpData httpData = new HttpData(list, this.outputCharset);
		String request = httpData.getData();

		String response = send(request);
		return response;
	}


	public String send(String request) throws IOException
	{
		this.httpsURLConnection.setSSLSocketFactory(SecurityContext.sslSocketFactory);
		this.httpsURLConnection.setRequestProperty("Content-Type", this.contentType);
		this.httpsURLConnection.setDoOutput(true);
		this.httpsURLConnection.setRequestMethod(this.method);
		this.httpsURLConnection.setConnectTimeout(this.connectTimeoutLimit);
		this.httpsURLConnection.setReadTimeout(this.readTimeoutLimit);

		OutputStream outputStream = null;
		InputStream inputStream = null;
		String response = null;
		try
		{
			outputStream = this.httpsURLConnection.getOutputStream();
			outputStream.write(request.getBytes(this.outputCharset));
			outputStream.flush();

			inputStream = this.httpsURLConnection.getInputStream();
			byte[] bytes = IoUtil.read(inputStream, 1024);

			response = new String(bytes, this.inputCharset).trim();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null)
					inputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

}
