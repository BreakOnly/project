package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

/**
 * @author chonglulu
 */
public class SignAgreementServiceAttachment extends ActionAttachment {
    /**
     * 请求序列单号
     */
    String serialNo;
    /**
     * 系统处理单号
     */
    String dealNo;

    @Override
    public String toString() {
        return "SignAgreementServiceAttachment{" + "serialNo='" + serialNo + '\'' + ", dealNo='" + dealNo + '\'' + '}';
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getDealNo() {
        return dealNo;
    }

    public void setDealNo(String dealNo) {
        this.dealNo = dealNo;
    }
}
