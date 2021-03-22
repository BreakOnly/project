package com.jrmf.payment.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
* @author zhanghuan
*
 */

public class PayRespCode {
	
	public static final String RESP_CODE = "code";
	public static final String RESP_MSG = "msg";
	
	public static final String RESP_SUCCESS = "0000";
	public static final String RESP_UNKNOWN = "0001";
	public static final String RESP_FAILURE = "9999";
	public static final String RESP_CHECK_FAIL = "9001";
	public static final String RESP_CHECK_COUNT_FAIL = "9002";
	public static final String AYG_RESP_ORDER_NOEXISTS = "2002";
	public static final String PA_RESP_ORDER_NOEXISTS = "MA0103";
	public static final String PA_RESP_ORDER_NOEXISTS_KHKF = "YQ9996";
	public static final String ALI_RESP_ORDER_NOEXISTS = "40004";
	
	public static final String ALI_TRANS_CODE_SUCCESS = "10000";
	public static final String ALI_ORDER_STATUS_SUCCESS = "SUCCESS";
	public static final String ALI_ORDER_STATUS_FAIL = "FAIL";
	public static final String ALI_ORDER_STATUS_DEALING = "DEALING";
	public static final String ALI_ORDER_STATUS_INIT = "INIT";
	public static final String ALI_ORDER_STATUS_REFUND = "REFUND";
	public static final String ALI_ORDER_STATUS_UNKNOWN = "UNKNOWN";
	
	public static final String RESP_TRANSFER_SUCCESS = "T0000";
	public static final String RESP_TRANSFER_UNKNOWN = "T0001";
	public static final String RESP_TRANSFER_FAILURE = "T9999";
	public static final String RESP_NETWORK_EXCEPTION = "T9998";

	
	public static final Map<String, String> codeMaps = new HashMap<String, String>();
	
	static {
		codeMaps.put(RESP_SUCCESS, "成功");
		codeMaps.put(RESP_FAILURE, "失败");
		codeMaps.put(RESP_UNKNOWN, "处理中");
		codeMaps.put(RESP_NETWORK_EXCEPTION, "网络异常");
		codeMaps.put(RESP_TRANSFER_UNKNOWN, "处理中");
	}
}
