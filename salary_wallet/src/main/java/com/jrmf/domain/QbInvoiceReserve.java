package com.jrmf.domain;

public class QbInvoiceReserve {
    private Integer id;

    private String companyId;

    private Integer invoiceType;

    private String invoiceLimitAmout;

    private Integer invoiceTotalNum;

    private String month;

    private String createTime;

    private String updateTime;

    private String addUser;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId == null ? null : companyId.trim();
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceLimitAmout() {
        return invoiceLimitAmout;
    }

    public void setInvoiceLimitAmout(String invoiceLimitAmout) {
        this.invoiceLimitAmout = invoiceLimitAmout == null ? null : invoiceLimitAmout.trim();
    }

    public Integer getInvoiceTotalNum() {
        return invoiceTotalNum;
    }

    public void setInvoiceTotalNum(Integer invoiceTotalNum) {
        this.invoiceTotalNum = invoiceTotalNum;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month == null ? null : month.trim();
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
}