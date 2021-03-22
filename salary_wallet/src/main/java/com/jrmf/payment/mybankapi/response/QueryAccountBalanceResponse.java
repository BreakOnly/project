package com.jrmf.payment.mybankapi.response;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryAccountBalanceResponse extends MyBankBaseResponse {

  private String transit_amount;
  private List<QueryAccountBalanceDetailResponse> account_list;


}
