package com.jrmf.domain;

public class LinkAccountTrans {
    private Integer id;

    private String customkey;

    private Integer tranType;

    private Integer status;

    private String msg;

    private String tranAmount;

    private String tranTime;

    private String payAccountNo;

    private String payAccount;

    private String payBank;

    private String pathNo;

    private String receiveAccount;

    private String receiveAccountNo;

    private String receiveBank;

    private String mainAccount;

    private Integer isSubAccountTrans;

    private String remark;

    private String orderNo;

    private String reqChannelNo;

    private String channelNo;

    private String createTime;

    private String updateTime;

    public LinkAccountTrans() {
    }

    public LinkAccountTrans(LinkageTransferRecord transferRecord) {
        this.customkey = transferRecord.getCustomKey();
//        this.tranType = tranType;
//        this.status = status;
        this.msg = transferRecord.getStatusDesc();
        this.tranAmount = transferRecord.getTranAmount();
        this.tranTime = transferRecord.getTranTime();
        this.payAccountNo = transferRecord.getPayAccountNo();
        this.payAccount = transferRecord.getPayAccountName();
        this.payBank = transferRecord.getPayBankName();
        this.pathNo = transferRecord.getPathNo();
        this.receiveAccount = transferRecord.getInAccountName();
        this.receiveAccountNo = transferRecord.getInAccountNo();
        this.receiveBank = transferRecord.getInBankName();
        this.mainAccount = transferRecord.getPayAccountNo();
        this.isSubAccountTrans = transferRecord.getIsSubAccount();
        this.remark = transferRecord.getTranRemark();
        this.orderNo = transferRecord.getOrderNo();
        this.reqChannelNo = transferRecord.getPathOrderNo();
        this.channelNo = transferRecord.getSelOrderNo();
    }


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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
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

    public String getPayAccount() {
        return payAccount;
    }

    public void setPayAccount(String payAccount) {
        this.payAccount = payAccount == null ? null : payAccount.trim();
    }

    public String getPayBank() {
        return payBank;
    }

    public void setPayBank(String payBank) {
        this.payBank = payBank == null ? null : payBank.trim();
    }

    public String getPathNo() {
        return pathNo;
    }

    public void setPathNo(String pathNo) {
        this.pathNo = pathNo == null ? null : pathNo.trim();
    }

    public String getReceiveAccount() {
        return receiveAccount;
    }

    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount == null ? null : receiveAccount.trim();
    }

    public String getReceiveAccountNo() {
        return receiveAccountNo;
    }

    public void setReceiveAccountNo(String receiveAccountNo) {
        this.receiveAccountNo = receiveAccountNo == null ? null : receiveAccountNo.trim();
    }

    public String getReceiveBank() {
        return receiveBank;
    }

    public void setReceiveBank(String receiveBank) {
        this.receiveBank = receiveBank == null ? null : receiveBank.trim();
    }

    public String getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(String mainAccount) {
        this.mainAccount = mainAccount == null ? null : mainAccount.trim();
    }

    public Integer getIsSubAccountTrans() {
        return isSubAccountTrans;
    }

    public void setIsSubAccountTrans(Integer isSubAccountTrans) {
        this.isSubAccountTrans = isSubAccountTrans;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getReqChannelNo() {
        return reqChannelNo;
    }

    public void setReqChannelNo(String reqChannelNo) {
        this.reqChannelNo = reqChannelNo == null ? null : reqChannelNo.trim();
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo == null ? null : channelNo.trim();
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
}