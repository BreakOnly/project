package com.jrmf.taxsettlement.api;

import com.alibaba.fastjson.JSON;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import com.jrmf.taxsettlement.api.service.ActionSet;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;

public class CacheAPIDockingManager implements APIDockingManager {

	private static final String MERCHANT_API_DOCKING_CONFIG_CACHE_KEY_PREFIX = "MADC-";

	private static final String MERCHANT_ACCESS_RUNTIME_CACHE_KEY_PREFIX = "MARC-";

	private static final String DEFAULT_SIGN_TYPE = "SHA256";

	@Autowired
	private UtilCacheManager cacheManager;

	@Autowired
	private MerchantAPIDockingConfigDao merchantAPIDockingConfigMapper;

	@Autowired
	private ActionSet<ActionParams, ActionAttachment> actionRouter;

	public CacheAPIDockingManager() {

	}

	/**
	* @Description
	**/
	@PostConstruct
	public void loadAndInit() {
		Map<String, Object> params = new HashMap<String, Object>();
		for (MerchantAPIDockingProfileDO profileDo : merchantAPIDockingConfigMapper
				.listMerchantAPIDockingProfile(params)) {
			loadMerchantAPIDockingConfig(profileDo.getMerchantId());
		}
	}

	@Override
	public void setMerchantAPIDockingMode(String merchantId, APIDockingMode mode) {
		MerchantAPIDockingConfig dockingConfig = getMerchantAPIDockingConfig(merchantId);
		if (dockingConfig.getAPIDockingMode().equals(mode)) {
			throw new APIDockingException(APIDockingRetCodes.SAME_MODE_SET.getCode(), mode.toString());
		}

		Map<String, Object> updateParams = new HashMap<String, Object>();
		updateParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		updateParams.put(APIDockingRepositoryConstants.API_DOCKING_MODE, mode);

		if (APIDockingMode.CLOSED.equals(dockingConfig.getAPIDockingMode())
				&& StringUtil.isEmpty(dockingConfig.getSignGenerationKey())) {
			updateParams.put(APIDockingRepositoryConstants.SIGN_TYPE, DEFAULT_SIGN_TYPE);

			String randomKey = UUID.randomUUID().toString().replaceAll("-", "");

			updateParams.put(APIDockingRepositoryConstants.SIGN_VERIFICATION_KEY, randomKey);
			updateParams.put(APIDockingRepositoryConstants.SIGN_GENERATION_KEY, randomKey);
		}

		updateMerchantAPIDockingConfig(updateParams);
	}

