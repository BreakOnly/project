package com.jrmf.domain;

import java.io.Serializable;
/**
 * 
* @author chonglulu  
*
 */

public class CustomInfo implements Serializable {

	private static final long serialVersionUID = 8684966537020187240L;

	private int id;
	/**
	 * 第三方渠道key
	 */
	private String customkey;
	private String ip;
	/**
	 * logo
	 */
	private String customlogo;
	/**
	 * 公司名称
	 */
	private String customname;
	
	/**
	 * 盐值
	 */
	private String salt;
	private String customLoginKey;//登录md5  盐值
	private String channelBankNo;//银行id
	private String moduleNo;//模块(产品线)编号
	private String channelNo;//渠道编号
	private String status;//状态  1，正常，0禁用
	private int showP2p;//是否显示固收 1,显示。  0,不显示
	private int showFund;//是否显示基金 1,显示。  0,不显示
	private int showInsurance;//是否显示保险 1,显示。  0,不显示
	private String companyOpenNotify;//公司开户成功通知地址
	private String compactNotifyUrl;//合同通知url（仅限moduleNo=000002）
	
	public String getCompactNotifyUrl() {
		return compactNotifyUrl;
	}
	public void setCompactNotifyUrl(String compactNotifyUrl) {
		this.compactNotifyUrl = compactNotifyUrl;
	}
	public int getShowP2p() {
		return showP2p;
	}
	public void setShowP2p(int showP2p) {
		this.showP2p = showP2p;
	}
	public int getShowFund() {
		return showFund;
	}
	public void setShowFund(int showFund) {
		this.showFund = showFund;
	}
	public int getShowInsurance() {
		return showInsurance;
	}
	public void setShowInsurance(int showInsurance) {
		this.showInsurance = showInsurance;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCustomkey() {
		return customkey;
	}
	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getCustomlogo() {
		return customlogo;
	}
	public void setCustomlogo(String customlogo) {
		this.customlogo = customlogo;
	}
	public String getCustomname() {
		return customname;
	}
	public void setCustomname(String customname) {
		this.customname = customname;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getCustomLoginKey() {
		return customLoginKey;
	}
	public void setCustomLoginKey(String customLoginKey) {
		this.customLoginKey = customLoginKey;
	}
	public String getChannelBankNo() {
		return channelBankNo;
	}
	public void setChannelBankNo(String channelBankNo) {
		this.channelBankNo = channelBankNo;
	}
	public String getModuleNo() {
		return moduleNo;
	}
	public void setModuleNo(String moduleNo) {
		this.moduleNo = moduleNo;
	}
	public String getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getCompanyOpenNotify() {
		return companyOpenNotify;
	}
	public void setCompanyOpenNotify(String companyOpenNotify) {
		this.companyOpenNotify = companyOpenNotify;
	}
	@Override
	public String toString() {
		return "CustomInfo [id=" + id + ", customkey=" + customkey + ", ip=" + ip + ", customlogo=" + customlogo
				+ ", customname=" + customname + ", salt=" + salt + ", customLoginKey=" + customLoginKey
				+ ", channelBankNo=" + channelBankNo + ", moduleNo=" + moduleNo + ", channelNo=" + channelNo
				+ ", status=" + status + ", showP2p=" + showP2p + ", showFund=" + showFund + ", showInsurance="
				+ showInsurance + ", companyOpenNotify=" + companyOpenNotify + "]";
	}
	
	
}
