package com.jrmf.payment.mybankapi.response;

import lombok.Data;

@Data
public class MyBankBaseResponse {

  String charset;
  String is_success;
  String error_code;
  String error_message;
  String memo;

}
