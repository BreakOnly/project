package com.jrmf.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-12 15:34
 * @desc 用户签约协议状态记录
 **/
public class UsersAgreement implements Serializable {

  /**
   * 主键
   */
  private int id;
  /**
   * 登记业务类型
   */
  private int regType;
  /**
   * 用户信息表ID
   */
  private String userId;
  /**
   * 签约协议模板表ID
   */
  private String agreementTemplateId;
  /**
   * 签约协议名称
   */
  private String agreementName;
  /**
   * 签约协议模版url
   */
  private String agreementTemplateURL;
  /**
   * 用户姓名
   */
  private String userName;
  /**
   * 用户证件号
   */
  private String certId;
  /**
   * 证件类型 1-身份证 2-港澳台通行证 3-护照 4-军官证
   */
  private String documentType;
  /**
   * 签约用户联系手机号
   */
  private String mobilePhone;
  /**
   * 签约状态 1-创建 2-签约处理中 3-签约待审核 4-签约失败 5-签约成功
   */
  private int signStatus;
  /**
   * 签约状态描述
   */
  private String signStatusDes;
  /**
   * 签约步骤 0-创建 1-签约协议模板成功 2-签约协议模板失败
   */
  private int signStep;
  /**
   * 上传身份证 0-创建 1-上传身份证成功 2-上传身份证失败
   */
  private int documentStep;
  /**
   * 魔方商户号
   */
  private String originalId;
  /**
   * 平台号  （ps：爱员工）
   */
  private String merchantId;
  /**
   * 服务公司编号
   */
  private String companyId;
  /**
   * 签约第三方商户ID (ps : 爱员工appid)
   */
  private String thirdMerchId;
  /**
   * 签约第三方签约模板ID
   */
  private String thirdTemplateId;
  /**
   * 签约方式 1-本地人工审核签约 2-调用第三方接口静默签约
   */
  private String agreementType;
  /**
   * 用户签约协议电子版URL
   */
  private String agreementURL;
  /**
   * 魔方签约协议号
   */
  private String agreementNo;
  /**
   * 第三方通道签约协议号
   */
  private String thirdAgreementNo;
  /**
   * 调用流水号
   */
  private String orderNo;
  /**
   * 三方流水号
   */
  private String thirdNo;
  /**
   * 用户证件影像保存URL地址1
   */
  private String imageURLA;
  /**
   * 用户证件影像保存URL地址2
   */
  private String imageURLB;
  /**
   * 用户证件影像保存URL地址3
   */
  private String imageURLC;
  /**
   * 用户证件影像保存URL地址4
   */
  private String imageURLD;
  /**
   * 创建时间
   */
  private String createTime;
  /**
   * 最后更新时间
   */
  private String lastUpdateTime;
  /**
   * 签约来源 0-H5、1-API、2-迁移签约、3-批次下发共享签约、4-平台发起共享签约、5-后台批量导入签约
   */
  private Integer signSubmitType;

  /**
   * 认证等级， L0:未认证 ， L1:本地认证，L2:二要素认证，L3:三要素认证,L4：四要素认证
   */
  private String checkLevel;

  /**
   * 是否通过证照认证：0-否，1-是
   */
  private Integer checkByPhoto;

  /**
   * 白名单 0，校验 1，不校验
   */
  private int whiteList;
  /**
   * 审批人
   */
  private String approver;

  /**
   * 预留
   */
  private String preparedA;
  /**
   * 预留
   */
  private String preparedB;

  private String customName;
  private String companyName;
  private transient String domainName;
  private transient String serverName;
  private transient String htmlTemplate;
  private transient String serviceTypeNames;
  private transient String customkey;
  /**
   * 银行卡号
   */
  private String bankCardNo;

  public String getBankCardNo() {
    return bankCardNo;
  }

  public void setBankCardNo(String bankCardNo) {
    this.bankCardNo = bankCardNo;
  }

  public String getCustomkey() {
    return customkey;
  }

