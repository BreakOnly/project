package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-06-19 11:20
 * @desc
 **/
public class SyncPrepareUnifiedOrderServiceParams extends ActionParams {
    /**
     * 渠道订单号
     */
    @NotNull
    private  String customOrderNo;
    /**
     * 智税通订单号
     */
    @NotNull
    private  String dealNo;
    /**
     * 订单状态
     */
    @NotNull
    private  String status;
    /**
     * 描述信息
     */
    @NotNull
    private  String message;

    public String getCustomOrderNo() {
        return customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SyncPrepareUnifiedOrderServiceParams{" + "customOrderNo='" + customOrderNo + '\'' + ", dealNo='" + dealNo + '\'' + ", status='" + status + '\'' + ", message='" + message + '\'' + '}';
    }

    public String getDealNo() {
        return dealNo;
    }

    public void setDealNo(String dealNo) {
        this.dealNo = dealNo;
    }
}
