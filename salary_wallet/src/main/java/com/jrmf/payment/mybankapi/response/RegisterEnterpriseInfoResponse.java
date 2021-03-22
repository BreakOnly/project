package com.jrmf.payment.mybankapi.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegisterEnterpriseInfoResponse extends MyBankBaseResponse {

  String member_id;
  String sub_account_no;

}
