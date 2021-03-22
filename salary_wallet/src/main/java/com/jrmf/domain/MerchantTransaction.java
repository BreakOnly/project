package com.jrmf.domain;

/**
 * 商户交易统计信息
 *
 * @author linsong
 * @date 2019/4/25
 */
public class MerchantTransaction {

    private String companyName; //商户
    private Integer customType; //商户类型(1普通 2集团)
    private String customkey; //商户Key
    private String businessCount; //成功交易总笔数
    private String businessAmount; //交易总金额
    private String agentName;    //代理商名称
    private String historyAmount;   //充值确认总金额
    private String balance; //当前可用余额
    private String businessPlatform; //商户所属的业务平台方
    private String customName;
    private String companyId;
    private String lastPaymentTime;
    private String operationsManager; //运营经理
    private String businessManager; //客户经理

    public String getOperationsManager() {
        return operationsManager;
    }

    public void setOperationsManager(String operationsManager) {
        this.operationsManager = operationsManager;
    }

    public String getBusinessManager() {
        return businessManager;
    }

    public void setBusinessManager(String businessManager) {
        this.businessManager = businessManager;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getCustomType() {
        return customType;
    }

    public void setCustomType(Integer customType) {
        this.customType = customType;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getBusinessCount() {
        return businessCount;
    }

    public void setBusinessCount(String businessCount) {
        this.businessCount = businessCount;
    }

    public String getBusinessAmount() {
        return businessAmount;
    }

    public void setBusinessAmount(String businessAmount) {
        this.businessAmount = businessAmount;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getHistoryAmount() {
        return historyAmount;
    }

    public void setHistoryAmount(String historyAmount) {
        this.historyAmount = historyAmount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBusinessPlatform() {
        return businessPlatform;
    }

    public void setBusinessPlatform(String businessPlatform) {
        this.businessPlatform = businessPlatform;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getLastPaymentTime() {
        return lastPaymentTime;
    }

    public void setLastPaymentTime(String lastPaymentTime) {
        this.lastPaymentTime = lastPaymentTime;
    }
}
