package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-06-19 11:20
 * @desc
 **/
public class AddMerchantInvoiceInfoServiceParams extends ActionParams {
    /**
     * 下发公司id
     */
    @NotNull
    private  String transferCorpId;
    /**
     * 开票类型
     * 1.普通发票
     2.增值税专用发票
     */
    @NotNull
    private  String invoiceType;
    /**
     * 开票类目
     */
    @NotNull
    private  String billingClass;
    /**
     * 税务登记证扫描件
     */
    private  String taxPicUrl;
    /**
     * 一般纳税人资格认证扫描件
     */
    private  String taxpayerPicUrl;
    /**
     * 纳税人识别号
     */
    @NotNull
    private  String taxRegistrationNumber;
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

    @Override
    public String toString() {
        return "AddMerchantInvoiceInfoServiceParams{" + "transferCorpId='" + transferCorpId + '\'' + ", invoiceType='" + invoiceType + '\'' + ", billingClass='" + billingClass + '\'' + ", taxPicUrl='" + taxPicUrl + '\'' + ", taxpayerPicUrl='" + taxpayerPicUrl + '\'' + ", taxRegistrationNumber='" + taxRegistrationNumber + '\'' + ", accountBankName='" + accountBankName + '\'' + ", phone='" + phone + '\'' + ", address='" + address + '\'' + ", accountNo='" + accountNo + '\'' + '}';
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

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
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

    public String getTaxPicUrl() {
        return taxPicUrl;
    }

    public void setTaxPicUrl(String taxPicUrl) {
        this.taxPicUrl = taxPicUrl;
    }

    public String getTaxpayerPicUrl() {
        return taxpayerPicUrl;
    }

    public void setTaxpayerPicUrl(String taxpayerPicUrl) {
        this.taxpayerPicUrl = taxpayerPicUrl;
    }
}
