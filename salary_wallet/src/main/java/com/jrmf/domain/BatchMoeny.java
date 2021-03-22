package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年8月19日 上午11:26:08 
* 类说明   交易记录
*/
public class BatchMoeny implements Serializable  {

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -368291925439065160L;
	private String inUserNo;
	private String amount;
	private String orderNo;
	private String userName;
	private String transDesc = "资金转出";
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getInUserNo() {
		return inUserNo;
	}
	public void setInUserNo(String inUserNo) {
		this.inUserNo = inUserNo;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getTransDesc() {
		return transDesc;
	}
	public void setTransDesc(String transDesc) {
		this.transDesc = transDesc;
	}
	
	
}
 