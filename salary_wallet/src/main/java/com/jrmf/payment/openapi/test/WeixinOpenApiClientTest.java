package com.jrmf.payment.openapi.test;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.weixin.WeixinOpenIdBindCheckParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.weixin.WeixinOpenIdBindCheckResult;

/**
 * 绑定微信服务
 * @author Napoleon.Chen
 * @date 2018年9月17日
 */
public class WeixinOpenApiClientTest {

	static OpenApiClient client;
	
	@BeforeClass
	public static void init(){
		String priKeyString = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOI4fc5POr0cZmyyGuwAonQGNVUvEd8KWtYk2ozNM1S6la7BK7DwWyk6kI10/NsY+Wues+C0fN86Ol0uckTF+s4NqMqw/W8PZ0HOV0mW1Ew/EV+HT1jRnbYviTRNWx9zS+xRjiDam3VdbElSwPQQulefAlQctcxsT1nPGSbWxvFpAgMBAAECgYEAnG6iGHNTVAh6j3mOAlrh+8d7Q9+bxRd8/w5XDvyrHVE1RrYPx3g+IcF8ykT2wW+Asrn4+07z9s1mJJ+EpygcqOmWtrExQlHjYcAn+27usxhTjWtNZ7AQoF0O0zIIAb8H2dm6Sin2fwvkZORaUy9gMlM7FW/LA48l49ptvs4aTZ0CQQDx5ezsesL8zA3SR+lr8xLTSjJokUOrK5wbYj+SGe+jgkUDxNU53zQXjTKM99rR+4cwomKbMFqGhZzDw1rEmccrAkEA72iYnXAKFHX05TB8YNkcxouwkFpi3rcD7m8LjEcXT/quNvsrcHfJB4iw63H2TwXvUQ4SJrxzjUMwSNkB19zfuwJBAOu6/0ns0Dv+trFndufF92CEe98/QMx8MSLWedDtCYU0HAFyPcCp7V/OL6cEmu/qyHHyrVlCo9VYO87if3/7xAUCQBi98Y/LxW7p5d5NzXzg00V9qEiy3qbvuRtKJKJhsnoUiS6rdIjSCFeb+9TJWVA/Z8UztBKGxVZjDDlrG/KoJAMCQQCRnIyn35lHhj5iXyB2Ofo5wHI45NvPpj7ut3rec58fMLTs3yw5w9OWnOSUCnbZ3gTQHeu4/3Ebu4ozzw4bi2iK";
		boolean isTest = true;
		String appId = "openapitest";
		if (isTest) {
			client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).useSandbox().build();
		} else {
			client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).build();
		}
	}
	
	@Test
	public void testWeixinBindCheck(){
		
		WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
		param.setOpenId("og2id1AGhPG__prVWrunr1xmmeKE");
		param.setPlatform("CH00000010");
		param.setRedirctUri("http://www.test.com/callback");
		
		OpenApiBaseResponse<WeixinOpenIdBindCheckResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}

}
