package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019年10月12日12:02:23
 * @desc
 **/
public class QueryMerchantInvoiceInfoServiceParams extends ActionParams {
    /**
     * 下发公司id
     */
    private  String transferCorpId;

    @Override
    public String toString() {
        return "QueryMerchantInvoiceInfoServiceParams{" + "transferCorpId='" + transferCorpId + '\'' + ", page='" + page + '\'' + ", size='" + size + '\'' + '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

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
