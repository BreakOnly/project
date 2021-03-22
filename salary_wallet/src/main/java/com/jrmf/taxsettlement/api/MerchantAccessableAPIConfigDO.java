package com.jrmf.taxsettlement.api;

public class MerchantAccessableAPIConfigDO {

	private String merchantId;
	
	private String apiKey;

	private int concurrentFlux;

	private String updateTime;

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public int getConcurrentFlux() {
		return concurrentFlux;
	}

	public void setConcurrentFlux(int concurrentFlux) {
		this.concurrentFlux = concurrentFlux;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

}
