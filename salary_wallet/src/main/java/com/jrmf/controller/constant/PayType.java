package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 身份证类型
 **/
public enum PayType {

    /**
     * 1 银行电子户  2 支付宝  3 微信 4 银企直联（银行卡）
     */
    HS_BANK(1,"hs","银行电子户"),

    ALI_PAY(2,"alipay","支付宝"),

    WECHAT(3,"wechat","微信"),

    PINGAN_BANK(4,"bankcard","银行卡");

    private final  int code;
    private final String englishDesc;
    private final String desc;

    PayType(int code, String englishDesc, String desc) {
        this.code = code;
        this.englishDesc = englishDesc;
        this.desc = desc;
    }

    public String getEnglishDesc() {
        return englishDesc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static PayType codeOf(int code) {
        for(PayType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return HS_BANK;
    }
    
    public static void main(String[] args){
    	System.out.println(HS_BANK);
    }
}
