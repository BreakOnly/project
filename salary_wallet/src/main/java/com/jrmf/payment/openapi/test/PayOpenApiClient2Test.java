package com.jrmf.payment.openapi.test;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.PayBalanceQueryParam;
import com.jrmf.payment.openapi.model.request.deliver.PayOrderQueryByDayParam;
import com.jrmf.payment.openapi.model.request.deliver.PayUnifiedOrderParam;
import com.jrmf.payment.openapi.model.request.deliver.PayUnifiedOrderQueryParam;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.deliver.PayBalanceDetailQueryResult;
import com.jrmf.payment.openapi.model.response.deliver.PayOrderQueryByDayResult;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderQueryResult;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderResult;
import com.jrmf.payment.openapi.utils.TokenGenerator;

/**
 * 代发/支付
 * @author Napoleon.Chen
 * @date 2018年9月17日
 */
public class PayOpenApiClient2Test {

	static OpenApiClient client;
	
	@BeforeClass
	public static void init(){
		String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";
		boolean isTest = true;
		String appId = "tjkqjy";//bjsytkay  //bjylmfay

		if (!isTest) {
			client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).useSandbox().build();
		} else {
			client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).build();
		}
	}

	@Test
	public void testPayUnifiedOrder() {
		PayUnifiedOrderParam param = new PayUnifiedOrderParam();
//		param.setOutOrderNo("2019031900002359");
		param.setServiceCompanyId(10000348L);
		param.setOutOrderNo(RandomStringUtils.random(16, false, true));
		param.setBank("alipay");	// 
		param.setAccountName("张桓2");
		param.setIdCard("370883198909121651");
		param.setAccountNo("17701393451");
		param.setAmount(new BigDecimal("0.01"));
		param.setPhone("17701393451");
		param.setMemo("test");
		OpenApiBaseResponse<PayUnifiedOrderResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	

}
