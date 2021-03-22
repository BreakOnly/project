package com.jrmf.payment.openapi.test;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.constants.CertificationType;
import com.jrmf.payment.openapi.model.request.prepare.AsynCertificationParam;
import com.jrmf.payment.openapi.model.request.prepare.PayAuthorizeParam;
import com.jrmf.payment.openapi.model.request.prepare.QueryCertificationParam;
import com.jrmf.payment.openapi.model.request.prepare.SyncCertificationParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.prepare.AsynCertificationResult;
import com.jrmf.payment.openapi.model.response.prepare.PayAuthorizeResult;
import com.jrmf.payment.openapi.model.response.prepare.QueryCertificationResult;

/**
 * 前置授权-白名单验证/二、三、四要素实名认证
 * @author Napoleon.Chen
 * @date 2018年9月17日
 */
public class PrepareOpenApiClientTest {

	private static OpenApiClient client;
	
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
	public void testQueryCertification(){
		QueryCertificationParam param = new QueryCertificationParam();
		param.setCerRequestId("881591e2091342b1a30156cc9f179400");
		param.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
		OpenApiBaseResponse<QueryCertificationResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testAsynCertification() {
		AsynCertificationParam param = new AsynCertificationParam();
		param.setName("王5");
		param.setIdcard("450403******00***");
		param.setMobile("138*********");
		param.setPayAccount("62******************");
		param.setPayAccountType("BANK_CARDNO"); // 支付宝：ALIPAY_USERID, ALIPAY_LOGONID;银行卡：BANK_CARDNO,
		param.setBankName("中国银行");
		param.setValidType(CertificationType._4element);	// 验证要素：银行卡是{二/三/四}, 支付宝是2或3
		param.setNotifyUrl("");
		param.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
		OpenApiBaseResponse<AsynCertificationResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
		
	}
	
	@Test
	public void testSyncCertification() {
		SyncCertificationParam param = new SyncCertificationParam();
		param.setName("王5");
		param.setIdcard("450403******00***");
		param.setMobile("138*********");
		param.setPayAccount("62******************");
		param.setPayAccountType("BANK_CARDNO"); // 支付宝：ALIPAY_USERID, ALIPAY_LOGONID;银行卡：BANK_CARDNO,
		param.setBankName("中国银行");
		param.setValidType(CertificationType._4element);	// 验证要素：银行卡是{二/三/四}, 支付宝是2或3
		param.setNotifyUrl("");
		param.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
		System.err.println(param.getRequestId());
		OpenApiBaseResponse<AsynCertificationResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
		
	}
	
	@Test
	public void testApplyRiskManagement() {
		PayAuthorizeParam param = new PayAuthorizeParam();
		param.setAmount("1.0");
		param.setIdcard("xxx");
		param.setMobile("13878481777");
		param.setName("茂聪");
		param.setOrderNo("11111");
		param.setPayAccount("yyy");
		param.setTime("yyyy-MM-dd");
		param.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
		OpenApiBaseResponse<PayAuthorizeResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
	}
	
}
