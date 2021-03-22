package com.jrmf.domain;

import java.io.Serializable;

/**
 * @Title: Notice
 * @Description: 公告基础信息类
 * @create 2020/1/15 17:44
 */
public class Notice implements Serializable {

    /**
     * 主键
     */
    private Integer id;
    /**
     * 通知标题
     */
    private String title;
    /**
     * 通知内容
     */
    private String content;
    /**
     * 通知范围 0：全部 1：商户 2：下发公司 3：代理商 4：账户管理员 5：集团型商户 6：自定义
     */
    private String organizationType;
    /**
     * 通知类型 1：业务通知，2：版本更新
     */
    private String noticeType;
    /**
     * 发布者
     */
    private String publishAuthor;
    /**
     * 发布时间
     */
    private String publishTime;
    /**
     * 操作人
     */
    private String addUser;
    /**
     * 接收账号范围 1：已创建商户，2：已创建商户及以后创建商户
     */
    private String accountScope;
    /**
     * 站点类型 1：B端站点，2：C端站点
     */
    private String stationType;
    /**
     * 文本html
     */
    private String textHtml;
    /**
     * 附件名称
     */
    private String accessoryName;
    /**
     * 附件地址
     */
    private String accessoryUrl;
    /**
     * 逻辑失效&删除 1：失效，2：未失效 3:删除
     */
    private Integer enabled;
    /**
     * 关联自定义表id
     */
    private String organizationId;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 已读数量
     */
    private String readAmount;

    /**
     * 查看率
     */
    private String checkRate;

    /**
     * 通知对象
     */
    private String organizationName;

    /**
     * 通知id
     */
    private String organization;

    public String getOrganization() {
        return organization;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getAccountScope() {
        return accountScope;
    }

    public void setAccountScope(String accountScope) {
        this.accountScope = accountScope;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getReadAmount() {
        return readAmount;
    }

    public void setReadAmount(String readAmount) {
        this.readAmount = readAmount;
    }

    public String getCheckRate() {
        return checkRate;
    }

    public void setCheckRate(String checkRate) {
        this.checkRate = checkRate;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getPublishAuthor() {
        return publishAuthor;
    }

    public void setPublishAuthor(String publishAuthor) {
        this.publishAuthor = publishAuthor;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getAddUser() {
        return addUser;
    }

    public void setAddUser(String addUser) {
        this.addUser = addUser;
    }

    public String getTextHtml() {
        return textHtml;
    }

    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
    }

    public String getAccessoryName() {
        return accessoryName;
    }

    public void setAccessoryName(String accessoryName) {
        this.accessoryName = accessoryName;
    }

    public String getAccessoryUrl() {
        return accessoryUrl;
    }

    public void setAccessoryUrl(String accessoryUrl) {
        this.accessoryUrl = accessoryUrl;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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
        return "Notice{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", organizationType='" + organizationType + '\'' +
                ", noticeType='" + noticeType + '\'' +
                ", publishAuthor='" + publishAuthor + '\'' +
                ", publishTime='" + publishTime + '\'' +
                ", addUser='" + addUser + '\'' +
                ", accountScope='" + accountScope + '\'' +
                ", stationType='" + stationType + '\'' +
                ", textHtml='" + textHtml + '\'' +
                ", accessoryName='" + accessoryName + '\'' +
                ", accessoryUrl='" + accessoryUrl + '\'' +
                ", enabled=" + enabled +
                ", organizationId='" + organizationId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", readAmount='" + readAmount + '\'' +
                ", checkRate='" + checkRate + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
