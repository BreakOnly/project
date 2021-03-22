package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author 种路路
 * @create 2019-06-19 11:20
 * @desc
 **/
public class AddMerchantInvoiceInfoServiceAttachment  extends ActionAttachment {
    private String addTime;

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
