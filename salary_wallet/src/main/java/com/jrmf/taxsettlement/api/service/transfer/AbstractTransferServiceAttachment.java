package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public abstract class AbstractTransferServiceAttachment extends ActionAttachment {

	private String dealNo;
	
	private String amount;
	
	private String dealStatus;
	
	private String dealStatusMsg;

	public String getDealNo() {
		return dealNo;
	}

	public String getAmount() {
		return amount;
	}

	public String getDealStatus() {
		return dealStatus;
	}

	public String getDealStatusMsg() {
		return dealStatusMsg;
	}

	public void setDealNo(String dealNo) {
		this.dealNo = dealNo;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}

	public void setDealStatusMsg(String dealStatusMsg) {
		this.dealStatusMsg = dealStatusMsg;
	}
}
