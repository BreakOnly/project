package com.jrmf.taxsettlement.api.service.prepareauthority;

/**
 * @author 种路路
 * @create 2019-06-14 10:46
 * @desc QueryOrderQuotaServiceAttachment明细
 **/
public class QueryOrderQuotaServiceAttachmentDetail {
    /**
     * 服务公司id
     */
    private String transferCorpId;
    /**
     * 金额
     */
    private String amount;

    @Override
    public String toString() {
        return "QueryOrderQuotaServiceAttachment{" + "transferCorpId='" + transferCorpId + '\'' + ", amount='" + amount + '\'' + '}';
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
