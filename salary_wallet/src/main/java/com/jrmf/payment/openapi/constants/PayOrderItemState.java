/**
 * 
 */
package com.jrmf.payment.openapi.constants;

/**
 * @author Napoleon.Chen
 * @date 2018年12月21日
 */
public enum PayOrderItemState {

	Paying(20, "支付中"),
    Success(30, "支付成功"),
    Fail(40, "支付失败"),
    ;

    private int value;
    private String name;

    PayOrderItemState(int value, String name) {
        this.value = value;
        this.name = name;
    }

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
}
