package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @version 创建时间：2018年5月22日 上午11:26:08 
* 类说明  薪资下发封装类
*/
public class CommissionDomain implements Serializable  {

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -368291925439065160L;
	private String userNo;
	private String amount;
	private int status;// 0 保存失败，数据不符合要求  1 保存成功
	private String statusDec;
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
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
	public String getStatusDec() {
		return statusDec;
	}
	public void setStatusDec(String statusDec) {
		this.statusDec = statusDec;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
 