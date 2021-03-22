package com.jrmf.taxsettlement.api;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantAPIDockingConfigDao {

	int countMerchantAPIDockingProfile(Map<String, Object> params);

	List<MerchantAPIDockingProfileDO> listMerchantAPIDockingProfile(Map<String, Object> params);

	MerchantAPIDockingConfigDO getMerchantAPIDockingConfig(String merchantId);

	List<MerchantAccessableAPIConfigDO> listMerchantAccessableAPIKey(String merchantId);

	void updateMerchantApiDockingConfig(Map<String, Object> params);

	void addMerchantApiDockingConfig(String merchantId);

	void removeMerchantAccessableAPIKeys(Map<String, Object> params);
	
	void addMerchantAccessableAPIKeys(Map<String, Object> params);

	List<MerchantAPIDockingProfileDO> listMerchantCompanyAPIDockingProfile(Map<String, Object> params);
	
}
