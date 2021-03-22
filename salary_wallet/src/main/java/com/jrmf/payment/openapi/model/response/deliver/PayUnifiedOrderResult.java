package com.jrmf.payment.openapi.model.response.deliver;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class PayUnifiedOrderResult implements IBizResult{

	private String reqNo;
	private String orderNo;
	private String outOrderNo;
	
	public String getReqNo() {
		return reqNo;
	}
	public void setReqNo(String reqNo) {
		this.reqNo = reqNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getOutOrderNo() {
		return outOrderNo;
	}
	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}

	
}
