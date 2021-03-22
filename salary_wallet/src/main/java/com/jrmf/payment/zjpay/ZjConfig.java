package com.jrmf.payment.zjpay;

public class ZjConfig {
	
	//单笔转账接口type
	public static final String SINGLE_TRANSFER_METHOD="4530";
	//单笔转账查询接口type
	public static final String SINGLE_TRANSFER_QUERY_METHOD="4532";
	//中金ssl证书路径
	public static final String SSL_PATH="/data/server/salaryboot/zjssl/trust.jks";
	//中金密钥库密码
	public static final String KEYSTORE_PASS = "cfca1234";
}
