package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

import java.util.List;

/**
 * @author 种路路
 * @create 2019-08-22 16:13
 * @desc
 **/
public class QueryInvoiceHistoryServiceAttachment extends ActionAttachment {
    /**
     * 总条数
     */
    private String total;
    /**
     * 发票历史记录
     */
    private List<InvoiceHistory> list;

    @Override
    public String toString() {
        return "QueryInvoiceHistoryServiceAttachment{" + "total='" + total + '\'' + ", list=" + list + '}';
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<InvoiceHistory> getList() {
        return list;
    }

    public void setList(List<InvoiceHistory> list) {
        this.list = list;
    }
}
