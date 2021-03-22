package com.jrmf.taxsettlement.api.service.invoice;

/**
 * @author 种路路
 * @create 2019-10-12 16:00
 * @desc 发票收件人地址
 **/
public class InvoiceAddress {
    /**
     * 收件人姓名
     */
    private String receiverName;
    /**
     * 收件人地址
     */
    private String receiverAddress;
    /**
     * 联系手机号
     */
    private String mobileNo;
    /**
     * 固定电话
     */
    private String fixedTelephone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 增加时间
     */
    private String addTime;
    /**
     * 收件人编号
     */
    private String receive_id;

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

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    @Override
    public String toString() {
        return "InvoiceAddress{" + "receiverName='" + receiverName + '\'' + ", receiverAddress='" + receiverAddress + '\'' + ", mobileNo='" + mobileNo + '\'' + ", fixedTelephone='" + fixedTelephone + '\'' + ", email='" + email + '\'' + ", addTime='" + addTime + '\'' + ", receive_id='" + receive_id + '\'' + '}';
    }

    public String getReceive_id() {
        return receive_id;
    }

    public void setReceive_id(String receive_id) {
        this.receive_id = receive_id;
    }
}
