package com.jrmf.signContract.channel;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.ymyf.YFService;
import com.jrmf.payment.ymyf.entity.ContractModle;
import com.jrmf.payment.ymyf.util.JsonUtils;
import com.jrmf.signContract.SignContractChannel;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.YmyfHasSignException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Component
public class YMChannel implements SignContractChannel{

	private static Logger logger = LoggerFactory.getLogger(YMChannel.class);

	private PaymentConfig paymentConfig;

	private AgreementTemplate agreementTemplate;

	@Override
	public Map<String, String> uploadPicInfo(String reqInfo,File backFile, File frontFile) {
		Map<String, String> respMap = new HashMap<String, String>();
		return respMap;
	}

	@Override
	public Map<String, String> signContract(String reqInfo) {
		Map<String, String> respMap = new HashMap<String, String>();
		try {
			JSONObject jsonObject = JSONObject.parseObject(reqInfo);
			ContractModle cm = new ContractModle();
			// 税优地通道ID
			cm.setLevyId(Long.parseLong(jsonObject.getString("serviceCompanyId")));
			// 签约类型 0：接口签约，1：公众号签约,2：签约接口
			cm.setSignType(2);
			// 身份证号
			cm.setIdCard(jsonObject.getString("certId"));
			// 姓名
			cm.setName(jsonObject.getString("userName"));
			cm.setOtherParam(jsonObject.getString("remark"));
			String cardNo = jsonObject.getString("bankCardNo");
			if (!StringUtil.isEmpty(cardNo)) {
				cm.setCardNo(cardNo);
			}
			String mobile = jsonObject.getString("mobilePhone");
			if (!StringUtil.isEmpty(mobile)) {
				cm.setMobile(mobile);
			}
			String json=JsonUtils.toJson(cm);
			YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getAppIdAyg(),
					paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(),
					paymentConfig.getApiKey(), json, "UTF-8");
			//响应信息
			String respInfo = yfService.signContract(jsonObject.getInteger("uploadFlag"),jsonObject.getString("backPicUrl"),jsonObject.getString("frontPicUrl"));
			Map<String, String> respUpInfo = JSONObject.parseObject(respInfo, Map.class);
			//封装响应结果
			if(respUpInfo.get("resCode").equals("0000")){
				respMap.put("code", "0000");
			}else{
				respMap.put("code", respUpInfo.get("resCode"));
			}
			respMap.put("msg", respUpInfo.get("resMsg"));
		}catch (YmyfHasSignException e) {
			//该用户信息已经做过签约
			respMap.put("code", "1014");
			respMap.put("msg", e.getMessage());
		} catch (Exception e) {
			respMap.put("code", "1009");
			respMap.put("msg", e.getMessage());
			logger.error("请求异常",e);
		}
		return respMap;
	}

	@Override
	public Map<String, String> signContractQuery(String reqInfo) {

		Map<String, String> respMap = new HashMap<String, String>();
		try {
			JSONObject jsonObject = JSONObject.parseObject(reqInfo);
			ContractModle cm = new ContractModle();
			// 税优地通道ID
			cm.setLevyId(Long.parseLong(jsonObject.getString("serviceCompanyId")));
			// 身份证号
			cm.setIdCard(jsonObject.getString("certId"));
			// 姓名
			cm.setName(jsonObject.getString("userName"));
			String json=JsonUtils.toJson(cm);
			YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getAppIdAyg(),
					paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(),
					paymentConfig.getApiKey(), json, "UTF-8");
			//响应信息
			String respInfo = yfService.signContractQuery();
			Map<String, String> respUpInfo = JSONObject.parseObject(respInfo, Map.class);
			//封装响应结果
			if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==1){
				//签约成功
				respMap.put("code", "0000");
				respMap.put("state", "1");
			}else if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==4){
				//签约失败
				respMap.put("code", "0000");
				respMap.put("state", "2");
				respMap.put("msg", respUpInfo.get("retMsg"));
			}else if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==0){
				//未申请签约
				respMap.put("code", "0000");
				respMap.put("state", "0");
			}else{
				//签约处理中
				respMap.put("code", "0000");
				respMap.put("state", "3");
			}
			//封装响应结果
		}catch (YmyfHasSignException e) {
			//该用户信息已经做过签约
			respMap.put("code", "1014");
			respMap.put("msg", e.getMessage());
		} catch (Exception e) {
			respMap.put("code", "1009");
			respMap.put("msg", e.getMessage());
			logger.error("请求异常",e);
		}
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
