package com.jrmf.payment.openapi.test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.constants.CertificationType;
import com.jrmf.payment.openapi.model.request.deliver.PayUnifiedOrderParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractCancelOrderParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractOrderBatchParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractRealtimeQueryParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractSignerItem;
import com.jrmf.payment.openapi.model.request.econtract.IdentityAsycUploadParam;
import com.jrmf.payment.openapi.model.request.econtract.IdentityUploadParam;
import com.jrmf.payment.openapi.model.request.econtract.QueryOrderParam;
import com.jrmf.payment.openapi.model.request.prepare.AsynCertificationParam;
import com.jrmf.payment.openapi.model.request.prepare.PayAuthorizeParam;
import com.jrmf.payment.openapi.model.request.prepare.QueryCertificationParam;
import com.jrmf.payment.openapi.model.request.weixin.WeixinOpenIdBindCheckParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderResult;
import com.jrmf.payment.openapi.model.response.econtract.QueryOrderResult;
import com.jrmf.payment.openapi.model.response.econtract.RealTimeResult;
import com.jrmf.payment.openapi.model.response.prepare.AsynCertificationResult;
import com.jrmf.payment.openapi.model.response.prepare.PayAuthorizeResult;
import com.jrmf.payment.openapi.model.response.prepare.QueryCertificationResult;
import com.jrmf.payment.openapi.model.response.weixin.WeixinOpenIdBindCheckResult;

public class OpenApiClientTest {

	static OpenApiClient client;
	
	@BeforeClass
	public static void init(){
		String priKeyString = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOI4fc5POr0cZmyyGuwAonQGNVUvEd8KWtYk2ozNM1S6la7BK7DwWyk6kI10/NsY+Wues+C0fN86Ol0uckTF+s4NqMqw/W8PZ0HOV0mW1Ew/EV+HT1jRnbYviTRNWx9zS+xRjiDam3VdbElSwPQQulefAlQctcxsT1nPGSbWxvFpAgMBAAECgYEAnG6iGHNTVAh6j3mOAlrh+8d7Q9+bxRd8/w5XDvyrHVE1RrYPx3g+IcF8ykT2wW+Asrn4+07z9s1mJJ+EpygcqOmWtrExQlHjYcAn+27usxhTjWtNZ7AQoF0O0zIIAb8H2dm6Sin2fwvkZORaUy9gMlM7FW/LA48l49ptvs4aTZ0CQQDx5ezsesL8zA3SR+lr8xLTSjJokUOrK5wbYj+SGe+jgkUDxNU53zQXjTKM99rR+4cwomKbMFqGhZzDw1rEmccrAkEA72iYnXAKFHX05TB8YNkcxouwkFpi3rcD7m8LjEcXT/quNvsrcHfJB4iw63H2TwXvUQ4SJrxzjUMwSNkB19zfuwJBAOu6/0ns0Dv+trFndufF92CEe98/QMx8MSLWedDtCYU0HAFyPcCp7V/OL6cEmu/qyHHyrVlCo9VYO87if3/7xAUCQBi98Y/LxW7p5d5NzXzg00V9qEiy3qbvuRtKJKJhsnoUiS6rdIjSCFeb+9TJWVA/Z8UztBKGxVZjDDlrG/KoJAMCQQCRnIyn35lHhj5iXyB2Ofo5wHI45NvPpj7ut3rec58fMLTs3yw5w9OWnOSUCnbZ3gTQHeu4/3Ebu4ozzw4bi2iK";
		client = new OpenApiClient.Builder().appId("openapitest").privateKey(priKeyString).useSandbox().build();
	}
	
	@Test
	public void testQueryCertification(){
		QueryCertificationParam param = new QueryCertificationParam();
		param.setCerRequestId("1234");
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
		param.setValidType(CertificationType._4element);	// 验证要素：银行卡是{二/三/四}, 支付宝是传2
		param.setNotifyUrl("");
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
		OpenApiBaseResponse<PayAuthorizeResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testWeixinBindCheck(){
		
		WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
		param.setOpenId("og2id1AGhPG__prVWrunr1xmmeKE");
		param.setPlatform("CH00000047");
		param.setRedirctUri("http://www.test.com/callback");
		
		OpenApiBaseResponse<WeixinOpenIdBindCheckResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testPayUnifiedOrder(){
		PayUnifiedOrderParam param = new PayUnifiedOrderParam();
		param.setAccountName("张三");
		param.setAccountNo("oCizG0ebYZxC2Z6ccEIxWqWJIlXU");
		param.setAmount(new BigDecimal(1.0));
		param.setBank("wx");
		param.setIdCard("511602197004013078");
		param.setMemo("测试");
		param.setOutOrderNo(RandomStringUtils.random(16, false, true));
		param.setPhone("13800138000");
		
		OpenApiBaseResponse<PayUnifiedOrderResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testEcontractOrderQuery(){
		QueryOrderParam param = new QueryOrderParam();
		param.setExtrOrderId("1111111111");
		param.setOrderId("2222222222");
		
		OpenApiBaseResponse<QueryOrderResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
		
	}
	
	@Test
	public void testEcontractOrderCancel(){
		ContractCancelOrderParam param = new ContractCancelOrderParam();
		param.setExtrOrderId("1111111111");
		
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
		
	}
	
	@Test
	public void testEcontractOrderRealtimeQuery(){
		ContractRealtimeQueryParam param = new ContractRealtimeQueryParam();
		param.setExtrOrderId("1111111111");
		
		OpenApiBaseResponse<RealTimeResult> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
		
	}
	
	@Test
	public void testIdentityAsycUpload(){
		IdentityAsycUploadParam param = new IdentityAsycUploadParam();
		param.setName("张无忌");
		param.setIdentityType("0");
		param.setIdentity("990723197712298499");
		param .setNotifyUrl("http://www.test.com/notify");
		param.setBackfile(new File("/Users/jiangwei/Documents/balance.png"));
		param.setFrontfile(new File("/Users/jiangwei/Documents/balance.png"));
		
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testIdentityUpload(){
		IdentityUploadParam param = new IdentityUploadParam();
		param.setName("张无忌");
		param.setIdentityType("0");
		param.setIdentity("990723197712298499");
		param.setBackfile(new File("E:\\images\\fbcbd100baa1cd116ec9da8ab312c8fcc3ce2d37.jpg"));
		param.setFrontfile(new File("E:\\images\\fbcbd100baa1cd116ec9da8ab312c8fcc3ce2d37.jpg"));
		
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testEcontractBatchSubmit() {
		ContractSignerItem item = new ContractSignerItem();
		item.setExtrOrderId(RandomStringUtils.random(16, false, true));
		item.setIdentity("420821198909256404");
		item.setIdentityType(CertificationType.IDENTITY_TYPE_ID_CARD);
		item.setName("顾昌天");
		item.setPersonalMobile("15920678408");
		ContractSignerItem dto1 = new ContractSignerItem();
		dto1.setExtrOrderId(RandomStringUtils.random(16, false, true));
		dto1.setIdentity("420821198909256404");
		dto1.setIdentityType(CertificationType.IDENTITY_TYPE_ID_CARD);
		dto1.setName("顾昊");
		dto1.setPersonalMobile("15920678408");
		List<ContractSignerItem> dtos = new ArrayList<>();
		dtos.add(item);
		dtos.add(dto1);
		ContractOrderBatchParam param = new ContractOrderBatchParam();
		param.setTemplateId("519");
		param.setNotifyUrl("http://www.test.com/notify");
		param.setList(dtos);
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
	}
}
