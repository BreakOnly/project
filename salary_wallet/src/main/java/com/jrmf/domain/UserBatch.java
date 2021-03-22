package com.jrmf.domain;

import java.io.Serializable;

/**
 * filename：com.jrmf.domain.common.ChannelCustomConf.java
 * 
 * @author: zhangyong
 * @time: 2016-4-17下午5:15:25
 */

public class UserBatch implements Serializable {
	
	private static final long serialVersionUID = 4087044254060549263L;
	private int id;
	private String createTime;
	private String batchId;
	private int passNum;
	private int batchNum;
	private int errorNum;
	private String customkey;
	
	public String getCustomkey() {
		return customkey;
	}
	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public int getPassNum() {
		return passNum;
	}
	public void setPassNum(int passNum) {
		this.passNum = passNum;
	}
	public int getBatchNum() {
		return batchNum;
	}
	public void setBatchNum(int batchNum) {
		this.batchNum = batchNum;
	}
	public int getErrorNum() {
		return errorNum;
	}
	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
