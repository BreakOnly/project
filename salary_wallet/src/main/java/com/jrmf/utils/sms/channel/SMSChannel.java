package com.jrmf.utils.sms.channel;

public interface SMSChannel {

  //发送一般短信
  public boolean sendSMS(String[] mobiles, String smsContent, String signName, String templateCode,
      String templateParam);

  //发送语音短信
  public boolean sendVoiceSMS(String[] mobiles, String smsContent);

}
