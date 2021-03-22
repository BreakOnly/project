package com.jrmf.domain;

import java.io.Serializable;

/**
 * @Title: CustomOrganization
 * @Description: 自定义组织表
 * @create 2020/1/16 15:13
 */
public class CustomOrganization implements Serializable {

    /**
     * 主键
     */
    private Integer id;
    /**
     * 商户表id,"," 分割
     */
    private String accountIds;
    /**
     * 组织名称
     */
    private String organizationName;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;
    /**
     * 逻辑失效 1：失效，2：未失效
     */
    private String enabled;

    /**
     * 范围 1：系统组织 2：自定义组织
     */
    private int type;

    /**
     * 父级id
     */
    private String parentId;
    /**
     * 层级编码 规则是 父节点id+''-''+本节点id+''-''
     */
    private String levelCode;

    /**
     * '账户类型：1 商户 2 下发公司 3 代理商 4 账户管理员 5集团型商户',
     */
    private Integer customType;

    private Integer loginRole;

    public Integer getLoginRole() {
        return loginRole;
    }

    public void setLoginRole(Integer loginRole) {
        this.loginRole = loginRole;
    }

    public Integer getCustomType() {
        return customType;
    }

    public void setCustomType(Integer customType) {
        this.customType = customType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "CustomOrganization{" +
                "id=" + id +
                ", accountIds='" + accountIds + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", enabled='" + enabled + '\'' +
                ", type=" + type +
                ", parentId='" + parentId + '\'' +
                ", levelCode='" + levelCode + '\'' +
                ", customType=" + customType +
                ", loginRole=" + loginRole +
                '}';
    }
}
