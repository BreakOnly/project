package com.jrmf.domain;

public class LinkageBaseConfig {
    private Integer id;

    private String corporationAccountName;

    private String corporationAccount;

    private String bankName;

    private String pathType;

    private Integer linkageType;

    private Integer status;

    private String pathNo;

    private String preHost;

    private Integer remotePort;

    private Integer readTimeOut;

    private Integer connectTimeOut;

    private String corpToBankStandardCode;

    private String corporationName;

    private Integer isSubAccount;

    private String shadowAcctNo;

    private String privateKey;

    private String selPublicKey;

    private String apiKey;

    private String parameter1;

    private String parameter2;

    private String parameter3;

    private String createTime;

    private String updateTime;

    private String addUser;

    private String companyName;

    private String subAccount;

    private String customkey;

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCorporationAccountName() {
        return corporationAccountName;
    }

    public void setCorporationAccountName(String corporationAccountName) {
        this.corporationAccountName = corporationAccountName == null ? null : corporationAccountName.trim();
    }

    public String getCorporationAccount() {
        return corporationAccount;
    }

    public void setCorporationAccount(String corporationAccount) {
        this.corporationAccount = corporationAccount == null ? null : corporationAccount.trim();
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getPathType() {
		return pathType;
	}

	public void setPathType(String pathType) {
		this.pathType = pathType;
	}

	public Integer getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(Integer linkageType) {
        this.linkageType = linkageType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPathNo() {
        return pathNo;
    }

    public void setPathNo(String pathNo) {
        this.pathNo = pathNo == null ? null : pathNo.trim();
    }

    public String getPreHost() {
        return preHost;
    }

    public void setPreHost(String preHost) {
        this.preHost = preHost == null ? null : preHost.trim();
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Integer getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(Integer readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public Integer getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(Integer connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public String getCorpToBankStandardCode() {
        return corpToBankStandardCode;
    }

    public void setCorpToBankStandardCode(String corpToBankStandardCode) {
        this.corpToBankStandardCode = corpToBankStandardCode == null ? null : corpToBankStandardCode.trim();
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName == null ? null : corporationName.trim();
    }

    public Integer getIsSubAccount() {
        return isSubAccount;
    }

    public void setIsSubAccount(Integer isSubAccount) {
        this.isSubAccount = isSubAccount;
    }

    public String getShadowAcctNo() {
        return shadowAcctNo;
    }

    public void setShadowAcctNo(String shadowAcctNo) {
        this.shadowAcctNo = shadowAcctNo == null ? null : shadowAcctNo.trim();
    }

    public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getSelPublicKey() {
		return selPublicKey;
	}

	public void setSelPublicKey(String selPublicKey) {
		this.selPublicKey = selPublicKey;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getParameter1() {
        return parameter1;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1 == null ? null : parameter1.trim();
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2 == null ? null : parameter2.trim();
    }

    public String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3 == null ? null : parameter3.trim();
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

	public String getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}

}