package com.jrmf.utils.sms.channel.alibaba;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.reflect.TypeToken;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.utils.JsonHelper;
import com.jrmf.utils.MapUtil;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.alibaba.response.ResponseData;
import com.jrmf.utils.sms.channel.ym.response.SmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信通道
 *
 * @author 林松
 */
@Component
public class AlibabaSMSChannel implements SMSChannel {

  private static Logger logger = LoggerFactory.getLogger(AlibabaSMSChannel.class);

  private SMSChannelConfig smsChannelConfig;

  public AlibabaSMSChannel() {
    super();
  }

  public AlibabaSMSChannel(SMSChannelConfig smsChannelConfig) {
    super();
    this.smsChannelConfig = smsChannelConfig;
  }


  @Override
  public boolean sendSMS(String[] mobiles, String smsContent, String signName,
      String templateCode, String templateParam) {
    try {

      //阿里巴巴
      DefaultProfile profile = DefaultProfile.getProfile(
          smsChannelConfig.getAppid(),          // 地域ID cn-hangzhou
          smsChannelConfig.getMerchantId(),      // RAM账号的AccessKey ID
          smsChannelConfig.getSignKey()); // RAM账号AccessKey Secret

      IAcsClient client = new DefaultAcsClient(profile);

      CommonRequest request = new CommonRequest();
      request.setSysMethod(MethodType.POST);
      request.setSysDomain(smsChannelConfig.getUrl());  //dysmsapi.aliyuncs.com
      request.setSysVersion(smsChannelConfig.getExtraParam()); //2017-05-25
      request.setSysAction(smsChannelConfig.getExtraParamTwo()); //SendSms
      request.putQueryParameter("RegionId", smsChannelConfig.getAppid());
      request.putQueryParameter("PhoneNumbers", MapUtil.strArrayToStr(mobiles));
      request.putQueryParameter("SignName", signName);
      request.putQueryParameter("TemplateCode", templateCode);
      request.putQueryParameter("TemplateParam", templateParam);

      logger.info("请求地址为：{},regionId：{},accessKeyId为：{},secret为:{}", smsChannelConfig.getUrl(),
          smsChannelConfig.getAppid(), smsChannelConfig.getMerchantId(),
          smsChannelConfig.getSignKey());

      logger.info("阿里云短信请求信息：{}", request);
      CommonResponse response = client.getCommonResponse(request);
      logger.info("阿里云短信响应信息：{}", response.getData());

      ResponseData responseData = JSON.parseObject(response.getData(), ResponseData.class);

      if ("OK".equals(responseData.getCode())) {
        //发送成功
        logger.info(MapUtil.strArrayToStr(mobiles) + "短信发送成功");

        return true;
      } else {
        logger
            .error(MapUtil.strArrayToStr(mobiles) + "短信发送失败，错误码：{},错误原因：{}", responseData.getCode(),
                responseData.getMessage());
        return false;
      }
    } catch (Exception e) {
      logger.error("短信发送异常", e.getMessage());
      return false;
    }
  }

  @Override
  public boolean sendVoiceSMS(String[] mobiles, String smsContent) {

    return false;
  }

  public SMSChannelConfig getsMSChannelConfig() {
    return smsChannelConfig;
  }

  public void setsMSChannelConfig(SMSChannelConfig smsChannelConfig) {
    this.smsChannelConfig = smsChannelConfig;
  }


}
