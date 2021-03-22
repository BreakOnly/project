package com.jrmf.taxsettlement.api.gateway;

public class APIDockingAccesserProfile {

	private String merchantId;
	
	private String partnerId;
	
	private String apiKey;
	
	private String accesserIP;

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getAccesserIP() {
		return accesserIP;
	}

	public void setAccesserIP(String accesserIP) {
		this.accesserIP = accesserIP;
	}
}
