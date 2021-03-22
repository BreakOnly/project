package com.jrmf.domain;

public class QbInvoiceApprovalRecord {
    private Integer id;

    private String invoiceSerialNo;

    private String rechargeOrderNo;

    private String invoiceAmount;

    private String approvalAmount;

    private String unApprovalAmount;

    private String createTime;

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
        this.invoiceSerialNo = invoiceSerialNo == null ? null : invoiceSerialNo.trim();
    }

    public String getRechargeOrderNo() {
        return rechargeOrderNo;
    }

    public void setRechargeOrderNo(String rechargeOrderNo) {
        this.rechargeOrderNo = rechargeOrderNo == null ? null : rechargeOrderNo.trim();
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount == null ? null : invoiceAmount.trim();
    }

    public String getApprovalAmount() {
        return approvalAmount;
    }

    public void setApprovalAmount(String approvalAmount) {
        this.approvalAmount = approvalAmount == null ? null : approvalAmount.trim();
    }

    public String getUnApprovalAmount() {
        return unApprovalAmount;
    }

    public void setUnApprovalAmount(String unApprovalAmount) {
        this.unApprovalAmount = unApprovalAmount == null ? null : unApprovalAmount.trim();
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }
}