package com.jrmf.domain;

/**
 * qb_bankname表实体类
 * @author guoto
 *
 */
public class BankName {
	private String name;
	private String mark;
	private String superNetNo;
	private String bankCompanyName;
	private String bankCodeYP;//易宝银行编码
	private String validated;//支付宝接口卡bin校验
	private String bankCodeZJ;//中金银行编码
	
	public String getBankCodeYP() {
		return bankCodeYP;
	}
	public void setBankCodeYP(String bankCodeYP) {
		this.bankCodeYP = bankCodeYP;
	}
	public String getValidated() {
		return validated;
	}
	public void setValidated(String validated) {
		this.validated = validated;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getSuperNetNo() {
		return superNetNo;
	}
	public void setSuperNetNo(String superNetNo) {
		this.superNetNo = superNetNo;
	}
	public String getBankCompanyName() {
		return bankCompanyName;
	}
	public void setBankCompanyName(String bankCompanyName) {
		this.bankCompanyName = bankCompanyName;
	}
	public String getBankCodeZJ() {
		return bankCodeZJ;
	}
	public void setBankCodeZJ(String bankCodeZJ) {
		this.bankCodeZJ = bankCodeZJ;
	}
	
	
}
