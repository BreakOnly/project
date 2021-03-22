package com.jrmf.domain;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 12:00
 * Version:1.0
 * @author guoto
 */
public class ReceiptDownLoad {
    private Integer id;
    /**
     * 收件地址邮箱
     **/
    private String receivingMail;
    /**
     * 交易状态
     **/
    private Integer status;
    /**
     * 交易描述
     **/
    private String statusDesc;
    /**
     * 操作机构名称
     **/
    private String orgName;
    /**
     * 操作机构账户
     **/
    private String orgAccount;
    /**
     * 操作机构系统身份类型
     **/
    private Integer customType;
    /**
     * 文件名称
     **/
    private String fileName;
    /**
     * 创建时间
     **/
    private String createTime;
    /**
     * 最后一次更新时间
     **/
    private String updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReceivingMail() {
        return receivingMail;
    }

    public void setReceivingMail(String receivingMail) {
        this.receivingMail = receivingMail;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgAccount() {
        return orgAccount;
    }

    public void setOrgAccount(String orgAccount) {
        this.orgAccount = orgAccount;
    }

    public Integer getCustomType() {
        return customType;
    }

    public void setCustomType(Integer customType) {
        this.customType = customType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    @Override
    public String toString() {
        return "ReceiptDownLoad{" +
                "id=" + id +
                ", receivingMail='" + receivingMail + '\'' +
                ", status=" + status +
                ", statusDesc='" + statusDesc + '\'' +
                ", orgName='" + orgName + '\'' +
                ", orgAccount='" + orgAccount + '\'' +
                ", customType=" + customType +
                ", fileName='" + fileName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
