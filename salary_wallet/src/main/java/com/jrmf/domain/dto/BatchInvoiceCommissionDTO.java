package com.jrmf.domain.dto;

import lombok.Data;

@Data
public class BatchInvoiceCommissionDTO {

  private String companyName;

  private String contractCompanyName;

  private String companyId;

  private Integer receiptStatus;

  private String accountStartDate;

  private String accountEndDate;

  private String individualName;

  private String certId;

  private String inAccountNo;

  private String invoiceStatus;

  private String amountStart;

  private String amountEnd;
}
