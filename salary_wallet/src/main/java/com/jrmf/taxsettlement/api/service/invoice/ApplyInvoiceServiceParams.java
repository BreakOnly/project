package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-08-22 16:12
 * @desc
 **/
public class ApplyInvoiceServiceParams extends ActionParams {
    /**
     * 开票金额
     */
    @NotNull
    private String invoiceAmount;
    /**
     * 备注
     */
    @NotNull
    private String remark;
    /**
     * 收件人编号
     */
    @NotNull
    private String receiveId;
    /**
     * 开票信息编号
     */
    @NotNull
    private String infoId;

    @Override
    public String toString() {
        return "ApplyInvoiceServiceParams{" + "invoiceAmount='" + invoiceAmount + '\'' + ", remark='" + remark + '\'' + ", receiveId='" + receiveId + '\'' + ", infoId='" + infoId + '\'' + ", orderNos='" + orderNos + '\'' + '}';
    }

    public String getOrderNos() {
        return orderNos;
    }

    public void setOrderNos(String orderNos) {
        this.orderNos = orderNos;
    }

    /**
     * 智税通订单号
     * 可以传多笔订单号

     */
    @NotNull
    private String orderNos;

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public String getInfoId() {
        return infoId;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
    }
}
