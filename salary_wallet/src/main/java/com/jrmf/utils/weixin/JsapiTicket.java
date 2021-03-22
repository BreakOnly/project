package com.jrmf.utils.weixin;


/**
 * 接口访问凭证model
 *
 * @author linsong
 */
public class JsapiTicket {

	// 获取到的凭证
	private String ticket;

	// 凭证有效时间，单位：秒
	private int expiresIn;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
}