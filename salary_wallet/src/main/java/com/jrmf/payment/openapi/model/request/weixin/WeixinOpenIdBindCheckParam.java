package com.jrmf.payment.openapi.model.request.weixin;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.weixin.WeixinOpenIdBindCheckResult;

/**
 * 微信openId绑定检查
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class WeixinOpenIdBindCheckParam implements IBaseParam<WeixinOpenIdBindCheckResult> {
	
	private String platform;
	private String openId;
	private String redirctUri;
	
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getRedirctUri() {
		return redirctUri;
	}
	public void setRedirctUri(String redirctUri) {
		this.redirctUri = redirctUri;
	}
	@Override
	public String requestURI() {
		return "/weixin/openapi/bind_openid";
	}
	
	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}
	
	@Override
	public Class<?> respDataClass() {
		return WeixinOpenIdBindCheckResult.class;
	}
	
}
