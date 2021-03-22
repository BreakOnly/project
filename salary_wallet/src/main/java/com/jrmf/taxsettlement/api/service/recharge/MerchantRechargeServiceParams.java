package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.gateway.Amount;
import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-08-19 11:27
 * @desc
 **/
public class MerchantRechargeServiceParams extends ActionParams {
    /**
     * 服务公司id
     */
    @NotNull
    private String transferCorpId;
    /**
     * 商户订单号
     */
    @NotNull
    private String customOrderNo;
    /**
     * 商户银行卡号或账号
     */
    @NotNull
    private String account;
    /**
     * 商户银行卡所属银行或账号所属机构
     */
    @NotNull
    private String accountName;
    /**
     * 商户银行卡所属银行或账号所属机构
     * 2-	支付宝下发
     3-	微信下发
     4-	银行卡下发
     */
    @NotNull
    private String payType;
    /**
     * 收款商户名称
     */
    @NotNull
    private String receiveMerchantName;
    /**
     * 收款银行卡号或账号
     */
    @NotNull
    private String receiveAccount;
    /**
     * 收款商户银行卡所属银行或账号所属机构
     */
    @NotNull
    private String receiveAccountName;
    /**
     * 充值类型
     * 1.余额充值
     * 2.补服务费
     */
    @NotNull
    private String rechargeType;
    /**
     * 收取服务费类型
     * 1.充值预扣收
     * 2.下发实时扣收
     */
    private String serviceFeeType;
    /**
     * 打款金额
     */
    @NotNull
    @Amount
    private String amount;
    /**
     * 到账金额
     */
    @NotNull
    @Amount
    private String balanceAmount;
    /**
     * 手续费费率
     */
    private String feeRate;
    /**
     * 充值凭证文件
     */
    private String chargeFile;
    /**
     * 回调地址
     */
    private String notifyUrl;
    /**
     * 备注
     */
    private String remark;

    @Override
    public String toString() {
        return "MerchantRechargeServiceParams{" +
            "transferCorpId='" + transferCorpId + '\'' +
            ", customOrderNo='" + customOrderNo + '\'' +
            ", account='" + account + '\'' +
            ", accountName='" + accountName + '\'' +
            ", payType='" + payType + '\'' +
            ", receiveMerchantName='" + receiveMerchantName + '\'' +
            ", receiveAccount='" + receiveAccount + '\'' +
            ", receiveAccountName='" + receiveAccountName + '\'' +
            ", rechargeType='" + rechargeType + '\'' +
            ", serviceFeeType='" + serviceFeeType + '\'' +
            ", amount='" + amount + '\'' +
            ", balanceAmount='" + balanceAmount + '\'' +
            ", feeRate='" + feeRate + '\'' +
            ", chargeFile='" + chargeFile + '\'' +
            ", notifyUrl='" + notifyUrl + '\'' +
            ", remark='" + remark + '\'' +
            '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getCustomOrderNo() {
        return customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getReceiveMerchantName() {
        return receiveMerchantName;
    }

    public void setReceiveMerchantName(String receiveMerchantName) {
        this.receiveMerchantName = receiveMerchantName;
    }

    public String getReceiveAccount() {
        return receiveAccount;
    }

    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount;
    }

    public String getReceiveAccountName() {
        return receiveAccountName;
    }

    public void setReceiveAccountName(String receiveAccountName) {
        this.receiveAccountName = receiveAccountName;
    }

    public String getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(String rechargeType) {
        this.rechargeType = rechargeType;
    }

    public String getServiceFeeType() {
        return serviceFeeType;
    }

    public void setServiceFeeType(String serviceFeeType) {
        this.serviceFeeType = serviceFeeType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(String feeRate) {
        this.feeRate = feeRate;
    }

    public String getChargeFile() {
        return chargeFile;
    }

    public void setChargeFile(String chargeFile) {
        this.chargeFile = chargeFile;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
