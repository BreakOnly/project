package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class QueryTransferDealServiceAttachment extends ActionAttachment {

	private String requestNo;
	
	private String dealNo;
	
	private String dealStatus;
	
	private String dealStatusMsg;
	
	private String accountDate;
	
	private String fee;

	public String getRequestNo() {
		return requestNo;
	}

	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}

	public String getDealNo() {
		return dealNo;
	}

	public void setDealNo(String dealNo) {
		this.dealNo = dealNo;
	}

	public String getDealStatusMsg() {
		return dealStatusMsg;
	}

	public void setDealStatusMsg(String dealStatusMsg) {
		this.dealStatusMsg = dealStatusMsg;
	}

	public String getDealStatus() {
		return dealStatus;
	}

	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}

	public String getAccountDate() {
		return accountDate;
	}

	public void setAccountDate(String accountDate) {
		this.accountDate = accountDate;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}
}
