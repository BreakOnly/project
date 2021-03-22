package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019年10月12日12:02:23
 * @desc
 **/
public class QueryInvoiceReceiverServiceParams extends ActionParams {
    /**
     * 页码
     */
    @NotNull
    private  String page;
    /**
     * 每页条数
     */
    @NotNull
    private  String size;

    @Override
    public String toString() {
        return "QueryInvoiceReceiverServiceParams{" + "page='" + page + '\'' + ", size='" + size + '\'' + '}';
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
