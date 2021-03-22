package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryMerchantBalanceServiceParams extends ActionParams {

	private String transferCorpId;
	
	private String payType;
	
	public String getTransferCorpId() {
		return transferCorpId;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

}
