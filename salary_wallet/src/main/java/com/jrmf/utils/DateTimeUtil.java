package com.jrmf.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/13 17:41
 * Version:1.0
 */
public class DateTimeUtil {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String Date2String(Date date){
        return sdf.format(date);
    }
    public static Date String2Date(String date){
        Date parse = null;
        try {
            if(!StringUtil.isEmpty(date)){
                parse = sdf.parse(date);
            }
        } catch (Exception e) {
            return null;
        }
        return parse;
    }
}
