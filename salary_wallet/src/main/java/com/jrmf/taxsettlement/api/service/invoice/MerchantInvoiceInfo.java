package com.jrmf.taxsettlement.api.service.invoice;

/**
 * @author 种路路
 * @create 2019-10-12 18:28
 * @desc 商户开票信息
 **/
public class MerchantInvoiceInfo {
    /**
     * 下发公司id
     */
    private String transferCorpId;
    /**
     * 纳税人类型
     * 1.一般纳税人
     2.小规模纳税人
     */
    private String taxpayerType;

    /**
     * 开票类型
     * 1.普通发票
     2.增值税专用发票
     */
    private String invoiceType;
    /**
     * 开票类目
     */
    private String billingClass;
    /**
     * 开票信息状态
     * 0.待确认
     * 1.确认
     * 2.驳回
     */
    private String status;
    /**
     * 增加时间
     */
    private String addTime;
    /**
     * 开票信息编号
     */
    private String infoId;
    /**
     * 纳税人识别号
     */
    private String taxRegistrationNumber;
    /**
     * 开户行名称
     */
    private  String accountBankName;
    /**
     * 电话
     */
    private  String phone;
    /**
     * 地址
     */
    private  String address;
    /**
     * 开户行账号
     */
    private  String accountNo;

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getTaxpayerType() {
        return taxpayerType;
    }

    public void setTaxpayerType(String taxpayerType) {
        this.taxpayerType = taxpayerType;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getBillingClass() {
        return billingClass;
    }

    public void setBillingClass(String billingClass) {
        this.billingClass = billingClass;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getInfoId() {
        return infoId;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    @Override
    public String toString() {
        return "MerchantInvoiceInfo{" + "transferCorpId='" + transferCorpId + '\'' + ", taxpayerType='" + taxpayerType + '\'' + ", invoiceType='" + invoiceType + '\'' + ", billingClass='" + billingClass + '\'' + ", status='" + status + '\'' + ", addTime='" + addTime + '\'' + ", infoId='" + infoId + '\'' + ", taxRegistrationNumber='" + taxRegistrationNumber + '\'' + ", accountBankName='" + accountBankName + '\'' + ", phone='" + phone + '\'' + ", address='" + address + '\'' + ", accountNo='" + accountNo + '\'' + '}';
    }
}
