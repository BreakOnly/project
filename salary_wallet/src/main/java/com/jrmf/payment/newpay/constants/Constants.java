package com.jrmf.payment.newpay.constants;

/**
 * 常量类
 *
 * @author juny-zhang
 *
 */
public final class Constants {

	/** 商户ID【请如实填写】. */
	public static final String MER_ID = "11000001234";
	/** 接收异步通知的URL地址【请如实填写】. */
	public static final String NOTIFY_URL = "http://www.xx.com/notifyUrl.do";

	/** 新生付款接口请求地址. */
	public static final String URL_PAY = "https://gateway.hnapay.com/website/singlePay.do";
	/** 新生付款查询接口请求地址. */
	public static final String URL_PAY_QUERY = "https://gateway.hnapay.com/website/singlePayQuery.do";

	/** 交易码 单笔付款-SGP01. */
	public static final String TRAN_CODE_PAY = "SGP01";
	/** 交易码 单笔付款查询-SGP02. */
	public static final String TRAN_CODE_PAY_QUERY = "SGP02";

	/** 版本号 2.1. */
	public static final String TRAN_VERSION = "2.1";
	/** 版本号 2.0. */
	public static final String QUERY_VERSION = "2.0";
	/** 签名类型 RSA-1. */
	public static final String SIGN_TYPE_RSA = "1";
	/** 编码方式 UTF8-1. */
	public static final String CHARSET_UTF8 = "1";

	/** 付款类型 付款到银行-1. */
	public static final String PAY_TYPE_TO_BANK = "1";
	/** 付款类型 付款到账户-1. */
	public static final String PAY_TYPE_TO_ACCOUNT = "2";

	/** 是否需要复核 不需要-0. */
	public static final String AUDIT_FLAG_NO = "0";
	/** 是否需要复核 需要-1. */
	public static final String AUDIT_FLAG_YES = "1";

	/** 收款方类型 个人-1. */
	public static final String PAYEET_YPE_PERSON = "1";
	/** 收款方类型 企业-2. */
	public static final String PAYEET_YPE_CORP = "2";

	/** 新生付款类RSA秘钥公钥(HEX字符串) */
	public static final String HNAPAY_PAY_PUBLIC_KEY_HEX = "30819f300d06092a864886f70d010101050003818d00308189028181009253cd86336904affb3d9af52632f6639bf62ad799d64875a5fab3de98e3f4fd6182bbbc48f7a3a72bde0803960c31d9a8bb6ecd964d8cf3e836f4810e065780c19908f0c5bd14ef1c78523e974bb206e8ac77a18428f2ce07b0906fb5ca922b3f6bd74bab147e6868968240bbfc4f1d94d526254579dd31ab819db28957b2550203010001";

	/** 对应TXT文件中的hnapay.partner.storepass. */
	public static final String MER_KEY_STROREPWD = "uhI9e2";
	/** 对就TXT文件中的hnapay.partner.alias. */
	public static final String MER_KEY_KEYALIAS = "hnapaySH";
	/** 对应TXT文件中的hnapay.partner.pwd. */
	public static final String MER_KEY_PRIKEYPWD = "OHgiBc";
}
