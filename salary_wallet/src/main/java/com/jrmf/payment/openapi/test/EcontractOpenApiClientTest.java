package com.jrmf.payment.openapi.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.constants.CertificationType;
import com.jrmf.payment.openapi.model.request.econtract.ContractCancelOrderParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractOrderBatchParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractOrderExtrSyncParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractOrderSingleParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractRealtimeQueryParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractSignerItem;
import com.jrmf.payment.openapi.model.request.econtract.ContractTemplateDownloadParam;
import com.jrmf.payment.openapi.model.request.econtract.IdentityAsycUploadParam;
import com.jrmf.payment.openapi.model.request.econtract.IdentityUploadBase64Param;
import com.jrmf.payment.openapi.model.request.econtract.IdentityUploadParam;
import com.jrmf.payment.openapi.model.request.econtract.QueryOrderParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.econtract.ContractOrderResult;
import com.jrmf.payment.openapi.model.response.econtract.IdentityUploadBase64Result;
import com.jrmf.payment.openapi.model.response.econtract.QueryOrderResult;
import com.jrmf.payment.openapi.model.response.econtract.RealTimeResult;
import com.jrmf.payment.openapi.param.econtract.CommonExtrResult;
import com.jrmf.payment.openapi.utils.FileUtils;
import com.jrmf.payment.openapi.utils.TokenGenerator;

/**
 * 电子签约
 * @author Napoleon.Chen
 * @date 2018年9月17日
 */
public class EcontractOpenApiClientTest {

	static OpenApiClient client;
	private static final String priKeyString = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOI4fc5POr0cZmyyGuwAonQGNVUvEd8KWtYk2ozNM1S6la7BK7DwWyk6kI10/NsY+Wues+C0fN86Ol0uckTF+s4NqMqw/W8PZ0HOV0mW1Ew/EV+HT1jRnbYviTRNWx9zS+xRjiDam3VdbElSwPQQulefAlQctcxsT1nPGSbWxvFpAgMBAAECgYEAnG6iGHNTVAh6j3mOAlrh+8d7Q9+bxRd8/w5XDvyrHVE1RrYPx3g+IcF8ykT2wW+Asrn4+07z9s1mJJ+EpygcqOmWtrExQlHjYcAn+27usxhTjWtNZ7AQoF0O0zIIAb8H2dm6Sin2fwvkZORaUy9gMlM7FW/LA48l49ptvs4aTZ0CQQDx5ezsesL8zA3SR+lr8xLTSjJokUOrK5wbYj+SGe+jgkUDxNU53zQXjTKM99rR+4cwomKbMFqGhZzDw1rEmccrAkEA72iYnXAKFHX05TB8YNkcxouwkFpi3rcD7m8LjEcXT/quNvsrcHfJB4iw63H2TwXvUQ4SJrxzjUMwSNkB19zfuwJBAOu6/0ns0Dv+trFndufF92CEe98/QMx8MSLWedDtCYU0HAFyPcCp7V/OL6cEmu/qyHHyrVlCo9VYO87if3/7xAUCQBi98Y/LxW7p5d5NzXzg00V9qEiy3qbvuRtKJKJhsnoUiS6rdIjSCFeb+9TJWVA/Z8UztBKGxVZjDDlrG/KoJAMCQQCRnIyn35lHhj5iXyB2Ofo5wHI45NvPpj7ut3rec58fMLTs3yw5w9OWnOSUCnbZ3gTQHeu4/3Ebu4ozzw4bi2iK";
	private static final String appId = "openapitest";
	
