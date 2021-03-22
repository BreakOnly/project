package com.jrmf.domain;

public class QbInvoicePic {
    private Integer id;

    private String invoiceSerialNo;

    private String invoicePicUrl;

    private String createTime;

    private String updateTime;

    private String addUser;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getInvoiceSerialNo() {
		return invoiceSerialNo;
	}

	public void setInvoiceSerialNo(String invoiceSerialNo) {
		this.invoiceSerialNo = invoiceSerialNo;
	}

	public String getInvoicePicUrl() {
		return invoicePicUrl;
	}

	public void setInvoicePicUrl(String invoicePicUrl) {
		this.invoicePicUrl = invoicePicUrl;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getAddUser() {
		return addUser;
	}

	public void setAddUser(String addUser) {
		this.addUser = addUser;
	}

 
}