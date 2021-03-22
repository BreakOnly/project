package com.jrmf.domain;

import java.util.Date;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/13 16:48
 * Version:1.0
 *
 * @author guoto
 */
public class CustomInvoiceInfoDO {
    private Integer id;
    private String customkey;
    private String invoicePhone;
    private String fixedTelephone;
    private String addressTitle;
    private String email;
    private String invoiceUserName;
    private String invoiceAddress;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer isDefault;
    private String addUser;
    
    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    
    public String getAddUser() {
		return addUser;
	}

	public void setAddUser(String addUser) {
		this.addUser = addUser;
	}

	@Override
    public String toString() {
        return "CustomInvoiceInfoDO{" +
                "id=" + id +
                ", customkey='" + customkey + '\'' +
                ", invoicePhone='" + invoicePhone + '\'' +
                ", fixedTelephone='" + fixedTelephone + '\'' +
                ", addressTitle='" + addressTitle + '\'' +
                ", email='" + email + '\'' +
                ", invoiceUserName='" + invoiceUserName + '\'' +
                ", invoiceAddress='" + invoiceAddress + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isDefault=" + isDefault +
                '}';
    }
}
