package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年8月31日 下午8:28:05 
* 类说明 
*/
public class BatchUser implements Serializable{

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 7807775506623869924L;
	private String userNo;
	private String bankCardNo;
	private String userName;
	private String certId;
	private String mobilePhone;
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getBankCardNo() {
		return bankCardNo;
	}
	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	@Override
	public String toString() {
		return "BatchUser [userNo=" + userNo + ", bankCardNo=" + bankCardNo + ", userName=" + userName + ", certId="
				+ certId + ", mobilePhone=" + mobilePhone + "]";
	}
	

}
 