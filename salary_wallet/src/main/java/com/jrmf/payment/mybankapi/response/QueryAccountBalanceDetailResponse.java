package com.jrmf.payment.mybankapi.response;

import lombok.Data;

@Data
public class QueryAccountBalanceDetailResponse {

  private String account_id;
  private String account_type;
  private String balance;
  private String available_balance;
  private String sub_account_no;


}
