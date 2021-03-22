package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

/**
 * @author 种路路
 * @create 2019-08-22 16:12
 * @desc
 **/
public class QueryRechargeAccountServiceParams extends ActionParams {
    /**
     * 服务公司id
     */
    @NotNull
    private String transferCorpId;
    /**
     * 商户银行卡所属银行或账号所属机构
     * 2-	支付宝下发
     3-	微信下发
     4-	银行卡下发
     */
    @NotNull
    private String payType;

    @Override
    public String toString() {
        return "QueryRechargeAccountServiceParams{" + "transferCorpId='" + transferCorpId + '\'' + ", payType='" + payType + '\'' + '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }
}
