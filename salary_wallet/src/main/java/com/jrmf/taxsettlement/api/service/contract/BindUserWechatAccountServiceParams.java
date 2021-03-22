package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class BindUserWechatAccountServiceParams extends ActionParams {

	private String transferCorpId;
	
	private String openId; 
	
	private String redirectUrl;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getTransferCorpId() {
		return transferCorpId;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}
	
}
