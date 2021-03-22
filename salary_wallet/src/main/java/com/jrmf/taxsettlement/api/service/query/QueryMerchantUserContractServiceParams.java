package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryMerchantUserContractServiceParams extends ActionParams {

	private String transferCorpId;
	
	private String name;
	
	private String certificateType;
	
	private String certificateNo;
	
	public String getTransferCorpId() {
		return transferCorpId;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCertificateType() {
		return certificateType;
	}

	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	public String getCertificateNo() {
		return certificateNo;
	}

	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}

}
