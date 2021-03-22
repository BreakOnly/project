package com.jrmf.taxsettlement.api.service.query;

import java.util.List;
import java.util.Map;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class QueryMerchantBalanceServiceAttachment extends ActionAttachment {

	private String transferCorpId;
	
	private String payType;
	
	private String balance;

	public String getTransferCorpId() {
		return transferCorpId;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
}
