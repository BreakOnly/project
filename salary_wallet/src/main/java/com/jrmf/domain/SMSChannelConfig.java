package com.jrmf.domain;

public class SMSChannelConfig {
	
	//配置id
	private Integer id;
	 
	//短信通道商编
	private String merchantId;
	
	//应用id
	private String appid;
	
	//短信请求url
	private String url;
	
	//短信签名key
	private String signKey;
	
	//短信签名类型
	private String signType;
	
	//扩展参数1
	private String extraParam;
	
	//扩展参数2
	private String extraParamTwo;

	//扩展参数3
	private String extraParamThree;
	
	//所属通道方式
	private Integer channelType;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getExtraParam() {
		return extraParam;
	}

	public void setExtraParam(String extraParam) {
		this.extraParam = extraParam;
	}

	public String getExtraParamTwo() {
		return extraParamTwo;
	}

	public void setExtraParamTwo(String extraParamTwo) {
		this.extraParamTwo = extraParamTwo;
	}

	public String getExtraParamThree() {
		return extraParamThree;
	}

	public void setExtraParamThree(String extraParamThree) {
		this.extraParamThree = extraParamThree;
	}

	public Integer getChannelType() {
		return channelType;
	}

	public void setChannelType(Integer channelType) {
		this.channelType = channelType;
	}
	
	
}
