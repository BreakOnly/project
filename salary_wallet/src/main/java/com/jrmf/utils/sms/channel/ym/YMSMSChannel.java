package com.jrmf.utils.sms.channel.ym;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.HttpClient;
import com.jrmf.utils.JsonHelper;
import com.jrmf.utils.MapUtil;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.ym.response.ResponseData;
import com.jrmf.utils.sms.channel.ym.response.SmsResponse;
import com.jrmf.utils.sms.channel.ym.util.Md5;
/**
 * 亿美短信通道
 * @author 孙春辉
 *
 */
@Component
public class YMSMSChannel implements SMSChannel{

	private static Logger logger = LoggerFactory.getLogger(YMSMSChannel.class);

	private SMSChannelConfig smsChannelConfig;

	public YMSMSChannel() {
		super();
	}

	public YMSMSChannel(SMSChannelConfig smsChannelConfig) {
		super();
		this.smsChannelConfig = smsChannelConfig;
	}

	@Override
	public boolean sendSMS(String[] mobiles, String smsContent, String signName,
			String templateCode, String templateParam) {
		try{
			//配置信息
			String appid = smsChannelConfig.getAppid();
			String secretKey = smsChannelConfig.getSignKey();
			String method = "/simpleinter/sendSMS";
			String url = smsChannelConfig.getUrl()+method;
			logger.info("请求地址为："+url+",appid为："+appid+",密钥为："+secretKey);
			Map<String, String> reqParams = new HashMap<String, String>();
			// 时间戳
			String timestamp = DateUtils.formartDate(new Date(), "yyyyMMddHHmmss");
			// 签名
			String signStr = appid + secretKey + timestamp;
			logger.info("待签名字符串："+signStr);
			String sign = Md5.md5((signStr).getBytes());
			logger.info("签名值："+sign);
			//封装请求参数
			reqParams.put("appId", appid);
			reqParams.put("sign", sign);
			reqParams.put("timestamp", timestamp);
			reqParams.put("mobiles", MapUtil.strArrayToStr(mobiles));
			reqParams.put("content", URLEncoder.encode(smsContent, "utf-8"));
			logger.info("短信发送内容为："+smsContent);
			//请求参数map转成字符串
			String reqStr = MapUtil.mapToStr(reqParams);
			logger.info("请求通道的信息为："+reqStr);
			//请求上游
			String respStr = HttpClient.post(url, reqStr, 30000, 30000, null, null);
			logger.info("通道响应的信息为："+respStr);
			//将json字符串转化成集合对象
			ResponseData<SmsResponse[]> data = JsonHelper.fromJson(new TypeToken<ResponseData<SmsResponse[]>>() {
			}, respStr);
			String code = data.getCode();
			if ("SUCCESS".equals(code)) {
				//发送成功
				logger.info(MapUtil.strArrayToStr(mobiles)+"短信发送成功");
				for (SmsResponse d : data.getData()) {
					System.out.println("data:" + d.getMobile() + "," + d.getSmsId() + "," + d.getCustomSmsId());
					logger.info("手机号："+d.getMobile()+",smsid："+d.getSmsId());
				}
				return true;
			}else{
				logger.info(MapUtil.strArrayToStr(mobiles)+"短信发送失败，错误码："+code);
				return false;
			}
		}catch(Exception e){
			logger.error("短信发送异常",e.getMessage());
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
