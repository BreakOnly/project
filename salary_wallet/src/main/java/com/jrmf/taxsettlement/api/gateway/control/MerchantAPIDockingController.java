package com.jrmf.taxsettlement.api.gateway.control;

import com.alibaba.fastjson.JSON;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OemConfig;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OemConfigService;
import com.jrmf.taxsettlement.api.*;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.taxsettlement.util.code.VerifyCodeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/merchant/apidocking")
public class MerchantAPIDockingController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(MerchantAPIDockingController.class);

	private static final String RET_STATE_KEY = "state";

	private static final String RET_CONFIG_DETAIL = "detail";

	private static final int SUCCESS_RET_CODE = 1;

	private static final String PAGE_DATA = "pageData";

	private static final String CACHE_SMSCODE_SHOW_KEY_PREFIX = "_SHOW_KEY";

	private static final String CACHE_SMSCODE_RESET_KEY_PREFIX = "_RESET_KEY";

	private static final String VERIFY_CODE_CHARS = "1234567890";

	private static final String DEFAULT_PARTNER_ID = "JRMF";

	private static final String DEFAULT_SIGN_TYPE = "SHA256";

	private static final int DEFAULT_PAGE_SIZE = 10;

	@Autowired
	private UtilCacheManager cacheManager;

	@Autowired
	private APIDockingManager apiDockingManager;

	@Autowired
	private MerchantAPITransferRecordDao transferRecordDao;

	@Autowired
	private ChannelCustomService channelCustomService;
	@Autowired
	private OemConfigService oemConfigService;

	@RequestMapping(value = "/listmerchantapirequest.do")
	@ResponseBody
	public Map<String, Object> listMerchantAPIRequest(HttpServletRequest request, String merchantId, String batchNo,
			String requestNo, String dealNo, String accountDate) {

		Map<String, Object> params = new HashMap<>(16);
		params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		params.put(APIDockingRepositoryConstants.BATCH_NO, batchNo);
		params.put(APIDockingRepositoryConstants.REQUEST_NO, requestNo);
		params.put(APIDockingRepositoryConstants.DEAL_NO, dealNo);
		params.put(APIDockingRepositoryConstants.ACCOUNT_DATE, accountDate);

		PageData<APITransferRecordDO> pageData = new PageData<APITransferRecordDO>();
		pageData.setRecordTotalCount(transferRecordDao.countDealRecord(params));

		int pageNo = Integer.parseInt(request.getParameter("pageNo"));
		params.put(APIDockingRepositoryConstants.RECORD_INDEX, DEFAULT_PAGE_SIZE * (pageNo - 1));
		params.put(APIDockingRepositoryConstants.PAGE_SIZE, DEFAULT_PAGE_SIZE);
		pageData.setPageRecords(transferRecordDao.listDealRecord(params));

		Map<String, Object> retMap = new HashMap<>(4);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		retMap.put(PAGE_DATA, pageData);
		return retMap;
	}

	@RequestMapping(value = "/setmerchantaccessableapikey.do")
	@ResponseBody
	public Map<String, Object> setMerchantAccessableAPIKey(String merchantId, String apiKeys) {

		Map<String, Integer> apiKeyMap = JSON.parseObject(apiKeys, HashMap.class);
		apiDockingManager.setMerchantAccessableAPIKeys(merchantId, apiKeyMap);
		Map<String, Object> retMap = new HashMap<>(2);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/getmerchantdockingapikeyset.do")
	@ResponseBody
	public Map<String, Object> getMerchantDockingAPIKeySet() {
		Map<String, Object> retMap = new HashMap<>(4);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		retMap.put(RET_CONFIG_DETAIL, apiDockingManager.listAPIService());
		return retMap;
	}

	@RequestMapping(value = "/changemerchantapidockingmode.do")
	@ResponseBody
	public Map<String, Object> changeMerchantAPIDockingMode(HttpServletRequest request, String merchantId) {
		apiDockingManager.setMerchantAPIDockingMode(merchantId,
				APIDockingMode.valueOf(request.getParameter("apiDockingMode")));

		Map<String, Object> retMap = new HashMap<>(2);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/listmerchantapidocking.do")
	@ResponseBody
	public Map<String, Object> listMerchantAPIDockingConfig(HttpServletRequest request) {
		Map<String, Object> params = new HashMap<String, Object>();

		int pageNo = Integer.parseInt(request.getParameter("pageNo"));
		String merchantId = request.getParameter(APIDockingRepositoryConstants.MERCHANT_ID);
		if (StringUtils.isNotEmpty(merchantId)) {
			params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		}

		String customName = request.getParameter(APIDockingRepositoryConstants.CUSTOM_NAME);
		if (StringUtils.isNotEmpty(customName)) {
			params.put(APIDockingRepositoryConstants.CUSTOM_NAME, customName);
		}

		String modeOption = request.getParameter(APIDockingRepositoryConstants.API_DOCKING_MODE);
		int modeCode = StringUtils.isEmpty(modeOption) ? -1 : Integer.parseInt(modeOption);
		if (modeCode != -1) {
			params.put(APIDockingRepositoryConstants.API_DOCKING_MODE, modeCode);
		}

		params.put(APIDockingRepositoryConstants.RECORD_INDEX, DEFAULT_PAGE_SIZE * (pageNo - 1));
		params.put(APIDockingRepositoryConstants.PAGE_SIZE, DEFAULT_PAGE_SIZE);
		PageData<MerchantAPIDockingProfile> pageData = apiDockingManager.listMerchantAPIDockingProfile(params);
		Map<String, Object> retMap = new HashMap<>(4);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		retMap.put(PAGE_DATA, pageData);
		return retMap;
	}

	@RequestMapping(value = "/getmerchantapidockingconfigdetail.do")
	@ResponseBody
	public Map<String, Object> getMerchantAPIDockingConfigDetail(HttpServletRequest request, Integer type) {
		String merchantId = request.getParameter("merchantId");
		MerchantAPIDockingConfig config = apiDockingManager.getMerchantAPIDockingConfig(merchantId);

        Map<String, Integer> accessableApiKeyTable = config.getAccessableApiKeyTable();
        if (type != null) {
            accessableApiKeyTable.clear();
            if (type == 1) {
                accessableApiKeyTable.put(APIDefinition.GET_MONTH_REPORT_FILE_URL.name(), 3);
                accessableApiKeyTable.put(APIDefinition.TRANSFER_TO_ALIPAY_ACCOUNT.name(), 2);
                accessableApiKeyTable.put(APIDefinition.TRANSFER_TO_BANK_CARD.name(), 5);
                accessableApiKeyTable.put(APIDefinition.ADD_MERCHANT_TRANSFER_RECEIVER.name(), 4);
                accessableApiKeyTable.put(APIDefinition.GET_DAY_SERIAL_FILE_URL.name(), 3);
                accessableApiKeyTable.put(APIDefinition.SIGN_AGREEMENT.name(), 5);
                accessableApiKeyTable.put(APIDefinition.QUERY_TRANSFER_DEAL.name(), 3);
                accessableApiKeyTable.put(APIDefinition.VERIFY_3_FACTORS.name(), 1);
                accessableApiKeyTable.put(APIDefinition.QUERY_MERCHANT_BALANCE.name(), 2);
                accessableApiKeyTable.put(APIDefinition.RECHARGE_QUERY_ACCOUNT.name(), 2);
                accessableApiKeyTable.put(APIDefinition.QUERY_MERCHANT_USER_CONTRACT.name(), 2);

				accessableApiKeyTable.put(APIDefinition.ADD_INVOICE_RECEIVER.name(), 2);
				accessableApiKeyTable.put(APIDefinition.QUERY_INVOICE_HISTORY.name(), 2);
				accessableApiKeyTable.put(APIDefinition.ADD_MERCHANT_INVOICE_INFO.name(), 2);
				accessableApiKeyTable.put(APIDefinition.QUERY_MERCHANT_INVOICE_INFO.name(), 2);
				accessableApiKeyTable.put(APIDefinition.APPLY_INVOICE.name(), 2);
				accessableApiKeyTable.put(APIDefinition.QUERY_INVOICE_RECEIVER.name(), 2);
				accessableApiKeyTable.put(APIDefinition.MERCHANT_RECHARGE.name(), 2);
				accessableApiKeyTable.put(APIDefinition.QUERY_RECHARGE_RECORD.name(), 2);
				accessableApiKeyTable.put(APIDefinition.QUERY_ORDER_QUOTA.name(), 2);
            } else if (type == 2) {
                accessableApiKeyTable.put(APIDefinition.PREPARE_UNIFIED_ORDER.name(), 2);
                accessableApiKeyTable.put(APIDefinition.SYNC_PREPARE_UNIFIED_ORDER.name(), 2);
            }
        }

        config.setAccessableApiKeyTable(accessableApiKeyTable);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		retMap.put(RET_CONFIG_DETAIL, config);
		return retMap;
	}

	@RequestMapping(value = "/getmerchantapidockingconfig.do")
	@ResponseBody
	public Map<String, Object> getMerchantAPIDockingConfig(HttpServletRequest request) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		MerchantAPIDockingConfig config = apiDockingManager.getMerchantAPIDockingConfig(merchantId);
		Map<String, Object> retMap = getRetDataOfAPIDockingConfig(merchantId, config);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	private Map<String, Object> getRetDataOfAPIDockingConfig(String merchantId, MerchantAPIDockingConfig config) {
		Map<String, Object> retMap = new HashMap<>(16);
		retMap.put(MerchantAPIDockingDefinitionConstants.MERCHANT_ID, merchantId);
		retMap.put(MerchantAPIDockingDefinitionConstants.PARTNER_ID, DEFAULT_PARTNER_ID);
		retMap.put(MerchantAPIDockingDefinitionConstants.API_DOCKING_MODE, config.getAPIDockingMode().getModeCode());
		retMap.put(MerchantAPIDockingDefinitionConstants.SIGN_TYPE, config.getSignType());
		retMap.put(MerchantAPIDockingDefinitionConstants.NOTIFY_URL, config.getNotifyUrl());
		retMap.put(MerchantAPIDockingDefinitionConstants.ACCESS_IP_WHITE_LIST_MODE, config.getAccessIPWhiteListMode());
		retMap.put(MerchantAPIDockingDefinitionConstants.ACCESSABLE_API_KEY_TABLE, config.getAccessableApiKeyTable());

		if (DEFAULT_SIGN_TYPE.equals(config.getSignType())) {
			String cutKeyForShow = mark(config.getSignGenerationKey());
			retMap.put(MerchantAPIDockingDefinitionConstants.SIGN_GENERATION_KEY, cutKeyForShow);
			retMap.put(MerchantAPIDockingDefinitionConstants.SIGN_VERIFICATION_KEY, cutKeyForShow);
		}
		return retMap;
	}

	private String mark(String toMarkStr) {

		if (toMarkStr == null) {
            return "";
        }

		int strLen = toMarkStr.length();
		int markLen = strLen / 3;

		StringBuilder str = new StringBuilder();
		char[] cArray = toMarkStr.toCharArray();
		for (int i = 0; i < strLen; i++) {
			if (i < markLen || i > markLen * 2) {
                str.append(cArray[i]);
            } else {
                str.append("*");
            }
		}

		return str.toString();
	}

	@RequestMapping(value = "/startmerchantapidocking.do")
	@ResponseBody
	public Map<String, Object> startMerchantAPIDocking(HttpServletRequest request) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		apiDockingManager.setMerchantAPIDockingMode(merchantId, APIDockingMode.PRODUCTION);
		Map<String, Object> retMap = getRetDataOfAPIDockingConfig(merchantId,
				apiDockingManager.getMerchantAPIDockingConfig(merchantId));
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/stopmerchantapidocking.do")
	@ResponseBody
	public Map<String, Object> stopMerchantAPIDocking(HttpServletRequest request) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		apiDockingManager.setMerchantAPIDockingMode(merchantId, APIDockingMode.CLOSED);
		Map<String, Object> retMap = getRetDataOfAPIDockingConfig(merchantId,
				apiDockingManager.getMerchantAPIDockingConfig(merchantId));
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/sendsmsforshowfullkey.do")
	@ResponseBody
	public Map<String, Object> sendSMSForShowFullKey(HttpServletRequest request) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		String verifyCode = VerifyCodeUtil.generateVerifyCode(6, VERIFY_CODE_CHARS);
		cacheManager.put(new StringBuilder(merchantId).append(CACHE_SMSCODE_SHOW_KEY_PREFIX).toString(), verifyCode,
				60);

		ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(merchantId);
		String phoneNo = channelCustom.getPhoneNo();

		logger.debug("check code for show full key:{}", verifyCode);
        Map<String, Object> map = new HashMap<>(4);
        map.put("portalDomain",request.getServerName());
        OemConfig oemConfig = oemConfigService.getOemByParam(map);
        String smsSignature = oemConfig.getSmsSignature();
		final String content = "【"+smsSignature+"】验证码 " + verifyCode + "，您可输入该验证码以显示完整对接签名秘钥，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务。";
		final String templateParam = "{\"code\":\"" + verifyCode + "\"}";
		sendContent(new String[] { phoneNo }, content,smsSignature, SmsTemplateCodeEnum.SHOW_FULL_KEY.getCode(),templateParam);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(MerchantAPIDockingDefinitionConstants.RELATIVE_MOBILE, mark(phoneNo));
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/showfullsigngenerationkey.do")
	@ResponseBody
	public Map<String, Object> showFullSignGenerationKey(HttpServletRequest request, String smsCode) {

		String merchantId = (String) request.getSession().getAttribute("customkey");
		String cacheKey = new StringBuilder(merchantId).append(CACHE_SMSCODE_SHOW_KEY_PREFIX).toString();
		String verifyCode = (String) cacheManager.get(cacheKey);
		if (verifyCode == null || "".equals(verifyCode)) {
			throw new APIDockingManagementException(APIDockingRetCodes.VERIFY_CODE_NOT_EFFECTIVE.getCode(), cacheKey);
		} else if (!verifyCode.equals(smsCode)) {
			throw new APIDockingManagementException(APIDockingRetCodes.VERIFY_CODE_NOT_MATCH.getCode(), cacheKey);
		}

		MerchantAPIDockingConfig config = apiDockingManager.getMerchantAPIDockingConfig(merchantId);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(MerchantAPIDockingDefinitionConstants.SIGN_GENERATION_KEY, config.getSignGenerationKey());
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/sendsmsforresetkey.do")
	@ResponseBody
	public Map<String, Object> sendSMSForResetKey(HttpServletRequest request) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		String verifyCode = VerifyCodeUtil.generateVerifyCode(6, VERIFY_CODE_CHARS);
		cacheManager.put(merchantId + CACHE_SMSCODE_RESET_KEY_PREFIX, verifyCode,
				60);
		logger.debug("check code for reset key:{}", verifyCode);
		ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(merchantId);
		String phoneNo = channelCustom.getPhoneNo();

        Map<String, Object> map = new HashMap<>(4);
        map.put("portalDomain",request.getServerName());
        OemConfig oemConfig = oemConfigService.getOemByParam(map);
        String smsSignature = oemConfig.getSmsSignature();
		final String content = "【"+smsSignature+"】验证码 " + verifyCode + "，您可输入该验证码以重置对接签名秘钥，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务。";
		final String templateParam = "{\"code\":\"" + verifyCode + "\"}";
        sendContent(new String[]{phoneNo}, content,smsSignature,SmsTemplateCodeEnum.RESET_KEY.getCode(),templateParam);
		Map<String, Object> retMap = new HashMap<>(4);
		retMap.put(MerchantAPIDockingDefinitionConstants.RELATIVE_MOBILE, mark(phoneNo));
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/resetsigngenerationkey.do")
	@ResponseBody
	public Map<String, Object> resetSignGenerationKey(HttpServletRequest request, String smsCode, String signType) {

		String merchantId = (String) request.getSession().getAttribute("customkey");
		String cacheKey = merchantId + CACHE_SMSCODE_RESET_KEY_PREFIX;
		String verifyCode = (String) cacheManager.get(cacheKey);
		if (verifyCode == null || "".equals(verifyCode)) {
			throw new APIDockingManagementException(APIDockingRetCodes.VERIFY_CODE_NOT_EFFECTIVE.getCode(), cacheKey);
		} else if (!verifyCode.equals(smsCode)) {
			throw new APIDockingManagementException(APIDockingRetCodes.VERIFY_CODE_NOT_MATCH.getCode(), cacheKey);
		}

		String newGenerationKey = apiDockingManager.resetSignKey(merchantId, signType);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		retMap.put(MerchantAPIDockingDefinitionConstants.SIGN_GENERATION_KEY, newGenerationKey);
		return retMap;
	}

	@RequestMapping(value = "/updateaccessipwhiteListmode.do")
	@ResponseBody
	public Map<String, Object> updateAccessIPWhiteListMode(HttpServletRequest request, String accessIPWhiteListMode) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		Map<String, Object> updateMap = new HashMap<>(4);
		updateMap.put(MerchantAPIDockingDefinitionConstants.MERCHANT_ID, merchantId);
		updateMap.put(MerchantAPIDockingDefinitionConstants.ACCESS_IP_WHITE_LIST_MODE, accessIPWhiteListMode);
		apiDockingManager.updateMerchantAPIDockingConfig(updateMap);
		Map<String, Object> retMap = new HashMap<>(2);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

	@RequestMapping(value = "/updatenotifyurl.do")
	@ResponseBody
	public Map<String, Object> updateNotifyUrl(HttpServletRequest request, String notifyUrl) {
		String merchantId = (String) request.getSession().getAttribute("customkey");
		Map<String, Object> updateMap = new HashMap<>(4);
		updateMap.put(MerchantAPIDockingDefinitionConstants.MERCHANT_ID, merchantId);
		updateMap.put(MerchantAPIDockingDefinitionConstants.NOTIFY_URL, notifyUrl);
		apiDockingManager.updateMerchantAPIDockingConfig(updateMap);
		Map<String, Object> retMap = new HashMap<>(2);
		retMap.put(RET_STATE_KEY, SUCCESS_RET_CODE);
		return retMap;
	}

}
