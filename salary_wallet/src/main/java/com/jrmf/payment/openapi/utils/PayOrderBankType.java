/**
 * 
 */
package com.jrmf.payment.openapi.utils;

/**
 * @author Napoleon.Chen
 * @date 2018年11月14日
 */
public enum PayOrderBankType {

	ALIPAY("alipay","支付宝"),
    WXPACK("wxpack","微信红包"),
    WX("wx","微信"),
    BANK("bank","银行卡"),
    ;
	
    public final String value;
    public final String name;

    PayOrderBankType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
	
}
