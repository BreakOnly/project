package com.jrmf.taxsettlement.api.service.invoice;

/**
 * @author 种路路
 * @create 2019-10-14 16:41
 * @desc 开票历史记录
 **/
public class InvoiceHistory {
    /**
     * 开票流水号
     */
    private String invoiceSerialNo;
    /**
     * 公司名称(发票抬头)
     */
    private String companyName;
    /**
     * 纳税人类型
     * 1.一般纳税人
     2.小规模纳税人
     */
    private String taxpayerType;
    /**
     * 税务登记号
     */
    private String taxRegistrationNumber;
    /**
     * 开户银行名称
     */
    private String accountBankName;
    /**
     * 开户账号
     */
    private String accountNo;
    /**
     * 地址
     */
    private String address;
    /**
     * 电话
     */
    private String phone;
    /**
     * 开票类目
     */
    private String billingClassName;
    /**
     * 开票金额
     */
    private String invoiceAmount;
    /**
     * 开票类型
     * 1.普通发票
     2.增值税专用发票
     */
    private String invoiceType;
    /**
     * 收递人
     */
    private String receiveUser;
    /**
     * 快递单号
     */
    private String expressNo;
    /**
     * 开票日期
     */
    private String invoiceTime;
    /**
     * 服务公司（开票方）
     */
    private String serviceName;
    /**
     * 服务类型
     */
    private String serviceTypeName;
    /**
     * 发票对应充值流水
     */
    private String orderNo;
    /**
     * 申请时间
     */
    private String createTime;
    /**
     * 开票状态
     * 1.申请待处理，2.申请已受理，3.申请驳回，4.成功，5.挂起
     */
    private String status;
    /**
     * 驳回原因
     */
    private String rejectionReason;
    /**
     * 作废标志
     * 0,否
     1,是
     */
    private String isDiscard;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 发票凭证
     * 多张凭证用,隔开
     */
    private String invoicePicUrl;

    @Override
    public String toString() {
        return "InvoiceHistory{" + "invoiceSerialNo='" + invoiceSerialNo + '\'' + ", companyName='" + companyName + '\'' + ", taxpayerType='" + taxpayerType + '\'' + ", taxRegistrationNumber='" + taxRegistrationNumber + '\'' + ", accountBankName='" + accountBankName + '\'' + ", accountNo='" + accountNo + '\'' + ", address='" + address + '\'' + ", phone='" + phone + '\'' + ", billingClassName='" + billingClassName + '\'' + ", invoiceAmount='" + invoiceAmount + '\'' + ", invoiceType='" + invoiceType + '\'' + ", receiveUser='" + receiveUser + '\'' + ", expressNo='" + expressNo + '\'' + ", invoiceTime='" + invoiceTime + '\'' + ", serviceName='" + serviceName + '\'' + ", serviceTypeName='" + serviceTypeName + '\'' + ", orderNo='" + orderNo + '\'' + ", createTime='" + createTime + '\'' + ", status='" + status + '\'' + ", rejectionReason='" + rejectionReason + '\'' + ", isDiscard='" + isDiscard + '\'' + ", updateTime='" + updateTime + '\'' + ", invoicePicUrl='" + invoicePicUrl + '\'' + '}';
    }

    public String getInvoiceSerialNo() {
        return invoiceSerialNo;
    }

    public void setInvoiceSerialNo(String invoiceSerialNo) {
        this.invoiceSerialNo = invoiceSerialNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxpayerType() {
        return taxpayerType;
    }

    public void setTaxpayerType(String taxpayerType) {
        this.taxpayerType = taxpayerType;
    }

    public String getTaxRegistrationNumber() {
        return taxRegistrationNumber;
    }

    public void setTaxRegistrationNumber(String taxRegistrationNumber) {
        this.taxRegistrationNumber = taxRegistrationNumber;
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

    public String getBillingClassName() {
        return billingClassName;
    }

    public void setBillingClassName(String billingClassName) {
        this.billingClassName = billingClassName;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getInvoiceTime() {
        return invoiceTime;
    }

    public void setInvoiceTime(String invoiceTime) {
        this.invoiceTime = invoiceTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    public void setServiceTypeName(String serviceTypeName) {
        this.serviceTypeName = serviceTypeName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getIsDiscard() {
        return isDiscard;
    }

    public void setIsDiscard(String isDiscard) {
        this.isDiscard = isDiscard;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getInvoicePicUrl() {
        return invoicePicUrl;
    }

    public void setInvoicePicUrl(String invoicePicUrl) {
        this.invoicePicUrl = invoicePicUrl;
    }
}
