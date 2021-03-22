package com.jrmf.domain;

import java.io.Serializable;

/**
 * @author: zhangzehui
 * @date: 2017-11-24
 * @description:渠道三方支付信息记录
 */
public class ChannelPaymentInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String createtime;
	private String jdPayAmount;//京东支付金额（元）
	private String jdCompanyPayAmount;//京东企业网银支付金额（元）
	private String alliPayAmount;//支付宝支付金额（元）
	private String weChatPayAmount;//微信支付金额（元）
	private String weChatGZPayAmount;//微信公众号支付金额（元）
	private String codePayAmount;//扫码付金额（元）
	private String customPayAmount;//渠道企业网银充值金额（元）
	private String customkey;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getJdPayAmount() {
		return jdPayAmount;
	}
	public void setJdPayAmount(String jdPayAmount) {
		this.jdPayAmount = jdPayAmount;
	}
	public String getJdCompanyPayAmount() {
		return jdCompanyPayAmount;
	}
	public void setJdCompanyPayAmount(String jdCompanyPayAmount) {
		this.jdCompanyPayAmount = jdCompanyPayAmount;
	}
	public String getAlliPayAmount() {
		return alliPayAmount;
	}
	public void setAlliPayAmount(String alliPayAmount) {
		this.alliPayAmount = alliPayAmount;
	}
	public String getWeChatPayAmount() {
		return weChatPayAmount;
	}
	public void setWeChatPayAmount(String weChatPayAmount) {
		this.weChatPayAmount = weChatPayAmount;
	}
	public String getWeChatGZPayAmount() {
		return weChatGZPayAmount;
	}
	public void setWeChatGZPayAmount(String weChatGZPayAmount) {
		this.weChatGZPayAmount = weChatGZPayAmount;
	}
	public String getCodePayAmount() {
		return codePayAmount;
	}
	public void setCodePayAmount(String codePayAmount) {
		this.codePayAmount = codePayAmount;
	}
	public String getCustomPayAmount() {
		return customPayAmount;
	}
	public void setCustomPayAmount(String customPayAmount) {
		this.customPayAmount = customPayAmount;
	}
	public String getCustomkey() {
		return customkey;
	}
	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
