package com.jrmf.payment.syxpay;

public class SyxConfig {
	
	//单笔转账接口type
	public static final String SINGLE_TRANSFER_METHOD="1";
	//单笔转账查询接口type
	public static final String SINGLE_TRANSFER_QUERY_METHOD="2";

	// 代付请求地址
	public static final String PAYMENT_REQ_URL = "/api/v3/tran/payment-apply";
	// 代付查询
	public static final String PAYMENT_QUERY_URL = "/api/v3/tran/query-payment-result";
}
