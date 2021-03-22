package com.jrmf.taxsettlement.api;

public enum APIDefinition {

	VERIFY_3_FACTORS("三要素验证"),

	TRANSFER_TO_BANK_CARD("银行卡下发"),

	TRANSFER_TO_ALIPAY_ACCOUNT("支付宝账户下发"),

	TRANSFER_TO_WECHAT_ACCOUNT("微信下发"),

	QUERY_MERCHANT_BALANCE("商户余额查询"),

	QUERY_TRANSFER_DEAL("下发交易查询"),

	QUERY_TRANSFER_BATCH("下发批次查询"),

	GET_DAY_SERIAL_FILE_URL("日流水文件URL获取"),

	GET_MONTH_REPORT_FILE_URL("月报表文件URL获取"),

	ADD_MERCHANT_TRANSFER_RECEIVER("新增商户收款者"),

    ADD_INVOICE_RECEIVER("新增开票收件人地址"),

    ADD_MERCHANT_INVOICE_INFO("新增商户开票信息"),

    QUERY_INVOICE_RECEIVER("开票收件人地址查询"),

    QUERY_MERCHANT_INVOICE_INFO("商户开票信息查询"),

    APPLY_INVOICE("发票申请"),

    QUERY_INVOICE_HISTORY("发票申请记录"),

	  QUERY_INVOICE_SUMMARY_HISTORY("发票统计信息查询"),

    MERCHANT_RECHARGE("商户充值"),

    RECHARGE_QUERY_ACCOUNT("查询充值账户信息"),

    QUERY_RECHARGE_RECORD("充值记录查询"),

	  QUERY_RECHARGE_LIST_RECORD("充值记录列表查询"),

	BIND_USER_WECHAT_ACCOUNT("绑定用户微信账号"),

	SIGN_AGREEMENT("签约协议"),

    QUERY_ORDER_QUOTA("可发放额度查询"),

    PREPARE_UNIFIED_ORDER("发放风控预授权"),

    SYNC_PREPARE_UNIFIED_ORDER("同步预授权下发结果"),

	QUERY_MERCHANT_USER_CONTRACT("商户用户签约查询"),
	YUNCR_USER_AUTHENTICATION_UPLOAD("上传认证文件"),
	YUNCR_USER_AUTHENTICATION_VIDEO_INFO("获取文案信息"),
	YUNCR_USER_AUTHENTICATION_TRUE_NAME("实名验证"),
	YUNCR_USER_AUTHENTICATION_INDIVIDUAL_REGISTER("提交注册工商户"),
	YUNCR_USER_AUTHENTICATION_APPROVAL_STATUS("查询申请状态"),
	YUNCR_USER_AUTHENTICATION_BANK_CARD("绑定银行卡"),
	YUNCR_USER_AUTHENTICATION_BUSINESSLICENSE_DOWNLOAD("下载营业执照");
	private String apiDesc;

	private APIDefinition(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getApiDesc() {
		return apiDesc;
	}
}
