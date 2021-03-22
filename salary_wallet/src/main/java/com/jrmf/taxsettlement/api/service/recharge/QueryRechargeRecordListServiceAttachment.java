package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import java.util.List;

public class QueryRechargeRecordListServiceAttachment extends ActionAttachment {
  /**
   * 总条数
   */
  private String total;
  /**
   * 发票收件人地址
   */
  private List<RechargeRecordListServiceAttachment> list;

  public String getTotal() {
    return total;
  }

  public void setTotal(String total) {
    this.total = total;
  }

  public List<RechargeRecordListServiceAttachment> getList() {
    return list;
  }

  public void setList(
      List<RechargeRecordListServiceAttachment> list) {
    this.list = list;
  }
}
