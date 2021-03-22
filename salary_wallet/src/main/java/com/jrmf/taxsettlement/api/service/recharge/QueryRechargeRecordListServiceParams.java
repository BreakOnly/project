package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

public class QueryRechargeRecordListServiceParams extends ActionParams {
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
  /**
   * 页码
   */
  @NotNull
  private String page;
  /**
   * 每页展示条数
   */
  @NotNull
  private String size;
  /**
   * 排序
   */
  private String sort;

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

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  @Override
  public String toString() {
    return "QueryRechargeRecordListServiceParams{" +
        "transferCorpId='" + transferCorpId + '\'' +
        ", startDate='" + startDate + '\'' +
        ", endDate='" + endDate + '\'' +
        ", page='" + page + '\'' +
        ", size='" + size + '\'' +
        ", sort='" + sort + '\'' +
        '}';
  }
}
