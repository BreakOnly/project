package com.jrmf.domain;

import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;

/**
 * @auth honglin
 * @time 2020/7/15 下午7:04
 * @desc 实发开票
 */
public class CommissionInvoice {
  private String month;
  private String startMonth;
  private String endMonth;
  private String createTime;
  private String originalId;//商户key
  private String customName;//商户名称
  private String companyId; //服务公司ID
  private String companyName; //服务公司名称
  private String invoiceStatus;//0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票 5 可开票
  private String commissionAmount;//下发总金额
  private String serviceRate;//服务费率
  private String serviceCharge;//服务费
  private String taxRate;//个税税率
  private String taxAmount;//个税税额
  private String totalInvoiceAmount;//待开票总金额
  private String taxInvoiceAmount;//待开实发个税票金额
  private String serviceChargeInvoiceAmount;//待开服务费票金额
  private String totalInvoicedAmount;//已开票总额
  private String taxInvoicedAmount;//已开实发个税票金额
  private String serviceChargeInvoicedAmount;//已开服务费票金额
  private String invoiceType;//类别
  private String operator;//操作账号
  private String invoiceSerialNo;//开票流水号

  private Integer pageNo;
  private Integer pageSize;

  public CommissionInvoice() {
  }

  public String getStartMonth() {
    return startMonth == null ? null : DateUtils.monthToStartTime(startMonth + "-01");
  }

  public void setStartMonth(String startMonth) {
    this.startMonth = startMonth;
  }

  public String getEndMonth() {
    return endMonth == null ? null : DateUtils.monthToEndTime(endMonth + "-01");
  }

  public void setEndMonth(String endMonth) {
    this.endMonth = endMonth;
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

  public String getInvoiceStatus() {
    return invoiceStatus;
  }

  public void setInvoiceStatus(String invoiceStatus) {
    this.invoiceStatus = invoiceStatus;
  }

  public Integer getPageNo() {
    return pageNo;
  }

  public void setPageNo(int pageNo) {
    this.pageNo = pageNo;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public String getCommissionAmount() {
    return commissionAmount;
  }

  public void setCommissionAmount(String commissionAmount) {
    this.commissionAmount = commissionAmount;
  }

  public String getServiceRate() {
    return serviceRate;
  }

  public void setServiceRate(String serviceRate) {
    this.serviceRate = serviceRate;
  }

  public String getServiceCharge() {
    return serviceCharge;
  }

  public void setServiceCharge(String serviceCharge) {
    this.serviceCharge = serviceCharge;
  }

  public String getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(String taxRate) {
    this.taxRate = taxRate;
  }

  public String getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(String taxAmount) {
    this.taxAmount = taxAmount;
  }

  public String getTotalInvoiceAmount() {
    return totalInvoiceAmount == null ? "0.00" : totalInvoiceAmount;
  }

  public void setTotalInvoiceAmount(String totalInvoiceAmount) {
    this.totalInvoiceAmount = totalInvoiceAmount;
  }

  public String getTaxInvoiceAmount() {
    return StringUtil.isEmpty(taxInvoiceAmount) ? "0.00" : taxInvoiceAmount;
  }

  public void setTaxInvoiceAmount(String taxInvoiceAmount) {
    this.taxInvoiceAmount = taxInvoiceAmount;
  }

  public String getServiceChargeInvoiceAmount() {
    return serviceChargeInvoiceAmount;
  }

  public void setServiceChargeInvoiceAmount(String serviceChargeInvoiceAmount) {
    this.serviceChargeInvoiceAmount = serviceChargeInvoiceAmount;
  }

  public String getTotalInvoicedAmount() {
    return StringUtil.isEmpty(totalInvoicedAmount) ? "0" : totalInvoicedAmount;
  }

  public void setTotalInvoicedAmount(String totalInvoicedAmount) {
    this.totalInvoicedAmount = totalInvoicedAmount;
  }

  public String getTaxInvoicedAmount() {
    return StringUtil.isEmpty(taxInvoicedAmount) ? "0" : taxInvoicedAmount;
  }

  public void setTaxInvoicedAmount(String taxInvoicedAmount) {
    this.taxInvoicedAmount = taxInvoicedAmount;
  }

  public String getServiceChargeInvoicedAmount() {
    return serviceChargeInvoicedAmount;
  }

  public void setServiceChargeInvoicedAmount(String serviceChargeInvoicedAmount) {
    this.serviceChargeInvoicedAmount = serviceChargeInvoicedAmount;
  }

  public String getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(String invoiceType) {
    this.invoiceType = invoiceType;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getOriginalId() {
    return originalId;
  }

  public void setOriginalId(String originalId) {
    this.originalId = originalId;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public String getInvoiceSerialNo() {
    return invoiceSerialNo;
  }

  public void setInvoiceSerialNo(String invoiceSerialNo) {
    this.invoiceSerialNo = invoiceSerialNo;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }
}
