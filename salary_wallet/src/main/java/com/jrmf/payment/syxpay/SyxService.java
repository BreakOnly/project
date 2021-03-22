package com.jrmf.payment.syxpay;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.demo.tool.DTHttpClient;
import com.dutiantech.demo.tool.DTSignTool;

public class SyxService {
	
	private Logger logger = LoggerFactory.getLogger(SyxService.class); 

	//商户私钥
	private String privateKey;
	//商银信公钥
	private String publicKey;
	//商户代码
	private String merchant_id;
	//请求url
	private String url;
	//请求参数
	private Map<String, String> paramsMap;
	
	public SyxService(String url, String merchant_id, String privateKey,
			String publicKey, Map<String, String> paramsMap) {
		super();
		this.url = url;
		this.merchant_id = merchant_id;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.paramsMap = paramsMap;
	}
	
	/**
	 * 单笔代付
	 * @return
	 */
	public Map<String, String> singleTransFer(){
		//发起请求
		Map<String, String> respMap = reqSyxSevice(SyxConfig.SINGLE_TRANSFER_METHOD);
		return respMap;
	}
	
	/**
	 * 单笔代付查询
	 */
	public Map<String, String> singleTransFerQuery(){
		//发起请求
		Map<String, String> respMap = reqSyxSevice(SyxConfig.SINGLE_TRANSFER_QUERY_METHOD);
		return respMap;
	}
	
	
	/**
	 * 请求商银信
	 * @param service
	 * @return
	 */
	public Map<String, String> reqSyxSevice(String service){
		Map<String, String>	respMap = new HashMap<String, String>();
		try {
			// 1. 参与签名参数，按参数名进行ASCII排序
			String oriData = DTSignTool.toRequestString(paramsMap, "UTF-8", false, true);
			logger.info("待签名原文：" + oriData);
			// 2. RSA2签名，返回base64字符
			String sign = DTSignTool.sign("RSA2", oriData, privateKey, "UTF-8");
			logger.info("签名值：" + sign);
			paramsMap.put("sign", sign);
			logger.info("请求地址为："+url);
			logger.info("请求信息："+JSONObject.toJSONString(paramsMap));
			// 3. 发起请求，15秒连接超时，45秒响应超时
			// 请求开始时间
			long reqStartTime = System.currentTimeMillis();
			String result = DTHttpClient.sendPost(url, paramsMap, "UTF-8", 15000, 45000);
			// 请求结束时间
			long reqEndTime = System.currentTimeMillis();
			//此次请求共用多长时间
			long time = reqEndTime-reqStartTime;
			logger.info("此次连接共用时为：【"+time+"】毫秒");
			logger.info("响应信息为：");
			logger.info(result);
			respMap = JSONObject.parseObject(result,Map.class);
		} catch (Exception e) {
			logger.error("请求商银信异常",e);
		}
		return respMap;
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
	public Map<String, String> getParamsMap() {
		return paramsMap;
	}
	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}
	
}
