package com.jrmf.controller.agreement;

import com.jrmf.common.Constant;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.User;
import com.jrmf.domain.UserRelated;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.exception.SessionDestroyedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign/agreement")
@Slf4j
public class SignViewController extends BaseController {

  @Autowired
  UsersAgreementService usersAgreementService;
  @Autowired
  ChannelCustomService channelCustomService;


  @RequestMapping(value = "/useragreement", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> signPage(HttpServletRequest request) {
    User user = (User) request.getSession().getAttribute("user");

    Map<String, Object> paramMap = new HashMap<>(4);
    paramMap.put("userId", user.getId());
    paramMap.put("customKeys", Constant.CHANNEL_CUSTOMS);
    paramMap.put("companyIds", Constant.COMPANYS);
    List<UsersAgreement> usersAgreementList = usersAgreementService
        .selectUsersAgreementsByParams(paramMap);

    Map<String, Object> resultMap = new HashMap<>(4);
    resultMap.put("usersAgreementList", usersAgreementList);
    resultMap.put("userName", user.getUserName());
    resultMap.put("certId", user.getCertId());
    return returnSuccess(resultMap);
  }


  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> login(@RequestParam String userName, @RequestParam String password) {
    if (Constant.LOGIN_USER.containsKey(userName)) {
      if (password.equals(Constant.LOGIN_USER.get(userName))) {
        return returnSuccess();
      }
      return returnFail(RespCode.error602, RespCode.codeMaps.get(RespCode.error602));
    }
    return returnFail(RespCode.error601, RespCode.codeMaps.get(RespCode.error601));
  }

}
