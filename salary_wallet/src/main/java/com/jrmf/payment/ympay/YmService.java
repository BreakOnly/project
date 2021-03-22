package com.jrmf.payment.ympay;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;

public class YmService {

	private Logger logger = LoggerFactory.getLogger(YmService.class); 

	//商户私钥
	private String privateKey;
	//溢美公钥
	private String publicKey;
	//商户代码
	private String merchant_id;
	//请求url
	private String url;
	//请求参数
	private Map<String, Object> paramsMap;

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

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public YmService() {
		super();
	}

	public YmService(String url, String merchant_id, String privateKey,
			String publicKey, Map<String, Object> paramsMap) {
		super();
		this.url = url;
		this.merchant_id = merchant_id;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.paramsMap = paramsMap;
	}

	/**
	 * 会员注册
	 */
	public Map<String, Object> userRegister(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.MERCHANT_REGISTER_METHOD);
		return respMap;
	}

	/**
	 * 会员查询
	 */
	public Map<String, Object> userQuery(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.MERCHANT_QUERY_METHOD);
		return respMap;
	}

	/**
	 * 会员修改
	 */
	public Map<String, Object> userUpdate(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.MERCHANT_UPDATE_METHOD);
		return respMap;
	}

	/**
	 * 银行卡账号绑定
	 */
	public Map<String, Object> bindBankAcc(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.ACCOUNT_BIND_METHOD);
		return respMap;
	}

	/**
	 * 银行卡账号绑定查询
	 */
	public Map<String, Object> bindBankAccQuery(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.BANK_BIND_QUERY_METHOD);
		return respMap;
	}

	/**
	 * 银行卡账号解除
	 */
	public Map<String, Object> bankAccountRemove(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.BANK_ACC_UNBIND_METHOD);
		return respMap;
	}

	/**
	 * 账号设置(页面form表单请求,后端只封装请求参数)
	 */
	public Map<String, Object> userSet(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.USER_SET_METHOD);
		return respMap;
	}

	/**
	 * 账号授权(页面form表单请求,后端只封装请求参数)
	 */
	public Map<String, Object> userAuth(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.USER_BIND_METHOD);
		return respMap;
	}

	/**
	 * 单笔汇款
	 */
	public Map<String, Object> singleTransFer(){
		//发起请求
		Map<String, Object> respMap = sendInterfaseReq(YmConfig.SINGLE_TRANSFER_METHOD);
		return respMap;
	}

	/**
	 * 单笔汇款查询
	 */
	public Map<String, Object> transFerQuery(){
		//发起请求
		Map<String, Object> respMap = sendInterfaseReq(YmConfig.SINGLE_TRANSFER_QUERY_METHOD);
		return respMap;
	}

	/**
	 * 单笔汇款查询
	 */
	public Map<String, Object> transFerDetailQuery(){
		//发起请求
		Map<String, Object> respMap = senNoInterfaseReq(YmConfig.TRANSFER_QUERY_DETAIL_METHOD);
		return respMap;
	}

	/**
	 * 发起企业信息请求
	 * @param url
	 * @param reqParams
	 * @return
	 */
	public Map<String, Object> senNoInterfaseReq(Integer type){
		Map<String, Object>	respMap = new HashMap<String, Object>();
		try{
			//1.交易数据转化成xml数据格式
			String tranData = XmlUtils.toXml(this.paramsMap);
			//2.获取签名
			String sign = Sign.signByPriKey(tranData,this.privateKey);
			//3.交易数据base64编码
			String tranDataTobase64 = Base64.encodeGbk(tranData);
			//4.封装最终请求参数
			Map<String, String> reqMap = new HashMap<String, String>();
			reqMap.put("merchantId", this.merchant_id);
			reqMap.put("tranData", tranDataTobase64);
			reqMap.put("merSignMsg", sign);
			String jsonReqStr = JSONObject.toJSONString(reqMap);
			logger.info("senNoInterfaseReq:请求地址为："+url);
			logger.info("senNoInterfaseReq:tranData明文信息为："+tranData);
			logger.info("senNoInterfaseReq:请求数据为："+jsonReqStr);
			try {
				if(type!=YmConfig.USER_SET_METHOD&&type!=YmConfig.USER_BIND_METHOD){
					//请求开始时间
					long reqStartTime = System.currentTimeMillis();
					String respInfo = HttpClientUtil.doPost(url, reqMap);
					//请求结束时间
					long reqEndTime = System.currentTimeMillis();
					//此次请求共用多长时间
					long time = reqEndTime-reqStartTime;
					logger.info("senNoInterfaseReq:此次连接共用时为：【"+time+"】毫秒");
					logger.info("senNoInterfaseReq:响应信息为："+respInfo);
					if(respInfo!=null &&!respInfo.equals("")){
						if(type==YmConfig.TRANSFER_QUERY_DETAIL_METHOD){
							respMap = JSONObject.parseObject(respInfo,Map.class);
						}else{
							String decodeRespInfo = Base64.decodeGbk(respInfo);
							logger.info("senNoInterfaseReq:base64解密后为："+decodeRespInfo);
							respMap = XmlUtils.xml2map(decodeRespInfo,false);
						}
					}else{
						logger.info("senNoInterfaseReq:响应信息为空");
					}
				}else{
					respMap.put("respInfo", reqMap);
				}
			} catch (Exception e) {
				logger.error("senNoInterfaseReq:请求异常",e);
			}
		}catch(Exception e){
			logger.error("senNoInterfaseReq:出现异常",e);
		}
		return respMap;
	}

	/**
	 * 发起支付请求
	 * @param url
	 * @param reqParams
	 * @return
	 */
	public Map<String, Object> sendInterfaseReq(Integer type){
		Map<String, Object>	respMap = new HashMap<String, Object>();
		try{
			//1.交易数据转化成xml数据格式
			String tranData = XmlUtils.toXml(this.paramsMap);
			//2.获取签名
			String sign = Sign.signByPriKey(tranData,this.privateKey);
			//3.交易数据base64编码
			String tranDataTobase64 = Base64.encodeGbk(tranData);
			//4.封装最终请求参数
			Map<String, String> reqMap = new HashMap<String, String>();
			reqMap.put("merchantId", this.merchant_id);
			reqMap.put("tranData", tranDataTobase64);
			reqMap.put("merSignMsg", sign);
			reqMap.put("interfaceName", getInterfaceName(type));
			reqMap.put("version", "B2C1.0");
			String jsonReqStr = JSONObject.toJSONString(reqMap);
			logger.info("sendInterfaseReq:请求地址为："+url);
			logger.info("sendInterfaseReq:tranData明文信息为："+tranData);
			logger.info("sendInterfaseReq:请求数据为："+jsonReqStr);
			try {
				//请求开始时间
				long reqStartTime = System.currentTimeMillis();
				String respInfo = HttpClientUtil.doPost(url, reqMap);
				//请求结束时间
				long reqEndTime = System.currentTimeMillis();
				//此次请求共用多长时间
				long time = reqEndTime-reqStartTime;
				logger.info("sendInterfaseReq:此次连接共用时为：【"+time+"】毫秒");
				logger.info("sendInterfaseReq:响应信息为："+respInfo);
				if(respInfo!=null &&!respInfo.equals("")){
					if(type==YmConfig.SINGLE_TRANSFER_METHOD){
						//单笔汇款
						respMap = JSONObject.parseObject(respInfo,Map.class);
					}else if(type==YmConfig.SINGLE_TRANSFER_QUERY_METHOD){
						//单笔汇款查询
						respInfo = Base64.decodeGbk(respInfo);
						logger.info("sendInterfaseReq:解码后信息为："+respInfo);
						respMap = XmlUtils.xml2map(respInfo, false);
						logger.info("sendInterfaseReq:xml转成map为："+respMap);
					}else{
						respMap = JSONObject.parseObject(respInfo,Map.class);
					}
				}else{
					logger.info("sendInterfaseReq:响应信息为空");
				}
			} catch (Exception e) {
				logger.error("sendInterfaseReq:请求异常",e);
			}
		}catch(Exception e){
			logger.error("sendInterfaseReq:出现异常",e);
		}
		return respMap;
	} 

	/**
	 * 返回接口名称
	 * @param type
	 * @return
	 */
	public String getInterfaceName(Integer type){
		String interfaceName="";
		switch (type) {
		case 9:
			interfaceName="danbihuikuan";
			break;
		default:
			interfaceName="QueryOrder";
			break;
		}
		return interfaceName;
	}
}
