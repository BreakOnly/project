package com.jrmf.domain;

public class LinkageTransferRecord {
    private Integer id;

    private String customKey;

    private String companyId;

    private Integer tranType;

    private Integer status;

    private String statusDesc;

    private String tranAmount;

    private String tranTime;

    private String payAccountNo;

    private String payAccountName;

    private String payBankName;

    private String payBankNo;

    private Integer isSubAccount;

    private String paySubAccount;

    private String inAccountNo;

    private String inAccountName;

    private String inBankName;

    private String inBankNo;

    private String pathNo;

    private String tranRemark;

    private String orderNo;

    private String pathOrderNo;

    private String selOrderNo;

    private String createTime;

    private String updateTime;


    //非数据库字段
    private String customName;
    private String startTime;
    private String endTime;

    public LinkageTransferRecord() {
    }

    public LinkageTransferRecord(ChannelHistory rechargeInfo, LinkageBaseConfig baseConfig) {
        this.customKey = rechargeInfo.getCustomkey();
        this.companyId = rechargeInfo.getRecCustomkey();
        this.tranAmount = rechargeInfo.getRechargeAmount();
        this.payAccountNo = baseConfig.getCorporationAccount();
        this.payAccountName = baseConfig.getCorporationAccountName();
        this.payBankName = baseConfig.getBankName();
        this.isSubAccount = baseConfig.getIsSubAccount();
        this.paySubAccount = baseConfig.getSubAccount();
//        this.payBankNo = payBankNo;
        this.inAccountNo = rechargeInfo.getInAccountNo();
        this.inAccountName = rechargeInfo.getInAccountName();
        this.inBankName = rechargeInfo.getInAccountBankName();
//        this.inBankNo = inBankNo;
        this.pathNo = baseConfig.getPathNo();
        this.tranRemark = rechargeInfo.getRemark();
        this.orderNo = rechargeInfo.getOrderno();
        this.pathOrderNo = rechargeInfo.getOrderno();
//        this.pathOrderNo = pathOrderNo;
//        this.selOrderNo = selOrderNo;
//        this.createTime = createTime;
//        this.updateTime = updateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey == null ? null : customKey.trim();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId == null ? null : companyId.trim();
    }

    public Integer getTranType() {
        return tranType;
    }

    public void setTranType(Integer tranType) {
        this.tranType = tranType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc == null ? null : statusDesc.trim();
    }

    public String getTranAmount() {
        return tranAmount;
    }

    public void setTranAmount(String tranAmount) {
        this.tranAmount = tranAmount == null ? null : tranAmount.trim();
    }

    public String getTranTime() {
        return tranTime;
    }

    public void setTranTime(String tranTime) {
        this.tranTime = tranTime == null ? null : tranTime.trim();
    }

    public String getPayAccountNo() {
        return payAccountNo;
    }

    public void setPayAccountNo(String payAccountNo) {
        this.payAccountNo = payAccountNo == null ? null : payAccountNo.trim();
    }

    public String getPayAccountName() {
        return payAccountName;
    }

    public void setPayAccountName(String payAccountName) {
        this.payAccountName = payAccountName == null ? null : payAccountName.trim();
    }

    public String getPayBankName() {
        return payBankName;
    }

    public void setPayBankName(String payBankName) {
        this.payBankName = payBankName == null ? null : payBankName.trim();
    }

    public String getPayBankNo() {
        return payBankNo;
    }

    public void setPayBankNo(String payBankNo) {
        this.payBankNo = payBankNo == null ? null : payBankNo.trim();
    }

    public Integer getIsSubAccount() {
        return isSubAccount;
    }

    public void setIsSubAccount(Integer isSubAccount) {
        this.isSubAccount = isSubAccount;
    }

    public String getPaySubAccount() {
        return paySubAccount;
    }

    public void setPaySubAccount(String paySubAccount) {
        this.paySubAccount = paySubAccount == null ? null : paySubAccount.trim();
    }

    public String getInAccountNo() {
        return inAccountNo;
    }

    public void setInAccountNo(String inAccountNo) {
        this.inAccountNo = inAccountNo == null ? null : inAccountNo.trim();
    }

    public String getInAccountName() {
        return inAccountName;
    }

    public void setInAccountName(String inAccountName) {
        this.inAccountName = inAccountName == null ? null : inAccountName.trim();
    }

    public String getInBankName() {
        return inBankName;
    }

    public void setInBankName(String inBankName) {
        this.inBankName = inBankName == null ? null : inBankName.trim();
    }

    public String getInBankNo() {
        return inBankNo;
    }

    public void setInBankNo(String inBankNo) {
        this.inBankNo = inBankNo == null ? null : inBankNo.trim();
    }

    public String getPathNo() {
        return pathNo;
    }

    public void setPathNo(String pathNo) {
        this.pathNo = pathNo == null ? null : pathNo.trim();
    }

    public String getTranRemark() {
        return tranRemark;
    }

    public void setTranRemark(String tranRemark) {
        this.tranRemark = tranRemark == null ? null : tranRemark.trim();
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getPathOrderNo() {
        return pathOrderNo;
    }

    public void setPathOrderNo(String pathOrderNo) {
        this.pathOrderNo = pathOrderNo == null ? null : pathOrderNo.trim();
    }

    public String getSelOrderNo() {
        return selOrderNo;
    }

    public void setSelOrderNo(String selOrderNo) {
        this.selOrderNo = selOrderNo == null ? null : selOrderNo.trim();
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

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}