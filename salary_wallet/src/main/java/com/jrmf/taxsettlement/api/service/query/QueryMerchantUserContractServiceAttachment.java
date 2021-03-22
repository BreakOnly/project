package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class QueryMerchantUserContractServiceAttachment extends ActionAttachment {

	private String signStatus;
	
	private int contractCount;
	
	private int signCount;

	public String getSignStatus() {
		return signStatus;
	}

	public void setSignStatus(String signStatus) {
		this.signStatus = signStatus;
	}

	public int getContractCount() {
		return contractCount;
	}

	public void setContractCount(int contractCount) {
		this.contractCount = contractCount;
	}

	public int getSignCount() {
		return signCount;
	}

	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}
}
