package com.jrmf.domain;

import java.io.Serializable;
/**
 * 2018-12-20
 * @author Administrator zh
 *
 */
public class WxBindInfo implements Serializable{

	private static final long serialVersionUID = 290415502926809662L;
	private int id;
	private String openId;
	private String clientOpenId;
	private String aygStatus;
	private String platForm;
	private String originId;
	private String companyId;
	private String partnetId;
	private String createTime;
	private String updateTime;
	
	public WxBindInfo(int id, String openId, String clientOpenId,
			String aygStatus, String platForm, String originId,
			String companyId, String partnetId, String createTime,
			String updateTime) {
		super();
		this.id = id;
		this.openId = openId;
		this.clientOpenId = clientOpenId;
		this.aygStatus = aygStatus;
		this.platForm = platForm;
		this.originId = originId;
		this.companyId = companyId;
		this.partnetId = partnetId;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}

	public WxBindInfo() {
		super();
	}

	public WxBindInfo(String openId,
			String clientOpenId,
			String aygStatus,
			String platForm,
			String originId,
			String companyId,
			String partnetId) {
		super();
		this.openId = openId;
		this.clientOpenId = clientOpenId;
		this.aygStatus = aygStatus;
		this.platForm = platForm;
		this.originId = originId;
		this.companyId = companyId;
		this.partnetId = partnetId;
	}
	
	public String getPlatForm() {
		return platForm;
	}

	public void setPlatForm(String platForm) {
		this.platForm = platForm;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getClientOpenId() {
		return clientOpenId;
	}
	public void setClientOpenId(String clientOpenId) {
		this.clientOpenId = clientOpenId;
	}
	public String getAygStatus() {
		return aygStatus;
	}
	public void setAygStatus(String aygStatus) {
		this.aygStatus = aygStatus;
	}
	public String getOriginId() {
		return originId;
	}
	public void setOriginId(String originId) {
		this.originId = originId;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getPartnetId() {
		return partnetId;
	}
	public void setPartnetId(String partnetId) {
		this.partnetId = partnetId;
	}
	
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public String toString() {
		return "WxBindInfo [id=" + id + ", openId=" + openId
				+ ", clientOpenId=" + clientOpenId + ", aygStatus=" + aygStatus
				+ ", originId=" + originId + ", companyId=" + companyId
				+ ", partnetId=" + partnetId + "]";
	}

	
}
 