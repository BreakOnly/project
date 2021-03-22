package com.jrmf.domain;

public class QbInvoiceVoucher {
    private Integer id;

    private String invoiceNo;

    private String invoiceCode;
    
    private String invoiceDate;

    private String invoiceAmount;

    private Integer status;

    private Integer approvalFlag;

    private String invoiceSerialNo;

    private String invoiceLimitAmout;

    private String customkey;

    private String companyId;

    private Integer invoiceType;

    private String createTime;

    private String updateTime;

    private String addUser;
    
    private Integer isDelete;
    
    private Integer deleteUser;

    private Integer hasCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo == null ? null : invoiceNo.trim();
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode == null ? null : invoiceCode.trim();
    }

    public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount == null ? null : invoiceAmount.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprovalFlag() {
        return approvalFlag;
    }

    public void setApprovalFlag(Integer approvalFlag) {
        this.approvalFlag = approvalFlag;
    }

    public String getInvoiceSerialNo() {
        return invoiceSerialNo;
    }

    public void setInvoiceSerialNo(String invoiceSerialNo) {
        this.invoiceSerialNo = invoiceSerialNo == null ? null : invoiceSerialNo.trim();
    }

    public String getInvoiceLimitAmout() {
        return invoiceLimitAmout;
    }

    public void setInvoiceLimitAmout(String invoiceLimitAmout) {
        this.invoiceLimitAmout = invoiceLimitAmout == null ? null : invoiceLimitAmout.trim();
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

    public void setCompanyId(String cmopanyId) {
        this.companyId = cmopanyId == null ? null : cmopanyId.trim();
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime == null ? null : updateTime.trim();
    }

    public String getAddUser() {
        return addUser;
    }

    public void setAddUser(String addUser) {
        this.addUser = addUser == null ? null : addUser.trim();
    }

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	public Integer getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(Integer deleteUser) {
		this.deleteUser = deleteUser;
	}

    public Integer getHasCount() {
        return hasCount;
    }

    public void setHasCount(Integer hasCount) {
        this.hasCount = hasCount;
    }
}