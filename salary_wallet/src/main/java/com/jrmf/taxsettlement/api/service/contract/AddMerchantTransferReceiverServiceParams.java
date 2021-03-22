package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class AddMerchantTransferReceiverServiceParams extends ActionParams {

	private String name; 
	
	private String certificateType; 
	
	private String certificateNo; 
	
	private String mobileNo;

	public String getName() {
		return name;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
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
