package com.jrmf.payment.zxpay;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.payment.ymyf.util.DataConvertUtil;
import com.jrmf.payment.zxpay.util.HttpUtils;
import com.jrmf.payment.zxpay.util.Md5EncryptUtils;
import com.jrmf.payment.zxpay.util.RSA;

public class ZXService {
	

	private static Logger logger = LoggerFactory.getLogger(ZXService.class);

	//商户私钥
	private String privateKey;
	//商银信公钥
	private String publicKey;
	//商户代码
	private String merchant_id;
	//请求url
	private String url;
	//签名key
	private String md5Key;
	//请求参数
	private Map<String, Object> paramsMap;

	private String charset ="UTF-8";

	public ZXService(String url, String merchant_id, String privateKey,
			String publicKey, String md5Key, Map<String, Object> paramsMap,
			String charset) {
		super();
		this.url = url;
		this.merchant_id = merchant_id;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.md5Key = md5Key;
		this.paramsMap = paramsMap;
		this.charset = charset;
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

	public String getMd5Key() {
		return md5Key;
	}

	public void setMd5Key(String md5Key) {
		this.md5Key = md5Key;
	}

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * 付款
	 * @throws Exception 
	 */
	public Map<String, Object> payment() throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("outMemberNo", merchant_id);
        map.put("outerOrderNo", paramsMap.get("orderNo"));
        map.put("name", paramsMap.get("userName"));
        map.put("certificateNo", paramsMap.get("certId"));
        map.put("predictAmount", paramsMap.get("amount"));
        String signs = Md5EncryptUtils.sign(map, md5Key);
        map.put("charset", charset);
        map.put("mobile", paramsMap.get("phoneNo"));
        map.put("version", "1.1");
        map.put("service", ZXConfig.PAYMENT_PAY);
        map.put("Md5Key", signs);
        map.put("notifyUrl", paramsMap.get("notifyUrl"));
        map.put("cardType", paramsMap.get("cardType"));
        map.put("salaryType", paramsMap.get("salaryType"));
        map.put("projectName", paramsMap.get("projectName"));
        map.put("payType", paramsMap.get("payType"));
        map.put("cardAttribute", paramsMap.get("cardAttribute"));
        map.put("payAccount", paramsMap.get("account"));
        String jsonStr = JSONObject.toJSONString(map);
        String encryptStr = RSA.encryptPub(jsonStr,publicKey);
        JSONObject mapParam = new JSONObject();
        mapParam.put("outMemberNo",merchant_id);
        mapParam.put("signType","RSA");
        mapParam.put("sign",encryptStr);
        String reqInfo = JSONObject.toJSONString(mapParam);
		String respInfo=reqZXFSevice(reqInfo,ZXConfig.PAYMENT_PAY_URL);
		Map<String, Object> respMap = DataConvertUtil.jsonToMap(respInfo);
		return respMap;
	}
	
	/**
	 * 付款结果查询
	 * @throws UnsupportedEncodingException 
	 * @throws Exception 
	 */
	public Map<String, Object> paymentQuery() throws Exception {
        Map<String, Object> map = new HashMap<>();
        //商户号
        map.put("outMemberNo", merchant_id);
        //订单号
        map.put("outerOrderNo", paramsMap.get("orderNo"));
        map.put("service", ZXConfig.PAYMENT_QUERY);
        map.put("version", "1.0");
        map.put("signType", "RSA");
        map.put("charset", charset);
        String jsonStr = JSONObject.toJSONString(map);
        String encryptStr = RSA.encryptPub(jsonStr,publicKey);
        JSONObject mapParam = new JSONObject();
        mapParam.put("outMemberNo", merchant_id);
        mapParam.put("sign", encryptStr);
        String reqInfo = JSONObject.toJSONString(mapParam);
		String respInfo=reqZXFSevice(reqInfo,ZXConfig.PAYMENT_QUERY_URL);
		Map<String, Object> respMap = DataConvertUtil.jsonToMap(respInfo);
		return respMap;
	}
	
	/**
	 * 请求众薪
	 * @param serviceType 
	 * @param service
	 * @return
	 * @throws Exception 
	 * @throws UnsupportedEncodingException 
	 */
	public String reqZXFSevice(String reqInfo, String method) 
			throws Exception{
        logger.info("单笔查询请求参数：{}",reqInfo);
        String resultJsonStr = HttpUtils.doPost(url+method, reqInfo);
        logger.info("单笔请求返回参数：{}",resultJsonStr);
        return resultJsonStr;
	}

}
