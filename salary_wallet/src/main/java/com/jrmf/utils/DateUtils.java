package com.jrmf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * filename：com.jrmf.utils.DateUtils.java
 *
 * @author: zhangyong
 * @time: 2013-10-23下午4:38:50
 */

public class DateUtils {

	private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

	/**
	 * 获得几天前时间
	 *
	 * @param day
	 * @return
	 */
	public static Date getBeforeDay(int day) {
		return getBeforeDay(null, day);
	}

	public static String getBeforeDayString(int day) {
		Date date = getBeforeDay(null, day);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}

	public static Date getBeforeDay(Date date, int day) {
		if (date == null) {
			date = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -day);
		date = calendar.getTime();
		return date;
	}

	/**
	 * 获得后几天时间
	 *
	 * @param day
	 * @return
	 */
	public static Date getAfterDay(int day) {
		return getAfterDay(null, day);
	}

	public static String getAfterDayString(int day) {
		Date date = getAfterDay(null, day);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}

	public static String getAfterDayString02(int day) {
		Date date = getAfterDay(null, day);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(date);
	}

	public static Date getAfterDay(Date date, int day) {
		if (date == null) {
			date = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		date = calendar.getTime();
		return date;
	}

	public static Date getAfterDateByYears(Date date, int years) {
		if (date == null) {
			date = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, years);
		date = calendar.getTime();
		return date;
	}


	/**
	 * 获得几分钟前时间
	 *
	 * @param minute
	 * @return
	 */
	public static Date getBeforeMinute(int minute) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -minute);
		date = calendar.getTime();
		return date;
	}

	/**
	 * 获得几天前时间
	 *
	 * @param day
	 * @return
	 */
	public static String getBeforeDayStr(int day) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -day);
		date = calendar.getTime();
		return StringUtil.formatDate(date);
	}

	public static String getBeforeDayStrByNYR(int day) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -day);
		date = calendar.getTime();
		return StringUtil.formatDateByShort(date);
	}

	public static String getBeforeDayStrByYear(int year) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -year);
		date = calendar.getTime();
		return StringUtil.formatDate(date);
	}

	public static String getBeforeDayStrShort(int day) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -day);
		date = calendar.getTime();
		return StringUtil.formatDateShort(date);
	}

	public static String getBeforeDayStrShort(String timeStr, String sdf,
			int day) {
		SimpleDateFormat format = new SimpleDateFormat(sdf);
		try {
			Date date = format.parse(timeStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, -day);
			date = calendar.getTime();
			return new SimpleDateFormat(sdf).format(date);
		} catch (ParseException e) {
			logger.error(e.getMessage(),e);
		}
		return "";
	}
	//相差天数
	public static int daysBetween(String timeStr, String sdf)
    {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = null;
		Date date2 = null;;
		try {
			date1 = format.parse(timeStr);
			date2 = format.parse(sdf);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
		}
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

       return Integer.parseInt(String.valueOf(between_days));
    }



	//当前时间是指定日期的之后几天
	public static int daysBetween(String sdf)
    {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = new Date();
			Date date2 = null;;
			try {
				date2 = format.parse(sdf);
			} catch (ParseException e) {
				logger.error(e.getMessage(),e);
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(date1);
			long time1 = cal.getTimeInMillis();
			cal.setTime(date2);
			long time2 = cal.getTimeInMillis();
			long between_days=(time1-time2)/(1000*3600*24);

            return Integer.parseInt(String.valueOf(between_days));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
			return 0;
		}
    }





	public static String getAfterDayStrShort(String timeStr, String sdf, int day) {
		SimpleDateFormat format = new SimpleDateFormat(sdf);
		try {
			Date date = format.parse(timeStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, day);
			date = calendar.getTime();
			return new SimpleDateFormat(sdf).format(date);
		} catch (ParseException e) {
			logger.error(e.getMessage(),e);
		}
		return "";
	}

	/**
	 * 获得几分钟前时间
	 *
	 * @param minute
	 * @return
	 */
	public static String getBeforeMinuteStr(int minute) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -minute);
		date = calendar.getTime();
		return StringUtil.formatDate(date);
	}

	/**
	 * 获得几秒前时间
	 *
	 * @return
	 */
	public static String getBeforeSecondStr(int second) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, -second);
		date = calendar.getTime();
		return StringUtil.formatDate(date);
	}

	// /**
	// * 字符串转换成日期
	// *
	// * @param str
	// * @return date
	// * @throws java.text.ParseException
	// */
	// public static Date StrToDate(String str, String temp)
	// throws java.text.ParseException {
	// SimpleDateFormat format = new SimpleDateFormat(temp);
	// Date date = format.parse(str);
	// return date;
	// }

	/**
	 * @title 判断是否为工作日
	 * @detail 工作日计算: 1、正常工作日，并且为非假期 2、周末被调整成工作日
	 * @author str
	 * @param sdf
	 *            日期
	 * @return 是工作日返回true，非工作日返回false
	 * @throws ParseException
	 */
	public static boolean isWeekday(String str, String sdf)
			throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(sdf);
		Date date = format.parse(str);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return (calendar.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY && calendar
				.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY);
	}





	/**
	 * @title 判断是否为工作日
	 * @detail 工作日计算: 1、正常工作日，并且为非假期 2、周末被调整成工作日
	 * @author str
	 * @param sdf
	 *            日期
	 * @return 是工作日返回true，非工作日返回false
	 * @throws ParseException
	 */
	public static boolean isWeekdayExceptFriday(String str, String sdf)
			throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(sdf);
		Date date = format.parse(str);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return (calendar.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY && calendar
				.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY && calendar
				.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.FRIDAY);
	}

	/**
	 * @return
	 */
	public static String getInsertTime(String str)
			throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(str);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		GregorianCalendar gcalendar = new GregorianCalendar();
		gcalendar.setTime(date);
		int day = 0;
		// 周二周三周四 为周六计息----周五周六周天周一为周三计息
		if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY){
			day = 4;//周二-周六
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY){
			day = 3;//周三-周六
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY){
			day = 2;//周四-周六
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY){
			day = 5;//周五-周三
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY){
			day = 4;//周六-周三
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY){
			day = 3;//周天-周三
		}else{
			day = 2;//周一-周三
		}
		calendar.add(Calendar.DAY_OF_MONTH, day);
		date = calendar.getTime();
		return format.format(date);
	}


	public static boolean isWeekday(String str, int weekday)
			throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(str);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(GregorianCalendar.DAY_OF_WEEK) == weekday;
	}

	/**
	 *
	 * @param date1 最近时间
	 * @param date2 最远时间
	 * @param day 差几天
	 * @return
	 * @throws ParseException
	 */
	public static boolean isDaysBefore(String date1, String date2, int day)
			throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date d1 = format.parse(date1);
		Date d2 = format.parse(date2);
		return diffTime(d1, d2) > (day * 12 * 3600 * 1000);
	}

	public static boolean isDaysIn(String date1, String date2, int day)
			throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date d1 = format.parse(date1);
		Date d2 = format.parse(date2);
		return diffTime(d1, d2) == (day * 24 * 3600 * 1000);
	}


	/**
	 * 获取当前时间
	 *
	 * @return
	 */
	public static String getNowTime() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		temp_str = sdf.format(dt);
		return temp_str;
	}
	/**
	 * 获取当前时间
	 *
	 * @return
	 */
	public static String getNowDate() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		temp_str = sdf.format(dt);
		return temp_str;
	}

	/**
	 * 获取当前日期
	 *
	 * @return
	 */
	public static String getNowDay() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		temp_str = sdf.format(dt);
		return temp_str;
	}

	/**
	 * 获取当前日期
	 *
	 * @return
	 */
	public static String getNowDayZH() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd");
		temp_str = sdf.format(dt);
		return temp_str;
	}
	/**
	 * 获取当前日期
	 *
	 * @return
	 */
	public static String getNowDay2() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		temp_str = sdf.format(dt);
		return temp_str;
	}


	/**
	 * 获取当前月份
	 *
	 * @return
	 */
	public static String getNowMonth() {
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		temp_str = sdf.format(dt);
		return temp_str;
	}

	/**
	 * 获取月份
	 *
	 * @return
	 */
	public static String getMonth(String dateStr){
		String temp_str = "";
		Date dt = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		if(!StringUtil.isEmpty(dateStr)){
			try {
				dt = format.parse(dateStr);
			} catch (ParseException e) {
				logger.error(e.getMessage(),e);
			}
		}
		format = new SimpleDateFormat("yyyy-MM");
		temp_str = format.format(dt);
		return temp_str;
	}



	/**
	 * 获得时间差
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int diffDate(Date d1, Date d2) {
		if (null == d1 || null == d2) {
			return -1;
		}
		return (int) ((d1.getTime() - d2.getTime()) / 86400000);
	}

	/**
	 * 获得时间差
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static String diffmin(Date d1, Date d2) {
		if (null == d1 || null == d2) {
			return "";
		}
		String day = "" + (int) ((d1.getTime() - d2.getTime()) / 86400000);

		int left1 = (int) ((d1.getTime() - d2.getTime()) % 86400000);

		String hour = "" + (int) (left1 / 3600000);

		int left2 = (int) (left1 % 3600000);

		String min = "" + (int) (left2 / 60000);

		return day + "天" + hour + "小时" + min + "分钟";
	}

	public static long diffTime(Date d1, Date d2) {
		if (null == d1 || null == d2) {
			return -1;
		}
		return d1.getTime() - d2.getTime();
	}

	/**
	 * 获取本月的第一天
	 * @return
	 */
	public static Date getMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return calendar.getTime();
	}

	/**
	 * 得到本月的最后一天
	 *
	 * @return
	 */
	public static Date  getMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return calendar.getTime();
	}

	/**
	 * 得到指定月的最后一天
	 *
	 * @return
	 */
	public static String getMonthLastDay(String date) throws ParseException {
		Calendar calendar = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(df.parse(date));
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return df.format(calendar.getTime());
	}


	public static String getWeekendDay(String day) throws ParseException {
		String weekendDay = "";
		if (isWeekday(day, GregorianCalendar.MONDAY)) {
			weekendDay = "星期一";
		} else if (isWeekday(day, GregorianCalendar.TUESDAY)) {
			weekendDay = "星期二";
		} else if (isWeekday(day, GregorianCalendar.WEDNESDAY)) {
			weekendDay = "星期三";
		} else if (isWeekday(day, GregorianCalendar.THURSDAY)) {
			weekendDay = "星期四";
		} else if (isWeekday(day, GregorianCalendar.FRIDAY)) {
			weekendDay = "星期五";
		} else if (isWeekday(day, GregorianCalendar.SATURDAY)) {
			weekendDay = "星期六";
		} else {
			weekendDay = "星期日";
		}
		return weekendDay;
	}







	/**
	 * 获取计息日（活期宝）
	 * @return
	 */
	public static String getInterestTime(String str)
			throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(str);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		GregorianCalendar gcalendar = new GregorianCalendar();
		gcalendar.setTime(date);
		int day = 0;
		// 周一周而周三 为周四计息----周四周五周六周天为周一计息
		if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY){
			day = 3;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY){
			day = 2;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY){
			day = 1;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY){
			day = 4;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY){
			day = 3;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY){
			day = 2;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY){
			day = 1;
		}
		calendar.add(Calendar.DAY_OF_MONTH, day);
		date = calendar.getTime();
		return format.format(date);
	}

	/**
	 * 获取当前时间的calender
	 * @return
	 */
	public static Calendar getCurrCalender(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal;
	}

	/**
	 * 格式化日期
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formartDate(Date date, String pattern){
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 格式化表单传来的日期数据
	 * @param dateStr
	 * @param patternFrom
	 * @param patternTo
	 * @return
	 * @throws ParseException
	 */
	public static String formartDateStr(String dateStr, String patternFrom, String patternTo) throws ParseException{
		if(StringUtil.isEmpty(dateStr) || StringUtil.isEmpty(patternFrom) || StringUtil.isEmpty(patternTo)){
			return null;
		}
		SimpleDateFormat fmt1 = new SimpleDateFormat(patternFrom);
		SimpleDateFormat fmt2 = new SimpleDateFormat(patternTo);
		return fmt2.format(fmt1.parse(dateStr));
	}

	public static String dateDiff(String startTime, String endTime,
			String format) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat(format);
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		// 获得两个时间的毫秒时间差异
		long diff = 0;
		long day = 0;
		long hour = 0;
		try {
			diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
			day = diff / nd;// 计算差多少天
			hour = diff % nd / nh;// 计算差多少小时
			// long min = diff % nd % nh / nm;// 计算差多少分钟
			// long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
		}

		return day+"天" + hour + "小时";
	}
	public static long dateDiffByDay(String startTime, String endTime,
                                     String format) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat(format);
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		// 获得两个时间的毫秒时间差异
		long diff = 0;
		long day = 0;
		long hour = 0;
		try {
			diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
			day = diff / nd;// 计算差多少天
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
		}

		return day;
	}

	/**
	 * 获取当前calender的前一个年、月、日时间字符串
	 * @param cal
	 * @param field
	 * @param pattern
	 * @return
	 */
	public static String getOneStepAgo(Calendar cal, int field, String pattern){
		getOneStepAgo(cal,field);
		return new SimpleDateFormat(pattern).format(cal.getTime());
	}

	/**
	 * 获取上一周周日是几号
	 * @throws ParseException
	 */
	public static String getDay(String nowDay) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(nowDay);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		GregorianCalendar gcalendar = new GregorianCalendar();
		gcalendar.setTime(date);
		int day = 8;
		if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY){
			day = 1;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY){
			day = 2;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY){
			day = 3;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY){
			day = 4;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY){
			day = 5;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY){
			day = 6;
		}else if(gcalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY){
			day = 7;
		}
		calendar.add(Calendar.DAY_OF_MONTH, -day);
		date = calendar.getTime();
		return format.format(date);
	}

	/**
	 * 获取当前calender的前一个年、月、日时间
	 * @param cal
	 * @param field
	 * @return
	 */
	public static Date getOneStepAgo(Calendar cal, int field){
		cal.add(field, -1);
		return cal.getTime();
	}

	/**
	 * 获取上个月的月份
	 */
	public static String getLastDate() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        date = cal.getTime();
        return sdf.format(date);
    }

	/**
	 * 获取上个月的最后一天
	 */
	public static String getLastMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, month-1);
		calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

	/**
	 * 获取上个月的第一天
	 */
	public static String getLastMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, month-1);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

	/**
	 * 获取指定月的下个月第一天
	 */
	public static String getNextMonthFirstDay(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sdf.parse(date));
		int month = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, month+1);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		Date nextMonth = calendar.getTime();
        return sdf.format(nextMonth);
	}

	public static String getAfterDayMonthString(int day) {
		Date date = getAfterDay(null, day);
		SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
		return format.format(date);
	}

	public static String getAfterDayMonthFormatString(int day) {
		Date date = getAfterDay(null, day);
		SimpleDateFormat format = new SimpleDateFormat("MM-dd");
		return format.format(date);
	}

	/**
	 * 根据月份，格式：YYYY-MM-dd 获取当月的第一天的开始时间
	 * @param month
	 * @return
	 */
	public static String monthToStartTime(String month){
		return month + " 00:00:00";
	}

	/**
	 * 根据月份，格式：YYYY-MM-dd 获取当月的最后一天的结束时间
	 * @param month
	 * @return
	 */
	public static String monthToEndTime(String month){
		try {
			String monthLastDay = getMonthLastDay(month);
			return monthLastDay + " 23:59:59";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return month + "-31 23:59:59";
	}

	public static void main(String[] args) throws ParseException {
		String s = monthToEndTime("2020-05"+"-01");
		System.out.println(s);
	}


	public static boolean isValidDate(String str) {
		boolean convertSuccess = true;
		// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写； 2017-11-30 09:58:18
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			// 设置lenient为false.
			// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
//			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// logger.error(e.getMessage(),e);
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}
	public static boolean isValidDate2(String str) {
		boolean convertSuccess = true;
		// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写； 2017-11-30 09:58:18
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			// 设置lenient为false.
			// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
//			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// logger.error(e.getMessage(),e);
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}

	public static int isSameDay(String time, String timeLater){
		int isSameDay = 0;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = null;
		Date date2 = null;;
		try {
			date1 = format.parse(time);
			date2 = format.parse(timeLater);
			isSameDay = date1.compareTo(date2);
		} catch (ParseException e) {
			logger.error(e.getMessage(),e);
		}
		return isSameDay;
	}

    /**
     * 获取几个月前的月份yyyy-MM
     * @param several  几个月前
     * @return String yyyy-MM
     */
	public static String getSeveralMonthAgo(int several){
        LocalDate date = LocalDate.now();
        date = date.minusMonths(several);
        return date.toString().substring(0,7);
	}

	/**
	 * 校验是否为日期
	 * @param pDateObj
	 * @return
	 */
	public static boolean checkValidDate(String pDateObj) {
		boolean ret = true;
		if (pDateObj == null || pDateObj.length() != 8) {
			ret = false;
		}
		try {
			int year = new Integer(pDateObj.substring(0, 4)).intValue();
			int month = new Integer(pDateObj.substring(4, 6)).intValue();
			int day = new Integer(pDateObj.substring(6)).intValue();
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);   //允许严格检查日期格式
			cal.set(year, month - 1, day);
			cal.getTime();//该方法调用就会抛出异常
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	/**
	 * 判断时间是否在服务范围
	 */
	public static Boolean DateCompare(Date time1,Date time2,int numYear) {
		Date time3 = add(time1, Calendar.YEAR,numYear);
		if(time3.getTime()<time2.getTime()){
			return true;
		}
		return false;
	}

	public static Date add(final Date date, final int calendarField, final int amount) {
		if (date == null) {
			return null;
		}
		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}
}
