package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author 种路路
 * @create 2019-08-22 16:13
 * @desc
 **/
public class QueryRechargeAccountServiceAttachment extends ActionAttachment {
    /**
     * 服务公司id
     */
    private String transferCorpId;
    /**
     * 收款商户名称
     */
    private String receiveMerchantName;
    /**
     * 收款银行卡号或账号
     */
    private String receiveAccount;
    /**
     * 收款商户银行卡所属银行或账号所属机构
     */
    private String receiveAccountName;

    @Override
    public String toString() {
        return "QueryRechargeAccountServiceAttachment{" + "transferCorpId='" + transferCorpId + '\'' + ", receiveMerchantName='" + receiveMerchantName + '\'' + ", receiveAccount='" + receiveAccount + '\'' + ", receiveAccountName='" + receiveAccountName + '\'' + '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
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
}
