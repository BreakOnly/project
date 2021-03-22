package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationVodeoInfoResponseDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: YJY
 * @date: 2020/10/20 11:14
 * @description:
 */
@ActionConfig(name = "获取文案信息")
public class UserAuthenticationVideoInfoService implements
    Action<UserAuthenticationRequestDTO, UserAuthenticationVodeoInfoResponseDTO> {


  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;

  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_VIDEO_INFO.name();
  }



  @Override
  public ActionResult<UserAuthenticationVodeoInfoResponseDTO> execute(
      UserAuthenticationRequestDTO actionParams) {

    /**
     * @Description 检查各项参数
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkNull(actionParams.getApplyNumber());

    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject response = littleBeeFeignClient.videoInfo();
    String info = null;
    if (response.getInteger("state").intValue() == SUCCESS_STATUS) {
      JSONObject resultData = response.getJSONObject("data");
      info = resultData.getString("message");
      UserAuthenticationVodeoInfoResponseDTO responseDTO = new UserAuthenticationVodeoInfoResponseDTO();
      responseDTO.setInfo(info);
      return new ActionResult<>(responseDTO,UUIDTool.getUUID());

    } else {

      throw new APIDockingException(response.getInteger("state") + "",
          response.getString("respmsg"), UUIDTool.getUUID());
    }


  }
}
