package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author 种路路
 * @create 2019-08-19 11:28
 * @desc
 **/
public class MerchantRechargeServiceAttachment extends ActionAttachment {
    private String orderNo;

    @Override
    public String toString() {
        return "MerchantRechargeServiceAttachment{" + "orderNo='" + orderNo + '\'' + '}';
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
