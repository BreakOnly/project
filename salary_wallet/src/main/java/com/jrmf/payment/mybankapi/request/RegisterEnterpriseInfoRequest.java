package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegisterEnterpriseInfoRequest extends MyBankBaseRequest {

  String enterprise_name;

  public RegisterEnterpriseInfoRequest() {
    this.service = Service.create_enterprise_member.getServiceName();
  }
}
