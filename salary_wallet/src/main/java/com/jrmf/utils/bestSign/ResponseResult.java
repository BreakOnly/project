package com.jrmf.utils.bestSign;

import com.alibaba.fastjson.JSON;

public class ResponseResult implements IObject {
	
	private int code;
	private String msg;
	private PayAuthorizeResult data;

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the data
	 */
	public PayAuthorizeResult getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(PayAuthorizeResult data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

}
