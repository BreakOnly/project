package com.jrmf.domain;

import lombok.Data;

import java.io.Serializable;


@Data
public class BatchInvoiceAssociation implements Serializable {
    private Integer id;
    private Integer commissionId;
    private Integer applyBatchInvoiceId;
    private String customKey;
    private String companyName;
    private String contractCompanyName;
}
