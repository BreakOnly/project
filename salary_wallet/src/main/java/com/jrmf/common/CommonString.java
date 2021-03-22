package com.jrmf.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/11/15 10:17
 * Version:1.0
 */
@Component
public class CommonString {
    public static final String ROOT = "mfkj";
    public static final String MINBALANCE = "0.00";
    public static final String CUSTOMKEY = "customkey";
    public static final String COMPANYID = "companyId";
    public static final String PAYTYPE = "payType";
    public static final String BALANCE = "amount";
    public static final String STATUS = "status";
    public static final String TRANSFERTYPE = "transfertype";
    public static final String PROCESS = "process";
    public static final String ORDERNAME = "佣金预充值订单";
    public static final int JRMF_PLATFORM_ID = 1;
    public static final int JRMF_PLATFORM_LIMIT_OPEN = 1;
    /**
     * 生产域名
     */
    public static final String PORTAL_DOMAIN = "wallet-s.jrmf360.com";
    /**
     * 退款
     **/
    public static final int REFUND = 1;
    public static final int EFFECTIVE = 1;
    /**
     * 扣款
     **/
    public static final int DEDUCTION = -1;
    public static final int ADDITION = 1;
    public static final int UNEFFECTIVE = -1;
    /**
     * redis 下发参数的超时时间
     */
    public static int LIFETIME;
    public static final int COMPANY = 2;
    public static final String CUSTOMLOGIN = "customLogin";
    public static final String CUSTOMLOGINACCOUNTID = "customLoginAccountId";
    public static String EXECLPATH;

    public int getLIFETIME() {
        return LIFETIME;
    }

    @Value("${lifetime}")
    public void setLIFETIME(int LIFETIME) {
        CommonString.LIFETIME = LIFETIME;
    }

    @Value("${splitexeclpath}")
    public void setEXECLPATH(String EXECLPATH) {
        CommonString.EXECLPATH = EXECLPATH;
    }

    public static final String INPUTTEMPLATECONF_BANKCARD_FIRST = "银行卡批量打款模板(单个批次文件最大支持2000条订单，证件类型 1 身份证  2 港澳台通行证 3 护照  4 军官证)";
    public static final String INPUTTEMPLATECONF_BANKCARD_SECEND = "姓名（必填）银行卡号（必填）银行卡验证(选填)身份证号（必填）手机号（必填）金额（必填）所属银行（选填）证件类型（必填）备注（选填）";
    public static final String INPUTTEMPLATECONF_ALIPAY_FIRST = "支付宝批量打款模板(单个批次文件最大支持2000条订单)";
    public static final String INPUTTEMPLATECONF_ALIPAY_SECEND = "姓名（必填）金额（必填）支付宝账号（必填）手机号（必填）身份证号（必填）证件类型（必填）备注（选填）";
    public static final String INPUTTASKTEMPLATECONF_BANKCARD_FIRST = "收款人姓名证件类型证件号收款账号账号所属金融机构结算金额结算交易时间单位价格(元)单位标签完成任务量绩效费其他附加费用任务名称任务类型业务订单号";
    public static final String INPUT_USERTEMPLATE_FIRST = "姓名（必填）证件类型（必填）身份证号（必填）用户手机号（选填）备注（选填）";
    // 并发处理拆单的线程数。
    public static final Integer CONCURRENTLIMIT = 10;

    // linux 生产路径
//    public static String EXECLPATH = "/data/server/salaryboot/splitorder";
    // linux 测试路径
//    public static String EXECLPATH = "/data/server/salaryboot/upload";
    // windows 本地路径
//  public static String EXECLPATH = "F:/ExcelSplit";
}
