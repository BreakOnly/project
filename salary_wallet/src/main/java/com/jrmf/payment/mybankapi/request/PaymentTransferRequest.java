package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentTransferRequest extends MyBankBaseRequest {

  String outer_trade_no;
  String outer_inst_order_no;
  String white_channel_code;
  String account_type;
  String bank_account_no;
  String account_name;
  String bank_code;
  String card_type;
  String card_attribute;
  String amount;
  String fee_info;
  String notify_url;

  public PaymentTransferRequest() {
    this.service = Service.payment_to_card.getServiceName();
  }

}
