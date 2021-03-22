package com.jrmf.taxsettlement.api.gateway.restful;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.APIDockingRepositoryConstants;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.contract.BindUserWechatAccountServiceAttachment;
import com.jrmf.taxsettlement.api.service.contract.BindUserWechatAccountServiceParams;
import com.jrmf.taxsettlement.api.service.contract.MerchantUserOpenIdRelationDO;
import com.jrmf.taxsettlement.api.service.contract.MerchantUserOpenIdRelationDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;

@Controller
public class APIUtilController {

	private static final Logger logger = LoggerFactory.getLogger(APIUtilController.class);

	private static final String WECHAT_BIND_API_KEY = "BIND_USER_WECHAT_ACCOUNT";

	private static final int DEFAULT_CACHE_LIFE = 120;

	@Autowired
	private ChannelRelatedDao channelRelatedDao;

	@Autowired
	private MerchantUserOpenIdRelationDao merchantUserOpenIdRelationDao;

	@Autowired
	private UtilCacheManager utilCacheManager;

	@Autowired
	private APIDockingManager apiDockingManager;

	@RequestMapping(value = "/util/wechat/bind.do")
	public void wechatBind(HttpServletRequest request, HttpServletResponse response, String partnerId,
			String merchantId, String openId, String redirectUrl, String serial, String corpId) {

//		System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " access here:" + serial + " corpId:" + corpId);
		
		if (StringUtils.isEmpty(serial)) {
			serial = UUID.randomUUID().toString();
			Map<String, String> cachedMap = new HashMap<String, String>();
			cachedMap.put(APIDefinitionConstants.CFN_MERCHANT_ID, merchantId);
			cachedMap.put(APIDefinitionConstants.CFN_PARTNER_ID, partnerId);
			cachedMap.put(APIDefinitionConstants.CFN_OPEN_ID, openId);
			cachedMap.put(APIDefinitionConstants.CFN_REDIRECT_URL, redirectUrl);
			utilCacheManager.setMap(serial, cachedMap, DEFAULT_CACHE_LIFE);
		} else {
			Map<String, String> cachedMap = utilCacheManager.getMap(serial);
			if (cachedMap == null) {
				logger.error("serial[{}] is expired", serial);
				return;
			}
			merchantId = cachedMap.get(APIDefinitionConstants.CFN_MERCHANT_ID);
			partnerId = cachedMap.get(APIDefinitionConstants.CFN_PARTNER_ID);
			openId = cachedMap.get(APIDefinitionConstants.CFN_OPEN_ID);
			redirectUrl = cachedMap.get(APIDefinitionConstants.CFN_REDIRECT_URL);

			Map<String, String> params = new HashMap<String, String>();
			params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
			params.put(APIDockingRepositoryConstants.USER_OPEN_ID, openId);
			params.put(APIDockingRepositoryConstants.RELATED_CORP_ID, corpId);
			try {
				merchantUserOpenIdRelationDao.addRelation(params);
			} catch (Exception e) {
				logger.error("error occured in add relation", e);
			}
		}

		List<String> wechatSupportedTransferCorps = getRelatedWechatSupportedTransferCorps(merchantId);
		if (wechatSupportedTransferCorps.isEmpty()) {
			try {
				response.getOutputStream().write("no contract with corpration wechat supported".getBytes());
			} catch (IOException e) {
				logger.error("error occured in responsing data", e);
			}
			return;
		}

		List<MerchantUserOpenIdRelationDO> relations = merchantUserOpenIdRelationDao.listRelation(merchantId, openId);
		for (MerchantUserOpenIdRelationDO relation : relations) {
			wechatSupportedTransferCorps.remove(relation.getRelatedCorpId());
		}

		if (wechatSupportedTransferCorps.isEmpty()) {
			try {
				response.sendRedirect(redirectUrl);
			} catch (IOException e) {
				logger.error("error occured in send redirect url", e);
			}
			return;
		}

		String toBindTransferCorpId = wechatSupportedTransferCorps.get(0);
		String redirectToMe = "http://wxtest3.jrmf360.com/util/wechat/bind.do?serial=" + serial + "&corpId="
				+ toBindTransferCorpId;

		Action<BindUserWechatAccountServiceParams, BindUserWechatAccountServiceAttachment> action = apiDockingManager
				.getDockingService(WECHAT_BIND_API_KEY);
		BindUserWechatAccountServiceParams params = new BindUserWechatAccountServiceParams();
		params.setMerchantId(merchantId);
		params.setOpenId(openId);
		params.setPartnerId(partnerId);
		params.setTransferCorpId(toBindTransferCorpId);
		params.setRedirectUrl(redirectToMe);

		ActionResult<BindUserWechatAccountServiceAttachment> result = action.execute(params);
		if (!result.isOk()) {
			logger.error("error occured with ret[{}:{}]", result.getRetCode(), result.getRetMsg());
			return;
		}

		BindUserWechatAccountServiceAttachment attachment = result.getRetData();
		try {
			response.sendRedirect(attachment.getRedirectUrl());
		} catch (IOException e) {
			logger.error("error occured in send redirect url", e);
		}
	}

	private List<String> getRelatedWechatSupportedTransferCorps(String merchantId) {
		List<String> wechatSupportedTransferCorps = new ArrayList<String>();

		for (ChannelRelated channel : channelRelatedDao.getRelatedList(merchantId)) {
			if (StringUtils.isNotEmpty(channel.getAygPlatForm())) {
				wechatSupportedTransferCorps.add(channel.getCompanyId());
			}
		}
		return wechatSupportedTransferCorps;
	}

}
