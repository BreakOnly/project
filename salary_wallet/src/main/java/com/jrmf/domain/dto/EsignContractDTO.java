package com.jrmf.domain.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class EsignContractDTO implements Serializable {

  private static final long serialVersionUID = -6675930397930055565L;
  private Integer id;

  private String esignPlatform;

  private String accountId;

  private String templateId;

  private String sealId;

  private Byte sealColor;

  private String contractName;

  private Boolean proxyFlag;

  private String contractNo;

  private String contractUrl;

  private Byte signStatus;

  private Date createTime;

  private Date lastUpdateTime;

  private Map<String, String> textValueInfo;

  private boolean msgFlag;

  private String checkCode;

  private String projectCode;

  private String signLocation;

  public String getSignLocation() {
    return signLocation;
  }

  public void setSignLocation(String signLocation) {
    this.signLocation = signLocation;
  }

  public String getProjectCode() {
    return projectCode;
  }

  public void setProjectCode(String projectCode) {
    this.projectCode = projectCode;
  }

  public String getCheckCode() {
    return checkCode;
  }

  public void setCheckCode(String checkCode) {
    this.checkCode = checkCode;
  }

  public boolean getMsgFlag() {
    return msgFlag;
  }

  public void setMsgFlag(boolean msgFlag) {
    this.msgFlag = msgFlag;
  }

  public Map<String, String> getTextValueInfo() {
    return textValueInfo;
  }

  public void setTextValueInfo(Map<String, String> textValueInfo) {
    this.textValueInfo = textValueInfo;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getEsignPlatform() {
    return esignPlatform;
  }

  public void setEsignPlatform(String esignPlatform) {
    this.esignPlatform = esignPlatform;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public String getSealId() {
    return sealId;
  }

  public void setSealId(String sealId) {
    this.sealId = sealId;
  }

  public Byte getSealColor() {
    return sealColor;
  }

  public void setSealColor(Byte sealColor) {
    this.sealColor = sealColor;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public Boolean getProxyFlag() {
    return proxyFlag;
  }

  public void setProxyFlag(Boolean proxyFlag) {
    this.proxyFlag = proxyFlag;
  }

  public String getContractNo() {
    return contractNo;
  }

  public void setContractNo(String contractNo) {
    this.contractNo = contractNo;
  }

  public String getContractUrl() {
    return contractUrl;
  }

  public void setContractUrl(String contractUrl) {
    this.contractUrl = contractUrl;
  }

  public Byte getSignStatus() {
    return signStatus;
  }

  public void setSignStatus(Byte signStatus) {
    this.signStatus = signStatus;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Date lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }
}
