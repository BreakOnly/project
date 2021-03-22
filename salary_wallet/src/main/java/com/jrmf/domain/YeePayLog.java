package com.jrmf.domain;


/**
 * 
* @author zh
* 转发易宝日志  
*
 */

public class YeePayLog {

	private int id;
	private String appkey;
	private String requestNo;
	private String requestUri;
	private String requestMsg;
	private String responseMsg;
	private String createTime;
	private String updateTime;

	public YeePayLog(String appkey,
			String requestNo,
			String requestUri,
			String requestMsg) {
		super();
		this.appkey = appkey;
		this.requestNo = requestNo;
		this.requestUri = requestUri;
		this.requestMsg = requestMsg;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getRequestNo() {
		return requestNo;
	}
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}
	public String getRequestUri() {
		return requestUri;
	}
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}
	public String getRequestMsg() {
		return requestMsg;
	}
	public void setRequestMsg(String requestMsg) {
		this.requestMsg = requestMsg;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
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
