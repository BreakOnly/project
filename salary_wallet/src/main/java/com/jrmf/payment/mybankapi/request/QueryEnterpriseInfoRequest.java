package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;

@Data
public class QueryEnterpriseInfoRequest extends MyBankBaseRequest {

  public QueryEnterpriseInfoRequest() {
    this.service = Service.query_enterprise_info.getServiceName();
  }

}
