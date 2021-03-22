package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;

@Data
public class ImitatePaymentTransferRequest extends MyBankBaseRequest {

  String payer_card_no;
  String payer_card_name;
  String payee_card_no;
  String payee_card_name;
  String amount;
  String payer_remark;
  String notify_url;

  public ImitatePaymentTransferRequest() {
    this.service = Service.remit_subaccount.getServiceName();
  }

}
