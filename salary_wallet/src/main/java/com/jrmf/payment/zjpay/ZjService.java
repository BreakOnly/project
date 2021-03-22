package com.jrmf.payment.zjpay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.payment.ympay.HttpClientUtil;
import com.jrmf.utils.XML;

public class ZjService {

	private Logger logger = LoggerFactory.getLogger(ZjService.class); 

	//商户私钥
	private String privateKey;
	//中金公钥
	private String publicKey;
	//商户代码
	private String merchant_id;
	//付款账号
	private String payment_account_number;
	//付款户名
	private String payment_account_name;
	//请求url
	private String url;	
	//ssl证书地址
	private String ssl_path;
	//ssl证书密码
	private String keystore_pass;
	//请求参数
	private Map<String, Object> paramsMap;



	public ZjService(String url, String merchant_id,
			String payment_account_number, String payment_account_name,
			String privateKey, String publicKey, Map<String, Object> paramsMap,String ssl_path,String keystore_pass) {
		super();
		this.url = url;
		this.merchant_id = merchant_id;
		this.payment_account_number = payment_account_number;
		this.payment_account_name = payment_account_name;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.paramsMap = paramsMap;
		this.ssl_path=ssl_path;
		this.keystore_pass=keystore_pass;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPayment_account_number() {
		return payment_account_number;
	}

	public void setPayment_account_number(String payment_account_number) {
		this.payment_account_number = payment_account_number;
	}

	public String getPayment_account_name() {
		return payment_account_name;
	}

	public void setPayment_account_name(String payment_account_name) {
		this.payment_account_name = payment_account_name;
	}

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public String getSsl_path() {
		return ssl_path;
	}

	public void setSsl_path(String ssl_path) {
		this.ssl_path = ssl_path;
	}

	public String getKeystore_pass() {
		return keystore_pass;
	}

	public void setKeystore_pass(String keystore_pass) {
		this.keystore_pass = keystore_pass;
	}

	/**
	 * 单笔转账
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> singleTransFer() throws Exception{
		Map<String, Object> reqMap = new LinkedHashMap<String, Object>();
		Map<String, Object> tranDataMap = new LinkedHashMap<String, Object>();
		Map<String, Object> headMap = new LinkedHashMap<String, Object>();
		//请求头部
		headMap.put("TxCode", ZjConfig.SINGLE_TRANSFER_METHOD);
		headMap.put("InstitutionID", merchant_id);
		//请求内容
		Map<String, Object> bodyMap = new LinkedHashMap<String, Object>();
		//付款人信息
		Map<String, Object> payerMap = new LinkedHashMap<String, Object>();
		//付款账户
		payerMap.put("PaymentAccountName", payment_account_name);
		//付款账号
		payerMap.put("PaymentAccountNumber", payment_account_number);
		//收款人信息
		Map<String, Object> payeeMap = new LinkedHashMap<String, Object>();
		//账户类型11：个人账户，12：企业账户
		payeeMap.put("AccountType", paramsMap.get("AccountType"));
		//银行卡编码
		payeeMap.put("BankID", paramsMap.get("BankID"));
		//收款账户名
		payeeMap.put("BankAccountName", paramsMap.get("BankAccountName"));
		//收款账号
		payeeMap.put("BankAccountNumber", paramsMap.get("BankAccountNumber"));
		//收款手机号码
		payeeMap.put("PhoneNumber", paramsMap.get("PhoneNumber"));
		bodyMap.put("TxSN", paramsMap.get("TxSN"));
		//代付标识1.支付账户余额代付2.代付通账户代付3.结算户代付
		bodyMap.put("PaymentFlag", "1");
		bodyMap.put("Payer", payerMap);
		bodyMap.put("Payee", payeeMap);
		bodyMap.put("Amount", paramsMap.get("Amount"));
		bodyMap.put("Remark", paramsMap.get("Remark"));
		tranDataMap.put("Head", headMap);
		tranDataMap.put("Body", bodyMap);
		reqMap.put("Request", tranDataMap);
		//将请求数据转换成xml格式
		String requestPlainText = XML.createXmlByMap(reqMap, "Request");
		logger.info("订单号："+paramsMap.get("TxSN")+"请求的明文信息为"+requestPlainText);
		byte[] data = requestPlainText.getBytes(StandardCharsets.UTF_8);
		//将请求数据base64编码
		String requestMessage = new String(Base64.encode(data));
		//签名
		String signature =Sign.signByPriKey(data, privateKey);
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("message", requestMessage);
		params.put("signature",signature);
		Map<String, Object> respMap = reqZjSevice(params);
		return respMap;
	}
	
	/**
	 * 单笔转账查询接口
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> transFerQuery() throws Exception{
		Map<String, Object> reqMap = new LinkedHashMap<String, Object>();
		Map<String, Object> tranDataMap = new LinkedHashMap<String, Object>();
		Map<String, Object> headMap = new LinkedHashMap<String, Object>();
		//请求头部
		headMap.put("TxCode", ZjConfig.SINGLE_TRANSFER_QUERY_METHOD);
		headMap.put("InstitutionID", merchant_id);
		//请求内容
		Map<String, Object> bodyMap = new LinkedHashMap<String, Object>();
		bodyMap.put("TxSN", paramsMap.get("TxSN"));
		tranDataMap.put("Head", headMap);
		tranDataMap.put("Body", bodyMap);
		reqMap.put("Request", tranDataMap);
		//将请求数据转换成xml格式
		String requestPlainText = XML.createXmlByMap(reqMap, "Request");
		logger.info("订单号："+paramsMap.get("TxSN")+"请求的明文信息为"+requestPlainText);
		byte[] data = requestPlainText.getBytes(StandardCharsets.UTF_8);
		//将请求数据base64编码
		String requestMessage = new String(Base64.encode(data));
		//签名
		String signature =Sign.signByPriKey(data, privateKey);
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("message", requestMessage);
		params.put("signature",signature);
		Map<String, Object> respMap = reqZjSevice(params);
		return respMap;	
	}

	/**
	 * 请求中金
	 * @return
	 * @throws CodeException 
	 */
	public Map<String, Object> reqZjSevice(Map<String, Object> params) throws Exception{
		Map<String, Object>	respMap = new HashMap<String, Object>();
		String[] response = new String[2];
		ArrayList list = new ArrayList();
		//请求加密后的内容
		String message = String.valueOf(params.get("message"));
		//请求的签名信息
		String signature = String.valueOf(params.get("signature"));
		NameValuePair messagePair = new NameValuePair("message", message);
		NameValuePair signaturePair = new NameValuePair("signature", signature);
		list.add(messagePair);
		list.add(signaturePair);
		try {
			logger.info("请求地址为："+url);
			logger.info("resMessage=【"+message+"】,reqSignature="+"【"+signature+"】");
			//请求开始时间
			long reqStartTime = System.currentTimeMillis();
			HttpsConnection httpsConnection = new HttpsConnection(url,ssl_path,keystore_pass);
			String responseText = httpsConnection.send(list);
			//请求结束时间
			long reqEndTime = System.currentTimeMillis();
			//此次请求共用多长时间
			long time = reqEndTime-reqStartTime;
			logger.info("此次连接共用时为：【"+time+"】毫秒");
			logger.info("响应信息为："+responseText);
			int index = responseText.indexOf(44);
			if (index > 0) {
				response[0] = responseText.substring(0, index);
				response[1] = responseText.substring(index + 1, responseText.length());
				logger.info("responseMessage=【"+response[0]+"】,responseSignature="+"【"+response[1]+"】");
				respMap.put("responseMessage", response[0]);
				respMap.put("responseSignature", response[1]);
			} else {
				logger.error("响应数据格式不正确。 正确格式：message,signature");
		        throw new CodeException("响应数据格式不正确。 正确格式：message,signature");
			}
		} catch (IOException e) {
			logger.error("中金通讯异常",e);
	        throw new CodeException("280001", "通讯异常");
		}
		return respMap;
	}


}
