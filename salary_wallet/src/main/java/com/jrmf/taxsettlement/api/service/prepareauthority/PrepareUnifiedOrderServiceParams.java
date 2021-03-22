package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-06-17 14:40
 * @desc
 **/
public class PrepareUnifiedOrderServiceParams extends ActionParams {
    /**
     * 渠道订单号
     */
    @NotNull
    private String customOrderNo;
    /**
     * 金额
     */
    @NotNull
    private String amount;
    /**
     * 说明
     */
    private String memo;
    /**
     * 收款方账号/银行卡号
     */
    @NotNull
    private String bankCardNo;
    /**
     * 收款方账号名称
     */
    @NotNull
    private String accountName;
    /**
     * 收款方账号开户行名称
     */
    private String openBankName;
    /**
     * 收款方账号开户支行
     */
    private String depositBank;
    /**
     * 身份证号
     */
    @NotNull
    private String certificateNo;
    /**
     * 手机号
     */
    private String phoneNo;
    /**
     * 姓名
     */
    @NotNull
    private String name;
    /**
     * 服务公司id
     */
    @NotNull
    private String transferCorpId;
    /**
     * 服务类型（参考爱员工服务类型）
     */
    @NotNull
    private String serviceType;
    /**
     * 回调地址
     */
    @NotNull
    private String notifyUrl;


    public String getCustomOrderNo() {
        return customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getOpenBankName() {
        return openBankName;
    }

    public void setOpenBankName(String openBankName) {
        this.openBankName = openBankName;
    }

    public String getDepositBank() {
        return depositBank;
    }

    public void setDepositBank(String depositBank) {
        this.depositBank = depositBank;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    @Override
    public String toString() {
        return "PrepareUnifiedOrderServiceParams{" + "customOrderNo='" + customOrderNo + '\'' + ", amount='" + amount + '\'' + ", memo='" + memo + '\'' + ", bankCardNo='" + bankCardNo + '\'' + ", accountName='" + accountName + '\'' + ", openBankName='" + openBankName + '\'' + ", depositBank='" + depositBank + '\'' + ", certificateNo='" + certificateNo + '\'' + ", phoneNo='" + phoneNo + '\'' + ", name='" + name + '\'' + ", transferCorpId='" + transferCorpId + '\'' + ", serviceType='" + serviceType + '\'' + ", notifyUrl='" + notifyUrl + '\'' + '}';
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
