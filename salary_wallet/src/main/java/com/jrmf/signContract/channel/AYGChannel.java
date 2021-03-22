package com.jrmf.signContract.channel;

import com.jrmf.domain.PaymentConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.econtract.ContractOrderSingleParam;
import com.jrmf.payment.openapi.model.request.econtract.IdentityAsycUploadParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.econtract.ContractOrderResult;
import com.jrmf.signContract.SignContractChannel;
@Component
public class AYGChannel implements SignContractChannel{

	private static Logger logger = LoggerFactory.getLogger(AYGChannel.class);

	private AgreementTemplate agreementTemplate;

	private PaymentConfig paymentConfig;

	@Override
	public Map<String, String> uploadPicInfo(String reqInfo,File backFile, File frontFile) {
		Map<String, String> respMap = new HashMap<String, String>();
		logger.info("爱员工签约上传证件信息...");
		OpenApiClient client = new OpenApiClient.Builder().appId(paymentConfig.getAppIdAyg())
				.privateKey(agreementTemplate.getPrivateKey()).baseUrl(agreementTemplate.getReqUrl()).build();
		IdentityAsycUploadParam param = new IdentityAsycUploadParam();
		JSONObject jsonObject = JSONObject.parseObject(reqInfo);
		param.setName(jsonObject.getString("userName"));
		param.setIdentityType("0");
		param.setIdentity(jsonObject.getString("certId"));
		param.setNotifyUrl(jsonObject.getString("notifyUrl"));
		param.setBackfile(backFile);
		param.setFrontfile(frontFile);
		OpenApiBaseResponse<Void> response = client.execute(param);
		respMap.put("code", response.getCode());
		respMap.put("msg", response.getMsg());
		return respMap;
	}


	@Override
	public Map<String, String>  signContract(String reqInfo) {
		Map<String, String> respMap = new HashMap<String, String>();
		OpenApiClient client = new OpenApiClient.Builder().appId(paymentConfig.getAppIdAyg())
				.privateKey(agreementTemplate.getPrivateKey()).baseUrl(agreementTemplate.getReqUrl()).build();
		ContractOrderSingleParam param = new ContractOrderSingleParam();
		JSONObject jsonObject = JSONObject.parseObject(reqInfo);
		param.setTemplateId(agreementTemplate.getThirdTemplateId());
		param.setNotifyUrl(jsonObject.getString("notifyUrl"));
		param.setExtrOrderId(jsonObject.getString("extrOrderId"));
		param.setIdentity(jsonObject.getString("certId"));
		param.setIdentityType("0");
		param.setName(jsonObject.getString("userName"));
		param.setPersonalMobile(jsonObject.getString("mobilePhone"));
		OpenApiBaseResponse<ContractOrderResult> response = client.execute(param);
		respMap.put("code", response.getCode());
		respMap.put("msg", response.getMsg());
		return respMap;
	}

	@Override
	public Map<String, String> signContractQuery(String reqInfo) {
		Map<String, String> respMap = new HashMap<String, String>();
		return respMap;
	}

	public AgreementTemplate getAgreementTemplate() {
		return agreementTemplate;
	}

	public void setAgreementTemplate(AgreementTemplate agreementTemplate) {
		this.agreementTemplate = agreementTemplate;
	}

	public PaymentConfig getPaymentConfig() {
		return paymentConfig;
	}

	public void setPaymentConfig(PaymentConfig paymentConfig) {
		this.paymentConfig = paymentConfig;
	}
}
