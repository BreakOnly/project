package com.jrmf.domain;

import java.io.Serializable;

public class CompanyPayment implements Serializable{

	private static final long serialVersionUID = 4850225555146684368L;

	private int id;
	private String implementor;
	private String payType;
	private String merchantId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getImplementor() {
		return implementor;
	}
	public void setImplementor(String implementor) {
		this.implementor = implementor;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

}
