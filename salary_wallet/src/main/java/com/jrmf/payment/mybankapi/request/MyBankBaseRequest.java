package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import lombok.Data;

@Data
public class MyBankBaseRequest {

  String version;
  String partner_id;
  String charset;
  String sign_type;
  String service;
  String uid;

  public MyBankBaseRequest() {
    this.version = BaseRequestConstant.VERSION;
    this.charset = BaseRequestConstant.CHARSET;
    this.sign_type = BaseRequestConstant.SIGN_TYPE;
  }
}
