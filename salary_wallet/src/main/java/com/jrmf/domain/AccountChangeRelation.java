package com.jrmf.domain;

public class AccountChangeRelation {
    private Integer id;

    private Integer accountId;

    private String customKey;

    private String customName;

    private Integer changeAccountId;

    private Integer relationType;

    private String changeAccountName;

    private String createTime;

    private String addUser;

    public String getChangeAccountName() {
        return changeAccountName;
    }

    public void setChangeAccountName(String changeAccountName) {
        this.changeAccountName = changeAccountName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAddUser() {
        return addUser;
    }

    public void setAddUser(String addUser) {
        this.addUser = addUser;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey == null ? null : customKey.trim();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName == null ? null : customName.trim();
    }

    public Integer getChangeAccountId() {
        return changeAccountId;
    }

    public void setChangeAccountId(Integer changeAccountId) {
        this.changeAccountId = changeAccountId;
    }

    public Integer getRelationType() {
        return relationType;
    }

    public void setRelationType(Integer relationType) {
        this.relationType = relationType;
    }
}