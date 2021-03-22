package com.jrmf.taxsettlement.api.service.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.controller.constant.WechatInfo;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.weixin.WeixinOpenIdBindCheckParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.weixin.WeixinOpenIdBindCheckResult;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;

@ActionConfig(name = "绑定用户微信账户")
public class BindUserWechatAccountService
		implements Action<BindUserWechatAccountServiceParams, BindUserWechatAccountServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(BindUserWechatAccountService.class);

	@Autowired
	private WechatInfo wechatInfo;

	@Autowired
	private ChannelRelatedDao channelRelatedDao;

	@Override
	public String getActionType() {
		return APIDefinition.BIND_USER_WECHAT_ACCOUNT.name();
	}

	@Override
	public ActionResult<BindUserWechatAccountServiceAttachment> execute(
			BindUserWechatAccountServiceParams actionParams) {

		ChannelRelated channelConfig = channelRelatedDao.getRelatedByCompAndOrig(actionParams.getMerchantId(),
				actionParams.getTransferCorpId());

		OpenApiClient client = new OpenApiClient.Builder().appId(channelConfig.getAppIdAyg())
				.privateKey(wechatInfo.getAygPrivateKey()).build();

		WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
		param.setOpenId(actionParams.getOpenId());
		param.setPlatform(channelConfig.getAygPlatForm());
		param.setRedirctUri(actionParams.getRedirectUrl());

		OpenApiBaseResponse<WeixinOpenIdBindCheckResult> response = client.execute(param);
		if (response.successed()) {
			BindUserWechatAccountServiceAttachment attachment = new BindUserWechatAccountServiceAttachment();
			attachment.setRedirectUrl(response.getData().getRedirectUrl());
			return new ActionResult<BindUserWechatAccountServiceAttachment>(attachment);
		}

		throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(),
				new StringBuilder(response.getCode()).append(":").append(response.getMsg()).toString());
	}

}
