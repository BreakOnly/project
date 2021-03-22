package com.jrmf.taxsettlement.api.service.yuncr.dto;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/30 17:42
 * @description:
 */
@Data
public class UserAuthenticationBankCardRequestDTO extends ActionParams {

  /**
   * @Description 用户名
   **/
  String userName;

  /**
   * @Description 身份证号码
   **/
  String idCardNumber;

  /**
   * @Description 银行卡号
   **/
  String bankCardNumber;

  /**
   * @Description 预留手机号
   **/
  String phoneNumber;

  /**
   * @Description 申请编码
   **/
  String applyNumber;
}
