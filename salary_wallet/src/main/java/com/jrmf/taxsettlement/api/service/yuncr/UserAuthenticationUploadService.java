package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.ERR_504;
import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.STRING_SUCCESS_STATUS;
import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.service.UserSerivce;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationResponseDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import com.jrmf.utils.ThreadPoolUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2020/10/20 11:14
 * @description:
 */
@Log4j
@ActionConfig(name = "上传文件")
public class UserAuthenticationUploadService implements
    Action<UserAuthenticationRequestDTO, UserAuthenticationResponseDTO> {


  @Autowired
  UserSerivce userSerivce;
  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;


  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_UPLOAD.name();
  }


  @Override
  public ActionResult<UserAuthenticationResponseDTO> execute(
      UserAuthenticationRequestDTO actionParams) {

    String checkType = actionParams.getType();
    String checkFile = actionParams.getFile();
    String checkUniqueCode = actionParams.getApplyNumber();


    CheckData.checkNull(actionParams.getThirdSerialNumber());
    CheckData.checkFile(checkFile,checkType);

    if ((!"1".equals(checkType)) && StringUtils.isEmpty(checkUniqueCode)) {
      throw new APIDockingException(CommonRetCodes.INVAILD_PARAMS.getCode(),
          CommonRetCodes.INVAILD_PARAMS.getDesc());
    }

    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("certFile", checkFile);
    jsonObject.put("type", checkType);
    jsonObject.put("applyNumber", checkUniqueCode);
    jsonObject.put("merchantId", actionParams.getMerchantId());
    log.info("获取到的商户ID为" + actionParams.getMerchantId());
    JSONObject response = littleBeeFeignClient.uploadFile(jsonObject);
    if (response.getInteger("state").intValue() == SUCCESS_STATUS) {
      if (actionParams.getType().equals(STRING_SUCCESS_STATUS)) {
        JSONObject resultData = response.getJSONObject("data");
        checkUniqueCode = resultData.getString("applyNumber");

        ThreadPoolUtils.getThread().execute(() -> {
          try {
            String idCard = resultData.getString("idCardNumber");
            String name = resultData.getString("name");
            userSerivce.addUserInfo(name, 1, idCard, null, null, actionParams.getMerchantId(), null,
                "个体工商户注册");

          } catch (Exception e) {
            log.info("个体工商户注册,向user表添加数据异常" + e);
          }
        });
      }
      UserAuthenticationResponseDTO responseDTO = new UserAuthenticationResponseDTO();
      responseDTO.setApplyNumber(checkUniqueCode);
      return new ActionResult<UserAuthenticationResponseDTO>(responseDTO, UUIDTool.getUUID());

    } else if (response.getInteger("state").intValue() == ERR_504) {
      UserAuthenticationResponseDTO responseDTO = new UserAuthenticationResponseDTO();
      if (!ObjectUtils.isEmpty(response.get("applyNumber"))) {
        responseDTO.setApplyNumber(response.get("applyNumber").toString());
      }
      return new ActionResult<UserAuthenticationResponseDTO>(ERR_504 + "",
          response.getString("respmsg"), responseDTO,
          UUIDTool.getUUID());
    } else {

      throw new APIDockingException(response.getInteger("state") + "",
          response.getString("respmsg"), UUIDTool.getUUID());
    }


  }
}
