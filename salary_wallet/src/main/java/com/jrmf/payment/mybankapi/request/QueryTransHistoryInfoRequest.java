package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;

@Data
public class QueryTransHistoryInfoRequest extends MyBankBaseRequest {

  String start_time;
  String end_time;
  String account_type;
  String current_page;

  public QueryTransHistoryInfoRequest() {
    this.service = Service.query_payment_info.getServiceName();
  }

}
