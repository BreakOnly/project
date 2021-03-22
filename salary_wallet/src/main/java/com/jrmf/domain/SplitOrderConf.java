package com.jrmf.domain;

import com.jrmf.utils.ArithmeticUtil;

import java.util.Date;

public class SplitOrderConf {
    private String customName;
    private String companyName;

    private String customKey;
    private String companyId;
    private Integer level;
    private String amountLimit;
    private String filePath;
    private String createTime;
    private String updateTime;
    private String interval;
    private String splitOrderBalance;

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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(String amountLimit) {
        this.amountLimit = amountLimit;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSplitOrderBalance() {
        return splitOrderBalance;
    }

    public void setSplitOrderBalance(String splitOrderBalance) {
        this.splitOrderBalance = splitOrderBalance;
    }

    public boolean deductAmount(String amount) {
        synchronized (this) {
            if (ArithmeticUtil.compareTod(this.splitOrderBalance, amount) == 1) {
                this.splitOrderBalance = ArithmeticUtil.subStr(splitOrderBalance, amount);
                return true;
            } else {
                return false;
            }
        }
    }
}
