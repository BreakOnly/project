package com.jrmf.domain.vo;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/13 17:00
 * Version:1.0
 *
 * @author guoto
 */

public class CustomInvoiceInfoVO {

    private Integer id;
    private String customkey;
    private String invoicePhone;
    private String fixedTelephone;
    private String addressTitle;
    private String email;
    private String invoiceUserName;
    private String invoiceAddress;
    private Integer status;
    private String StatusDesc;
    private String createTime;
    private String updateTime;
    private Integer isDefault;
    private String isDefaultDesc;
    private String addUser;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getInvoicePhone() {
        return invoicePhone;
    }

    public void setInvoicePhone(String invoicePhone) {
        this.invoicePhone = invoicePhone;
    }

    public String getFixedTelephone() {
        return fixedTelephone;
    }

    public void setFixedTelephone(String fixedTelephone) {
        this.fixedTelephone = fixedTelephone;
    }

    public String getAddressTitle() {
        return addressTitle;
    }

    public void setAddressTitle(String addressTitle) {
        this.addressTitle = addressTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInvoiceUserName() {
        return invoiceUserName;
    }

    public void setInvoiceUserName(String invoiceUserName) {
        this.invoiceUserName = invoiceUserName;
    }

    public String getInvoiceAddress() {
        return invoiceAddress;
    }

    public void setInvoiceAddress(String invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return StatusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        StatusDesc = statusDesc;
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

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public String getIsDefaultDesc() {
        return isDefaultDesc;
    }

    public void setIsDefaultDesc(String isDefaultDesc) {
        this.isDefaultDesc = isDefaultDesc;
    }

    public String getAddUser() {
		return addUser;
	}

	public void setAddUser(String addUser) {
		this.addUser = addUser;
	}

	@Override
    public String toString() {
        return "CustomInvoiceInfoVO{" +
                "id=" + id +
                ", customkey='" + customkey + '\'' +
                ", invoicePhone='" + invoicePhone + '\'' +
                ", fixedTelephone='" + fixedTelephone + '\'' +
                ", addressTitle='" + addressTitle + '\'' +
                ", email='" + email + '\'' +
                ", invoiceUserName='" + invoiceUserName + '\'' +
                ", invoiceAddress='" + invoiceAddress + '\'' +
                ", status=" + status +
                ", StatusDesc='" + StatusDesc + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", isDefault=" + isDefault +
                ", isDefaultDesc='" + isDefaultDesc + '\'' +
                '}';
    }
}
