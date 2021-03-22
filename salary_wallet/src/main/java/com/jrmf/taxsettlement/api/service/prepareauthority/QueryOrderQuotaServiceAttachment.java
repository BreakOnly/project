package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

import java.util.List;

/**
 * @author chonglulu
 */
public class QueryOrderQuotaServiceAttachment extends ActionAttachment {
    /**
     * 身份证
     */
    private String certificateNo;
    /**
     * 限额明细
     */
    private List<QueryOrderQuotaServiceAttachmentDetail> details;

    @Override
    public String toString() {
        return "QueryOrderQuotaServiceAttachment{" + "certificateNo='" + certificateNo + '\'' + ", details=" + details + '}';
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public List<QueryOrderQuotaServiceAttachmentDetail> getDetails() {
        return details;
    }

    public void setDetails(List<QueryOrderQuotaServiceAttachmentDetail> details) {
        this.details = details;
    }
}
