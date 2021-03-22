package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-08-22 16:53
 * @desc
 **/
public class QueryRechargeRecordServiceParams extends ActionParams {
    /**
     * 商户订单号
     */
    private String customOrderNo;
    /**
     * 智税通订单号
     */
    private String orderNo;

    @Override
    public String toString() {
        return "QueryRechargeRecordServiceParams{" + "customOrderNo='" + customOrderNo + '\'' + ", orderNo='" + orderNo + '\'' + '}';
    }

    public String getCustomOrderNo() {
        return customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
