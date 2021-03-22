package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class QueryInvoiceSummaryHistoryServiceAttachment extends ActionAttachment {

  /**
   * 已开票金额，单位：元（所选时间段内开票金额）
   */
  private String invoicedAmount;

  /**
   * 累计未开发票金额（当前还有多少未开发票金额）
   */
  private String totalUninvoicedAmount;

  public String getInvoicedAmount() {
    return invoicedAmount;
  }

  public void setInvoicedAmount(String invoicedAmount) {
    this.invoicedAmount = invoicedAmount;
  }

  public String getTotalUninvoicedAmount() {
    return totalUninvoicedAmount;
  }

  public void setTotalUninvoicedAmount(String totalUninvoicedAmount) {
    this.totalUninvoicedAmount = totalUninvoicedAmount;
  }

  @Override
  public String toString() {
    return "QueryInvoiceSummaryHistoryServiceAttachment{" +
        "invoicedAmount='" + invoicedAmount + '\'' +
        ", totalUninvoicedAmount='" + totalUninvoicedAmount + '\'' +
        '}';
  }
}
