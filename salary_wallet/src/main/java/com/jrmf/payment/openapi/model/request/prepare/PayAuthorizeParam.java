package com.jrmf.payment.openapi.model.request.prepare;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.prepare.PayAuthorizeResult;


/**
 * 支付授权请求
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class PayAuthorizeParam implements IBaseParam<PayAuthorizeResult> {

	private String name;		//	收款人姓名
	private String payAccount;	//	收款方账号(银行卡或第三方支付账号)
	private String orderNo;		//	订单号(发送银行的订单号)
	private String amount;		//	支付金额
	private String time;		//	申请时间
	private String mobile;		// 	手机号
	private String idcard;		// 	身份证号
	private String requestId;	//  请求唯一标识
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPayAccount() {
		return payAccount;
	}

	public void setPayAccount(String payAccount) {
		this.payAccount = payAccount;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the idcard
	 */
	public String getIdcard() {
		return idcard;
	}

	/**
	 * @param idcard the idcard to set
	 */
	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String requestURI() {
		return "/prepare/payment/authorize";
	}

	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}

	@Override
	public Class<?> respDataClass() {
		return PayAuthorizeResult.class;
	}
	
}
