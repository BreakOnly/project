package com.jrmf.utils.bestSign;
public enum ConstantsEnum {

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

	CONTRACT_ORDER_SUB_STATE_SIGN_NOTNEED("0","待签署"),
	CONTRACT_ORDER_SUB_STATE_SIGNING("1","待签署"),
	CONTRACT_ORDER_SUB_STATE_SIGNED("2","已签署"),
	CONTRACT_ORDER_SUB_STATE_SIGN_REJECTED("3","拒绝签署")
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