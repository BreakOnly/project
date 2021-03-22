package com.jrmf.taxsettlement.api.service.yuncr.dto;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/23 11:07
 * @description:
 */
@Data
public class UserAuthenticationRealNameRequestDTO extends ActionParams {

  String userName;

  String idCardNumber;

  String uniqueCode;

}
