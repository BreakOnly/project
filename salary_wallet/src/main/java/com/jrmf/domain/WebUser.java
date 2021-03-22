package com.jrmf.domain;

import java.io.Serializable;
import java.util.Date;

public class WebUser implements Serializable {

	private static final long serialVersionUID = -8459459847561168432L;
	private Integer id;// 主键
	private String customkey;// 渠道商唯一标识
	private String customName;// 渠道商名称
	private String username;// 用户名
	private int enabled;// 0代表禁用，1代表启用 -2注销
	private String companyName;//
	private String phoneNo;//
	private Date createtime;//
	private String status;//
	private String fatherCustomkey;// 二级商家key
	private String password;//
	private String email;//
	private String activeEmail;// 邮箱是否激活0未激活 1已激活
	private String activeMobiletelno;// 手机号是否激活0未激活 1已激活
	private Integer isOpen;// 是否是云模式，还是隔离模式 1:云模式 0:隔离模式
	private Date last_login;// 最后登录时间
	private String position;
	private Integer expectedProduct;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCustomkey() {
		return customkey;
	}

	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getStatus() {
		return status;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public String getFatherCustomkey() {
		return fatherCustomkey;
	}

	public void setFatherCustomkey(String fatherCustomkey) {
		this.fatherCustomkey = fatherCustomkey;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getActiveEmail() {
		return activeEmail;
	}

	public void setActiveEmail(String activeEmail) {
		this.activeEmail = activeEmail;
	}

	public String getActiveMobiletelno() {
		return activeMobiletelno;
	}

	public void setActiveMobiletelno(String activeMobiletelno) {
		this.activeMobiletelno = activeMobiletelno;
	}

	public Integer getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(Integer isOpen) {
		this.isOpen = isOpen;
	}

	public Date getLast_login() {
		return last_login;
	}

	public void setLast_login(Date last_login) {
		this.last_login = last_login;
	}

	public Integer getExpectedProduct() {
		return expectedProduct;
	}

	public void setExpectedProduct(Integer expectedProduct) {
		this.expectedProduct = expectedProduct;
	}

	
	
	



}
