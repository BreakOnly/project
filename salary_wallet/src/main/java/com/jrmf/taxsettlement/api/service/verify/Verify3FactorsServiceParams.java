package com.jrmf.taxsettlement.api.service.verify;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class Verify3FactorsServiceParams extends ActionParams {

	private String certificateType;
	
	private String certificateNo;
	
	private String name;
	
	private String bankCardNo;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBankCardNo() {
		return bankCardNo;
	}

	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}
}
