package com.jrmf.domain;

import java.util.Date;

/**
 * @Title: CustomProxy
 * @Description: 代理商属性
 * @create 2019/10/14 14:32
 */
public class CustomProxy {
    /**
     * 主键
     */
    private int id;
    /**
     * 商户的customkey
     */
    private String customkey;
    /**
     * 商户的父节点id
     */
    private int parentId;
    /**
     * 层级编码 规则是 父节点id+''-''+本节点id+''-''
     */
    private String levelCode;
    /**
     * 节点层次等级 1 一级(最高） 2 下级节点
     */
    private int contentLevel;
    /**
     * 记录等级类型
     */
    private int regType;
    /**
     * 关联关系是否启用 1启用 -1不启用
     */
    private int enable;
    /**
     * 是否包含子节点 1包含 -1不包含
     */
    private int hasChilden;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public int getContentLevel() {
        return contentLevel;
    }

    public void setContentLevel(int contentLevel) {
        this.contentLevel = contentLevel;
    }

    public int getRegType() {
        return regType;
    }

    public void setRegType(int regType) {
        this.regType = regType;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getHasChilden() {
        return hasChilden;
    }

    public void setHasChilden(int hasChilden) {
        this.hasChilden = hasChilden;
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
}