  public void setCustomkey(String customkey) {
    this.customkey = customkey;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getHtmlTemplate() {
    return htmlTemplate;
  }

  public void setHtmlTemplate(String htmlTemplate) {
    this.htmlTemplate = htmlTemplate;
  }

  public String getServiceTypeNames() {
    return serviceTypeNames;
  }

  public void setServiceTypeNames(String serviceTypeNames) {
    this.serviceTypeNames = serviceTypeNames;
  }


  public int getDocumentStep() {
    return documentStep;
  }

  public void setDocumentStep(int documentStep) {
    this.documentStep = documentStep;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getRegType() {
    return regType;
  }

  public void setRegType(int regType) {
    this.regType = regType;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAgreementTemplateId() {
    return agreementTemplateId;
  }

  public void setAgreementTemplateId(String agreementTemplateId) {
    this.agreementTemplateId = agreementTemplateId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getCertId() {
    return certId;
  }

  public void setCertId(String certId) {
    this.certId = certId;
  }

  public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public int getSignStatus() {
    return signStatus;
  }

  public void setSignStatus(int signStatus) {
    this.signStatus = signStatus;
  }

  public int getSignStep() {
    return signStep;
  }

  public void setSignStep(int signStep) {
    this.signStep = signStep;
  }

  public String getSignStatusDes() {
    return signStatusDes;
  }

  public void setSignStatusDes(String signStatusDes) {
    this.signStatusDes = signStatusDes;
  }

  public String getOriginalId() {
    return originalId;
  }

  public void setOriginalId(String originalId) {
    this.originalId = originalId;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public String getThirdMerchId() {
    return thirdMerchId;
  }

  public void setThirdMerchId(String thirdMerchId) {
    this.thirdMerchId = thirdMerchId;
  }

  public String getThirdTemplateId() {
    return thirdTemplateId;
  }

  public void setThirdTemplateId(String thirdTemplateId) {
    this.thirdTemplateId = thirdTemplateId;
  }

  public String getAgreementType() {
    return agreementType;
  }

  public void setAgreementType(String agreementType) {
    this.agreementType = agreementType;
  }

  public String getAgreementURL() {
    return agreementURL;
  }

  public void setAgreementURL(String agreementURL) {
    this.agreementURL = agreementURL;
  }

  public String getAgreementNo() {
    return agreementNo;
  }

  public void setAgreementNo(String agreementNo) {
    this.agreementNo = agreementNo;
  }

  public String getThirdAgreementNo() {
    return thirdAgreementNo;
  }

  public void setThirdAgreementNo(String thirdAgreementNo) {
    this.thirdAgreementNo = thirdAgreementNo;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public String getThirdNo() {
    return thirdNo;
  }

  public void setThirdNo(String thirdNo) {
    this.thirdNo = thirdNo;
  }

  public String getImageURLA() {
    return imageURLA;
  }

  public void setImageURLA(String imageURLA) {
    this.imageURLA = imageURLA;
  }

  public String getImageURLB() {
    return imageURLB;
  }

  public void setImageURLB(String imageURLB) {
    this.imageURLB = imageURLB;
  }

  public String getImageURLC() {
    return imageURLC;
  }

  public void setImageURLC(String imageURLC) {
    this.imageURLC = imageURLC;
  }

  public String getImageURLD() {
    return imageURLD;
  }

  public void setImageURLD(String imageURLD) {
    this.imageURLD = imageURLD;
  }

  public String getPreparedA() {
    return preparedA;
  }

  public void setPreparedA(String preparedA) {
    this.preparedA = preparedA;
  }

  public String getPreparedB() {
    return preparedB;
  }

  public void setPreparedB(String preparedB) {
    this.preparedB = preparedB;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(String lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getAgreementName() {
    return agreementName;
  }

  public void setAgreementName(String agreementName) {
    this.agreementName = agreementName;
  }

  public String getAgreementTemplateURL() {
    return agreementTemplateURL;
  }

  public void setAgreementTemplateURL(String agreementTemplateURL) {
    this.agreementTemplateURL = agreementTemplateURL;
  }


  public Integer getSignSubmitType() {
    return signSubmitType;
  }

  public void setSignSubmitType(Integer signSubmitType) {
    this.signSubmitType = signSubmitType;
  }

  @Override
  public String toString() {
    return "UsersAgreement{" +
        "id=" + id +
        ", regType=" + regType +
        ", userId='" + userId + '\'' +
        ", agreementTemplateId='" + agreementTemplateId + '\'' +
        ", agreementName='" + agreementName + '\'' +
        ", agreementTemplateURL='" + agreementTemplateURL + '\'' +
        ", userName='" + userName + '\'' +
        ", certId='" + certId + '\'' +
        ", documentType='" + documentType + '\'' +
        ", mobilePhone='" + mobilePhone + '\'' +
        ", signStatus=" + signStatus +
        ", signStatusDes='" + signStatusDes + '\'' +
        ", signStep=" + signStep +
        ", documentStep=" + documentStep +
        ", originalId='" + originalId + '\'' +
        ", merchantId='" + merchantId + '\'' +
        ", companyId='" + companyId + '\'' +
        ", thirdMerchId='" + thirdMerchId + '\'' +
        ", thirdTemplateId='" + thirdTemplateId + '\'' +
        ", agreementType='" + agreementType + '\'' +
        ", agreementURL='" + agreementURL + '\'' +
        ", agreementNo='" + agreementNo + '\'' +
        ", thirdAgreementNo='" + thirdAgreementNo + '\'' +
        ", orderNo='" + orderNo + '\'' +
        ", thirdNo='" + thirdNo + '\'' +
        ", imageURLA='" + imageURLA + '\'' +
        ", imageURLB='" + imageURLB + '\'' +
        ", imageURLC='" + imageURLC + '\'' +
        ", imageURLD='" + imageURLD + '\'' +
        ", createTime='" + createTime + '\'' +
        ", lastUpdateTime='" + lastUpdateTime + '\'' +
        ", signSubmitType=" + signSubmitType +
        ", checkLevel='" + checkLevel + '\'' +
        ", checkByPhoto=" + checkByPhoto +
        ", whiteList=" + whiteList +
        ", approver='" + approver + '\'' +
        ", preparedA='" + preparedA + '\'' +
        ", preparedB='" + preparedB + '\'' +
        '}';
  }

  public String getApprover() {
    return approver;
  }

  public void setApprover(String approver) {
    this.approver = approver;
  }

  public int getWhiteList() {
    return whiteList;
  }

  public void setWhiteList(int whiteList) {
    this.whiteList = whiteList;
  }

  public String getCheckLevel() {
    return checkLevel;
  }

  public void setCheckLevel(String checkLevel) {
    this.checkLevel = checkLevel;
  }


  public Integer getCheckByPhoto() {
    return checkByPhoto;
  }

  public void setCheckByPhoto(Integer checkByPhoto) {
    this.checkByPhoto = checkByPhoto;
  }

  public String getCustomName() {
    return customName;
  }

  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public void map2Object(Map<String, Object> map) {
    this.setImageURLA(map.get("imageURLA") == null ? "" : (String) map.get("imageURLA"));
    this.setImageURLB(map.get("imageURLB") == null ? "" : (String) map.get("imageURLB"));
    this.setUserName(map.get("userName") == null ? "" : (String) map.get("userName"));
    this.setCertId(map.get("certId") == null ? "" : (String) map.get("certId"));
    this.setCompanyId(map.get("companyId") == null ? "" : (String) map.get("companyId"));
    this.setCustomkey(map.get("customkey") == null ? "" : (String) map.get("customkey"));
    this.setCustomName(map.get("customName") == null ? "" : (String) map.get("customName"));
    this.setCompanyName(map.get("companyName") == null ? "" : (String) map.get("companyName"));
    this.setMobilePhone(map.get("mobilePhone") == null ? "" : (String) map.get("mobilePhone"));
    this.setHtmlTemplate(map.get("htmlTemplate") == null ? "newpay_henanbaoling_agreement"
        : (String) map.get("htmlTemplate"));
    this.setLastUpdateTime(
        map.get("lastUpdateTime") == null ? "" : (String) map.get("lastUpdateTime"));
  }
}
