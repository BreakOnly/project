package com.jrmf.domain;

import java.io.Serializable;

public class PaymentConfig implements Serializable {

    private static final long serialVersionUID = 7083669943031560550L;

    private int id;
    private String pathNo;
    private int companyId;

    private String corporationAccount;
    private String corporationAccountName;
    private String corpToBankStandardCode;
    private String corporationName;
    private String preHost;
    private int remotePort;
    private int readTimeOut;
    private int status;

    private String apiKey;
    private String payPublicKey;
    private String payPrivateKey;
    private String parameter1;
    private String parameter2;
    private String parameter3;
    private String parameter4;
    private String parameter5;
    private String parameter6;
    private String parameter7;
    private String parameter8;
    private String parameter9;

    private String createTime;
    private String updateTime;//最后更新时间

    private String appIdAyg;//爱员工专用字段---与服务公司对应
    private Long serviceCompanyId;//爱员工专用字段---落地服务商id
    private Integer isSubAccount;

    private String keyWords;
    private String containKeyWords;
    private String shadowAcctNo;
    private String bankName;
    private String thirdMerchId;
    private Integer pathKeyType;

    //非数据库字段
    private String subAcctNo;

    public PaymentConfig() {
    }

    public PaymentConfig(LinkageBaseConfig baseConfig) {
        this.pathNo = baseConfig.getPathNo();
        this.corporationAccount = baseConfig.getCorporationAccount();
        this.corporationAccountName = baseConfig.getCorporationAccountName();
        this.corpToBankStandardCode = baseConfig.getCorpToBankStandardCode();
        this.corporationName = baseConfig.getCorporationName();
        this.preHost = baseConfig.getPreHost();
        this.remotePort = baseConfig.getRemotePort();
        this.readTimeOut = baseConfig.getReadTimeOut();
        this.status = baseConfig.getStatus();
        this.payPublicKey = baseConfig.getSelPublicKey();
        this.payPrivateKey = baseConfig.getPrivateKey();
        this.parameter1 = baseConfig.getParameter1();
        this.parameter2 = baseConfig.getParameter2();
        this.parameter3 = baseConfig.getParameter3();
        this.bankName = baseConfig.getBankName();
        this.isSubAccount = baseConfig.getIsSubAccount();
        this.subAcctNo = baseConfig.getSubAccount();
    }


    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Long getServiceCompanyId() {
        return serviceCompanyId;
    }

    public void setServiceCompanyId(Long serviceCompanyId) {
        this.serviceCompanyId = serviceCompanyId;
    }

    public String getAppIdAyg() {
        return appIdAyg;
    }

    public void setAppIdAyg(String appIdAyg) {
        this.appIdAyg = appIdAyg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public String getPreHost() {
        return preHost;
    }

    public void setPreHost(String preHost) {
        this.preHost = preHost;
    }

    public String getCorporationAccount() {
        return corporationAccount;
    }

    public void setCorporationAccount(String corporationAccount) {
        this.corporationAccount = corporationAccount;
    }

    public String getCorporationAccountName() {
        return corporationAccountName;
    }

    public void setCorporationAccountName(String corporationAccountName) {
        this.corporationAccountName = corporationAccountName;
    }

    public String getCorpToBankStandardCode() {
        return corpToBankStandardCode;
    }

    public void setCorpToBankStandardCode(String corpToBankStandardCode) {
        this.corpToBankStandardCode = corpToBankStandardCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPayPublicKey() {
        return payPublicKey;
    }

    public void setPayPublicKey(String payPublicKey) {
        this.payPublicKey = payPublicKey;
    }

    public String getPayPrivateKey() {
        return payPrivateKey;
    }

    public void setPayPrivateKey(String payPrivateKey) {
        this.payPrivateKey = payPrivateKey;
    }

    public String getParameter1() {
        return parameter1;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
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

    public String getPathNo() {
        return pathNo;
    }

    public void setPathNo(String pathNo) {
        this.pathNo = pathNo;
    }

    public Integer getIsSubAccount() {
        return isSubAccount;
    }

    public void setIsSubAccount(Integer isSubAccount) {
        this.isSubAccount = isSubAccount;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getContainKeyWords() {
        return containKeyWords;
    }

    public void setContainKeyWords(String containKeyWords) {
        this.containKeyWords = containKeyWords;
    }

    public String getShadowAcctNo() {
        return shadowAcctNo;
    }

    public void setShadowAcctNo(String shadowAcctNo) {
        this.shadowAcctNo = shadowAcctNo;
    }

    public String getSubAcctNo() {
        return subAcctNo;
    }

    public void setSubAcctNo(String subAcctNo) {
        this.subAcctNo = subAcctNo;
    }

    public String getThirdMerchId() {
        return thirdMerchId;
    }

    public void setThirdMerchId(String thirdMerchId) {
        this.thirdMerchId = thirdMerchId;
    }

    public Integer getPathKeyType() {
        return pathKeyType;
    }

    public void setPathKeyType(Integer pathKeyType) {
        this.pathKeyType = pathKeyType;
    }

  public String getParameter4() {
    return parameter4;
  }

  public void setParameter4(String parameter4) {
    this.parameter4 = parameter4;
  }

  public String getParameter5() {
    return parameter5;
  }

  public void setParameter5(String parameter5) {
    this.parameter5 = parameter5;
  }

  public String getParameter6() {
    return parameter6;
  }

  public void setParameter6(String parameter6) {
    this.parameter6 = parameter6;
  }

  public String getParameter7() {
    return parameter7;
  }

  public void setParameter7(String parameter7) {
    this.parameter7 = parameter7;
  }

  public String getParameter8() {
    return parameter8;
  }

  public void setParameter8(String parameter8) {
    this.parameter8 = parameter8;
  }

  public String getParameter9() {
    return parameter9;
  }

  public void setParameter9(String parameter9) {
    this.parameter9 = parameter9;
  }

  @Override
    public String toString() {
        return "PaymentConfig [id=" + id + ", pathNo=" + pathNo
                + ", companyId=" + companyId + ", corporationAccount="
                + corporationAccount + ", corporationAccountName="
                + corporationAccountName + ", corpToBankStandardCode="
                + corpToBankStandardCode + ", corporationName="
                + corporationName + ", preHost=" + preHost + ", remotePort="
                + remotePort + ", readTimeOut=" + readTimeOut + ", status="
                + status + ", payPublicKey=" + payPublicKey
                + ", payPrivateKey=" + payPrivateKey + ", parameter1="
                + parameter1 + ", parameter2=" + parameter2 + ", parameter3="
                + parameter3 + ", thirdMerchId=" + thirdMerchId + ", updateTime="
                + updateTime + ", appIdAyg=" + appIdAyg + ", serviceCompanyId="
                + serviceCompanyId + "]";
    }

}
