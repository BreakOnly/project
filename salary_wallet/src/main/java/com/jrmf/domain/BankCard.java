package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年8月31日 下午8:28:05 
* 类说明 
*/
public class BankCard implements Serializable{

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -5082398586266162447L;
	private int id;
	private String bankName;
	private String bankFullName;
	private int length;
	private String cardFullNo;
	private int startLength;
	private String start;
	private String bankNo;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankFullName() {
		return bankFullName;
	}
	public void setBankFullName(String bankFullName) {
		this.bankFullName = bankFullName;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getCardFullNo() {
		return cardFullNo;
	}
	public void setCardFullNo(String cardFullNo) {
		this.cardFullNo = cardFullNo;
	}
	public int getStartLength() {
		return startLength;
	}
	public void setStartLength(int startLength) {
		this.startLength = startLength;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	@Override
	public String toString() {
		return "BankCard{" +
				"id=" + id +
				", bankName='" + bankName + '\'' +
				", bankFullName='" + bankFullName + '\'' +
				", length=" + length +
				", cardFullNo='" + cardFullNo + '\'' +
				", startLength=" + startLength +
				", start='" + start + '\'' +
				", bankNo='" + bankNo + '\'' +
				'}';
	}
}
 