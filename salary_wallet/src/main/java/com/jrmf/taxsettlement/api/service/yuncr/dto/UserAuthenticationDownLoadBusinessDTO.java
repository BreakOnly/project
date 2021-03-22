package com.jrmf.taxsettlement.api.service.yuncr.dto;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/12/1 10:21
 * @description: 下载营业执照
 */
@Data
public class UserAuthenticationDownLoadBusinessDTO  extends ActionAttachment {

  private String data;

}
