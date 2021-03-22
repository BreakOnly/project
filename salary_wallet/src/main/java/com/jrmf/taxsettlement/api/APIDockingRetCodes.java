package com.jrmf.taxsettlement.api;

public enum APIDockingRetCodes {

	COMMUNICATION_ABORT("D001", "通讯中断"),

	VERIFY_CODE_NOT_EFFECTIVE("P001", "验证码已失效"),

	VERIFY_CODE_NOT_MATCH("P002", "验证码不匹配"),

	ILLEGAL_ACCESS("S001", "非法访问"),

	SIGN_ERROR("S002", "签名验证失败"),

	FIELD_LACK("S003", "缺少参数"),

	FIELD_FORMAT_ERROR("S004", "参数格式错误"),

	ACCESS_BEYOND_LIMIT("S005", "超限访问"),

	SAME_MODE_SET("M001", "相同对接模式已设定"),

	REQUEST_NO_EXISTED("T001", "请求单号重复"),

	REQUEST_NO_NOT_EXISTED("T002", "请求单号不存在"),

	DEAL_NO_NOT_EXISTED("T003", "处理单号不存在"),

	TRANSFER_CORPORATION_NOT_EXISTED("T004", "服务公司不存在"),

	NO_CONTRACT_WITH_AGENT("T005", "协议失效"),

	BALANCE_NOT_SUFFICIENT("T006", "商户余额不足"),

	NO_CONTRACT_WITH_USER("T007", "未签约"),

	AMOUNT_BEYOND_LIMIT("T008", "交易超限额"),

	REAL_NAME_VERIFY_FAILED("T009", "实名校验失败"),

	BATCH_NO_EXISTED("T010", "下发批次号重复"),

	BATCH_NO_NOT_EXISTED("T011", "下发批次号不存在"),

	RATECONF_NO_NOT_EXISTED("T012", "无对应费率档位配置信息"),//提示信息无意义，应该抽离统一提示信息--例如系统不支持请联系客户人员等等呢个

	ORDER_AMOUNT_BEYOND_LIMIT("T013", "单笔交易超限额"),

	DAY_AMOUNT_BEYOND_LIMIT("T014", "日累计交易超限额"),

	MONTH_AMOUNT_BEYOND_LIMIT("T015", "月累计交易超限额"),

    QUARTER_AMOUNT_BEYOND_LIMIT("T016", "季度累计交易超限额"),

    AGREEMENT_TEMPLATE_NOT_FOUND("T017", "商户未配置协议，请联系客户人员"),

    AGREEMENT_IS_SIGNING_OR_PRE_REVIEW("T018", "协议签署中或者正在审核中，请稍后再试"),

    IMAGE_IO_EXCEPTION("T019", "图片解析异常"),

    AGREEMENT_NOT_FOUND("T020", "不存在需要签约的协议"),

    USER_NOT_FOUND("T021", "用户不存在"),

	COMPANY_MONTH_AMOUNT_BEYOND_LIMIT("T022", "实际服务公司月累计交易超限额"),

	COMPANY_QUARTER_AMOUNT_BEYOND_LIMIT("T023", "服务公司季度累计交易超限额"),

    SERIAL_NO_EXISTED("T024", "请求流水号重复"),

    CALCULATE_TYPE_NO_EXISTED("T025", "下发可用额度计算方式未设置"),

    PARAMETER_ANALYSIS_ERROR("T026", "参数解码异常"),

    ORDER_IS_DONE_OR_NOT_FOUND("T027", "订单已落地或订单不存在"),

    COMPANY_PAYMENT_LIMIT_NOT_FOUND("T028", "服务公司限额未配置"),

	ID_ERROR_OR_AGE_MAX("T029", "身份证格式错误或年龄不符合服务公司限制"),

	BLACK_USER("T030", "风控限制用户，请联系运营人员"),

	RECHARGE_TYPE_ERROR("T031", "充值类型错误"),

    FEE_TYPE_ERROR("T032", "服务费类型错误"),

    FEE_RATE_ERROR("T033", "服务费率错误"),

    CHARGE_AMOUNT_ERROR("T034", "充值金额计算错误"),

