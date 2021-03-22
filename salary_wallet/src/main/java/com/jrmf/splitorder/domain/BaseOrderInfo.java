package com.jrmf.splitorder.domain;

import com.jrmf.domain.UserCommission;

import java.util.List;


public abstract class BaseOrderInfo {
    private String customName;
    private String companyName;
    private String customKey;
    private String companyId;
    private List<UserCommission> data;

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public List<UserCommission> getData() {
        return data;
    }

    public void setData(List<UserCommission> data) {
        this.data = data;
    }
}
