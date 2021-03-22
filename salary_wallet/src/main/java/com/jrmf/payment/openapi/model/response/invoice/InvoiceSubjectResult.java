package com.jrmf.payment.openapi.model.response.invoice;

public class InvoiceSubjectResult {

	/**
	 * 必填(是) long 说明(id）
	 */
	private Long id;
	/**
	 * 必填(是) string(64) 说明(发票类目名）
	 */
	private String name;
	/**
	 * 必填(否) long 说明(税率,万分位6.00% == 600）
	 */
	private Long taxRate;
	/**
	 * 必填(是) string(32) 说明(税收分类编码）
	 */
	private String code;
	/**
	 * 必填(是) string(256) 说明(简称）
	 */
	private String abbreviation;

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

	public Long getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Long taxRate) {
		this.taxRate = taxRate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

}