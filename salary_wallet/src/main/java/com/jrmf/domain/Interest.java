package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年11月23日 14:49:49
* 类说明 日结利息
*/
public class Interest implements Serializable{

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 3787502829679696582L;

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	private String transAmount;//利息金额
	private String createTime;//生成时间
	private String userNo;//用户id
	
	public String getTransAmount() {
		return transAmount;
	}
	public void setTransAmount(String transAmount) {
		this.transAmount = transAmount;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	@Override
	public String toString() {
		return "Interest [transAmount=" + transAmount + ", createTime=" + createTime + ", userNo=" + userNo + "]";
	}
	
}
 