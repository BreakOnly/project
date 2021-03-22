package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryAccountBalanceRequest extends MyBankBaseRequest {

  private String account_type;

  public QueryAccountBalanceRequest() {
    this.service = Service.query_account_balance.getServiceName();
  }

}
