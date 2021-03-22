package com.jrmf.domain;

import com.jrmf.bankapi.pingansub.PinganBankTransactionConstants;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryRecord;
import com.jrmf.controller.constant.ConfirmStatus;
import com.jrmf.controller.constant.CustomTransferRecordType;

public class CustomTransferRecord {
    private Integer id;

    private String customKey;

    private String companyId;

    private String mainAccount;

    private String mainAccountName;

    private String subAccount;

    private String subAccoutName;

    private String flag;

    private String tranAmount;

    private String oppAccountNo;

    private String oppAccountName;

    private String oppBankName;

    private String oppBankNo;

    private String bizFlowNo;

    private Integer isConfirm;

    private String confirmOrderNo;

    private String confirmDate;

    private Integer tranType;

    private String tranTime;

    private String remark;

    private String createTime;

    private String updateTime;

    private Integer currentStatus;

    private String pathNo;


    public CustomTransferRecord() {
    }

    public CustomTransferRecord(SubAccountTransHistoryRecord record) {
        this.mainAccount = record.getMainAccount();
        this.mainAccountName = record.getMainAccountName();
        this.subAccount = record.getSubAccount();
        this.subAccoutName = record.getSubAccoutName();
        this.flag = record.getFlag();
        this.tranAmount = record.getTranAmount();
        this.oppAccountNo = record.getOppAccountNo();
        this.oppAccountName = record.getOppAccountName();
        this.oppBankName = record.getOppBankName();
        this.oppBankNo = record.getOppBankNo();
        this.bizFlowNo = record.getBizFlowNo();
        this.tranTime = record.getAccountDate() + record.getTranTime();
        this.remark = record.getRemark();
        this.isConfirm = ConfirmStatus.FAILURE.getCode();
        this.tranType = CustomTransferRecordType.codeOfFlag(record.getFlag()).getCode();
    }

    public CustomTransferRecord(CustomReceiveConfig receiveConfig, PaymentConfig paymentConfig) {

        this.customKey = receiveConfig.getCustomkey();
        this.companyId = receiveConfig.getCompanyId();
        this.mainAccount = paymentConfig.getCorporationAccount();
        this.mainAccountName = paymentConfig.getCorporationAccountName();
        this.subAccount = receiveConfig.getReceiveAccount();
        this.subAccoutName = receiveConfig.getContractCompanyName();

        this.oppAccountNo = paymentConfig.getShadowAcctNo();
        this.oppAccountName = paymentConfig.getCorporationAccountName();
        this.oppBankNo = paymentConfig.getParameter1();
        this.oppBankName = paymentConfig.getCorporationName();


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

    public String getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(String mainAccount) {
        this.mainAccount = mainAccount == null ? null : mainAccount.trim();
    }

    public String getMainAccountName() {
        return mainAccountName;
    }

    public void setMainAccountName(String mainAccountName) {
        this.mainAccountName = mainAccountName == null ? null : mainAccountName.trim();
    }

    public String getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount == null ? null : subAccount.trim();
    }

    public String getSubAccoutName() {
        return subAccoutName;
    }

    public void setSubAccoutName(String subAccoutName) {
        this.subAccoutName = subAccoutName == null ? null : subAccoutName.trim();
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag == null ? null : flag.trim();
    }

    public String getTranAmount() {
        return tranAmount;
    }

    public void setTranAmount(String tranAmount) {
        this.tranAmount = tranAmount == null ? null : tranAmount.trim();
    }

    public String getOppAccountNo() {
        return oppAccountNo;
    }

    public void setOppAccountNo(String oppAccountNo) {
        this.oppAccountNo = oppAccountNo == null ? null : oppAccountNo.trim();
    }

    public String getOppAccountName() {
        return oppAccountName;
    }

    public void setOppAccountName(String oppAccountName) {
        this.oppAccountName = oppAccountName == null ? null : oppAccountName.trim();
    }

    public String getOppBankName() {
        return oppBankName;
    }

    public void setOppBankName(String oppBankName) {
        this.oppBankName = oppBankName == null ? null : oppBankName.trim();
    }

    public String getOppBankNo() {
        return oppBankNo;
    }

    public void setOppBankNo(String oppBankNo) {
        this.oppBankNo = oppBankNo == null ? null : oppBankNo.trim();
    }

    public String getBizFlowNo() {
        return bizFlowNo;
    }

    public void setBizFlowNo(String bizFlowNo) {
        this.bizFlowNo = bizFlowNo == null ? null : bizFlowNo.trim();
    }

    public Integer getIsConfirm() {
        return isConfirm;
    }

    public void setIsConfirm(Integer isConfirm) {
        this.isConfirm = isConfirm;
    }

    public String getConfirmOrderNo() {
        return confirmOrderNo;
    }

    public void setConfirmOrderNo(String confirmOrderNo) {
        this.confirmOrderNo = confirmOrderNo == null ? null : confirmOrderNo.trim();
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate == null ? null : confirmDate.trim();
    }

    public Integer getTranType() {
        return tranType;
    }

    public void setTranType(Integer tranType) {
        this.tranType = tranType;
    }

    public String getTranTime() {
        return tranTime;
    }

    public void setTranTime(String tranTime) {
        this.tranTime = tranTime == null ? null : tranTime.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Integer getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getPathNo() {
        return pathNo;
    }

    public void setPathNo(String pathNo) {
        this.pathNo = pathNo;
    }
}