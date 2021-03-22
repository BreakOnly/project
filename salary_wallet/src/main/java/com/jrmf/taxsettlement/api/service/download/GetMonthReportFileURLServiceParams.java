package com.jrmf.taxsettlement.api.service.download;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class GetMonthReportFileURLServiceParams extends ActionParams {

	private String accountMonth;

	public String getAccountMonth() {
		return accountMonth;
	}

	public void setAccountMonth(String accountMonth) {
		this.accountMonth = accountMonth;
	}


}
