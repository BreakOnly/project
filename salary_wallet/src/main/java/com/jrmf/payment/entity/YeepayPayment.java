package com.jrmf.payment.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.ArithmeticUtil;
import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.error.YopError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class YeepayPayment implements Payment<Map<String, Object>, YopResponse, String> {

	private Logger logger = LoggerFactory.getLogger(YeepayPayment.class);

	public PaymentConfig payment;

	public YeepayPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public Map<String, Object> getTransferTemple(UserCommission userCommission) {

		String batchNo = userCommission.getOrderNo();
		String orderId = "YP" + userCommission.getOrderNo();
		String amount = userCommission.getAmount();
		String product = "WTJS";//WTJS：代付代发
		String urgency = "1"; // 是否加急出款   0：非加急  1：加急
		String accountName = userCommission.getUserName();//收款帐户的开户名称
		String accountNumber = userCommission.getAccount();
		String bankCode = userCommission.getBankNo();//银行编码

		String bankName = userCommission.getBankName();//银行名称 二选一
		String bankBranchName = "";//15家银行外必填
		String provinceCode = "";
		String cityCode = "";
		String feeType = "SOURCE";//SOURCE：商户承担     TARGET：用户承担         为空时，默认值：SOURCE
		String desc = userCommission.getRemark();
		String abstractInfo = "";

		Map<String, Object> params = new HashMap<>();
		params.put("batchNo", batchNo);
		params.put("orderId", orderId);
		params.put("amount", ArithmeticUtil.addZeroAndDot(amount));
		params.put("product", product);
		params.put("urgency", urgency);
		params.put("accountName", accountName);
		params.put("accountNumber", accountNumber);
		params.put("bankCode", bankCode);
		params.put("bankName", bankName);
		params.put("bankBranchName", bankBranchName);
		params.put("provinceCode", provinceCode);
		params.put("cityCode", cityCode);
		params.put("feeType", feeType);
		params.put("desc", desc);

		String leaveWord = userCommission.getRemark() == null ? "" : userCommission.getRemark();
		if("ABC".equals(bankCode)){
			if (leaveWord.length() > 4) {
				leaveWord = leaveWord.substring(0, 4);
			}
		}
		params.put("leaveWord", leaveWord);
		params.put("abstractInfo", abstractInfo);

		return params;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		try {
			String baseUrl = payment.getPreHost();
			String priavateKey = payment.getPayPrivateKey();
			String merchantNo = payment.getCorporationAccount();
			String uri = "/rest/v1.0/balance/transfer_send";

			Map<String, Object> params = getTransferTemple(userCommission);
			YopRequest yopRequest = new YopRequest(merchantNo, priavateKey, baseUrl);
	        Set<Entry<String, Object>> entry = params.entrySet();
	        for (Entry<String, Object> s : entry) {
	        	yopRequest.addParam(s.getKey(), s.getValue());
	        }
	        logger.info("-----下发--易宝通道---yoprequest:" + yopRequest.getParams());

//	        yoprequest.setSignAlg("RSA");
//	        YopResponse yopResponse = YopClient.post(uri, yoprequest);
			YopResponse yopResponse = YopClient3.postRsa(uri, yopRequest);
			logger.info("易宝通道--代付代发---yopResponse:" + yopResponse.getStringResult());

			PaymentReturn<String> transferReturn = getTransferResult(yopResponse);
			transferReturn.setAttachment(userCommission.getOrderNo());

			return transferReturn;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return new PaymentReturn<String>(PayRespCode.RESP_UNKNOWN,
					PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),
					userCommission.getOrderNo());
		}
	}

	@Override
	public PaymentReturn<String> getTransferResult(YopResponse yopResponse) {

		String code = "";
		String message = "";
		String orderNo = "";

		String yopState = yopResponse.getState();

        if ("FAILURE".equals(yopState)) {
        	YopError ypoError =  yopResponse.getError();
            if (ypoError != null) {
            	String errorCode = ypoError.getCode();
            	if("40049".equals(errorCode)){
        			code = PayRespCode.RESP_UNKNOWN;
        			message = PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN);
            	}else{
        			code = PayRespCode.RESP_FAILURE;
        			message = ypoError.getMessage();
            	}
            }else{
    			code = PayRespCode.RESP_UNKNOWN;
    			message = PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN);
            }
        }else{
			code = PayRespCode.RESP_SUCCESS;
			message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

			String responseResult = yopResponse.getStringResult();
			logger.info("易宝通道--代付代发--yopResponse.getStringResult：" + responseResult);

			if (responseResult != null) {
				Map<String, Object> jsonMap = new HashMap<>();
				jsonMap = JSON.parseObject(responseResult, new TypeReference<TreeMap<String, Object>>(){});
				orderNo = (String) jsonMap.get("batchNo");
			}
        }

		PaymentReturn<String> transferReturn = new PaymentReturn<String>(code,
				message,
				orderNo);

		 logger.info("易宝通道--代付代发--返回参数：" + transferReturn.toString());

		return transferReturn;

	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

		String code = "";
		String message = "";
		String transMsg = "";
		String transCode = "";
		String pageSize = "1";
		Map<String, Object> params = new HashMap<>();
		PaymentReturn<TransStatus> paymentReturn = null;

		params.put("batchNo", orderNo);
		params.put("pageSize", pageSize);

		String baseUrl = payment.getPreHost();
		String priavateKey = payment.getPayPrivateKey();
		String merchantNo = payment.getCorporationAccount();
		String uri = "/rest/v1.0/balance/transfer_query";

		YopRequest yopRequest = new YopRequest(merchantNo, priavateKey, baseUrl);
        Set<Entry<String, Object>> entry = params.entrySet();
        for (Entry<String, Object> s : entry) {
        	yopRequest.addParam(s.getKey(), s.getValue());
        }
        logger.info("-----下发--易宝通道--查询--yoprequest:" + yopRequest.getParams());

		try {
			YopResponse	yopResponse = YopClient3.postRsa(uri, yopRequest);
			logger.info("易宝通道--代付代发--查询--yopResponse:" + yopResponse.getStringResult());

			String yopState = yopResponse.getState();
			if ("SUCCESS".equals(yopState)) {
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);

				String responseResult = yopResponse.getStringResult();
				logger.info("易宝通道--代付代发--查询--yopResponse.responseResult：" + responseResult);
				if (responseResult != null) {
					Map<String, Object> jsonMap = new HashMap<>();
					jsonMap = JSON.parseObject(responseResult, new TypeReference<TreeMap<String, Object>>(){});
					logger.info("易宝通道--代付代发--查询--stringResult--jsonMap：" + jsonMap.toString());
					String errorCode = (String) jsonMap.get("errorCode");
					String errorMsg = (String) jsonMap.get("errorMsg");
					if("BAC000048".equals(errorCode)){//无此记录
						transCode = PayRespCode.RESP_TRANSFER_FAILURE;
						transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_NETWORK_EXCEPTION);
					}else{
						JSONArray jsonArray = (JSONArray) jsonMap.get("list");
						logger.info("易宝通道--代付代发--查询--jsonArray：" + jsonArray);
						if(jsonArray != null){
							JSONObject jsonObject = jsonArray.getJSONObject(0);
							logger.info("易宝通道--代付代发--查询--jsonObject：" + jsonObject.toString());
							String bankTrxStatusCode = (String) jsonObject.get("bankTrxStatusCode");
							String transferStatusCode = (String) jsonObject.get("transferStatusCode");
							logger.info("易宝通道--代付代发--查询--bankTrxStatusCode:" + bankTrxStatusCode + "---transferStatusCode:" + transferStatusCode);
							if("BAC001".equals(errorCode)
									&& "0026".equals(transferStatusCode)
									&& "S".equals(bankTrxStatusCode)){
								transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
								transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
								logger.info("易宝通道--代付代发--成功--");
							}else if ("BAC001".equals(errorCode)
									&& ("0027".equals(transferStatusCode) || "0028".equals(transferStatusCode))){
								transCode = PayRespCode.RESP_TRANSFER_FAILURE;
								transMsg = errorMsg == null? (String) jsonObject.get("bankMsg") : errorMsg;
								logger.info("易宝通道--代付代发--"+ transferStatusCode + "--失败--");
							}else if ("BAC001".equals(errorCode)
									&& "0026".equals(transferStatusCode)
									&& "F".equals(bankTrxStatusCode)){
								transCode = PayRespCode.RESP_TRANSFER_FAILURE;
								transMsg = errorMsg == null? (String) jsonObject.get("bankMsg") : errorMsg;
								logger.info("易宝通道--代付代发--"+ transferStatusCode + "-失败--");
							}else{
								transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
								transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
							}
						}else{
							transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
							transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
						}
					}
				}else{
					transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
				}
	        }else{
    			code = PayRespCode.RESP_FAILURE;
    			message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
	        }

			TransStatus transStatus = new TransStatus(orderNo,
					transCode,
					transMsg);

			paymentReturn = new PaymentReturn<TransStatus>(code,
					message,
					transStatus);

		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			code = PayRespCode.RESP_FAILURE;
			message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
			paymentReturn = new PaymentReturn<TransStatus>(code,
					message,
					null);
		}

		return paymentReturn;
	}

	@Override
	public PaymentReturn<String> queryBalanceResult(String type) {
		return null;
	}

	@Override
	public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
		return null;
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory queryParams) {
		return null;
	}

}
