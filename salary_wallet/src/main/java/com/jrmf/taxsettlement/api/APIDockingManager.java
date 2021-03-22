package com.jrmf.taxsettlement.api;

import java.util.List;
import java.util.Map;

import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;

public interface APIDockingManager {

	Map<String, String> listAPIService();
	
	void addMerchantAPIDockingMode(String merchantId);
	
	void setMerchantAPIDockingMode(String merchantId, APIDockingMode mode);

	MerchantAPIDockingConfig getMerchantAPIDockingConfig(String merchantId);

	PageData<MerchantAPIDockingProfile> listMerchantAPIDockingProfile(Map<String, Object> params);

	void updateMerchantAPIDockingConfig(Map<String, Object> params);

	<AP extends ActionParams, AA extends ActionAttachment> Action<AP, AA> getDockingService(String apiKey);

	String resetSignKey(String merchantId, String signType);

	boolean applyFluxFor(String merchantId, String apiKey, int accessableConcurrentCount);

	void releaseFlux(String merchantId, String apiKey);

	void setMerchantAccessableAPIKeys(String merchantId, Map<String, Integer> apiKeys);

	List<MerchantAPIDockingProfile> listMerchantCompanyIdAPIDockingProfile(Map<String, Object> params);

}
