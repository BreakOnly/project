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
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationDownLoadBusinessDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRequestDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: YJY
 * @date: 2020/11/30 18:26
 * @description:
 */
@ActionConfig(name = "下载营业执照")
public class UserAuthenticationBusinessLicenseDownloadService implements Action<UserAuthenticationRequestDTO, UserAuthenticationDownLoadBusinessDTO> {


  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;



  @Override
  public String getActionType() {

      return APIDefinition.YUNCR_USER_AUTHENTICATION_BUSINESSLICENSE_DOWNLOAD.name();
  }

  @Override
  public ActionResult<UserAuthenticationDownLoadBusinessDTO> execute(
      UserAuthenticationRequestDTO actionParams) {

    /**
     * @Description 检查各项参数
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkNull(actionParams.getApplyNumber());
    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("applyNumber",actionParams.getApplyNumber());
    JSONObject response = littleBeeFeignClient.businessLicenseDownload(jsonObject);
    String info = null;

    if(response.getInteger("state").intValue() == SUCCESS_STATUS){
      UserAuthenticationDownLoadBusinessDTO responseDTO = new UserAuthenticationDownLoadBusinessDTO();
      JSONObject resultData = response.getJSONObject("data");
      if(resultData.getInteger("state").intValue() == SUCCESS_STATUS){
        responseDTO.setData(resultData.getString("file"));
      }
      return new ActionResult<>(responseDTO, UUIDTool.getUUID());

    }else{

      throw new APIDockingException(response.getInteger("state")+"",response.getString("respmsg"), UUIDTool.getUUID());
    }


  }
}
