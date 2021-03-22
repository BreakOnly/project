package com.jrmf.domain;

import java.io.Serializable;

public class PartnerShip implements Serializable {

	private static final long serialVersionUID = -8538515393733972034L;
	private int id;//主键userId
	private String companyName;//企业名称
	private String companyBalance;//企业余额
	private String remainingBalance;//企业待发余额
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCompanyBalance() {
		return companyBalance;
	}
	public void setCompanyBalance(String companyBalance) {
		this.companyBalance = companyBalance;
	}
	public String getRemainingBalance() {
		return remainingBalance;
	}
	public void setRemainingBalance(String remainingBalance) {
		this.remainingBalance = remainingBalance;
	}

}
 