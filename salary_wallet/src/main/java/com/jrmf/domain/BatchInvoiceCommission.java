package com.jrmf.domain;

import lombok.Data;

@Data
public class BatchInvoiceCommission {

  private Integer id;

  private String companyName;

  private String customKey;

  private Integer receiptStatus;

  private Integer invoiceStatus;

  private String individualName;

  private String accountTime;

  private String accountDate;

  private String amount;

  private String fee;

  private String inAccountNo;

  private String inAccountName;

  private String contractCompanyName;

  private Integer companyId;

  private Integer documentType;

  private String certId;

  private String remark;

  private String receiptUrl;

  private String orderNo;

  private String createTime;

  private String updateTime;

  private String serviceCompanyName;
}
