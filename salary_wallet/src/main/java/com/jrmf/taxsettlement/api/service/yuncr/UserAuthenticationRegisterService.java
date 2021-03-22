package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationCommonRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationResponseDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationVodeoInfoResponseDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: YJY
 * @date: 2020/10/20 11:14
 * @description:
 */
@ActionConfig(name = "提交注册")
public class UserAuthenticationRegisterService implements Action<UserAuthenticationCommonRequestDTO, UserAuthenticationResponseDTO> {


  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;

  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_INDIVIDUAL_REGISTER.name();
  }


  @Override
  public ActionResult<UserAuthenticationResponseDTO> execute(
      UserAuthenticationCommonRequestDTO actionParams) {

    /**
     * @Description 检查各项参数
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkNull(actionParams.getApplyNumber());
    CheckData.checkNull(actionParams.getPhoneNumber());
    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("applyNumber",actionParams.getApplyNumber());
    jsonObject.put("callbackAddress",actionParams.getCallbackAddress());
    jsonObject.put("mobileNo",actionParams.getPhoneNumber());
    JSONObject response = littleBeeFeignClient.individualRegister(jsonObject);

    if(response.getInteger("state").intValue() == SUCCESS_STATUS){
      UserAuthenticationResponseDTO responseDTO = new UserAuthenticationResponseDTO();
      responseDTO.setApplyNumber(actionParams.getApplyNumber());
      return new ActionResult<>(responseDTO,UUIDTool.getUUID());

    }else{
      throw new APIDockingException(response.getInteger("state")+"",response.getString("respmsg"), UUIDTool.getUUID());
    }


  }
}
