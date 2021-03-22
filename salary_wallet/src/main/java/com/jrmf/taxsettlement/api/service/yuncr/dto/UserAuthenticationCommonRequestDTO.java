package com.jrmf.taxsettlement.api.service.yuncr.dto;

import com.jrmf.taxsettlement.api.service.ActionParams;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/23 13:53
 * @description:
 */
@Data
public class UserAuthenticationCommonRequestDTO extends ActionParams {

  /**
  * @Description 申请编号
  **/
  String applyNumber;

  /**
   * @Description 手机号
   **/
  String phoneNumber;

  /**
  * @Description 审核结果回调地址
  **/
  String callbackAddress;
}
