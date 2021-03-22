package com.jrmf.utils;

import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * filename：com.jrmf.utils.StringUtil.java
 *
 * @author: linan
 * @time: 2015-9-16 下午3:32:47
 */
public class StringUtil {

    private static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * Author Nicholas-Ning
     * Description //TODO 去除字符串中的特殊字符以及空格
     * Date 18:35 2018/11/27
     * Param [str]
     * return java.lang.String
     **/
    public static String replaceSpecialStr(String str) {
        String regEx = "[\\u007f-\\u009f]|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥ \n\r\t% ……&*（）——+|{}【】‘；：”“’。，、？]|\\u00ad|[\\u0483-\\u0489]|[\\u0559-\\u055a]|\\u058a|[\\u0591-\\u05bd]|\\u05bf|[\\u05c1-\\u05c2]|[\\u05c4-\\u05c7]|[\\u0606-\\u060a]|[\\u063b-\\u063f]|\\u0674|[\\u06e5-\\u06e6]|\\u070f|[\\u076e-\\u077f]|\\u0a51|\\u0a75|\\u0b44|[\\u0b62-\\u0b63]|[\\u0c62-\\u0c63]|[\\u0ce2-\\u0ce3]|[\\u0d62-\\u0d63]|\\u135f|[\\u200b-\\u200f]|[\\u2028-\\u202e]|\\u2044|\\u2071|[\\uf701-\\uf70e]|[\\uf710-\\uf71a]|\\ufb1e|[\\ufc5e-\\ufc62]|\\ufeff|\\ufffc";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 判断是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        return !isEmpty(str) && str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    /**
     * 判断是否为空
     *
     * @param arg
     * @return
     */
    public static boolean isEmpty(String arg) {
        return (arg == null) || ("".equals(arg.trim()) || "null".equals(arg.trim()) || "NULL".equals(arg.trim()));
    }

    /**
     * 是否包含空字符串
     *
     * @param arg
     * @return
     */
    public static boolean hasNullStr(String arg) {
        return arg == null || "".equals(arg.trim())
                || "null".equalsIgnoreCase(arg.trim());
    }

    /**
     * 验证邮箱是否正确
     *
     * @param searchPhrase 邮箱
     * @return 布尔值
     */
    public static boolean isEmail(final String searchPhrase) {
        if (!isEmpty(searchPhrase)) {
            final String check = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
            final Pattern regex = Pattern.compile(check);
            final Matcher matcher = regex.matcher(searchPhrase);
            return matcher.matches();
        }
        return false;
    }

    /**
     *
     */
    public static String ifNullAsNullStr(String str) {
        String result = "";
        if (!isEmpty(str)) {
            result = str;
        }
        return result;
    }

    private static String baseString(BigInteger num, int base) {
        String str, digit = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (num.shortValue() == 0) {
            return "0";
        } else {
            BigInteger valueOf = BigInteger.valueOf(base);
            str = baseString(num.divide(valueOf), base);
            return str + digit.charAt(num.mod(valueOf).shortValue());
        }
    }


    public static String tenTo36(String orderNo) {
        String result = baseString(new BigInteger(orderNo), 36);
        StringBuilder addResult = new StringBuilder();
        if (result.length() < 6) {
            for (int i = 0; i < (6 - result.length()); i++) {
                addResult.append("0");
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
        return addResult.append(result).toString();
    }

    /**
     * 获得交易流水号
     *
     * @return
     */
    public static String getChannelSerialno() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String newDate = sdf.format(new Date());
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            result.append(random.nextInt(10));
        }
        return newDate + result;
    }

    /**
     * 获得交易批次号
     *
     * @return
     */
    public static String getBatchNo() {
        String serialno = new SimpleDateFormat("yyyyMMddhhmmssSSS")
                .format(new Date());
        return serialno + GetRandomNumberStr4();
    }

    public static String GetRandomNumberStr4() {
        Random r = new Random();
        return "" + (r.nextInt(9000) + 1000);
    }

    /**
     * 数字长度不够右对齐左补0
     *
     * @param num
     * @param length
     * @return
     */
    public static String fillNumWithLeft(String num, int length) {
        StringBuffer sb = new StringBuffer();
        if (!isEmpty(num)) {
            int i = getStrLength(num);
            if (i > length) {
                sb.append(limitString(num, length));
                // sb.append(num.substring(0, length));
            } else {
                for (; i < length; i++) {
                    sb.append("0");
                }
                sb.append(num);
            }

        } else {
            for (int i = 0; i < length; i++) {
                sb.append("0");
            }

        }
        return sb.toString();
    }

    /**
     * 获得字符串长度,中文两个长度
     *
     * @param str
     * @return
     */
    public static int getStrLength(String str) {
        if (!isEmpty(str)) {
            try {
                return str.getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
                return 0;
            }
        }
        return 0;
    }

    public static String limitString(String str, int subLength) {
        try {
            if (str == null) {
                return "";
            } else {
                int tempSubLength = subLength;//
                String subStr = str.substring(0, Math.min(str.length(), subLength));
                int subByetsLength = subStr.getBytes("GBK").length;// 截取子串的字节长度
                // 说明截取的字符串中包含有汉字
                while (subByetsLength > tempSubLength) {
                    int subLengthTemp = --subLength;
                    subStr = str.substring(0,
                            (Math.min(subLengthTemp, str.length())));
                    subByetsLength = subStr.getBytes("GBK").length;
                }
                return subStr;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    public static String formatDate(Date date, String format) {
        return (new SimpleDateFormat(format)).format(date);
    }

    public static String formatDate(Date date) {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
    }

    public static String formatDateShort(Date date) {
        return (new SimpleDateFormat("yyyy-MM-dd")).format(date);
    }

    public static String formatDateByShort(Date date) {
        return (new SimpleDateFormat("yyyyMMdd")).format(date);
    }

    /**
     * 字符串转时间
     *
     * @param dateStr
     * @param formatStr
     * @return
     */
    public static Date stringToDate(String dateStr, String formatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return date;
    }

    /**
     * 判断是否为金额，最多两位小数，且不能为负
     *
     * @return
     */
    public static boolean isMoney(String money) {
        if ("0.00".equals(money)) {
            return false;
        }
        //金额不得大于100W
        if (!isEmpty(money)) {
            if (money.length() > 9) {
                return false;
            }
            Pattern p = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");
            Matcher m = p.matcher(money);
            return m.matches();
        }
        return false;
    }


    public static String getFormatResult(String result, int d) {
        if (StringUtil.isNumber(result)) {
            result = new BigDecimal(result).setScale(d, BigDecimal.ROUND_HALF_UP) + "";
        } else {
            result = "0.00";
        }
        return result;
    }

    public static String getXSSFCell(XSSFCell cell) {
        String result;
        if (cell != null) {
            switch (cell.getCellType()) {
                case XSSFCell.CELL_TYPE_STRING:
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    result = cell.getStringCellValue();
                    break;
                case XSSFCell.CELL_TYPE_NUMERIC:
                case XSSFCell.CELL_TYPE_FORMULA: // 公式
                    String str = String.valueOf(cell.getNumericCellValue());
                    int eDiv = str.indexOf("E");
                    if (eDiv == -1) {
                        result = str;
                    } else {
                        str = new BigDecimal(str.substring(0, eDiv))
                                .multiply(new BigDecimal(Math.pow(10, Double.parseDouble(str.substring(eDiv + 1))))).toPlainString();
                        if (str.contains(".")) {
                            while (str.endsWith("0")) {
                                str = str.substring(0, str.length() - 1);
                            }
                            if (str.endsWith(".")) {
                                str += "00";
                            }
                        }
                        result = str;
                    }
                    break;
                case XSSFCell.CELL_TYPE_BLANK:
                    result = "";
                    break;
                case XSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                    result = cell.getBooleanCellValue() + "";
                    break;
                default:
                    result = "未知类型";
                    break;
            }
        } else {
            return "";
        }
        /**
         * 去除空格 小写转大写
         */
        //去除excel中的空格 字符编码  160
        result=result.replace(String.valueOf((char)160)," ");
        result = result.trim();
        result = result.toUpperCase();
        return result;
    }


    /**
     * 获得六位号随机数
     *
     * @return
     */
    public static String GetRandomNumberStr6() {
        Random r = new Random();
        // return "888888";
        return "" + (r.nextInt(900000) + 100000);
    }

    public static String getStringRandom() {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < 20; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    private static volatile String currentTimeStr = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

    private static volatile int serialNo = 0;

    public static synchronized String getBankOrderNO() {
        String preTimeStr = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

        if (!currentTimeStr.equals(preTimeStr)) {
            currentTimeStr = preTimeStr;
            serialNo = 0;
        }
        return currentTimeStr + format(++serialNo);
    }

    private static String format(int source) {
        StringBuilder sb = new StringBuilder(String.valueOf(source));
        for (; sb.length() < 3; sb.insert(0, '0')) ;
        return sb.toString();
    }

    /**
     * 是否是银行卡
     *
     * @param bankCard
     * @return
     */
    public static boolean checkBankCard(String bankCard) {
        if (bankCard.length() < 10 || bankCard.length() > 21) {
            return false;
        }
        return getBankCardCheckCode(bankCard);
    }

    /**
     * 说明:校验卡号正确性，仅仅校验位数和是否由数字组成
     *
     * @param nonCheckCodeBankCard
     * @return:
     */
    public static boolean getBankCardCheckCode(String nonCheckCodeBankCard) {
//		 if(nonCheckCodeBankCard == null || nonCheckCodeBankCard.trim().length() == 0
//		         || !nonCheckCodeBankCard.matches("\\d+")) {
//			 //如果传的不是数据返回N
//			 return 'N';
//		 }
//		 char[] chs = nonCheckCodeBankCard.trim().toCharArray();
//		 int luhmSum = 0;
//		 for(int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
//		     int k = chs[i] - '0';
//		     if(j % 2 == 0) {
//		         k *= 2;
//		         k = k / 10 + k % 10;
//		     }
//		     luhmSum += k;
//		 }
//		 return (luhmSum % 10 == 0) ? '0' : (char)((10 - luhmSum % 10) + '0');
        char[] charArray = nonCheckCodeBankCard.toCharArray();
        for (char c : charArray) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 功能：身份证的有效验证
     *
     * @param IDStr 身份证号
     * @return 有效：返回"" 无效：返回String信息
     * @throws ParseException
     */
    public static boolean iDCardValidate(String IDStr) throws ParseException {
        String Ai = "";
        // ================ 号码的长度 15位或18位 ================
        if (IDStr.length() != 15 && IDStr.length() != 18) {
            return false;
        }
        // =======================(end)========================

        // ================ 数字 除最后一位都为数字 ================
        if (IDStr.length() == 18) {
            Ai = IDStr.substring(0, 17);
        } else if (IDStr.length() == 15) {
            Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
        }
        if (!isNumeric(Ai)) {
            return false;
        }
        // =======================(end)========================

        // ================ 出生年月是否有效 ================
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        if (!isDate(strYear + "-" + strMonth + "-" + strDay)) {
            return false;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - s.parse(
                    strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                return false;
            }
        } catch (NumberFormatException | ParseException e) {
            logger.error(e.getMessage(), e);
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            return false;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            return false;
        }
        // =====================(end)=====================

        // ================ 地区码时候有效 ================
        @SuppressWarnings("rawtypes")
        Hashtable h = GetAreaCode();
        return h.get(Ai.substring(0, 2)) != null;
        // ==============================================

        // ================ 判断最后一位的值 ================
       /* int TotalmulAiWi = 0;
        for (int i = 0; i < 17; i++) {
            TotalmulAiWi = TotalmulAiWi
                    + Integer.parseInt(String.valueOf(Ai.charAt(i)))
                    * Integer.parseInt(Wi[i]);
        }
        int modValue = TotalmulAiWi % 11;
        String strVerifyCode = ValCodeArr[modValue];
        Ai = Ai + strVerifyCode;
        if (IDStr.length() == 18) {
            if (Ai.equals(IDStr) == false) {
                return false;
            }
        } else {
            return true;
        }*/
        // =====================(end)=====================
    }

    /**
     * 校验身份证是否合法
     *
     * @param cardId
     * @return boolean true 合法
     * false 非法
     */
    public static boolean checkCertId(String cardId) {
        if (cardId.length() == 15 || cardId.length() == 18) {
            if (!cardCodeVerifySimple(cardId)) {
                logger.error("15位或18位身份证号码不正确");
                return false;
            } else {
                if (cardId.length() == 18 && !cardCodeVerify(cardId)) {
                    logger.error("18位身份证号码不符合国家规范");
                    return false;
                }
            }
        } else {
            logger.error("身份证号码长度必须等于15或18位");
            return false;
        }
        return true;
    }

    /**
     * 正则校验身份证是否符合第一代第二代标准
     *
     * @param card
     * @return boolean true 合法
     * false 非法
     */
    private static boolean cardCodeVerifySimple(String card) {
        //第一代身份证正则表达式(15位)
        String isIDCard1 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        //第二代身份证正则表达式(18位)
        String isIDCard2 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[A-Z])$";

        //验证身份证
        if (card.matches(isIDCard1) || card.matches(isIDCard2)) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    private static boolean cardCodeVerify(String card) {
        int i = 0;
        String r = "error";

        i += Integer.parseInt(card.substring(0, 1)) * 7;
        i += Integer.parseInt(card.substring(1, 2)) * 9;
        i += Integer.parseInt(card.substring(2, 3)) * 10;
        i += Integer.parseInt(card.substring(3, 4)) * 5;
        i += Integer.parseInt(card.substring(4, 5)) * 8;
        i += Integer.parseInt(card.substring(5, 6)) * 4;
        i += Integer.parseInt(card.substring(6, 7)) * 2;
        i += Integer.parseInt(card.substring(7, 8));
        i += Integer.parseInt(card.substring(8, 9)) * 6;
        i += Integer.parseInt(card.substring(9, 10)) * 3;
        i += Integer.parseInt(card.substring(10, 11)) * 7;
        i += Integer.parseInt(card.substring(11, 12)) * 9;
        i += Integer.parseInt(card.substring(12, 13)) * 10;
        i += Integer.parseInt(card.substring(13, 14)) * 5;
        i += Integer.parseInt(card.substring(14, 15)) * 8;
        i += Integer.parseInt(card.substring(15, 16)) * 4;
        i += Integer.parseInt(card.substring(16, 17)) * 2;
        i = i % 11;
        String last = card.substring(17, 18);
        if (i == 0) {
            r = "1";
        }
        if (i == 1) {
            r = "0";
        }
        if (i == 2) {
            r = "x";
        }
        if (i == 3) {
            r = "9";
        }
        if (i == 4) {
            r = "8";
        }
        if (i == 5) {
            r = "7";
        }
        if (i == 6) {
            r = "6";
        }
        if (i == 7) {
            r = "5";
        }
        if (i == 8) {
            r = "4";
        }
        if (i == 9) {
            r = "3";
        }
        if (i == 10) {
            r = "2";
        }
        return r.equals(last.toLowerCase());
    }

    /**
     * 功能：设置地区编码
     *
     * @return Hashtable 对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 功能：判断字符串是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 功能：判断字符串是否为日期格式
     *
     * @return
     */
    public static boolean isDate(String strDate) {
        Pattern pattern = Pattern
                .compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isChinese(String str) {
        //增加少数名族中文名校验
        String regEx = "^[\u3400-\u9fa5]+(·[\u3400-\u9fa5]+)*$";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find())
            flg = true;

        return flg;
    }

    public static boolean isMobileNO(String phone) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[1|8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }

    /**
     * 判断是否为11位数字
     * @param phone  手机号
     * @return true/false
     */
    public static boolean isMobileNOBy11(String phone) {
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile("^1\\d{10}$");
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }

    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n; // (先计算出余数)
        int number = source.size() / n; // 然后是商
        int offset = 0;// 偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    public static String isValidateData(String amount, String idCard,
                                        String bankCard, String mobileNo, String name) {
        logger.info("验证格式" + name);
        String msg = "";
        try {
            if (!StringUtil.isEmpty(mobileNo) && !StringUtil.isMobileNO(mobileNo)) {
                logger.info(mobileNo + "手机号格式错误");
                msg = "手机号格式错误";
                return msg;
            }
            if (!StringUtil.isEmpty(idCard) && !StringUtil.iDCardValidate(idCard)) {
                logger.info(idCard + "身份证号格式错误");
                msg = "身份证号格式错误";
                return msg;
            }
            if (!StringUtil.isEmpty(bankCard) && !StringUtil.checkBankCard(bankCard)) {
                logger.info(bankCard + "银行卡号格式错误");
                msg = "银行卡号格式错误";
                return msg;
            }
            if (!StringUtil.isEmpty(amount) && !StringUtil.isMoney(amount)) {
                logger.info(amount + "金额格式错误");
                msg = "金额格式错误";
                return msg;
            }
            if (!StringUtil.isEmpty(name) && !StringUtil.isChinese(name)) {
                logger.info(name + "姓名格式错误");
                msg = "姓名格式错误";
                return msg;
            }
        } catch (Exception e) {
            logger.info("格式校验错误");
            msg = "格式校验错误";
            return msg;
        }

        return msg;
    }



    /**
     * 拆单时list为空的不返回,防止多开线程
     *
     * @author linsong
     * @date 2019/4/28
     */
    public static <T> List<List<T>> averageAssign2(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n; // (先计算出余数)
        int number = source.size() / n; // 然后是商
        int offset = 0;// 偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            if (value.size() != 0) {
                result.add(value);
            }
        }
        return result;
    }


    /**
     * 根据身份证号计算年龄
     * @param idCard
     * @return
     */
    public static String checkAge(String idCard, Integer minAge, Integer maxAge) {

        String msg = null;

        if (minAge == null && maxAge == null) {
            return msg;
        }
        try {
            if (!StringUtil.isEmpty(idCard) && !StringUtil.iDCardValidate(idCard)) {
                logger.info(idCard + "身份证号格式错误");
                msg = "身份证号格式错误";
                return msg;
            }


            if (idCard.length() == 18) {
                //截取身份证中出行人出生日期中的年、月、日
                Integer personYear = Integer.parseInt(idCard.substring(6, 10));
                Integer personMonth = Integer.parseInt(idCard.substring(10, 12));
                Integer personDay = Integer.parseInt(idCard.substring(12, 14));

                Calendar cal = Calendar.getInstance();
                // 得到当前时间的年、月、日
                Integer yearNow = cal.get(Calendar.YEAR);
                Integer monthNow = cal.get(Calendar.MONTH) + 1;
                Integer dayNow = cal.get(Calendar.DATE);

                // 用当前年月日减去生日年月日
                int yearMinus = yearNow - personYear;
                int monthMinus = monthNow - personMonth;
                int dayMinus = dayNow - personDay;

                Integer age = yearMinus; //先大致赋值

                if (yearMinus == 0) { //出生年份为当前年份
                    age = 0;
                } else { //出生年份大于当前年份
                    if (monthMinus < 0) {//出生月份小于当前月份时，还没满周岁
                        age = age - 1;
                    }
                    if (monthMinus == 0) {//当前月份为出生月份时，判断日期
                        if (dayMinus < 0) {//出生日期小于当前月份时，没满周岁
                            age = age - 1;
                        }
                    }
                }

                if (minAge != null && age < minAge) {
                    msg = "年龄小于服务公司限制";
                    return msg;
                }

                if (maxAge != null && age > maxAge) {
                    msg = "年龄大于服务公司限制";
                    return msg;
                } else if (age.equals(maxAge) && !(monthMinus == 0 && dayMinus == 0)) {
                    msg = "年龄大于服务公司限制";
                    return msg;
                }


            } else if (idCard.length() == 15) {
                msg = "年龄超过服务公司限制";
                return msg;
            }


        } catch (Exception e) {
            logger.info("格式校验错误");
            msg = "格式校验错误";
        }

        return msg;
    }


    public static String convertStreamToString(InputStream is) {

        StringBuilder sb = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            logger.error("字符转换读取IO异常");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.error("字符转换流关闭IO异常");
            }
        }

        return sb.toString();
    }

    //手机号加*号脱敏处理
    public static String rePhone(String realPhone) {
        String phoneNumber = "";
        if (realPhone.length() == 11) {
            phoneNumber = realPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        } else {
            phoneNumber = realPhone;
        }

        return phoneNumber;
    }


    /**身份证号脱敏 前三后四中间* */
    public static String desensitizedIdNumber(String idNumber) {
        if (isEmpty(idNumber)) {
            return "";
        }
        if (idNumber.length() == 15) {
            idNumber = idNumber.replaceAll("(\\w{3})\\w*(\\w{4})", "$1******$2");
        }
        if (idNumber.length() == 18) {
            idNumber = idNumber.replaceAll("(\\w{3})\\w*(\\w{4})", "$1*********$2");
        }
        return idNumber;
    }

    /**银行卡号脱敏，前六后四中间 * */
    public static String desensitizedBankNo(String bankNo){
        if (isEmpty(bankNo)){
            return "";
        }
        return bankNo.replaceAll("(\\w{6})\\w*(\\w{4})", "$1******$2");
    }

    public static String replaceHeadTailSpecialChar(String str){
        if (isEmpty(str)){
            return str;
        }
        str=str.replace("\u202d","").replace("\u202c","");
        return str;
    }
   // 压缩

    public static String zipString(String unzip) {

        Deflater deflater = new Deflater(9); // 0 ~ 9 压缩等级 低到高

        deflater.setInput(unzip.getBytes());

        deflater.finish();

        final byte[] bytes = new byte[256];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);

        while (!deflater.finished()) {

            int length = deflater.deflate(bytes);

            outputStream.write(bytes, 0, length);

        }

        deflater.end();

        return new sun.misc.BASE64Encoder().encodeBuffer(outputStream.toByteArray());

    }
    /**
     * 字符串的解压
     */
    public static String unCompress(String str) throws IOException {
        if (null == str || str.length() <= 0) {
            return str;
        }
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(str
            .getBytes("ISO-8859-1"));
        // 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n = 0;
        while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
            // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
            out.write(buffer, 0, n);
        }
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toString("GBK");
    }

    public static String insertSubString(String string,String subString,int offset){
        StringBuffer stringBuffer =new StringBuffer(string);
        for (int i=0;i<stringBuffer.length();i++){
            if (i%offset==0){
                stringBuffer.insert(i,subString);
            }
        }
        return stringBuffer.toString();
    }

    private static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

}
