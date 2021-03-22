package com.jrmf.payment.entity;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.hddpay.HddService;
import com.jrmf.payment.hddpay.util.SHA1WithRSA;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.AmountConvertUtil;

public class HddPayment implements Payment<Map<String, String>, Map<String, Object>, String>{
	
	private Logger logger = LoggerFactory.getLogger(HddPayment.class);
	public PaymentConfig payment;

	public HddPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}
	
	@Override
	public Map<String, String> getTransferTemple(UserCommission userCommission) {
		Map<String, String> map = new HashMap<>();
		map.put("orderNo", userCommission.getOrderNo());
		map.put("userName", userCommission.getUserName());
		map.put("certId", userCommission.getCertId());
		map.put("amount", AmountConvertUtil.changeY2F(userCommission.getAmount()));
		map.put("phoneNo", userCommission.getPhoneNo());
		map.put("remark", userCommission.getRemark());
		switch (userCommission.getPayType()) {
		case 2:
			map.put("alipay_account", userCommission.getAccount());
			break;
		default:
			map.put("account", userCommission.getAccount());
			break;
		}
		return map;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为惠多多");
		PaymentReturn<String> transferReturn = null;
		try{
			//请求参数封装
			Map<String, String> params = getTransferTemple(userCommission);
			//调用服务
			HddService zxService = new HddService(payment.getPreHost(),payment.getAppIdAyg(),payment.getPayPrivateKey(),payment.getPayPublicKey(),payment.getParameter1(),params,"UTF-8");
			//请求惠多多
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
			if(result.get("code")!=null && result.get("code").equals("SUCCESS")){
				//受理成功
				logger.info("付款受理成功");
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
			}else{
				//付款受理失败
				logger.info("付款受理失败");
				logger.info("失败原因："+result.get("code")+"，失败描述："+result.get("msg"));
				code = PayRespCode.RESP_CHECK_FAIL;
				message = String.valueOf(result.get("msg"));
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
			Map<String, String> params = new HashMap<String, String>();
			params.put("orderNo", orderNo);//商户流水号
			HddService zxService = new HddService(payment.getPreHost(),payment.getAppIdAyg(),payment.getPayPrivateKey(),payment.getPayPublicKey(),payment.getParameter1(),params,"UTF-8");
			//请求众薪
			Map<String, Object> respMap = zxService.paymentQuery();
			if(respMap.get("code")!=null && respMap.get("code").equals("SUCCESS")){
				//查询成功
				logger.info("订单号："+orderNo+"查询成功");
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				String content = String.valueOf(respMap.get("data"));
				JSONObject contentJson = JSONObject.parseObject(content);
				String status = contentJson.getString("status");
				//WAIT_PAY("待支付"),PAYMENT("确认支付"),PROCESSING("银行处理中"),SUCCEED("交易成功"),CLOSED("交易关闭")
				if("SUCCEED".equals(status)){
					//交易成功
					logger.info("订单号："+orderNo+"交易成功,交易流水号为："+contentJson.getString("trade_no"));
					transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				}else if("CLOSED".equals(status)){
					//交易失败
					logger.info("订单号："+orderNo+"交易失败："+contentJson.getString("remark"));
					transCode = PayRespCode.RESP_TRANSFER_FAILURE;
					transMsg = contentJson.getString("remark")==null?"":contentJson.getString("remark");
				}else{
					//待付款/汇款处理中
					transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
				}
			}else{
				code = PayRespCode.RESP_FAILURE;
				message = String.valueOf(respMap.get("msg"));
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
