package com.jrmf.domain;

public class LinkageCustomConfig {
    private Integer id;

    private String customKey;

    private Integer linkageType;

    private String configId;
    
    private String createTime;
    
    private String addUser;

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
        this.customKey = customKey == null ? null : customKey.trim();
    }

    public Integer getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(Integer linkageType) {
        this.linkageType = linkageType;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId == null ? null : configId.trim();
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
    
}