package com.jrmf.taxsettlement.api.service.yuncr.dto;


import com.alibaba.fastjson.JSONArray;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/22 9:22
 * @description:
 */
@Data
public class UserAuthenticationApprovalStatusResponseDTO extends ActionAttachment {


  //审核状态
  private String status;

  //描述
  private String msg;
  //错误节点
  private JSONArray failNodes;

}
