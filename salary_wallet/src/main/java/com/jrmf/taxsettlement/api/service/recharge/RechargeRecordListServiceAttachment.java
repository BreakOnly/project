package com.jrmf.taxsettlement.api.service.recharge;

public class RechargeRecordListServiceAttachment {
  /**
   * 到账金额
   */
  private String amount;
  /**
   * 充值金额
   */
  private String balanceAmount;
  /**
   * 手续费
   */
  private String serviceFee;
  /**
   * 充值状态： 0000 成功，0001 待确认，0002 失败
   */
  private String dealStatus;
  /**
   * 充值结果描述
   */
  private String dealStatusMsg;
  /**
   * 到账时间
   */
  private String accountTime;
  /**
   * 到账时间
   */
  private String invoiceAmount;
  /**
   * 开票处理中金额
   */
  private String invoiceingAmount;
  /**
   * 待开票余额
   */
  private String uninvoiceAmount;
  /**
   * 开票状态：0，未开票，1，部分开票，2，完成开票，3，开票处理中
   */
  private String invoiceStatus;
  /**
   * 智税通订单号
   */
  private String orderNo;
  /**
   * 渠道订单号
   */
  private String customOrderNo;
  /**
   * 备注
   */
  private String remark;

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

  public String getServiceFee() {
    return serviceFee;
  }

  public void setServiceFee(String serviceFee) {
    this.serviceFee = serviceFee;
  }

  public String getDealStatus() {
    return dealStatus;
  }

  public void setDealStatus(String dealStatus) {
    this.dealStatus = dealStatus;
  }

  public String getDealStatusMsg() {
    return dealStatusMsg;
  }

  public void setDealStatusMsg(String dealStatusMsg) {
    this.dealStatusMsg = dealStatusMsg;
  }

  public String getAccountTime() {
    return accountTime;
  }

  public void setAccountTime(String accountTime) {
    this.accountTime = accountTime;
  }

  public String getInvoiceAmount() {
    return invoiceAmount;
  }

  public void setInvoiceAmount(String invoiceAmount) {
    this.invoiceAmount = invoiceAmount;
  }

  public String getInvoiceingAmount() {
    return invoiceingAmount;
  }

  public void setInvoiceingAmount(String invoiceingAmount) {
    this.invoiceingAmount = invoiceingAmount;
  }

  public String getUninvoiceAmount() {
    return uninvoiceAmount;
  }

  public void setUninvoiceAmount(String uninvoiceAmount) {
    this.uninvoiceAmount = uninvoiceAmount;
  }

  public String getInvoiceStatus() {
    return invoiceStatus;
  }

  public void setInvoiceStatus(String invoiceStatus) {
    this.invoiceStatus = invoiceStatus;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public String getCustomOrderNo() {
    return customOrderNo;
  }

  public void setCustomOrderNo(String customOrderNo) {
    this.customOrderNo = customOrderNo;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  @Override
  public String toString() {
    return "RechargeRecordListServiceAttachment{" +
        "amount='" + amount + '\'' +
        ", balanceAmount='" + balanceAmount + '\'' +
        ", serviceFee='" + serviceFee + '\'' +
        ", dealStatus='" + dealStatus + '\'' +
        ", dealStatusMsg='" + dealStatusMsg + '\'' +
        ", accountTime='" + accountTime + '\'' +
        ", invoiceAmount='" + invoiceAmount + '\'' +
        ", invoiceingAmount='" + invoiceingAmount + '\'' +
        ", uninvoiceAmount='" + uninvoiceAmount + '\'' +
        ", invoiceStatus='" + invoiceStatus + '\'' +
        ", orderNo='" + orderNo + '\'' +
        ", customOrderNo='" + customOrderNo + '\'' +
        ", remark='" + remark + '\'' +
        '}';
  }
}
