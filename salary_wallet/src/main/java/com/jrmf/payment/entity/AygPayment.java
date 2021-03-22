package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.PayBalanceQueryParam;
import com.jrmf.payment.openapi.model.request.deliver.PayUnifiedOrderParam;
import com.jrmf.payment.openapi.model.request.deliver.PayUnifiedOrderQueryParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.deliver.PayBalanceDetailQueryResult;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderQueryResult;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderResult;
import com.jrmf.payment.util.*;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class AygPayment implements Payment<PayUnifiedOrderParam, OpenApiBaseResponse<PayUnifiedOrderResult>, String> {

	private Logger logger = LoggerFactory.getLogger(AygPayment.class);

	public final int BANK_TYPE = 4;

	public PaymentConfig payment;

	public AygPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public PayUnifiedOrderParam getTransferTemple(UserCommission userCommission) {

		PayUnifiedOrderParam param = new PayUnifiedOrderParam();
		param.setAccountName(userCommission.getUserName());
		param.setAccountNo(userCommission.getAccount());
		param.setAmount(new BigDecimal(userCommission.getAmount()));

		String bankName = userCommission.getBankName();
		int payType = userCommission.getPayType();
		if(payType != BANK_TYPE){
			bankName = AygPayBankName.codeOf(payType).getBankName();
		}

		param.setBank(bankName);
		param.setIdCard(userCommission.getCertId());
	    param.setMemo(userCommission.getRemark() == null ? "-" : userCommission.getRemark());
	    param.setOutOrderNo(userCommission.getOrderNo());

	    String phone = userCommission.getPhoneNo();
	    if(StringUtil.isEmpty(phone)){
	    	phone = "17701393451";
	    }
	    param.setPhone(phone);

	    return param;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

		String aygAppId = payment.getAppIdAyg();
		String privateKey = payment.getPayPrivateKey();
		logger.info("---------爱员工appid------------:" + aygAppId);
		logger.info("---------爱员工privateKey------------:" + privateKey);
		OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);

		if(client == null){
			synchronized(ClientMapUtil.httpClient){
				client = ClientMapUtil.httpClient.get(aygAppId);
				if(client == null){
					client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
					ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
				}
			}
		}
		PayUnifiedOrderParam payUnifiedOrderParam = getTransferTemple(userCommission);
		if (payment.getServiceCompanyId()!=null){
            payUnifiedOrderParam.setServiceCompanyId(payment.getServiceCompanyId());
        }
		logger.info("---------爱员工 请求参数对象 PayUnifiedOrderParam ------------:" + com.alibaba.fastjson.JSONObject.toJSONString(payUnifiedOrderParam));
		OpenApiBaseResponse<PayUnifiedOrderResult> result = client.execute(payUnifiedOrderParam);

		logger.info("---------爱员工 返回参数对象 OpenApiBaseResponse<PayUnifiedOrderResult> ------------:" + com.alibaba.fastjson.JSONObject.toJSONString(result));

		return getTransferResult(result);
	}

	@Override
	public PaymentReturn<String> getTransferResult(OpenApiBaseResponse<PayUnifiedOrderResult> payUnifiedOrderResult) {

		PaymentReturn<String> transferReturn = new PaymentReturn<String>(payUnifiedOrderResult.getCode(),
				payUnifiedOrderResult.getMsg(),
				payUnifiedOrderResult.getData().getOutOrderNo());

		  logger.info("爱员工支付下发返回参数：" + transferReturn.toString());

		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

		String aygAppId = payment.getAppIdAyg();
		String privateKey = payment.getPayPrivateKey();
		logger.info("---------爱员工appid------------:" + aygAppId);
		logger.info("---------爱员工privateKey------------:" + privateKey);

		OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);
		if(client == null){
			synchronized(ClientMapUtil.httpClient){
				client = ClientMapUtil.httpClient.get(aygAppId);
				if(client == null){
					client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
					ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
				}
			}
		}

		PayUnifiedOrderQueryParam param = new PayUnifiedOrderQueryParam();
		param.setOutOrderNo(orderNo);
		logger.info("---------爱员工 请求参数对象 PayUnifiedOrderQueryParam ------------:" + com.alibaba.fastjson.JSONObject.toJSONString(param));
		OpenApiBaseResponse<PayUnifiedOrderQueryResult> payUnifiedOrderQueryResult = client.execute(param);
		logger.info("---------爱员工 返回参数对象 OpenApiBaseResponse<PayUnifiedOrderQueryResult> ------------:" + com.alibaba.fastjson.JSONObject.toJSONString(payUnifiedOrderQueryResult));

		String code = "";
		String massage = "";
		String transCode = "";
		String transMsg = "";
		String transOrderNo = "";

		if(OpenApiBaseResponse.SUCCESS_CODE.equals(payUnifiedOrderQueryResult.getCode())){
			code = PayRespCode.RESP_SUCCESS;
			if("30".equals(payUnifiedOrderQueryResult.getData().getCode())){
				transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
			}else if("40".equals(payUnifiedOrderQueryResult.getData().getCode())){
				transCode = PayRespCode.RESP_TRANSFER_FAILURE;
			}else{
				transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
			}
		}else if(PayRespCode.AYG_RESP_ORDER_NOEXISTS.equals(payUnifiedOrderQueryResult.getCode())){
			code = PayRespCode.RESP_SUCCESS;
			transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
			transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
		}else{
			code = PayRespCode.RESP_FAILURE;
		}

		massage = payUnifiedOrderQueryResult.getMsg();
		PayUnifiedOrderQueryResult queryResult = payUnifiedOrderQueryResult.getData();
		if(queryResult != null){
			transMsg = queryResult.getMsg();
			transOrderNo = queryResult.getOutOrderNo();
		}

		TransStatus transStatus = new TransStatus(transOrderNo,
				transCode,
				transMsg);

		PaymentReturn<TransStatus>  paymentReturn = new PaymentReturn<TransStatus>(code,
				massage,
				transStatus);
		logger.info("爱员工支付查询返回参数：" + paymentReturn.toString());

		return paymentReturn;
	}

	@Override
	public PaymentReturn<String> queryBalanceResult(String type) {

		String aygAppId = payment.getAppIdAyg();
		OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);
		if(client == null){
			String privateKey = payment.getPayPrivateKey();
			client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
			ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
		}

		PayBalanceQueryParam param = new PayBalanceQueryParam();
		OpenApiBaseResponse<PayBalanceDetailQueryResult> paramter = client.execute(param);

		String balance = "";
		if(String.valueOf(PayType.ALI_PAY.getCode()).equals(type)){
			balance = paramter.getData().getAlipayBalance().toString();
		}
		if(String.valueOf(PayType.WECHAT.getCode()).equals(type)){
			balance = paramter.getData().getWxBalance().toString();
		}
		if(String.valueOf(PayType.PINGAN_BANK.getCode()).equals(type)){
			balance = paramter.getData().getBankBalance().toString();
		}

		PaymentReturn<String> transferReturn = new PaymentReturn<String>(paramter.getCode(),
				paramter.getMsg(),
				balance);

		logger.info("爱员工查询返回参数：" + transferReturn.toString());

		return transferReturn;
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
