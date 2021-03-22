package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @version 创建时间：2017年8月19日 上午11:26:08 
* 类说明  用户封装类
*/
public class UserDomain implements Serializable  {

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -368291925439065160L;
	private String userName;
	private String cardNo;
	private String bankcardNo;
	private String mobile;
	private String userNo;
	private String statusDec;
	private int status;  //0 保存失败，数据不符合要求  1 保存成功
	private String userId;
	
	public String getStatusDec() {
		return statusDec;
	}
	public void setStatusDec(String statusDec) {
		this.statusDec = statusDec;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getBankcardNo() {
		return bankcardNo;
	}
	public void setBankcardNo(String bankcardNo) {
		this.bankcardNo = bankcardNo;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
 