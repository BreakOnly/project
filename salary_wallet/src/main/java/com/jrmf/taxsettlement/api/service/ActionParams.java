package com.jrmf.taxsettlement.api.service;

import java.io.Serializable;
import java.util.UUID;

abstract public class ActionParams implements Serializable{

	private final String actionSerial = UUID.randomUUID().toString().replaceAll("-", "");
	
	private String merchantId;
	
	private String partnerId;

	private String thirdSerialNumber;
	
	public ActionParams() {
		
	}

	public String getThirdSerialNumber() {
		return thirdSerialNumber;
	}

	public void setThirdSerialNumber(String thirdSerialNumber) {
		this.thirdSerialNumber = thirdSerialNumber;
	}

	public String getActionSerial() {
		return actionSerial;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

}
