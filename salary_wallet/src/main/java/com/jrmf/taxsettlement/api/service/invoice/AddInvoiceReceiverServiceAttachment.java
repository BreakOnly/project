package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author chonglulu
 * @time: 2019年10月12日10:24:37
 */
public class AddInvoiceReceiverServiceAttachment extends ActionAttachment {
    private String addTime;

    @Override
    public String toString() {
        return "AddInvoiceReceiverServiceAttachment{" + "addTime='" + addTime + '\'' + '}';
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
