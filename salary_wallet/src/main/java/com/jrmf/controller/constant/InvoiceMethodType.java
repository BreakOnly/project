package com.jrmf.controller.constant;

public enum InvoiceMethodType {

  NORMAL_INVOICE_TYPE(1, "先充值开票"),
  PRE_INVOICE_TYPE(2, "预开票"),
  COMMISSION_TAX_INVOICE_TYPE(3, "实发开票"),
  SERVICE_FEE_INVOICE_TYPE(4, "服务费开票");

  private final int code;
  private final String desc;

  private InvoiceMethodType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public static InvoiceMethodType codeOf(int code) {
    for (InvoiceMethodType invoiceMethodType : values()) {
      if (invoiceMethodType.getCode() == code) {
        return invoiceMethodType;
      }
    }
    return NORMAL_INVOICE_TYPE;
  }

}
