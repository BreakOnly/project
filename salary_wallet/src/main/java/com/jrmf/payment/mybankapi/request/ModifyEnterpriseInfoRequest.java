package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ModifyEnterpriseInfoRequest extends MyBankBaseRequest {

  String enterprise_name;

  public ModifyEnterpriseInfoRequest() {
    this.service = Service.modify_enterprise_member.getServiceName();
  }


}
