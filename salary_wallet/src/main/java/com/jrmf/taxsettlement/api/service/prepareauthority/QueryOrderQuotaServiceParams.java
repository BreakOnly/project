package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryOrderQuotaServiceParams extends ActionParams {

    /**
     * 证件号
     */
    @NotNull
    private String certificateNo;

    /**
     * 流水号
     */
    @NotNull
    private String serialNo;

    @Override
    public String toString() {
        return "QueryOrderQuotaServiceParams{" + "certificateNo='" + certificateNo + '\'' + ", serialNo='" + serialNo + '\'' + '}';
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
}
