package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年9月3日 上午11:05:04 
* 类说明 
*/
public class ReturnUrl implements Serializable {

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 3301645691956248731L;
	private int id;
	private String user_id;//用户id
	private String passwordSetReturnUrl;//设置密码后台通知
	private String passwordResetUrl;  //重置密码后台通知
	private String passwordChangeUrl;  //修改密码后台通知
	
	public String getPasswordChangeUrl() {
		return passwordChangeUrl;
	}
	public void setPasswordChangeUrl(String passwordChangeUrl) {
		this.passwordChangeUrl = passwordChangeUrl;
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
	public String getPasswordSetReturnUrl() {
		return passwordSetReturnUrl;
	}
	public void setPasswordSetReturnUrl(String passwordSetReturnUrl) {
		this.passwordSetReturnUrl = passwordSetReturnUrl;
	}
	public String getPasswordResetUrl() {
		return passwordResetUrl;
	}
	public void setPasswordResetUrl(String passwordResetUrl) {
		this.passwordResetUrl = passwordResetUrl;
	}
	@Override
	public String toString() {
		return "ReturnUrl [id=" + id + ", user_id=" + user_id + ", passwordSetReturnUrl=" + passwordSetReturnUrl
				+ ", passwordResetUrl=" + passwordResetUrl + ", passwordChangeUrl=" + passwordChangeUrl + "]";
	}
	
	
}
 