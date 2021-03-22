package com.jrmf.controller.common;

import com.google.common.base.Joiner;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.service.SmsService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/common/sms")
public class SendSmsController extends BaseController {

  private Logger logger = LoggerFactory.getLogger(SendSmsController.class);

  @Autowired
  private SmsService smsService;


  @PostMapping(value = "/sendSMS")
  @ResponseBody
  public Map<String, Object> sendSMS(String signName, String smsContent, String mobiles) {

    try {

      String content =
          "【" + signName + "】" + smsContent;
      smsService.sendNoticeSMS(mobiles, content, null,
          SmsTemplateCodeEnum.YXY_UNSIGN_NOTICE.getCode(), null);
      return returnSuccess();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
    }

  }


}
