package com.jrmf.taxsettlement.api.service.yuncr;

import static com.jrmf.taxsettlement.api.service.yuncr.common.Utils.SUCCESS_STATUS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.LittleBeeFeignClient;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationBankCardRequestDTO;
import com.jrmf.taxsettlement.api.service.yuncr.dto.UserAuthenticationResponseDTO;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: YJY
 * @date: 2020/11/30 14:25
 * @description:
 */
@ActionConfig(name = "绑定银行卡")
public class UserAuthenticationBankCardService implements Action<UserAuthenticationBankCardRequestDTO, UserAuthenticationResponseDTO> {

  @Autowired
  LittleBeeFeignClient littleBeeFeignClient;
  @Override
  public String getActionType() {
    return APIDefinition.YUNCR_USER_AUTHENTICATION_BANK_CARD.name();
  }

  @Override
  public ActionResult<UserAuthenticationResponseDTO> execute(
      UserAuthenticationBankCardRequestDTO actionParams) {

    /**
     * @Description 检查各项参数
     **/
    CheckData.checkNull(actionParams.getThirdSerialNumber());

    CheckData.checkNull(actionParams.getBankCardNumber());

    CheckData.checkNull(actionParams.getIdCardNumber());

    CheckData.checkNull(actionParams.getUserName());

    CheckData.checkNull(actionParams.getApplyNumber());

    /**
     * @Description 请求 小黄蜂项目接口
     **/
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("userName",actionParams.getUserName());
    jsonObject.put("applyNumber",actionParams.getApplyNumber());
    jsonObject.put("bankCardNumber",actionParams.getBankCardNumber());
    jsonObject.put("idCardNumber",actionParams.getIdCardNumber());
    jsonObject.put("phoneNumber",actionParams.getPhoneNumber());
    jsonObject.put("merchantId",actionParams.getMerchantId());
    JSONObject response = littleBeeFeignClient.bankCard(jsonObject);
    if(response.getInteger("state").intValue() == SUCCESS_STATUS){
      UserAuthenticationResponseDTO responseDTO = new UserAuthenticationResponseDTO();
      responseDTO.setApplyNumber(actionParams.getApplyNumber());
      return new ActionResult<UserAuthenticationResponseDTO>(responseDTO, UUIDTool.getUUID());

    }else{
      throw new APIDockingException(response.getInteger("state")+"",response.getString("respmsg"),UUIDTool.getUUID());
    }

  }

}
