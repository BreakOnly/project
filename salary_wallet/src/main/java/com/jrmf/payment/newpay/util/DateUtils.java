package com.jrmf.payment.newpay.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * 
 * @author juny-zhang
 *
 */
public class DateUtils {
	public final static String DEFAULT_PATTERN_DAY = "yyyyMMdd";
	public final static String DEFAULT_PATTERN = "yyyyMMddHHmmss";
	public final static String DEFAULT_PATTERN_MILLISECOND = "yyyyMMddHHmmssSSS";

	/**
	 * 获取当前时间(格式:yyyyMMddHHmmss)
	 */
	public static String getCurrTimeDay() {
		return format(new Date(), DEFAULT_PATTERN_DAY);
	}

	/**
	 * 获取当前时间(格式:yyyyMMddHHmmss)
	 */
	public static String getCurrTime() {
		return format(new Date(), DEFAULT_PATTERN);
	}

	/**
	 * 获取当前时间(格式:yyyyMMddHHmmssSSS)
	 * 
	 * @return
	 */
	public static String getCurrTimeMillisecond() {
		return format(new Date(), DEFAULT_PATTERN_MILLISECOND);
	}

	/**
	 * 日期格式化
	 *
	 * @param date
	 *            日期
	 * @param pattern
	 *            格式
	 * @return
	 */
	public static String format(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

}
