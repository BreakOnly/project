package com.jrmf.domain;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/28 16:50
 * Version:1.0
 */
public class OrganizationNode {
    private int id;
    private int parentId;
    /**节点等级*/
    private int contentLevel;
    /**组织机构层级编码*/
    private String levelCode;
    private String customKey;
    /**系统角色类型*/
    private int customType;
    /**节点名称*/
    private String organizationName;
    /**是否含有子节点*/
    private int hasChilden;
    /**是否启用*/
    private int enable;
    private int regType;
    private String createTime;

    public int getRegType() {
        return regType;
    }

    public void setRegType(int regType) {
        this.regType = regType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getContentLevel() {
        return contentLevel;
    }

    public void setContentLevel(int contentLevel) {
        this.contentLevel = contentLevel;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public int getCustomType() {
        return customType;
    }

    public void setCustomType(int customType) {
        this.customType = customType;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public int getHasChilden() {
        return hasChilden;
    }

    public void setHasChilden(int hasChilden) {
        this.hasChilden = hasChilden;
    }

    @Override
    public String toString() {
        return "OrganizationNode{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", contentLevel=" + contentLevel +
                ", levelCode='" + levelCode + '\'' +
                ", customKey='" + customKey + '\'' +
                ", customType=" + customType +
                ", organizationName='" + organizationName + '\'' +
                ", hasChilden=" + hasChilden +
                ", enable=" + enable +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
