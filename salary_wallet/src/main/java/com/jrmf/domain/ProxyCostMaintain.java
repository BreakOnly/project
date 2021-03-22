package com.jrmf.domain;

import java.io.Serializable;

/**
 * @Title: ProxyCostMaintain
 * @Description: 代理商成本维护类
 * @create 2019/10/31 10:08
 */
public class ProxyCostMaintain implements Serializable {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 代理商唯一标识
     */
    private String customkey;

    /**
     * 代理商名称
     */
    private String proxyName;

    /**
     * 所属业务平台
     */
    private String businessPlatform;

    /**
     * 代理商级别
     */
    private int proxyLevel;

    /**
     * 服务公司id
     */
    private int companyId;

    /**
     * 服务公司名称
     */
    private String companyName;

    /**
     * 代理商成本费率
     */
    private String proxyFeeRate;
    /**
     * 代理商统计范围 0:本级商户 1：本级商户和下级代理商户
     */
    private int proxyType;

    /**
     * 统计计算方式 0:下级代理差额成本统计 1：本级代理商直接成本统计
     */
    private int countType;

    /**
     * 报税档位id
     */
    private int netfileId;

    /**
     * 上级代理商唯一标识
     */
    private String masterCustomkey;

    /**
     * 上级代理商名称
     */
    private String masterName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 代理商状态 1：正常 2：废弃
     */
    private int status;
    /**
     * 档位金额报税标签 1:小金额 2：大金额
     */
    private int gearLabel;

    private String amountStart;

    private String amountEnd;

    private String operator;

    private String gearPosition;

    private String phoneNo;

    private int contentLevel;

    private int parentId;

    private String customname;

    private String merchantId;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomname() {
        return customname;
    }

    public void setCustomname(String customname) {
        this.customname = customname;
    }

    public String getAmountStart() {
        return amountStart;
    }

    public void setAmountStart(String amountStart) {
        this.amountStart = amountStart;
    }

    public String getAmountEnd() {
        return amountEnd;
    }

    public void setAmountEnd(String amountEnd) {
        this.amountEnd = amountEnd;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getGearPosition() {
        return gearPosition;
    }

    public void setGearPosition(String gearPosition) {
        this.gearPosition = gearPosition;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public int getContentLevel() {
        return contentLevel;
    }

    public void setContentLevel(int contentLevel) {
        this.contentLevel = contentLevel;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getGearLabel() {
        return gearLabel;
    }

    public void setGearLabel(int gearLabel) {
        this.gearLabel = gearLabel;
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

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public int getProxyLevel() {
        return proxyLevel;
    }

    public void setProxyLevel(int proxyLevel) {
        this.proxyLevel = proxyLevel;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProxyFeeRate() {
        return proxyFeeRate;
    }

    public void setProxyFeeRate(String proxyFeeRate) {
        this.proxyFeeRate = proxyFeeRate;
    }

    public int getProxyType() {
        return proxyType;
    }

    public void setProxyType(int proxyType) {
        this.proxyType = proxyType;
    }

    public int getCountType() {
        return countType;
    }

    public void setCountType(int countType) {
        this.countType = countType;
    }

    public int getNetfileId() {
        return netfileId;
    }

    public void setNetfileId(int netfileId) {
        this.netfileId = netfileId;
    }

    public String getMasterCustomkey() {
        return masterCustomkey;
    }

    public void setMasterCustomkey(String masterCustomkey) {
        this.masterCustomkey = masterCustomkey;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBusinessPlatform() {
        return businessPlatform;
    }

    public void setBusinessPlatform(String businessPlatform) {
        this.businessPlatform = businessPlatform;
    }

    @Override
    public String toString() {
        return "ProxyCostMaintain{" +
                "id=" + id +
                ", customkey='" + customkey + '\'' +
                ", proxyName='" + proxyName + '\'' +
                ", businessPlatform='" + businessPlatform + '\'' +
                ", proxyLevel=" + proxyLevel +
                ", companyId=" + companyId +
                ", companyName='" + companyName + '\'' +
                ", proxyFeeRate='" + proxyFeeRate + '\'' +
                ", proxyType=" + proxyType +
                ", countType=" + countType +
                ", netfileId=" + netfileId +
                ", masterCustomkey='" + masterCustomkey + '\'' +
                ", masterName='" + masterName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", status=" + status +
                ", gearLabel=" + gearLabel +
                ", amountStart='" + amountStart + '\'' +
                ", amountEnd='" + amountEnd + '\'' +
                ", operator='" + operator + '\'' +
                ", gearPosition='" + gearPosition + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", contentLevel=" + contentLevel +
                ", parentId=" + parentId +
                ", customname='" + customname + '\'' +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}
