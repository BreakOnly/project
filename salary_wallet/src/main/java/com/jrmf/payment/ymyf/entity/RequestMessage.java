package com.jrmf.payment.ymyf.entity;


import java.io.Serializable;


/**
 * 公共接口请求信息实体类
 * @author Admin
 *
 */
public class RequestMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//请求标识(每一次请求唯一,在返回报文是原样返回)
	private String reqId;

	//商户号
	private String merId;
	
	//接口编码
	private String funCode;
	
	//版本信息
	private String version ="V1.0";
	
	//请求数据
	private String reqData;
	
	
	private String remark1;
	private String remark2;

	//签名
	private String sign;
	
	//IP
	private String ip;

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getFunCode() {
		return funCode;
	}

	public void setFunCode(String funCode) {
		this.funCode = funCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getReqData() {
		return reqData;
	}

	public void setReqData(String reqData) {
		this.reqData = reqData;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	@Override
	public String toString() {
		return "RequestMessage [reqId=" + reqId + ", merId=" + merId
				+ ", funCode=" + funCode + ", version=" + version
				+ ", reqData=" + reqData + ", remark1=" + remark1
				+ ", remark2=" + remark2 + ", sign=" + sign + ", ip=" + ip
				+ "]";
	}


}
