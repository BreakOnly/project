package com.jrmf.domain;

import java.util.Date;

public class SubcontractRouter {
    private Integer id;

    private String customKey;

    private Integer subcontractCompanyId;

    private Integer realCompanyId;

    private Date createTime;

    private Date lastUpdateTime;

    private transient String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public Integer getSubcontractCompanyId() {
        return subcontractCompanyId;
    }

    public void setSubcontractCompanyId(Integer subcontractCompanyId) {
        this.subcontractCompanyId = subcontractCompanyId;
    }

    public Integer getRealCompanyId() {
        return realCompanyId;
    }

    public void setRealCompanyId(Integer realCompanyId) {
        this.realCompanyId = realCompanyId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
