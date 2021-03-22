package com.jrmf.utils.sms.channel;

import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.utils.SpringContextUtil;
import com.jrmf.utils.sms.channel.alibaba.AlibabaSMSChannel;
import com.jrmf.utils.sms.channel.ym.YMSMSChannel;

public class SMSChannelFactory {


  private final static int YIMEI = 1;
  private final static int ALIBABA = 2;

  /**
   * 创建短信通道
   *
   * @param smsChannelConfig
   * @return
   */
  public static SMSChannel createChannel(SMSChannelConfig smsChannelConfig) {
    switch (smsChannelConfig.getChannelType()) {
      case YIMEI:
        //亿美
        YMSMSChannel ymsmsChannel = SpringContextUtil.getBean(YMSMSChannel.class);
        ymsmsChannel.setsMSChannelConfig(smsChannelConfig);
        return ymsmsChannel;
      case ALIBABA:
        AlibabaSMSChannel smsChannel = SpringContextUtil.getBean(AlibabaSMSChannel.class);
        smsChannel.setsMSChannelConfig(smsChannelConfig);
        return smsChannel;
      default:
        throw new IllegalStateException("Unexpected value: " + smsChannelConfig.getChannelType());
    }
  }

}
