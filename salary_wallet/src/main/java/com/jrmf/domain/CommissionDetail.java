package com.jrmf.domain;

import java.io.Serializable;

public class CommissionDetail implements Serializable  {

	private static final long serialVersionUID = 3688482706431216226L;
	
	private int id; 
	private int userId;//用户id
	private String certId;//身份证号
	private String amount;  //金额
	private int status;  //交易状态 0待发 1成功 2失败
	private String remark; //交易名称
	private String createTime; //创建时间
	private String updateTime; //更新时间
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
	
}
 