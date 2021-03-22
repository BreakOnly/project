package com.jrmf.payment.openapi.param.econtract;

public enum ConstantsEnum {

	CONTRACT_RESULTCODE_ACCEPTED("ACCEPTED","受理成功"),
	CONTRACT_RESULTCODE_ACCEPTFAIL("ACCEPTFAIL","受理失败"),

	CONTRACT_RESULTCODE_SUCCESS("SUCCESS","成功"),
	CONTRACT_RESULTCODE_FAIL("FAIL","失败"),

	NOTIFY_RESULTCODE_SUCCESS("SUCCESS","通知成功"),
	NOTIFY_RESULTCODE_FAIL ("FAIL","通知失败"),

	SIGN_USER_TYPE_PERSONAL("1", "个人"), 
	SIGN_USER_TYPE_ENTERPRISE("2", "企业"), 
	SIGN_USER_TYPE_ENTERPRISE_PRO("2.1", "服务商"), 
	IDENTITY_TYPE_ID_CARD("0","居民身份证"),
	IDENTITY_TYPE_TMP_ID_CARD("F","临时居民身份证"),
	IDENTITY_TYPE_PASSPORT("1","护照"),
	IDENTITY_TYPE_HK_PASS("B","港澳居民往来内地通行证"),
	IDENTITY_TYPE_TW_PASS("C","台湾居民来往大陆通行证"),
	IDENTITY_TYPE_FR_RESIDENCE_PERMIT("P","外国人永久居留证"),

	REPEAT_FLAG_RETRYING("RETRYING","重发通知中"),
	REPEAT_FLAG_REDOING("REDOING","重试中"),
	REPEAT_FLAG_RENOTIFYING("RENOTIFYING","重新生成合同中"),

	IMP_MSG_ERROR_IDENTITY_TYPE("0","证件类型只支持身份证"),
	IMP_MSG_FORMAT_ERROR_ID("1","身份证号格式不正确"),
	IMP_MSG_FORMAT_ERROR_MOBILE("2","手机号码格式不正确"),
	IMP_MSG_DUPL_ID("3","身份证号码重复"),
	IMP_MSG_DUPL_MOBILE("4","手机号码重复"),
	IMP_MSG_USER_SIGNED("5","用户已签署或正签署中"),
	IMP_MSG_INVALID_NAME("6","姓名只允许输入中文、英文、数字"),
	IMP_MSG_NAME_OVER_LENGTH("7","姓名长度超长,已置空"),
	IMP_MSG_IDENTITY_TYPE_OVER_LENGTH("8","证件字段长度超长,已置空"),
	IMP_MSG_MOBILE_OVER_LENGTH("9","手机长度超长,已置空"),
	IMP_MSG_IDENTITY_OVER_LENGTH("10","证件号长度超长,已置空"),
	IMP_MSG_BLANK_NAME("11","姓名不能为空"),
	IMP_MSG_BLANK_MOBILE("12","手机不能为空"),
	IMP_MSG_DUPL_EXTORDER("13","外部订单号重复"),
	IMP_MSG_BLANK_EXTORDER("14","外部订单号不能为空"),
	IMP_MSG_EXTORDER_OVER_LENGTH("15","外部订单号长度超长,已置空"),
	IMP_MSG_INVALID_EXTORDER("16","外部订单号只能包含数字、英文、下划线，横杠"),

	CONTRACT_ORDER_STATE_IMP_SUCCESS("IMP_SUCCESS", "导入成功"), 
	CONTRACT_ORDER_STATE_IMP_ERR("IMP_ERR", "导入异常"), 
	CONTRACT_ORDER_STATE_AUTHING("AUTHING", "实名认证进行中"), 
	CONTRACT_ORDER_STATE_AUTH_ERR("AUTH_ERR", "实名认证异常"), 
	CONTRACT_ORDER_STATE_AUTH_FAIL("AUTH_FAIL", "实名认证不通过"),
	CONTRACT_ORDER_STATE_CREATE_ERR("CREATE_ERR", "合同生成异常"), 
	CONTRACT_ORDER_STATE_SIGN_ERR("SIGN_ERR", "签约异常"), 
	CONTRACT_ORDER_STATE_SIGNING("SIGNING", "待签约"),
	CONTRACT_ORDER_STATE_NOTIFY_ERR("NOTIFY_ERR", "签约通知异常"), 
	CONTRACT_ORDER_STATE_REJECTED("REJECTED", "已拒签"),
	CONTRACT_ORDER_STATE_EXPIRED("EXPIRED", "合同签署过期"),
	CONTRACT_ORDER_STATE_EXPIRE_CLOSED("EXPIRE_CLOSED", "合同签署过期关闭"),
	CONTRACT_ORDER_STATE_CANCEL("CANCEL", "已取消"),
	CONTRACT_ORDER_STATE_CLOSED("CLOSED", "已完成"),
	CONTRACT_ORDER_STATE_CLOSE_ERR("CLOSE_ERR", "完成异常"),
	
	CONTRACT_ORDER_STATE_FLAG_SIGNING("SIGN_FLAG_ING", "待签约"),
	CONTRACT_ORDER_STATE_FLAG_SIGN_FAIL("SIGN_FLAG_FAIL", "签约失败"),
	CONTRACT_ORDER_STATE_FLAG_RETRYING("SIGN_FLAG_RETRYING", "重发通知中"),

	CONTRACT_ORDER_SUB_STATE_SIGN_NOTNEED("0","待签署"),
	CONTRACT_ORDER_SUB_STATE_SIGNING("1","待签署"),
	CONTRACT_ORDER_SUB_STATE_SIGNED("2","已签署"),
	CONTRACT_ORDER_SUB_STATE_SIGN_REJECTED("3","拒绝签署"),
	CONTRACT_ORDER_SUB_STATE_SIGN_EXPIRED("4","过期签署"),
	CONTRACT_ORDER_SUB_STATE_SIGN_AWAIT("0","待确认"),
	;

	private String code;
	private String msg;

	private ConstantsEnum(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	private ConstantsEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
	public int getIntCode() {
		try {
			return Integer.parseInt(this.code);
		} catch (Exception e) {
			return this.code.hashCode();
		}
	}

	public String getMsg() {
		return msg;
	}

	public String toString() {
		return this.code;
	}
}