	@BeforeClass
	public static void init(){
		boolean useSandbox = true;
		client = useSandbox 
			? new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).useSandbox().build()
			: new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).build();
	}
	
	@Test
	public void testEcontractOrderQuery() {
		QueryOrderParam param = new QueryOrderParam();
		param.setExtrOrderId("9675353640508791");
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
		param.setExtrOrderId("9675353640508791");
		
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
		param.setName("顾叔天");
		param.setIdentityType("0");
		param.setIdentity("130181199009104214");
		param.setBackfile(new File("E:\\images\\fbcbd100baa1cd116ec9da8ab312c8fcc3ce2d37.jpg"));
		param.setFrontfile(new File("E:\\images\\fbcbd100baa1cd116ec9da8ab312c8fcc3ce2d37.jpg"));
		
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testIdentityUploadBase64() {
		IdentityUploadBase64Param param = new IdentityUploadBase64Param();
		param.setExtrSystemId(appId);
		param.setName("顾季天");
		param.setIdentity("130181199009104214");
		param.setIdentityType("0");
		param.setBackfile(Base64.getEncoder().encodeToString(FileUtils.getByteByFile("E:\\idcard-image\\idcard-back.jpg")));
		param.setFrontfile(Base64.getEncoder().encodeToString(FileUtils.getByteByFile("E:\\idcard-image\\idcard-front.jpg")));
		OpenApiBaseResponse<IdentityUploadBase64Result> response = client.execute(param);
		System.out.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testEcontractTemplateDownload() {
		try {
			ContractTemplateDownloadParam param = new ContractTemplateDownloadParam();
			param.setTemplateId("477");
			param.setExtrSystemId("aiyuangong");
			byte [] fileBytes = client.downloadContract(param);
			if (ArrayUtils.isEmpty(fileBytes)) {
				System.err.println("No data");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEcontractBatchSubmit() {
		ContractSignerItem item = new ContractSignerItem();
		item.setExtrOrderId(RandomStringUtils.random(16, false, true));
		item.setIdentity("420821198909256404");
		item.setIdentityType(CertificationType.IDENTITY_TYPE_ID_CARD);
		item.setName("顾伯天");
		item.setPersonalMobile("15920678408");
		
		ContractSignerItem dto1 = new ContractSignerItem();
		dto1.setExtrOrderId(RandomStringUtils.random(16, false, true));
		dto1.setIdentity("420821198909256404");
		dto1.setIdentityType(CertificationType.IDENTITY_TYPE_ID_CARD);
		dto1.setName("顾仲天");
		dto1.setPersonalMobile("15920678408");
		
		List<ContractSignerItem> dtos = new ArrayList<>();
		dtos.add(item);
		dtos.add(dto1);
		
		ContractOrderBatchParam param = new ContractOrderBatchParam();
		param.setTemplateId("477");
		param.setNotifyUrl("http://www.test.com/notify"); // 必填
		param.setList(dtos);
		
		OpenApiBaseResponse<Void> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testEcontractSingleSubmit() {
		ContractOrderSingleParam param = new ContractOrderSingleParam();
		param.setTemplateId("477");
		param.setNotifyUrl("https://");	// 必填
		param.setExtrOrderId(RandomStringUtils.random(16, false, true));
		param.setIdentity("420821198909256404");
		param.setIdentityType(CertificationType.IDENTITY_TYPE_ID_CARD.getCode());
		param.setName("顾红晶");
		param.setPersonalMobile("15920678498");
		OpenApiBaseResponse<ContractOrderResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response, true));
	}
	
	@Test
	public void testEcontractSyncOuterOrder() {
		ContractOrderExtrSyncParam param = new ContractOrderExtrSyncParam();
		param.setContractId(TokenGenerator.generateSerialNumber(1));
		param.setCreateTime(new Date());
		param.setExtrOrderId(TokenGenerator.generate(""));
		param.setManufacturer("1");
		param.setOuterDownloadUrl("http://www.aiyuangong.com");
		param.setPartyaSignTime(new Date());
		param.setPartyaUserId("92320826MA1Q43Y18Q");
		param.setPartyaUserName("Aa");
		param.setPartybSignTime(new Date());
		param.setPartybUserId("B");
		param.setPartybUserName("Bb");
		param.setPartycSignTime(new Date());
		param.setPartycUserId("C");
		param.setPartycUserName("Cc");
		param.setPersonalIdentity("111111111111111111111111");
		param.setPersonalName("杨广");
		param.setPersonalIdentityType("0");
		param.setPersonalMobile("15988662130");
		param.setTemplateId("998");
		param.setPersonalCertId("GZCA-XX-" + System.currentTimeMillis());
		param.setPersonalCertType("GZCA");
		param.setContractContentBase64(Base64.getEncoder().encodeToString(FileUtils.getByteByFile("E:\\data\\econtract\\original\\C端服务合作协议.pdf")));
		param.setCheck(false);	
		OpenApiBaseResponse<CommonExtrResult> response = client.execute(param);
		System.err.println(JSON.toJSONString(response));
	}

}