	@Override
	public void setMerchantAccessableAPIKeys(String merchantId, Map<String, Integer> apiKeys) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		params.put(APIDockingRepositoryConstants.ACCESSABLE_API_KEYS, apiKeys);
		merchantAPIDockingConfigMapper.removeMerchantAccessableAPIKeys(params);
		if(!apiKeys.isEmpty()) {
			merchantAPIDockingConfigMapper.addMerchantAccessableAPIKeys(params);
		}
		loadMerchantAPIDockingConfig(merchantId);
	}

	@Override
	public List<MerchantAPIDockingProfile> listMerchantCompanyIdAPIDockingProfile(
			Map<String, Object> params) {

		List<MerchantAPIDockingProfile> profilePageList = new ArrayList<MerchantAPIDockingProfile>();
		for (MerchantAPIDockingProfileDO profileDo : merchantAPIDockingConfigMapper
				.listMerchantCompanyAPIDockingProfile(params)) {
			profilePageList.add(toProfile(profileDo));
		}
		return profilePageList;

	}

	@Override
	public MerchantAPIDockingConfig getMerchantAPIDockingConfig(String merchantId) {
		String cacheKey = getMerchantAPIDockingConfigCacheKey(merchantId);
		String cacheConfigJsonStr = (String) cacheManager.get(cacheKey);
		return (MerchantAPIDockingConfig) JSON.parseObject(cacheConfigJsonStr, MerchantAPIDockingConfig.class);
	}

	private MerchantAPIDockingConfig loadMerchantAPIDockingConfig(String merchantId) {
		String cacheKey = getMerchantAPIDockingConfigCacheKey(merchantId);
		MerchantAPIDockingConfigDO configDo = merchantAPIDockingConfigMapper.getMerchantAPIDockingConfig(merchantId);
		List<MerchantAccessableAPIConfigDO> accessableAPIKeyTable = merchantAPIDockingConfigMapper.listMerchantAccessableAPIKey(merchantId);
		MerchantAPIDockingConfig dockingConfig = toMerchantAPIDockingConfig(configDo, accessableAPIKeyTable);
		cacheManager.put(cacheKey, JSON.toJSONString(dockingConfig), -1);
		return dockingConfig;
	}

	private String getMerchantAPIDockingConfigCacheKey(String merchantId) {
		return new StringBuilder(MERCHANT_API_DOCKING_CONFIG_CACHE_KEY_PREFIX).append(merchantId).toString();
	}

	@Override
	public PageData<MerchantAPIDockingProfile> listMerchantAPIDockingProfile(Map<String, Object> params) {
		PageData<MerchantAPIDockingProfile> pageData = new PageData<MerchantAPIDockingProfile>();
		pageData.setRecordTotalCount(merchantAPIDockingConfigMapper.countMerchantAPIDockingProfile(params));
		List<MerchantAPIDockingProfile> profilePageList = new ArrayList<MerchantAPIDockingProfile>();
		for (MerchantAPIDockingProfileDO profileDo : merchantAPIDockingConfigMapper
				.listMerchantAPIDockingProfile(params)) {
			profilePageList.add(toProfile(profileDo));
		}
		pageData.setPageRecords(profilePageList);
		return pageData;
	}

	private MerchantAPIDockingProfile toProfile(MerchantAPIDockingProfileDO profileDo) {
		MerchantAPIDockingProfile profile = new MerchantAPIDockingProfile();
		profile.setMerchantId(profileDo.getMerchantId());
		profile.setCompanyId(profileDo.getCompanyId());
		profile.setAPIDockingMode(APIDockingMode.codeOf(profileDo.getApiDockingMode()));
		profile.setSignType(profileDo.getSignType());
		profile.setCreateTime(profileDo.getCreateTime());
		profile.setUpdateTime(profileDo.getUpdateTime());
		profile.setCustomName(profileDo.getCustomName());
		return profile;
	}

	private MerchantAPIDockingConfig toMerchantAPIDockingConfig(MerchantAPIDockingConfigDO configDo,
			List<MerchantAccessableAPIConfigDO> accessableAPIKeyTable) {

		MerchantAPIDockingConfig dockingConfig = new MerchantAPIDockingConfig();
		dockingConfig.setMerchantId(configDo.getMerchantId());

		Map<String, Integer> fluxTable = new HashMap<String, Integer>();
		for(MerchantAccessableAPIConfigDO fluxConfigDo : accessableAPIKeyTable) {
			fluxTable.put(fluxConfigDo.getApiKey(), fluxConfigDo.getConcurrentFlux());
		}
		dockingConfig.setAccessableApiKeyTable(fluxTable);

		dockingConfig.setAccessIPWhiteListMode(configDo.getAccessIPWhiteListMode());
		dockingConfig.setAPIDockingMode(APIDockingMode.codeOf(configDo.getApiDockingMode()));
		dockingConfig.setNotifyUrl(configDo.getNotifyUrl());
		dockingConfig.setSignType(configDo.getSignType());
		dockingConfig.setSignGenerationKey(configDo.getSignGenerationKey());
		dockingConfig.setSignVerificationKey(configDo.getSignVerificationKey());
		dockingConfig.setCreateTime(configDo.getCreateTime());
		dockingConfig.setUpdateTime(configDo.getUpdateTime());
		return dockingConfig;
	}

	@Override
	public String resetSignKey(String merchantId, String signType) {

		Map<String, Object> updateParams = new HashMap<String, Object>();
		updateParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		updateParams.put(APIDockingRepositoryConstants.SIGN_TYPE, DEFAULT_SIGN_TYPE);

		String randomKey = UUID.randomUUID().toString().replaceAll("-", "");

		updateParams.put(APIDockingRepositoryConstants.SIGN_VERIFICATION_KEY, randomKey);
		updateParams.put(APIDockingRepositoryConstants.SIGN_GENERATION_KEY, randomKey);

		updateMerchantAPIDockingConfig(updateParams);
		return randomKey;
	}

	@Override
	public void updateMerchantAPIDockingConfig(Map<String, Object> params) {
		APIDockingMode mode = (APIDockingMode) params.get(APIDockingRepositoryConstants.API_DOCKING_MODE);
		if (mode != null) {
			params.put(APIDockingRepositoryConstants.API_DOCKING_MODE, mode.getModeCode());
		}
		merchantAPIDockingConfigMapper.updateMerchantApiDockingConfig(params);
		loadMerchantAPIDockingConfig((String) params.get(APIDockingRepositoryConstants.MERCHANT_ID));
	}

	@Override
	public <AP extends ActionParams, AA extends ActionAttachment> Action<AP, AA> getDockingService(String apiKey) {
		return actionRouter.routeAction(apiKey, null);
	}

	@Override
	public boolean applyFluxFor(String merchantId, String apiKey, int accessableConcurrentCount) {
		String cacheKey = getMerchantAccessRuntimeCacheKey(merchantId);
		long currentAccessingCount = cacheManager.changeMapValueBy(cacheKey, apiKey, 1);
		if(currentAccessingCount > accessableConcurrentCount) {
			cacheManager.changeMapValueBy(cacheKey, apiKey, -1);
			return false;
		}
		return true;
	}

	@Override
	public void releaseFlux(String merchantId, String apiKey) {
		String cacheKey = getMerchantAccessRuntimeCacheKey(merchantId);
		cacheManager.changeMapValueBy(cacheKey, apiKey, -1);
	}

	private String getMerchantAccessRuntimeCacheKey(String merchantId) {
		return new StringBuilder(MERCHANT_ACCESS_RUNTIME_CACHE_KEY_PREFIX).append(merchantId).toString();
	}

	@Override
	public void addMerchantAPIDockingMode(String merchantId) {
		merchantAPIDockingConfigMapper.addMerchantApiDockingConfig(merchantId);
		loadMerchantAPIDockingConfig(merchantId);
	}

	@Override
	public Map<String, String> listAPIService() {
		return actionRouter.listActionNames();
	}

}
