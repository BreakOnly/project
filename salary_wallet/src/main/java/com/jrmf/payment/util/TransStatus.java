package com.jrmf.payment.util;

public class TransStatus {

	private String orderNo;
	private String resultCode;
	private String resultMsg;
	
	public TransStatus(String orderNo, String resultCode, String resultMsg) {
		super();
		this.orderNo = orderNo;
		this.resultCode = resultCode;
		this.resultMsg = resultMsg;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultMsg() {
		return resultMsg;
	}
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
	@Override
	public String toString() {
		return "TransStatus [orderNo=" + orderNo + ", resultCode=" + resultCode
				+ ", resultMsg=" + resultMsg + "]";
	}
	
}
