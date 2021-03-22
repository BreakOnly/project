package com.jrmf.domain;

import java.io.Serializable;

public class ParterTransferHistory implements Serializable  {

	private static final long serialVersionUID = 6693359835885577468L;
	
	private int id; 
	private int userId;//用户id
	private String transferRequestNo;//交易订单号
	private String amount;  //金额
	private String tradeName; //交易名称
	private String createTime;  //创建时间
	private String creatMonth;   //创建日期
	private int status;  //交易状态  1成功，0失败  2待确认
	private int tradeType;//交易类型
	
	public int getTradeType() {
		return tradeType;
	}
	public void setTradeType(int tradeType) {
		this.tradeType = tradeType;
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
	public String getTransferRequestNo() {
		return transferRequestNo;
	}
	public void setTransferRequestNo(String transferRequestNo) {
		this.transferRequestNo = transferRequestNo;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTradeName() {
		return tradeName;
	}
	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getCreatMonth() {
		return creatMonth;
	}
	public void setCreatMonth(String creatMonth) {
		this.creatMonth = creatMonth;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
 