    CHARGE_BALANCE_AMOUNT_ERROR("T035", "充值到账金额计算错误"),

    RECHARGE_INFO_NOT_FOUND("T036", "充值账户未配置"),

    RECHARGE_INFO_ERROR("T037", "充值信息错误"),

    FEE_RATE_NOT_FOUND("T038", "服务费率未配置"),

    FEE_RATE_GRADE_NOT_FOUND("T039", "服务费费率档位未配置"),

    AMOUNT_TOO_SMALL_ERROR("T040", "下发金额不能小于最小下发金额"),

    COMPANY_RECHARGE_ACCOUNT_DISABLED("T041", "服务公司该收款账户充值限制，请联系客户经理！"),

    COMPANY_RECHARGE_DISABLED("T042", "服务公司充值限制，请联系客户经理！"),

    IMAGE_NOT_FOUND("T043", "图片未上传"),

    MERCHANT_INVOICE_INFO_NOT_FOUND("T044", "商户开票信息不存在"),

    MERCHANT_INVOICE_INFO_NOT_AVAILABLE("T045", "商户开票信息未审核"),

    CUSTOM_INVOICE_INFO_NOT_FOUND("T046", "收件人信息未找到或不可用"),

    APPLY_INVOICE_FINISH("T047", "开票申请已完成，没有可开票金额"),

    APPLY_INVOICE_PROCESSING("T048", "开票申请进行中，无法继续开票"),

    INVOICE_ClASS_DIFFERENT("T049", "部分开票，开票类目请保持一致"),

    NO_INVOICE_AMOUNT("T050", "没有可开票金额"),

    NO_ENOUGH_INVOICE_AMOUNT("T051", "申请开票金额大于可开票金额"),

    SINGLE_INVOICE_PROCESS_CAN_NOT_MERGE("T052", "部分开票订单·不能合并开票"),

    INVOICE_AMOUNT_NOT_EQUALS_ORDER_AMOUNT("T053", "部分开票订单·不能合并开票"),

    RECHARGE_ORDERS_NOT_FOUND("T054", "未查询到充值订单"),

    RECHARGE_ORDERS_NOT_SUCCESS("T055", "充值订单未成功"),

    DIFFERENT_SERVICE_COMPANY_CAN_NOT_MERGER_APPLY_INVOICE("T056", "不同的服务公司不能合并开票"),

    INVOICE_CLASS_NOT_FOUND("T057", "开票类目不存在"),

    SERVICE_COMPANY_NOT_FOUND("T058", "服务公司未配置"),

    INVOICE_TYPE_NOT_FOUND("T059", "开票类型不存在"),

    REAL_COMPANY_QUARTER_AMOUNT_BEYOND_LIMIT("T060", "实际服务公司月度累计交易超限额"),

    ACCOUNT_DATE_BEYOND_LIMIT("Q001", "账期日超出限制"),

    ACCOUNT_MONTH_BEYOND_LIMIT("Q002", "账期月超出限制"),

    DATA_FILE_NOT_EXISTED("Q003", "数据文件不存在"),

    SAME_CERTID_PAY("T061", "同一证件号并发访问请间隔5秒"),

    IDENTITY_CARD_PICTURE_NOT_FOUND("T062", "身份证图片信息未上传"),

    INDUSTRIAL_COMMERCIAL_PUBLICITY_PERSONNEL_SAME_NAME("T063", "工商公示人员同名"),

    NO_YUNCR_USER_AUTHENTICATION("T066", "未注册工商户"),

    UN_BIND_BANK_CARD("T068", "不在此个体工商户绑定的银行卡范围内"),

    UNSUPPORT_MERCHANT_MUTIL_SUBCONTRACT_COMPANY("T065","存在配置多条完税服务公司，请联系客户经理"),

    PLATFORM_MONTH_AMOUNT_BEYOND_LIMIT("T067","超系统9.8万限额"),

    ILLEGALITY_TIMESTAMP_PARAMETER("A041", "非法的时间戳参数"),

    TIME_NOT_SERVICE_AREA("A042", "时间不在服务范围");

	private final String code;

	private final String desc;

    APIDockingRetCodes(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
