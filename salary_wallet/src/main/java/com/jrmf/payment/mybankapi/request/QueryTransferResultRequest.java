package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryTransferResultRequest extends MyBankBaseRequest{

  String outer_trade_no;

  public QueryTransferResultRequest() {
    this.service = Service.query_payment_info.getServiceName();
  }

}
