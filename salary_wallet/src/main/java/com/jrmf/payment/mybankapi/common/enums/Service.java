/**
 *
 */
package com.jrmf.payment.mybankapi.common.enums;

import org.apache.commons.lang3.StringUtils;


/**
 * <p>注释</p>
 * @author fjl
 * @version $Id: BaseField.java, v 0.1 2013-11-12 下午3:38:12 fjl Exp $
 */
public enum Service {
  payment_to_card("mybank.tc.trade.withdrawtocard", "tpu"),
  remit_subaccount("mybank.tc.trade.remit.subaccount", "tpu"),
  create_enterprise_member("mybank.tc.user.enterprise.register", "mag"),
  modify_enterprise_member("mybank.tc.user.enterprise.info.modify", "mag"),
  query_enterprise_info("mybank.tc.user.enterprise.info.query", "mag"),
  query_personal_info("mybank.tc.user.personal.info.query", "mag"),
  query_account_balance("mybank.tc.user.account.balance", "mag"),
  query_payment_info("mybank.tc.trade.info.query", "mag"),
  query_payment_history_info("mybank.tc.trade.account.query", "mag"),
  payment_to_subaccount("mybank.tc.trade.transfer", "tpu");


  private String serviceName;

  private String serviceUrl;

  Service(String serviceName, String serviceUrl) {
    this.serviceName = serviceName;
    this.setServiceUrl(serviceUrl);
  }


  public String getServiceName() {
    return serviceName;
  }


  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }


  public static Service getByServiceName(String serviceName) {
    if (StringUtils.isBlank(serviceName)) {
      return null;
    }
    for (Service item : values()) {
      if (item.getServiceName().equals(serviceName)) {
        return item;
      }
    }
    return null;
  }


  public String getServiceUrl() {
    return serviceUrl;
  }


  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }
}
