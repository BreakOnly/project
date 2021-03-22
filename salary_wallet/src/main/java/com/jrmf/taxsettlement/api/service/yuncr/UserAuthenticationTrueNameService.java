package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRealNameRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationResponseDTO;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: YJY
 * @date: 2020/10/20 11:14
 * @description:
 */
@ActionConfig(name = "实名认证")
public class UserAuthenticationTrueNameService implements Action<UserAuthenticationRealNameRequestDTO, UserAuthenticationResponseDTO> {


  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;

  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_TRUE_NAME.name();
  }



  @Override
  public ActionResult<UserAuthenticationResponseDTO> execute(
      UserAuthenticationRealNameRequestDTO actionParams) {

    /**
     * @Description 检查各项参数
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkNull(actionParams.getUniqueCode());

    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObj = (JSONObject) JSON.toJSON(actionParams);
    JSONObject response = littleBeeFeignClient.trueName(jsonObj);
    if(response.getInteger("state").intValue() == SUCCESS_STATUS){
      return new ActionResult<>(null);
    }else{
      System.out.println(response.getString("respmsg"));
      throw new APIDockingException(response.getInteger("state")+"",response.getString("respmsg"));
    }


  }

  public static void main(String[] args) {

  }
}
