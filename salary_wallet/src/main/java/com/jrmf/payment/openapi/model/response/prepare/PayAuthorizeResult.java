package com.jrmf.payment.openapi.model.response.prepare;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class PayAuthorizeResult implements IBizResult{

	private String orderNo;
	private String authorizationNo;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getAuthorizationNo() {
		return authorizationNo;
	}

	public void setAuthorizationNo(String authorizationNo) {
		this.authorizationNo = authorizationNo;
	}

}
