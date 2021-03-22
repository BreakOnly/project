package com.jrmf.taxsettlement.api;

import java.util.Map;

public class MerchantAPIDockingConfig extends MerchantAPIDockingProfile {

	public static final String ANY_PASS = "*";
	
	private String signVerificationKey;
	
	private String signGenerationKey;

	private String notifyUrl;

	private String accessIPWhiteListMode;

	private Map<String, Integer> accessableApiKeyTable;
	
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

	public Integer checkInterfaceAccessable(String apiKey) {
		return accessableApiKeyTable.get(apiKey);
	}

	public boolean checkIPAccessable(String accessIP) {
		return ANY_PASS.equals(accessIPWhiteListMode)
				|| accessIPWhiteListMode.contains(accessIP);
	}

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

	public Map<String, Integer> getAccessableApiKeyTable() {
		return accessableApiKeyTable;
	}

	public void setAccessableApiKeyTable(Map<String, Integer> accessableApiKeyTable) {
		this.accessableApiKeyTable = accessableApiKeyTable;
	}

}
