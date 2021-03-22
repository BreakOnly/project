package com.jrmf.payment.openapi.model.response.weixin;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class WeixinOpenIdBindCheckResult implements IBizResult{

	//0：未绑定，1：已绑定
	private String bindStatus;
	private String redirectUrl;
	private String nonce;
	
	
	public String getBindStatus() {
		return bindStatus;
	}
	public void setBindStatus(String bindStatus) {
		this.bindStatus = bindStatus;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
	
	
}
