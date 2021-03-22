package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author 种路路
 * @create 2019-08-22 16:13
 * @desc
 **/
public class ApplyInvoiceServiceAttachment extends ActionAttachment {
    private String addTime;

    @Override
    public String toString() {
        return "ApplyInvoiceServiceAttachment{" + "addTime='" + addTime + '\'' + '}';
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
