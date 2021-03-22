package com.jrmf.domain;

import java.util.List;

public class QbInvoiceRecord {

  private Integer id;

  private String invoiceSerialNo;

  private String customkey;

  private String companyId;

  private Integer status;//开票状态（1.申请待处理，2.申请已受理，3.申请驳回，4.成功，5.挂起）

  private String rejectionReason;

  private String invoiceAmount;

  private Integer invoiceType;

  private Integer billingClass;

  private String invoiceTime;

  private String taxRegistrationNumber;

  private Integer serviceType;

  private Integer taxpayerType;

  private Integer isDiscard;

  private String orderNo;

  private String expressNo;

  private String receiveUser;

  private String companyName;

  private String createTime;

  private String updateTime;

  private Integer invoiceNum;

  private String remark;

  private String accountBankName;

  private String accountNo;

  private String address;

  private String phone;

  private String addUser;

  private String approvalAmount;

  private Integer invoiceMethod;//开票方式1.先充值开票,2.预开票,3.实发开票,4.服务费开票

  private Integer approval;

  private String months;//开票月份

	private String invoiceTypes;//发票类型,实发个税票、服务费票


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getInvoiceSerialNo() {
    return invoiceSerialNo;
  }

  public void setInvoiceSerialNo(String invoiceSerialNo) {
    this.invoiceSerialNo = invoiceSerialNo;
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

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public String getInvoiceAmount() {
    return invoiceAmount;
  }

  public void setInvoiceAmount(String invoiceAmount) {
    this.invoiceAmount = invoiceAmount;
  }

  public Integer getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(Integer invoiceType) {
    this.invoiceType = invoiceType;
  }

  public Integer getBillingClass() {
    return billingClass;
  }

  public void setBillingClass(Integer billingClass) {
    this.billingClass = billingClass;
  }

  public String getInvoiceTime() {
    return invoiceTime;
  }

  public void setInvoiceTime(String invoiceTime) {
    this.invoiceTime = invoiceTime;
  }

  public String getTaxRegistrationNumber() {
    return taxRegistrationNumber;
  }

  public void setTaxRegistrationNumber(String taxRegistrationNumber) {
    this.taxRegistrationNumber = taxRegistrationNumber;
  }

  public Integer getServiceType() {
    return serviceType;
  }

  public void setServiceType(Integer serviceType) {
    this.serviceType = serviceType;
  }

  public Integer getTaxpayerType() {
    return taxpayerType;
  }

  public void setTaxpayerType(Integer taxpayerType) {
    this.taxpayerType = taxpayerType;
  }

  public Integer getIsDiscard() {
    return isDiscard;
  }

  public void setIsDiscard(Integer isDiscard) {
    this.isDiscard = isDiscard;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public String getExpressNo() {
    return expressNo;
  }

  public void setExpressNo(String expressNo) {
    this.expressNo = expressNo;
  }

  public String getReceiveUser() {
    return receiveUser;
  }

  public void setReceiveUser(String receiveUser) {
    this.receiveUser = receiveUser;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
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

  public Integer getInvoiceNum() {
    return invoiceNum;
  }

  public void setInvoiceNum(Integer invoiceNum) {
    this.invoiceNum = invoiceNum;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getAccountBankName() {
    return accountBankName;
  }

  public void setAccountBankName(String accountBankName) {
    this.accountBankName = accountBankName;
  }

  public String getAccountNo() {
    return accountNo;
  }

  public void setAccountNo(String accountNo) {
    this.accountNo = accountNo;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAddUser() {
    return addUser;
  }

  public void setAddUser(String addUser) {
    this.addUser = addUser;
  }

  public String getApprovalAmount() {
    return approvalAmount;
  }

  public void setApprovalAmount(String approvalAmount) {
    this.approvalAmount = approvalAmount;
  }

  public Integer getInvoiceMethod() {
    return invoiceMethod;
  }

  public void setInvoiceMethod(Integer invoiceMethod) {
    this.invoiceMethod = invoiceMethod;
  }

  public Integer getApproval() {
    return approval;
  }

  public void setApproval(Integer approval) {
    this.approval = approval;
  }

  public String getMonths() {
    return months;
  }

  public void setMonths(String months) {
    this.months = months;
  }

	public String getInvoiceTypes() {
		return invoiceTypes;
	}

	public void setInvoiceTypes(String invoiceTypes) {
		this.invoiceTypes = invoiceTypes;
	}
}