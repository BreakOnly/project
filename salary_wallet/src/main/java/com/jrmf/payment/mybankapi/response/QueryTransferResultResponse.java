package com.jrmf.payment.mybankapi.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryTransferResultResponse extends MyBankBaseResponse {

  String outer_trade_no;
  String inner_trade_no;
  String outer_inst_order_no;
  String trade_status;
  String fail_reason;

}
