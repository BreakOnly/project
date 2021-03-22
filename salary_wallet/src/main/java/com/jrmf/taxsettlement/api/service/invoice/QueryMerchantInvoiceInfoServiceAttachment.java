package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

import java.util.List;

/**
 * @author 种路路
 * @create 2019年10月12日12:02:15
 * @desc
 **/
public class QueryMerchantInvoiceInfoServiceAttachment extends ActionAttachment {
    /**
     * 总条数
     */
    private String total;
    /**
     * 发票收件人地址
     */
    private List<MerchantInvoiceInfo> list;

    @Override
    public String toString() {
        return "QueryInvoiceReceiverServiceAttachment{" + "total='" + total + '\'' + ", list=" + list + '}';
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<MerchantInvoiceInfo> getList() {
        return list;
    }

    public void setList(List<MerchantInvoiceInfo> list) {
        this.list = list;
    }
}
