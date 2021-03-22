package com.jrmf.payment.openapi.model.response.invoice;

/**
 * 开票公司信息
 */

public class CustomCompanyResult {

	private Long id;

	//客户名称
	private String name;

	//纳税识别号
	private String taxIdcd;
	
	//开票类型(10:账单开票,20:预开票)
	private String openInvoiceType;

	//地址（省 市 区 详细地址）
	private String addr;

	//电话
	private String phone;

	//开户银行
	private String bankName;

	//银行账号
	private String bankAccount;

	//收票人
	private String collector;

	//收票人电话
	private String collectorPhone;

	// 收票人收件地址（省 市 区 详细地址）
	//（省 市 区 详细地址）
	private String collectorAddr;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaxIdcd() {
		return taxIdcd;
	}

	public void setTaxIdcd(String taxIdcd) {
		this.taxIdcd = taxIdcd;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}

	public String getCollector() {
		return collector;
	}

	public void setCollector(String collector) {
		this.collector = collector;
	}

	public String getCollectorPhone() {
		return collectorPhone;
	}

	public void setCollectorPhone(String collectorPhone) {
		this.collectorPhone = collectorPhone;
	}

	public String getCollectorAddr() {
		return collectorAddr;
	}

	public void setCollectorAddr(String collectorAddr) {
		this.collectorAddr = collectorAddr;
	}

	public String getOpenInvoiceType() {
		return openInvoiceType;
	}

	public void setOpenInvoiceType(String openInvoiceType) {
		this.openInvoiceType = openInvoiceType;
	}

}