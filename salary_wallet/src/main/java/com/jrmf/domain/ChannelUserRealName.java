package com.jrmf.domain;

public class ChannelUserRealName {
    private Integer id;

    private Integer userId;

    private String userPhoneNo;

    private String name;

    private Integer certType;

    private String certId;

    private String certFrontUrl;

    private String certBackUrl;

    private String orderId;

    private String linkPhoneNo;

    private Integer state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserPhoneNo() {
        return userPhoneNo;
    }

    public void setUserPhoneNo(String userPhoneNo) {
        this.userPhoneNo = userPhoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getCertType() {
        return certType;
    }

    public void setCertType(Integer certType) {
        this.certType = certType;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId == null ? null : certId.trim();
    }

    public String getCertFrontUrl() {
        return certFrontUrl;
    }

    public void setCertFrontUrl(String certFrontUrl) {
        this.certFrontUrl = certFrontUrl == null ? null : certFrontUrl.trim();
    }

    public String getCertBackUrl() {
        return certBackUrl;
    }

    public void setCertBackUrl(String certBackUrl) {
        this.certBackUrl = certBackUrl == null ? null : certBackUrl.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getLinkPhoneNo() {
        return linkPhoneNo;
    }

    public void setLinkPhoneNo(String linkPhoneNo) {
        this.linkPhoneNo = linkPhoneNo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}