/**
 *
 */
package com.jrmf.utils;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * filename：com.jrmf.utils.ArithmeticUtil.java
 *
 * @author: linan
 * @time: 2015-9-17 下午5:38:06
 */
public class ArithmeticUtil {
    /**
     * 浮点型相加,返回值为字符串
     * @param v1
     * @param v2
     * @return
     */
    public static String addStr(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(4);
        nf.setGroupingUsed(false);
        String Str = subZeroAndDot(nf.format(b1.add(b2).doubleValue()));
        return formatDecimals(Str);
    }

    /**
     * 四舍五入
     */
    public static String getScale(String value, int scale) {
        BigDecimal b = new BigDecimal(ArithmeticUtil.getFormatDouble(value));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }
    /**
     * 四舍五入 返回BigDecimal
     */
    public static BigDecimal getBigDecimalScale(String value, int scale) {
        BigDecimal b = new BigDecimal(ArithmeticUtil.getFormatDouble(value));
        return b.setScale(scale,BigDecimal.ROUND_HALF_UP);
    }
    /**
     * 浮点型相减,返回值为字符串
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String subStr(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(4);
        nf.setGroupingUsed(false);
        return nf.format(b1.subtract(b2).doubleValue());

    }

    /**
     * 默认返回0
     *
     * @param str
     *            除数
     * @return
     */
    public static String getFormatDouble(String str) {
        if (StringUtil.isNumber(str)) {
            return str;
        }
        return "0";
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        if (StringUtil.isEmpty(s)){
            return "";
        }

        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");// 去掉多余的0
            s = s.replaceAll("[.]$", "");// 如最后一位是.则去掉
            s = s.replaceAll(" ", "");// 去掉多余的空格
        }
        return s;
    }

    /**
     * 判断字符串是否为0
     *
     * @param v1
     * @return
     */
    public static boolean isZero(String v1) {
        if (!StringUtil.isNumber(v1)) {
            return true;
        }
        int c = compareTod("0", v1);
        return c == 0;
    }

    /**
     * 比较大小
     *
     * @param v1
     * @param v2
     * @return
     * 如果指定的数与参数相等返回0
     * 如果指定的数小于参数返回 -1
     * 如果指定的数大于参数返回 1
     */
    public static int compareTod(String v1, String v2) {
        if (!StringUtil.isNumber(v1)) {
            return -1;
        }
        if (!StringUtil.isNumber(v2)) {
            return 1;
        }
        return new BigDecimal(v1).compareTo(new BigDecimal(v2));
    }

    /**
     * 浮点型相乘,返回值为字符串
     *
     * @param v1
     * @param v2
     * @return
     */

    public static String mulStr(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(4);
        nf.setGroupingUsed(false);
        return subZeroAndDot(nf.format(b1.multiply(b2).doubleValue()));
    }

    public static String mulStr(String v1, String v2, int digits) {
        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(digits);
        nf.setGroupingUsed(false);
        return nf.format(b1.multiply(b2).doubleValue());
    }

    public static String mulStr(String v1, String v2, int digits,int roundingMode) {
        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        //BigDecimal.ROUND_HALF_UP
        return nf.format(b1.multiply(b2).setScale(digits, roundingMode).doubleValue());
    }

    public static String formatDecimals(String v1) {
        if (StringUtil.isEmpty(v1)) {
            return "";
        }
        DecimalFormat decimalFormat = new DecimalFormat("###############0.00");
        return decimalFormat.format(Double.parseDouble(v1));
    }


    /**
     * 去除空格
     *
     * @param s
     * @return
     */
    public static String subSpace(String s) {
        s = s.replaceAll("\\s*", "");
        return s;
    }

    /**
     * 去除导入姓名空格
     *
     * @param s
     * @return
     */
    public static String subNameSpace(String s) {
        String name = s.replaceAll("\\s*", "");
        if (StringUtil.isChinese(name)) {
            return name;
        } else {
            return s.trim();
        }
    }


    /**
     * 保持两位小数
     *
     * @param s
     * @return
     */
    public static String addZeroAndDot(String s) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(new BigDecimal(s));
    }

    /**
     * 浮点型相除,返回值为字符串
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String divideStr(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        BigDecimal c = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
        return c.toString();

    }

    public static String divideStr2(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        BigDecimal c = b1.divide(b2);
        return c.toString();

    }

    /**
     * %运算取商
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String modStr(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        return String.valueOf(b1.divideAndRemainder(b2)[0].intValue());

    }

    /**
     * %运算取余
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String modStr2(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        return b1.divideAndRemainder(b2)[1].toString();

    }


    /**
     * 浮点型相减,返回值为字符串
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String subStr2(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        return nf.format(b1.subtract(b2).doubleValue());

    }


    /**
     *
     * @param v1 字符串类型数字
     * @param v2 字符串类型数字
     * @return 返回较小的
     */
    public static String getLesser(String v1, String v2) {
        return compareTod(v1, v2) < 0 ? v1 : v2;
    }


    /**
     * 不限制返回double
     */
    public static String addStr2(String v1, String v2) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(4);
        nf.setGroupingUsed(false);
        String Str = subZeroAndDot(nf.format(b1.add(b2)));

        if (StringUtil.isEmpty(Str)) {
            return "";
        }
        DecimalFormat decimalFormat = new DecimalFormat("###############0.0000");
        return decimalFormat.format(Double.parseDouble(Str));
    }


    /**
     * 判断第二个数字是否在第一个数字的区间浮动范围内
     *
     * @param lowInterval 浮动下限 两位小数
     * @param upInterval  浮动上限 两位小数
     */
    public static boolean isInTheInterval(String v1, String v2, String lowInterval, String upInterval) {
        //浮动上限
        BigDecimal upFloatLimits = new BigDecimal(getFormatDouble(upInterval));
        //浮动下限
        BigDecimal lowerFloatLimits = new BigDecimal(getFormatDouble(lowInterval));
        //源数值
        BigDecimal n1 = new BigDecimal(getFormatDouble(v1));
        //比较浮动范围的数值
        BigDecimal n2 = new BigDecimal(getFormatDouble(v2));
        //实际到账金额上限值
        BigDecimal upDic = n1.multiply(upFloatLimits).setScale(2, BigDecimal.ROUND_HALF_UP);
        //实际到账金额下限值
        BigDecimal lowerDic = n1.multiply(lowerFloatLimits).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (n2.compareTo(upDic) > 0 || n2.compareTo(lowerDic) < 0) {
            return false;
        }

        return true;
    }

    /**
     * 校验数字区间是否有重叠
     * @param list 格式:amountStart-amountEnd
     * @date 2019/11/5
     */
    public static boolean checkOverlap(List<String> list) {

        list.sort((s1, s2) -> { //排序ASC
            Double d1 = Double.valueOf(s1.split("-")[0]);
            Double d2 = Double.valueOf(s2.split("-")[0]);
            return d1.compareTo(d2);
        });

        boolean flag = false;//是否重叠标识
        for (int i = 0; i < list.size(); i++) {
            //跳过第一个不做判断
            if (i > 0) {

                String[] amountStart = list.get(i).split("-");
                for (int j = 0; j < list.size(); j++) {
                    if (j == i || j > i) {
                        continue;
                    }

                    String[] amountEnd = list.get(j).split("-");
                    int compare = ArithmeticUtil.compareTod(amountStart[0], amountEnd[1]);

                    if (compare < 0) {
                        flag = true;
                        break;//只要存在一个重叠则可退出内循环
                    }
                }
            }

            //当标识已经认为重叠了则可退出外循环
            if (flag) {
                break;
            }
        }

        return flag;
    }


    /**
     * 浮点型相加,返回值为字符串
     * @param v1
     * @param v2
     * @return
     */
    public static String addStr(String v1, String v2, int digits) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));
        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(digits);
        nf.setGroupingUsed(false);
        String Str = subZeroAndDot(nf.format(b1.add(b2).doubleValue()));
        return formatDecimals(Str);
    }

    @Test
    public static void main(String[] args) {

//        System.out.println(modStr("552.9","170"));
//        System.out.println(modStr2("552.9","170"));

//        System.out.print(ArithmeticUtil.addStr2("0.0672", "1"));
        List<String> list = new ArrayList<>();
        list.add("500-20000");
//        list.add("500");
//        list.add("28000-99999.99");
//        list.add("90000");
//        list.add("299999");
        list.add("299999-300000");
        list.add("30000-100000");

//        list.sort((s1, s2) -> {
//            Double d1 = Double.valueOf(s1.split("-")[0]);
//            Double d2 = Double.valueOf(s2.split("-")[0]);
//            return d1.compareTo(d2);
//        });

//        System.out.println(list);
//
//        System.out.println(ArithmeticUtil.checkOverlap(list));

        String s = mulStr("352", "0.0056", 2, BigDecimal.ROUND_HALF_UP);
        System.out.println(s);

        String balance = ArithmeticUtil.getScale(ArithmeticUtil.mulStr("1.2222","100"),0);

        System.out.println("aaaaa"+balance);
    }

    /**
     * 浮点型相减,返回值为字符串
     *
     * @param v1
     * @param v2
     * @return
     */
    public static String subStr(String v1, String v2, int digits) {

        BigDecimal b1 = new BigDecimal(getFormatDouble(v1));

        BigDecimal b2 = new BigDecimal(getFormatDouble(v2));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(digits);
        nf.setGroupingUsed(false);
        return nf.format(b1.subtract(b2).doubleValue());

    }

}
