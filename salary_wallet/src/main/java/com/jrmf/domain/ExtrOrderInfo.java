package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2018年1月10日 下午8:12:28 
* 类说明 
*/
public class ExtrOrderInfo implements Serializable {

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 4630873066081923284L;
	private int id;
	private int user_id;
	private String extrOrderId;//合同id
	private int status;//1,签约，0未签约 -1待签约   
//									初始化  0，
//									通知到渠道 -1，
//									签约成功  1
	private String createTime;//创建时间
	private String url;//签约地址
	private String customkey;//所属渠道
	
	public String getCustomkey() {
		return customkey;
	}
	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getExtrOrderId() {
		return extrOrderId;
	}
	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
 