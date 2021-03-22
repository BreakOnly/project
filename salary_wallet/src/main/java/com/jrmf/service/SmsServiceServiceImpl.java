package com.jrmf.service;

import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.SMSChannelFactory;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SmsServiceServiceImpl implements SmsService {

  private static final Logger logger = LoggerFactory.getLogger(SmsServiceServiceImpl.class);

  @Autowired
  private CompanyService companyService;
  @Autowired
  protected ParameterService parameterService;

  @Override
  public boolean sendNoticeSMS(String phoneNo, String content, String signName, String templateCode,
      String templateParam) {

    SMSChannelConfig channelConfig = companyService.getSmsConfig();
    SMSChannel smsChannel = SMSChannelFactory.createChannel(channelConfig);

    boolean flag = smsChannel
        .sendSMS(new String[]{phoneNo}, content, signName,
            templateCode, templateParam);

    Parameter param = new Parameter();
    param.setParamName(phoneNo);
    param.setParamValue(content);
    param.setParamDate(new Date());
    param.setParamFlag("smsContent");
    param.setIsVoice(0);

    if (!flag) {
      param.setParamStatus(-1);
      logger.error("手机号:{},通知类短信发送失败", phoneNo);
    } else {
      param.setParamStatus(1);
    }
    parameterService.saveParameter(param);

    return flag;
  }
}
