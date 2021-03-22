package com.jrmf.payment.hddpay;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.jrmf.payment.hddpay.entity.TradeItemReq;
import com.jrmf.payment.hddpay.entity.TradeOrderAddReq;
import com.jrmf.payment.hddpay.util.SHA1WithRSA;
import com.jrmf.payment.hddpay.util.TtpayUtils;
import com.jrmf.payment.hddpay.util.WebUtils;
import com.jrmf.payment.ymyf.util.DataConvertUtil;

public class HddService {
	

	private static Logger logger = LoggerFactory.getLogger(HddService.class);

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
	private Map<String, String> paramsMap;

	private String charset ="UTF-8";

	public HddService(String url, String merchant_id, String privateKey,
			String publicKey, String md5Key, Map<String, String> paramsMap,
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

	public Map<String, String> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, String> paramsMap) {
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
		Map<String, String> data = new HashMap<>();
		data.put("app_id", merchant_id);
		data.put("method", HddConfig.PAYMENT_PAY);
		data.put("charset", "UTF-8");
		data.put("sign_type", "RSA");
		data.put("timestamp", String.valueOf(System.currentTimeMillis()));
		data.put("version", "1.0.0");

		TradeOrderAddReq tradeOrderAddReq = new TradeOrderAddReq();
		tradeOrderAddReq.setOutTradeNo(paramsMap.get("orderNo"));
		tradeOrderAddReq.setRemark("资金下发");
		tradeOrderAddReq.setAccountType("WBANK");

		TradeItemReq tradeItemReq = new TradeItemReq();
		tradeItemReq.setAmount(paramsMap.get("amount"));
		tradeItemReq.setCardNo(paramsMap.get("account"));
		tradeItemReq.setIdCard(paramsMap.get("certId"));
		tradeItemReq.setMobile(paramsMap.get("phoneNo"));
		tradeItemReq.setName(paramsMap.get("userName"));
		tradeItemReq.setRemark(paramsMap.get("remark"));
		tradeOrderAddReq.setItems(Arrays.asList(tradeItemReq));
		data.put("params", JSON.toJSONString(tradeOrderAddReq));

		String dataStr = TtpayUtils.createLinkString(TtpayUtils.filter(data));
		String sign = SHA1WithRSA.sign(dataStr, privateKey, charset);
		data.put("sign", sign);
		String respInfo = reqHddFSevice(data);
		Map<String, Object> respMap = DataConvertUtil.jsonToMap(respInfo);
		return respMap;
	}
	
	/**
	 * 付款结果查询
	 * @throws UnsupportedEncodingException 
	 * @throws Exception 
	 */
	public Map<String, Object> paymentQuery() throws Exception {
		Map<String, String> data = new HashMap<>();
		data.put("app_id", merchant_id);
		data.put("method", HddConfig.PAYMENT_QUERY);
		data.put("charset", "UTF-8");
		data.put("sign_type", "RSA");

		data.put("timestamp", String.valueOf(System.currentTimeMillis()));
		data.put("version", "1.0.0");

		Map<String, String> params = new HashMap<>();
		//商户订单号
		params.put("out_trade_no", paramsMap.get("orderNo"));
		data.put("params", JSON.toJSONString(params));
		String dataStr = TtpayUtils.createLinkString(TtpayUtils.filter(data));
		String sign = SHA1WithRSA.sign(dataStr, privateKey, charset);
		data.put("sign", sign);
		String respInfo = reqHddFSevice(data);
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
	public String reqHddFSevice(Map<String, String> params) 
			throws Exception{
        logger.info("单笔查询请求参数：{}",params);
		String rsp = WebUtils.post(url,params);
        logger.info("单笔请求返回参数：{}",rsp);
        return rsp;
	}

}
