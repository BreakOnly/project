package com.jrmf.payment.openapi.model.response.econtract;

import java.util.Date;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class QueryOrderResult implements IBizResult {

	private String impSubStateDesc;
	// 甲方签署状态描述
	private String partyaSubStateDesc;
	// 乙方签署状态描述
	private String partybSubStateDesc;
	// 丙方签署状态描述
	private String partycSubStateDesc;
	// 状态描述
	private String stateDesc;
	// 订单ID
	private String orderId;
	// 合同过期时间
	private Date expireTime;
	// 合同创建时间
	private Date createTime;
	// 甲方名称
	private String partyaUserName;
	// 甲方签约链接
	private String partyaSignUrl;
	// 甲方签约时间
	private Date partyaSignTime;
	// 乙方名称
	private String partybUserName;
	// 乙方签约链接
	private String partybSignUrl;
	// 乙方签约时间
	private Date partybSignTime;
	// 丙方名称
	private String partycUserName;
	// 丙方签约链接
	private String partycSignUrl;
	// 丙方签约时间
	private Date partycSignTime;
	// 状态
	private String state;
	// //导入状态
	private String impSubState;
	// 签署状态
	private String subState;
	// //错误信息
	private String msg;
	// 姓名
	private String personalName;
	// 手机
	private String personalMobile;
	// 证件类型
	private String personalIdentityType;
	// 身份证
	private String personalIdentity;
	// 上次通知签约时间
	private Date lastNotifyTime;
	// 外部下载链接
	private String outerDownloadUrl;
	// 外部订单号
	private String extrOrderId;
	// 外部系统ID
	private String extrSystemId;
	// 返回码
	private String notifyResultcode;
	// 返回信息
	private String notifyResultmessage;
	// 通知时间
	private Date notifyTime;
	// 通知链接
	private String notifyUrl;

	public String getAppId() {
		return extrSystemId;
	}
	
	public String getCallbackUrl() {
		return notifyUrl;
	}
	
	public String getImpSubStateDesc() {
		return impSubStateDesc;
	}

	public void setImpSubStateDesc(String impSubStateDesc) {
		this.impSubStateDesc = impSubStateDesc;
	}

	public String getPartyaSubStateDesc() {
		return partyaSubStateDesc;
	}

	public void setPartyaSubStateDesc(String partyaSubStateDesc) {
		this.partyaSubStateDesc = partyaSubStateDesc;
	}

	public String getPartybSubStateDesc() {
		return partybSubStateDesc;
	}

	public void setPartybSubStateDesc(String partybSubStateDesc) {
		this.partybSubStateDesc = partybSubStateDesc;
	}

	public String getPartycSubStateDesc() {
		return partycSubStateDesc;
	}

	public void setPartycSubStateDesc(String partycSubStateDesc) {
		this.partycSubStateDesc = partycSubStateDesc;
	}

	public String getStateDesc() {
		return stateDesc;
	}

	public void setStateDesc(String stateDesc) {
		this.stateDesc = stateDesc;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getPartyaUserName() {
		return partyaUserName;
	}

	public void setPartyaUserName(String partyaUserName) {
		this.partyaUserName = partyaUserName;
	}

	public String getPartyaSignUrl() {
		return partyaSignUrl;
	}

	public void setPartyaSignUrl(String partyaSignUrl) {
		this.partyaSignUrl = partyaSignUrl;
	}

	public Date getPartyaSignTime() {
		return partyaSignTime;
	}

	public void setPartyaSignTime(Date partyaSignTime) {
		this.partyaSignTime = partyaSignTime;
	}

	public String getPartybUserName() {
		return partybUserName;
	}

	public void setPartybUserName(String partybUserName) {
		this.partybUserName = partybUserName;
	}

	public String getPartybSignUrl() {
		return partybSignUrl;
	}

	public void setPartybSignUrl(String partybSignUrl) {
		this.partybSignUrl = partybSignUrl;
	}

	public Date getPartybSignTime() {
		return partybSignTime;
	}

	public void setPartybSignTime(Date partybSignTime) {
		this.partybSignTime = partybSignTime;
	}

	public String getPartycUserName() {
		return partycUserName;
	}

	public void setPartycUserName(String partycUserName) {
		this.partycUserName = partycUserName;
	}

	public String getPartycSignUrl() {
		return partycSignUrl;
	}

	public void setPartycSignUrl(String partycSignUrl) {
		this.partycSignUrl = partycSignUrl;
	}

	public Date getPartycSignTime() {
		return partycSignTime;
	}

	public void setPartycSignTime(Date partycSignTime) {
		this.partycSignTime = partycSignTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getImpSubState() {
		return impSubState;
	}

	public void setImpSubState(String impSubState) {
		this.impSubState = impSubState;
	}

	public String getSubState() {
		return subState;
	}

	public void setSubState(String subState) {
		this.subState = subState;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getPersonalName() {
		return personalName;
	}

	public void setPersonalName(String personalName) {
		this.personalName = personalName;
	}

	public String getPersonalMobile() {
		return personalMobile;
	}

	public void setPersonalMobile(String personalMobile) {
		this.personalMobile = personalMobile;
	}

	public String getPersonalIdentityType() {
		return personalIdentityType;
	}

	public void setPersonalIdentityType(String personalIdentityType) {
		this.personalIdentityType = personalIdentityType;
	}

	public String getPersonalIdentity() {
		return personalIdentity;
	}

	public void setPersonalIdentity(String personalIdentity) {
		this.personalIdentity = personalIdentity;
	}

	public Date getLastNotifyTime() {
		return lastNotifyTime;
	}

	public void setLastNotifyTime(Date lastNotifyTime) {
		this.lastNotifyTime = lastNotifyTime;
	}

	public String getOuterDownloadUrl() {
		return outerDownloadUrl;
	}

	public void setOuterDownloadUrl(String outerDownloadUrl) {
		this.outerDownloadUrl = outerDownloadUrl;
	}

	public String getExtrOrderId() {
		return extrOrderId;
	}

	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}

	public String getExtrSystemId() {
		return extrSystemId;
	}

	public void setExtrSystemId(String extrSystemId) {
		this.extrSystemId = extrSystemId;
	}

	public String getNotifyResultcode() {
		return notifyResultcode;
	}

	public void setNotifyResultcode(String notifyResultcode) {
		this.notifyResultcode = notifyResultcode;
	}

	public String getNotifyResultmessage() {
		return notifyResultmessage;
	}

	public void setNotifyResultmessage(String notifyResultmessage) {
		this.notifyResultmessage = notifyResultmessage;
	}

	public Date getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(Date notifyTime) {
		this.notifyTime = notifyTime;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

}
