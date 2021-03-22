package com.jrmf.domain;

public class QbInvoiceBase {
    private Integer id;

    private String customkey;

    private String companyId;

    private Integer invoiceType;

    private Integer billingClass;

    private Integer serviceType;

    private String companyName;

    private String taxRegistrationNumber;

    private Integer taxpayerType;

    private String remark;

    private String accountBankName;

    private String accountNo;

    private String address;

    private String phone;

    private String taxPicUrl;

    private String taxpayerPicUrl;

    private Integer status;

    private String addUser;

    private String reviewUser;

    private String createTime;

    private String updateTime;
    
    private String downReason;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey == null ? null : customkey.trim();
    }

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Integer getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(Integer invoiceType) {
		this.invoiceType = invoiceType;
	}

	public Integer getBillingClass() {
		return billingClass;
	}

	public void setBillingClass(Integer billingClass) {
		this.billingClass = billingClass;
	}

	public Integer getServiceType() {
		return serviceType;
	}

	public void setServiceType(Integer serviceType) {
		this.serviceType = serviceType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getTaxRegistrationNumber() {
		return taxRegistrationNumber;
	}

	public void setTaxRegistrationNumber(String taxRegistrationNumber) {
		this.taxRegistrationNumber = taxRegistrationNumber;
	}

	public Integer getTaxpayerType() {
		return taxpayerType;
	}

	public void setTaxpayerType(Integer taxpayerType) {
		this.taxpayerType = taxpayerType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAccountBankName() {
		return accountBankName;
	}

	public void setAccountBankName(String accountBankName) {
		this.accountBankName = accountBankName;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTaxPicUrl() {
		return taxPicUrl;
	}

	public void setTaxPicUrl(String taxPicUrl) {
		this.taxPicUrl = taxPicUrl;
	}

	public String getTaxpayerPicUrl() {
		return taxpayerPicUrl;
	}

	public void setTaxpayerPicUrl(String taxpayerPicUrl) {
		this.taxpayerPicUrl = taxpayerPicUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getAddUser() {
		return addUser;
	}

	public void setAddUser(String addUser) {
		this.addUser = addUser;
	}

	public String getReviewUser() {
		return reviewUser;
	}

	public void setReviewUser(String reviewUser) {
		this.reviewUser = reviewUser;
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

	public String getDownReason() {
		return downReason;
	}

	public void setDownReason(String downReason) {
		this.downReason = downReason;
	}
    
}