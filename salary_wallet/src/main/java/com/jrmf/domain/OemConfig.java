package com.jrmf.domain;


/**
 * @author 种路路
 * @create 2019-03-12 14:25
 * @desc oem配置信息
 **/
public class OemConfig {
    /**
     * 主键
     */
    private Integer id;
    /**
     * oem平台名称
     */
    private String oemName;
    /**
     * b端域名url
     */
    private String portalDomain;
    /**
     * c端域名url
     */
    private String clientDomain;
    /**
     * b端首页图片
     */
    private String portalLogo;
    /**
     * b端icp备案信息
     */
    private String protalIcp;
    /**
     * b端首页标题栏
     */
    private String portalTitle;
    /**
     * 欢迎页图片
     */
    private String welcomePicture;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;
    /**
     * 是否启用 0 禁用   1启用
     */
    private String status;
    /**
     * c端icp备案
     */
    private String clientIcp;
    /**
     * c端logo
     */
    private String clientLogo;
    /**
     * 短信签名
     */
    private String smsSignature;
    /**
     * 客服电话
     */
    private String serviceHotline;
    /**
     * 是否发送短信
     1.发送
     0.不发
     */
    private Integer smsStatus;

    /**
    * @Description 短链接
    **/
    private String  shortClientDomain;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOemName() {
        return oemName;
    }

    public void setOemName(String oemName) {
        this.oemName = oemName;
    }

    public String getPortalDomain() {
        return portalDomain;
    }

    public void setPortalDomain(String portalDomain) {
        this.portalDomain = portalDomain;
    }

    public String getClientDomain() {
        return clientDomain;
    }

    public void setClientDomain(String clientDomain) {
        this.clientDomain = clientDomain;
    }

    public String getPortalLogo() {
        return portalLogo;
    }

    public void setPortalLogo(String portalLogo) {
        this.portalLogo = portalLogo;
    }

    public String getProtalIcp() {
        return protalIcp;
    }

    public void setProtalIcp(String protalIcp) {
        this.protalIcp = protalIcp;
    }

    public String getPortalTitle() {
        return portalTitle;
    }

    public void setPortalTitle(String portalTitle) {
        this.portalTitle = portalTitle;
    }

    public String getWelcomePicture() {
        return welcomePicture;
    }

    public void setWelcomePicture(String welcomePicture) {
        this.welcomePicture = welcomePicture;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientIcp() {
        return clientIcp;
    }

    public void setClientIcp(String clientIcp) {
        this.clientIcp = clientIcp;
    }

    public String getClientLogo() {
        return clientLogo;
    }

    public void setClientLogo(String clientLogo) {
        this.clientLogo = clientLogo;
    }

    public String getSmsSignature() {
        return smsSignature;
    }

    public void setSmsSignature(String smsSignature) {
        this.smsSignature = smsSignature;
    }

    public String getServiceHotline() {
        return serviceHotline;
    }

    public Integer getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(Integer smsStatus) {
        this.smsStatus = smsStatus;
    }

    public String getShortClientDomain() {
        return shortClientDomain;
    }

    public void setShortClientDomain(String shortClientDomain) {
        this.shortClientDomain = shortClientDomain;
    }


    @Override
    public String toString() {
        return "OemConfig{" + "id=" + id + ", oemName='" + oemName + '\'' + ", portalDomain='" + portalDomain + '\'' + ", clientDomain='" + clientDomain + '\'' + ", portalLogo='" + portalLogo + '\'' + ", protalIcp='" + protalIcp + '\'' + ", portalTitle='" + portalTitle + '\'' + ", welcomePicture='" + welcomePicture + '\'' + ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\'' + ", status='" + status + '\'' + ", clientIcp='" + clientIcp + '\'' + ", clientLogo='" + clientLogo + '\'' + ", smsSignature='" + smsSignature + '\'' + ", serviceHotline='" + serviceHotline + '\'' + ", smsStatus=" + smsStatus + '}';
    }

    public void setServiceHotline(String serviceHotline) {
        this.serviceHotline = serviceHotline;
    }
}
