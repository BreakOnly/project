package com.jrmf.payment.ympay;


public class YmConfig {
	
	/**
	 * c企业会员注册
	 */
	//企业信息注册接口url
	public static final String MERCHANT_REGISTER_URL="/ent/corApiUserRegister.do";
	//企业信息查询接口url
	public static final String MERCHANT_QUERY_URL="/ent/corApiQuery.do";
	//企业信息修改接口url
	public static final String MERCHANT_UPDATE_URL="/ent/corApiRegistUpdate.do";
	/**
	 * 线下充值账户绑定
	 */
	//企业信息账号绑定url
	public static final String ACCOUNT_BIND_URL="/ent/bindBankAcc.do";
	//已绑定银行账户查询接口url
	public static final String BANK_BIND_QUERY_URL="/ent/bankAccQuery.do";
	//解绑银行账户接口url
	public static final String BANK_ACC_UNBIND_URL="/ent/bankAccUnBind.do";
	/**
	 * 账号设置
	 */
	//用户授权接口url
	public static final String USER_BIND_URL="/ent/userBindApi.do";
	//账号设置接口url
	public static final String USER_SET_URL="/ent/userSettingApi.do";
	
	/**
	 * 支付相关接口
	 */
	//单笔汇款接口url
	public static final String SINGLE_TRANSFER_URL="/pay/danbihuikuan.do";
	//单笔汇款明细查询接口url
	public static final String SINGLE_TRANSFER_QUERY_URL="/pay_query/orderQuery.do";
	//汇款明细查询接口url
	public static final String TRANSFER_DETAIL_QUERY_URL="/pay/queryRemitDetail.do";
	
	//企业信息注册接口type
	public static final Integer MERCHANT_REGISTER_METHOD=1;
	//企业信息查询接口type
	public static final Integer MERCHANT_QUERY_METHOD=2;
	//企业信息修改接口type
	public static final Integer MERCHANT_UPDATE_METHOD=3;
	/**
	 * 线下充值账户绑定
	 */
	//企业信息账号绑定type
	public static final Integer ACCOUNT_BIND_METHOD=4;
	//已绑定银行账户查询接口type
	public static final Integer BANK_BIND_QUERY_METHOD=5;
	//解绑银行账户接口type
	public static final Integer BANK_ACC_UNBIND_METHOD=6;
	/**
	 * 账号设置
	 */
	//用户授权接口type
	public static final Integer USER_BIND_METHOD=7;
	//账号设置接口type
	public static final Integer USER_SET_METHOD=8;
	
	/**
	 * 支付相关接口
	 */
	//单笔汇款接口type
	public static final Integer SINGLE_TRANSFER_METHOD=9;
	//单笔汇款明细查询接口type
	public static final Integer SINGLE_TRANSFER_QUERY_METHOD=10;
	//汇款明细查询接口type
	public static final Integer TRANSFER_QUERY_DETAIL_METHOD=11;

}
