package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 签约状态
 *
 **/
public enum BestSignType {

    /**
     * 1-创建
     2-签约处理中
     3-签约待审核
     4-签约失败
     5-签约成功
     6-回调地址
     */
    SIGN_PROCESSING(1,"待签约"),

    SIGN_SUCCESS(2,"签约成功"),

    SIGN_FAIL(3,"签约失败"),

    SIGN_CHECK_FAIL(4,"爱员工验签失败"),

    SIGN_URL(5,"/extr/order/submit"),

    RETURN_URL(6,"/wallet/subscriber/bestsign/signSuccess.do");

    private final  int code;
    private final String desc;

    BestSignType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
