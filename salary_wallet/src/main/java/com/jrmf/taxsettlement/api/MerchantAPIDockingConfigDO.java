package com.jrmf.taxsettlement.api;

public class MerchantAPIDockingConfigDO extends MerchantAPIDockingProfileDO {

	private String signVerificationKey;
	
	private String signGenerationKey;

	private String notifyUrl;

	private String accessIPWhiteListMode;

	public String getSignVerificationKey() {
		return signVerificationKey;
	}

	public void setSignVerificationKey(String signVerificationKey) {
		this.signVerificationKey = signVerificationKey;
	}

	public String getSignGenerationKey() {
		return signGenerationKey;
	}

	public void setSignGenerationKey(String signGenerationKey) {
		this.signGenerationKey = signGenerationKey;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getAccessIPWhiteListMode() {
		return accessIPWhiteListMode;
	}

	public void setAccessIPWhiteListMode(String accessIPWhiteListMode) {
		this.accessIPWhiteListMode = accessIPWhiteListMode;
	}
}
