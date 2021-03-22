package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-08-22 16:12
 * @desc
 **/
public class QueryInvoiceHistoryServiceParams extends ActionParams {
    @Override
    public String toString() {
        return "QueryInvoiceHistoryServiceParams{" + "transferCorpId='" + transferCorpId + '\'' + ", page='" + page + '\'' + ", size='" + size + '\'' + ", status='" + status + '\'' + '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    /**
     * 下发公司id

     */
    private  String transferCorpId;
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
    /**
     * 开票状态
     * 1.申请待处理，2.申请已受理，3.申请驳回，4.成功，5.挂起
     */
    private  String status;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPage() {

        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}
