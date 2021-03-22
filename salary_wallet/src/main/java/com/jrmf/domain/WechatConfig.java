package com.jrmf.domain;

/**
 * 微信配置信息
 * @author zh
 *
 */
public class WechatConfig {
	private int id;
	
	private String appId;
	private String appName;
	private String domainName;
	private String appSeckey;
	
	private String officialAccAppId;
	private String officialAccAppSeckey;
	private String officialAccAppName;
	
	private String createTime;
	private String updateTime;
	
	public String getAppSeckey() {
		return appSeckey;
	}
	public void setAppSeckey(String appSeckey) {
		this.appSeckey = appSeckey;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getOfficialAccAppId() {
		return officialAccAppId;
	}
	public void setOfficialAccAppId(String officialAccAppId) {
		this.officialAccAppId = officialAccAppId;
	}
	public String getOfficialAccAppSeckey() {
		return officialAccAppSeckey;
	}
	public void setOfficialAccAppSeckey(String officialAccAppSeckey) {
		this.officialAccAppSeckey = officialAccAppSeckey;
	}
	public String getOfficialAccAppName() {
		return officialAccAppName;
	}
	public void setOfficialAccAppName(String officialAccAppName) {
		this.officialAccAppName = officialAccAppName;
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
	
}
