package com.jrmf.taxsettlement.api.service.yuncr.dto;

import com.jrmf.taxsettlement.api.gateway.IgnoreSign;
import com.jrmf.taxsettlement.api.service.ActionParams;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/21 16:30
 * @description:
 */
@Data
public class UserAuthenticationRequestDTO extends ActionParams {

  /**
  * @Description 上传的文件的base64信息
  **/
  @IgnoreSign
  String file;

  /**
   * @Description 类型
   **/
  String type;

  /**
   * @Description 申请编码
   **/
  String applyNumber;
}
