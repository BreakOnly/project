package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationApprovalStatusResponseDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationCommonRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationResponseDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

/**
 * @author: YJY
 * @date: 2020/10/20 11:14
 * @description:
 */
@ActionConfig(name = "查询审核状态")
public class UserAuthenticationApprovalStatusService implements
    Action<UserAuthenticationCommonRequestDTO, UserAuthenticationApprovalStatusResponseDTO> {


  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;

  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_APPROVAL_STATUS.name();
  }


  @Override
  public ActionResult<UserAuthenticationApprovalStatusResponseDTO> execute(
      UserAuthenticationCommonRequestDTO actionParams) {

    /**
     * @Description 检查商户流水号和申请编码是否为空
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkNull(actionParams.getApplyNumber());

    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("applyNumber", actionParams.getApplyNumber());
    JSONObject response = littleBeeFeignClient.approvalStatus(jsonObject);
    /**
     * @Description 解析返回数据
     **/
    if (response.getInteger("state").intValue() == SUCCESS_STATUS) {
      UserAuthenticationApprovalStatusResponseDTO responseDTO = new UserAuthenticationApprovalStatusResponseDTO();
      JSONObject resultData = response.getJSONObject("data");
      responseDTO.setStatus(resultData.getString("state"));
      responseDTO.setMsg(resultData.getString("reason"));
      if (!ObjectUtils.isEmpty(resultData.get("failNodes"))) {
        responseDTO.setFailNodes(resultData.getJSONArray("failNodes"));
      }
      return new ActionResult<>(responseDTO, UUIDTool.getUUID());
    } else {
      /**
       * @Description 失败
       **/
      throw new APIDockingException(response.getInteger("state") + "",
          response.getString("respmsg"), UUIDTool.getUUID());
    }


  }
}
