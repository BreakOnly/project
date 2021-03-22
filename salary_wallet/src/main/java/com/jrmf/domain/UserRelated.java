package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @time 2017-12-14 
* @description: 用户商户关联表(薪税钱包)
*/
public class UserRelated implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private int userId;//用户表主键
	private String originalId;//商户标识（原公司）
	private String companyId;//关联的佣金待发商户（佣金代发公司） 对应company表的userid
	private String userNo;//用户商户标识
	private int status;// 0 未签约 1 签约
	private String createTime;//用户注册时间
	private String updateTime;//用户修改时间
	private String agreementUrl;//签约地址
	private Integer isWhiteList;
	private String mobileNo;
	
	public String getAgreementUrl() {
		return agreementUrl;
	}
	public void setAgreementUrl(String agreementUrl) {
		this.agreementUrl = agreementUrl;
	}
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
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Integer getIsWhiteList() {
		return isWhiteList;
	}

	public void setIsWhiteList(Integer isWhiteList) {
		this.isWhiteList = isWhiteList;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	@Override
	public String toString() {
		return "UserRelated [id=" + id + ", userId=" + userId + ", originalId=" + originalId + ", userNo=" + userNo
				+ ", " + "mobileNo=" + mobileNo + ", status=" + status + ", createTime=" + createTime + ", updateTime=" + updateTime + ", agreementUrl=" + agreementUrl + "]";
	}
	
}
 