package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryInvoiceSummaryHistoryServiceParams extends ActionParams {
  /**
   * 下发公司ID，多个服务公司之间用”,”分隔
   */
  private String transferCorpId;
  /**
   * 开始日期
   */
  @NotNull
  private String startDate;
  /**
   * 结束日期
   */
  @NotNull
  private String endDate;

  public String getTransferCorpId() {
    return transferCorpId;
  }

  public void setTransferCorpId(String transferCorpId) {
    this.transferCorpId = transferCorpId;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  @Override
  public String toString() {
    return "QueryInvoiceSummaryHistoryServiceParams{" +
        "transferCorpId='" + transferCorpId + '\'' +
        ", startDate='" + startDate + '\'' +
        ", endDate='" + endDate + '\'' +
        '}';
  }
}
