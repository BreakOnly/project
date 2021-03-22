package com.jrmf.payment.openapi.model.response.invoice;

/**
 * 开票公司信息
 */

public class ServiceCompanyResult {

	/**
	 * 必填(是) long 说明(id）
	 */
	private Long id;

	/**
	 *  必填(是) string(64) 说明(客户名称)
	 */
	private String name;

	/**
	 *  必填(是) string(32) 说明(纳税识别号)
	 */
	private String taxIdcd;

	/**
	 *  必填(是) long(14) 说明(普票最大限额，以分为单位)
	 */
	private Long ppMaxAmount;

	/**
	 *  必填(是) long 说明(专票最大限额，以分为单位)
	 */
	private Long zpMaxAmount;

	/**
	 * 必填(是) string(64) 说明( 地址（省 市 区 详细地址）)
	 */
	private String addr;

	/**
	 *  必填(是) string(18) 说明(电话)
	 */
	private String phone;

	/**
	 *  必填(是) string() 说明(开户银行)
	 */
	private String bankName;

	/**
	 *  必填(是) string(64) 说明(银行账号)
	 */
	private String bankAccount;

	/**
	 *  必填(是) string(32) 说明(收款人)
	 */
	private String payee;

	/**
	 *  必填(是) string(32) 说明(复核人)
	 */
	private String checker;

	/**
	 *  必填(是) string(32) 说明(开票人)
	 */
	private String drawer;

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

	public Long getPpMaxAmount() {
		return ppMaxAmount;
	}

	public void setPpMaxAmount(Long ppMaxAmount) {
		this.ppMaxAmount = ppMaxAmount;
	}

	public Long getZpMaxAmount() {
		return zpMaxAmount;
	}

	public void setZpMaxAmount(Long zpMaxAmount) {
		this.zpMaxAmount = zpMaxAmount;
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

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public String getChecker() {
		return checker;
	}

	public void setChecker(String checker) {
		this.checker = checker;
	}

	public String getDrawer() {
		return drawer;
	}

	public void setDrawer(String drawer) {
		this.drawer = drawer;
	}

		
}