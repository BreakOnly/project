package com.jrmf.domain;

import java.io.Serializable;

public class Partner implements Serializable {

	private static final long serialVersionUID = 6771255163127995200L;
	private int id;//主键userId
	private String userName;//姓名
	private String certId;//用户身份证号码
	private String mobilePhone;//用户手机号
	private String bankCardNo;//银行卡号
	private String companyUserNo;//用户所在企业对应的编号
	private String companyName;//用户所在企业名称
	private String userCertFrontPicture;//身份证照片路径--正面
	private String userCertBackPicture;//身份证照片路径--反面
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getCompanyUserNo() {
		return companyUserNo;
	}
	public void setCompanyUserNo(String companyUserNo) {
		this.companyUserNo = companyUserNo;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getBankCardNo() {
		return bankCardNo;
	}
	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}
	public String getUserCertFrontPicture() {
		return userCertFrontPicture;
	}
	public void setUserCertFrontPicture(String userCertFrontPicture) {
		this.userCertFrontPicture = userCertFrontPicture;
	}
	public String getUserCertBackPicture() {
		return userCertBackPicture;
	}
	public void setUserCertBackPicture(String userCertBackPicture) {
		this.userCertBackPicture = userCertBackPicture;
	}
}
 