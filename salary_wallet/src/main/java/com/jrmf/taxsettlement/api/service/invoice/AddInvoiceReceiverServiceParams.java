package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author chonglulu
 * @time: 2019年10月12日10:24:37
 */
public class AddInvoiceReceiverServiceParams extends ActionParams {
    /**
     * 收件人姓名
     */
    @NotNull
    private String receiverName;
    /**
     * 收件人地址
     */
    @NotNull
    private String receiverAddress;
    /**
     * 联系手机号
     */
    @NotNull
    private String mobileNo;
    /**
     * 固定电话
     */
    private String fixedTelephone;
    /**
     * 邮箱
     */
    private String email;

    @Override
    public String toString() {
        return "AddInvoiceReceiverServiceParams{" + "receiverName='" + receiverName + '\'' + ", receiverAddress='" + receiverAddress + '\'' + ", mobileNo='" + mobileNo + '\'' + ", fixedTelephone='" + fixedTelephone + '\'' + ", email='" + email + '\'' + '}';
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getFixedTelephone() {
        return fixedTelephone;
    }

    public void setFixedTelephone(String fixedTelephone) {
        this.fixedTelephone = fixedTelephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
