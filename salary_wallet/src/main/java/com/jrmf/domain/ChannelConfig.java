package com.jrmf.domain;

import java.io.Serializable;

/**
 * filename：com.jrmf.domain.common.ChannelCustomConf.java
 *
 * @author: zhangyong
 * @time: 2016-4-17下午5:15:25
 */

public class ChannelConfig implements Serializable {
	private static final long serialVersionUID = 4087044254060549263L;
	private int id;
	private int channelId;//关联服务公司id
	private int payType;//支付类型
	private String accountName;//开户银行
	private String accountNum;//账户号
	private String bankName;//账户名称
	private String usefor;//用途
	private Integer status;//充值账户使用状态1.正常，2.废弃
	private String companyName; // 服务公司名称
	private String startTime; // 开始时间
	private String endTime; // 结束时间
	private String createTime; // 添加时间
	private String updateTime; // 修改时间
	//充值确认方式1.自动确认，2.人工确认
	private Integer rechargeConfirmType;
	
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getChannelId() {
		return channelId;
	}
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getUsefor() {
		return usefor;
	}
	public void setUsefor(String usefor) {
		this.usefor = usefor;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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

	public Integer getRechargeConfirmType() {
		return rechargeConfirmType;
	}

	public void setRechargeConfirmType(Integer rechargeConfirmType) {
		this.rechargeConfirmType = rechargeConfirmType;
	}
}
