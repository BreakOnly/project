package com.jrmf.payment.entity;

import com.jrmf.controller.constant.PayType;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.payment.zxpay.ZXService;
import com.jrmf.payment.zxpay.util.RSA;
import com.jrmf.utils.AmountConvertUtil;
import com.jrmf.utils.SpringContextUtil;

public class ZXPayment implements Payment<Map<String, Object>, Map<String, Object>, String>{

	private Logger logger = LoggerFactory.getLogger(ZXPayment.class);
	public PaymentConfig payment;

	public ZXPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);

	@Override
	public Map<String, Object> getTransferTemple(UserCommission userCommission) {
		Map<String, Object> map = new HashMap<>();
		map.put("orderNo", userCommission.getOrderNo());
		map.put("userName", userCommission.getUserName());
		map.put("certId", userCommission.getCertId());
		map.put("amount", AmountConvertUtil.changeY2F(userCommission.getAmount()));
		map.put("phoneNo", userCommission.getPhoneNo());
		map.put("notifyUrl", baseInfo.getDomainName()+"/zxNotify.do?orderNo="+userCommission.getOrderNo());
		map.put("cardType", "DC");
		map.put("salaryType", "2");
		map.put("projectName", "资金下发");
		if (PayType.PINGAN_BANK.getCode() == userCommission.getPayType()) {
			map.put("payType", "1");
		} else if (PayType.ALI_PAY.getCode() == userCommission.getPayType()) {
			map.put("payType", "2");
		}
		map.put("cardAttribute", "C");//(C:对私 ,B：对公）
		map.put("account", userCommission.getAccount());
		return map;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为众薪");
		PaymentReturn<String> transferReturn = null;
		try{
			//请求参数封装
			Map<String, Object> params = getTransferTemple(userCommission);
			//调用服务
			ZXService zxService = new ZXService(payment.getPreHost(),payment.getAppIdAyg(),payment.getPayPrivateKey(),payment.getPayPublicKey(),payment.getParameter1(),params,"UTF-8");
			//请求众薪
			Map<String, Object> respMap = zxService.payment();
			transferReturn = getTransferResult(respMap);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			transferReturn = new PaymentReturn<String>(PayRespCode.RESP_UNKNOWN,PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),userCommission.getOrderNo());
		}
		return transferReturn;
	}

	@Override
	public PaymentReturn<String> getTransferResult(Map<String, Object> result) {
		String code = "";
		String message = "";
		String orderNo = "";
		try {
			//处理响应
			if(result.get("return_code")!=null && result.get("return_code").equals("T")){
				//受理成功
				logger.info("付款受理成功");
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
			}else{
				//付款受理失败
				logger.info("付款受理失败");
				logger.info("失败原因："+result.get("return_code")+"，失败描述："+result.get("return_message"));
				code = PayRespCode.RESP_CHECK_FAIL;
				message = String.valueOf(result.get("return_message"));
			}
		} catch (Exception e) {
			logger.error("付款受理异常",e);
			code = PayRespCode.RESP_UNKNOWN;
			message = "付款受理异常";
		}
		PaymentReturn<String> transferReturn = new PaymentReturn<String>(code,message,orderNo);
		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {
		String code = "";
		String message = "";
		String transCode = "";
		String transMsg = "";
		PaymentReturn<TransStatus> paymentReturn = null;
		try{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderNo", orderNo);//商户流水号
			ZXService zxService = new ZXService(payment.getPreHost(),payment.getAppIdAyg(),payment.getPayPrivateKey(),payment.getPayPublicKey(),payment.getParameter1(),params,"UTF-8");
			//请求众薪
			Map<String, Object> respMap = zxService.paymentQuery();
			if(respMap.get("return_code")!=null && respMap.get("return_code").equals("T")){
				//查询成功
				logger.info("订单号："+orderNo+"查询成功");
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				String content = String.valueOf(respMap.get("content"));
				JSONObject contentJson = JSONObject.parseObject(content);
				String jsonStr = RSA.decryptPri(String.valueOf(contentJson.get("sign")) , payment.getPayPrivateKey());
				JSONObject respInfo = JSONObject.parseObject(jsonStr);
				logger.info("订单号："+orderNo+"解密信息为："+respInfo);
				//汇款状态（0-处理中；1-成功；2-失败；其他-汇款处理中）
				Integer tranStat = Integer.parseInt(String.valueOf(respInfo.get("status")));
				if(tranStat==1){
					//交易成功
					logger.info("订单号："+orderNo+"交易成功,交易时间为"+respMap.get("endTime")+"，交易流水号为："+respMap.get("orderNo"));
					transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				}else if(tranStat==2){
					//交易失败
					logger.info("订单号："+orderNo+"交易失败："+respInfo.get("errorCode"));
					transCode = PayRespCode.RESP_TRANSFER_FAILURE;
					transMsg = respInfo.get("errorMsg")==null?"":String.valueOf(respInfo.get("errorMsg"));
				}else{
					//待付款/汇款处理中
					transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
				}
			}else{
				code = PayRespCode.RESP_FAILURE;
				message = String.valueOf(respMap.get("return_message"));
			}	
			TransStatus transStatus = new TransStatus(orderNo,transCode,transMsg);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,transStatus);
		}catch(Exception e){
			code = PayRespCode.RESP_FAILURE;
			message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,null);
		}
		return paymentReturn;
	}

	@Override
	public PaymentReturn<String> queryBalanceResult(String type) {
		return null;
	}

	@Override
	public PaymentReturn<String> linkageTransfer(
			LinkageTransferRecord transferRecord) {
		return null;
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(
			LinkageQueryTranHistory queryParams) {
		return null;
	}

}
