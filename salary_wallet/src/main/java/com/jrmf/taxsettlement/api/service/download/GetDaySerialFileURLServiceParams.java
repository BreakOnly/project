package com.jrmf.taxsettlement.api.service.download;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class GetDaySerialFileURLServiceParams extends ActionParams {

	private String accountDate;
	private String transferCorpId;

	public String getAccountDate() {
		return accountDate;
	}

	public void setAccountDate(String accountDate) {
		this.accountDate = accountDate;
	}

	public String getTransferCorpId() {
		return transferCorpId;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}
}
