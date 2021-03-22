package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class BindUserWechatAccountServiceAttachment extends ActionAttachment {

	private String redirectUrl;

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
}
