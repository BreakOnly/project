package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryTransferBatchServiceParams extends ActionParams {

	private String batchNo;

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

}
