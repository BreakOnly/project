package com.jrmf.payment.openapi.constants;

/**
 * 身份认证类型
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public enum CertificationType {

	/**二要素认证（姓名+身份证）*/_2element("2", "二要素认证"),
	/**三要素认证（姓名+身份证+银行卡）*/_3element("3", "二要素认证"),
	/**四要素认证（姓名+身份证+银行卡+手机）*/_4element("4", "二要素认证"),

	IDENTITY_TYPE_ID_CARD("0","居民身份证"),
	IDENTITY_TYPE_TMP_ID_CARD("F","临时居民身份证"),
	IDENTITY_TYPE_PASSPORT("1","护照"),
	IDENTITY_TYPE_HK_PASS("B","港澳居民往来内地通行证"),
	IDENTITY_TYPE_TW_PASS("C","台湾居民来往大陆通行证"),
	IDENTITY_TYPE_FR_RESIDENCE_PERMIT("P","外国人永久居留证");
	
	private final String code;
	private final String alias;

	private CertificationType(String code, String alias) {
		this.code = code;
		this.alias = alias;
	}

	public String getCode() {
		return code;
	}

	public String getAlias() {
		return alias;
	}

	
}
