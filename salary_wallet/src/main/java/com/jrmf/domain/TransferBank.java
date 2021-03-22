package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年8月17日 下午8:53:40 
* 类说明   提现银行卡
*/
public class TransferBank implements Serializable{
	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -6310045034328949319L;
	private int id;
	private String user_id; //用户id
	private String bankCardNo;//用户银行卡号
	private int status;//绑定状态0--解绑，  1--绑定   -1待激活
	private String bankNo;//银行编号
	private String bankLogo;//银行图标 
	private String bankBgImg;//银行背景图标 
	private String bankName;//银行名称
	private String transferType;//类型  1.充值  2，提现 3 企业充值
	private String bankCardPhoneNo;//银行预留手机号
	private String creat_time;//创建时间
	
	
	public String getBankBgImg() {
		return bankBgImg;
	}
	public void setBankBgImg(String bankBgImg) {
		this.bankBgImg = bankBgImg;
	}
	public String getCreat_time() {
		return creat_time;
	}
	public void setCreat_time(String creat_time) {
		this.creat_time = creat_time;
	}
	public String getBankCardPhoneNo() {
		return bankCardPhoneNo;
	}
	public void setBankCardPhoneNo(String bankCardPhoneNo) {
		this.bankCardPhoneNo = bankCardPhoneNo;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	
	
	public String getBankCardNo() {
		return bankCardNo;
	}
	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getBankLogo() {
		return bankLogo;
	}
	public void setBankLogo(String bankLogo) {
		this.bankLogo = bankLogo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	public String getTransferType() {
		return transferType;
	}
	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}
	@Override
	public String toString() {
		return "TransferBank [id=" + id + ", user_id=" + user_id + ", bankCardNo=" + bankCardNo + ", status=" + status
				+ ", bankNo=" + bankNo + ", bankLogo=" + bankLogo + ", bankName=" + bankName + ", transferType="
				+ transferType + "]";
	}
	
	
}
 