package com.jrmf.domain;

public class CustomReceiveConfig {
    //主键
    private Integer id;
    //渠道商唯一标识
    private String customkey;
    //下发公司ID
    private String companyId;
    //支付方式 1 徽商银行  2 支付宝  3 微信 4 银企直联
    private Integer payType;
    //充值确认方式1.自动确认，2.人工确认
    private Integer rechargeConfirmType;
    //收款账号
    private String receiveAccount;
    //收款账户名称
    private String receiveUser;
    //收款账户银行
    private String receiveBank;
    //收款账户行号
    private String receiveBankNo;
    //操作人
    private String addUser;
    //创建时间
    private String createTime;
    //更新时间
    private String updateTime;
    //是否子账号模式 0否 1是
    private Integer isSubAccount;
    //充值账户使用状态1.正常，2.废弃
    private Integer status;

    //非数据库字段
    private String companyName;
    private String contractCompanyName;
    private int customId;
    private String customBalance;
    private String subAccountBalance;
    private String mainAccount;

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
        this.customkey = customkey;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getRechargeConfirmType() {
        return rechargeConfirmType;
    }

    public void setRechargeConfirmType(Integer rechargeConfirmType) {
        this.rechargeConfirmType = rechargeConfirmType;
    }

    public String getReceiveAccount() {
        return receiveAccount;
    }

    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount;
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser;
    }

    public String getReceiveBank() {
        return receiveBank;
    }

    public void setReceiveBank(String receiveBank) {
        this.receiveBank = receiveBank;
    }

    public String getReceiveBankNo() {
        return receiveBankNo;
    }

    public void setReceiveBankNo(String receiveBankNo) {
        this.receiveBankNo = receiveBankNo;
    }

    public String getAddUser() {
        return addUser;
    }

    public void setAddUser(String addUser) {
        this.addUser = addUser;
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

    public String getContractCompanyName() {
        return contractCompanyName;
    }

    public void setContractCompanyName(String contractCompanyName) {
        this.contractCompanyName = contractCompanyName;
    }

    public int getCustomId() {
        return customId;
    }

    public void setCustomId(int customId) {
        this.customId = customId;
    }

    public Integer getIsSubAccount() {
        return isSubAccount;
    }

    public void setIsSubAccount(Integer isSubAccount) {
        this.isSubAccount = isSubAccount;
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

    public String getCustomBalance() {
        return customBalance;
    }

    public void setCustomBalance(String customBalance) {
        this.customBalance = customBalance;
    }

    public String getSubAccountBalance() {
        return subAccountBalance;
    }

    public void setSubAccountBalance(String subAccountBalance) {
        this.subAccountBalance = subAccountBalance;
    }

    public String getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(String mainAccount) {
        this.mainAccount = mainAccount;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
