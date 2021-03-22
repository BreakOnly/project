package com.jrmf.payment.entity;

import com.dutiantech.demo.tool.DTDateUtil;
import com.dutiantech.demo.tool.DTRSACode;
import com.dutiantech.demo.tool.DTSignTool;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.syxpay.SyxConfig;
import com.jrmf.payment.syxpay.SyxService;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.AddressUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class SyxPayment implements Payment<Map<String, String>, Map<String, String>, String>{

	private Logger logger = LoggerFactory.getLogger(SyxPayment.class);

	public PaymentConfig payment;

	public SyxPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}


	@Override
	public Map<String, String> getTransferTemple(UserCommission userCommission){
		Map<String, String> tranDataMap = new HashMap<String, String>();
		try {
			//商户公钥
			PublicKey publicKey = DTRSACode.getPublicKey(payment.getPayPublicKey(), "base64");
			tranDataMap.put("mercCd", payment.getCorporationAccount());        // 商编
			tranDataMap.put("outOrderNo", userCommission.getOrderNo());        // 商户订单号，【32位】
			String outOrderDt;
			outOrderDt = DateUtils.formartDateStr(userCommission.getCreatetime(),"yyyy-MM-dd HH:mm:ss","yyyyMMddHHmmss");
			tranDataMap.put("outOrderDt", outOrderDt.substring(0, 8));         // 商户下单日期，yyyyMMdd
			tranDataMap.put("outOrderTm", outOrderDt.substring(8,14));         // 商户下单时间，HHmmss
			tranDataMap.put("orderAmt", userCommission.getAmount());           // 订单金额
			tranDataMap.put("ppFlg", "01");                                    // 账户类型:00:对公 01:对私
			String accountEncrypt = DTRSACode.encrypt(userCommission.getAccount(), publicKey, false, "UTF-8", "base64");
			tranDataMap.put("cardNo", accountEncrypt);                         // 付款卡号（加密）
			String userNameEncrypt = DTRSACode.encrypt(userCommission.getUserName(), publicKey, false, "UTF-8", "base64");
			tranDataMap.put("acctName", userNameEncrypt);                      // 户名（加密）
			tranDataMap.put("lbankCd", userCommission.getBankNo());            // 联行行号大于等于5万必须有值
			if(!StringUtil.isEmpty(userCommission.getPhoneNo())){
				String phoneEncrypt = DTRSACode.encrypt(userCommission.getPhoneNo(), publicKey, false, "UTF-8", "base64");
				tranDataMap.put("phoneNo", phoneEncrypt);                      // 预留手机号（加密）
			}
			//tranDataMap.put("addition", "");//附言
			tranDataMap.put("spbillIp", AddressUtil.getLocalIP());             // 终端ip
		} catch (Exception e) {
			tranDataMap = null;
			logger.info("封装请求参数异常",e);
		}
		return tranDataMap;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为商银信");
		PaymentReturn<String> transferReturn = null;
		try{
			String baseUrl = payment.getPreHost();
			String methodUrl = SyxConfig.PAYMENT_REQ_URL;
			//请求地址
			String url = baseUrl+methodUrl;
			//请求参数封装
			Map<String, String> params = getTransferTemple(userCommission);
			if(params==null){
				transferReturn = new PaymentReturn<String>(PayRespCode.RESP_FAILURE,"参数封装异常",userCommission.getOrderNo());
			}else{
				//调用服务
				SyxService syxService = new SyxService(url,payment.getCorporationAccount(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params);
				//请求商银信
				Map<String, String> respMap = syxService.singleTransFer();
				transferReturn = getTransferResult(respMap);
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			transferReturn = new PaymentReturn<String>(PayRespCode.RESP_UNKNOWN,PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),userCommission.getOrderNo());
		}
		return transferReturn;
	}

	@Override
	public PaymentReturn<String> getTransferResult(Map<String, String> result) {
		String code = "";
		String message = "";
		String orderNo = "";
		String returnCode = result.get("returnCode");
		String returnInfo = result.get("returnInfo");
		if (returnCode.equals("0000")) {
			// 0000时，验签
			String rspSign = String.valueOf(result.get("sign"));
			logger.info("响应的签名值：" + rspSign);
			result.remove("sign");
			String vOriData = DTSignTool.toRequestString(result, "UTF-8", false, true);
			logger.info("响应的待签名原文：" + vOriData);
			logger.info("开始验签......");
			boolean v = DTSignTool.verify("RSA2", vOriData, rspSign, payment.getPayPublicKey(), "UTF-8");
			if (v) {
				logger.info("验签成功");
				String resultCode = result.get("resultCode");
				// 业务请求成功，准备验签
				if (resultCode.equals("SUCCESS")) {
					System.out.println("订单号："+orderNo+"单笔代付受理成功");
					logger.info("付款受理流水号为："+result.get("outOrderNo"));
					orderNo = result.get("outOrderNo");
					code = PayRespCode.RESP_SUCCESS;
					message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				}else if(resultCode.equals("process")&&result.get("errCode").equals("0099")){
					code = PayRespCode.RESP_UNKNOWN;
					message = "状态未明，需要上游人工干预";
				}else {// 业务请求失败，查询具体业务错误原因
					String errCode = result.get("errCode");
					String errCodeDesc = result.get("errCodeDesc");
					logger.info("付款受理失败");
					logger.info("失败原因："+errCode+"，失败描述："+errCodeDesc);
					code = PayRespCode.RESP_CHECK_FAIL;
					message = String.valueOf(errCodeDesc);
				}
			} else{
				logger.info("验签失败");
				code = PayRespCode.RESP_UNKNOWN;
				message = "上游验签失败";
			}
		}else {
			logger.info("付款请求失败");
			logger.info("失败原因："+returnCode+"，失败描述："+returnInfo);
			code = PayRespCode.RESP_CHECK_FAIL;
			message = String.valueOf(returnInfo);

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
			String baseUrl = payment.getPreHost();
			String methodUrl = SyxConfig.SINGLE_TRANSFER_QUERY_METHOD;
			//请求地址
			String url = baseUrl+methodUrl;
			//封装请求参数
			Map<String, String> params = new HashMap<String, String>();
			params.put("mercCd", payment.getCorporationAccount());   //商户代码
			params.put("outOrderNo", orderNo);                       //商户流水号
			params.put("tmSmp", DTDateUtil.getCurrentTimeMillis());  //时间戳
			//调用服务
			SyxService syxService = new SyxService(url,payment.getCorporationAccount(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params);
			//请求商银信
			Map<String, String> respMap = syxService.singleTransFerQuery();
			if(respMap!=null){
				//查询成功
				logger.info("订单号："+orderNo+"查询成功");
				String returnCode = respMap.get("returnCode");
				String returnInfo = respMap.get("returnInfo");
				if (returnCode.equals("0000")) {
					// 0000时，验签
					String rspSign = respMap.get("sign");
					logger.info("订单号："+orderNo+"响应的签名值：" + rspSign);
					respMap.remove("sign");
					String vOriData = DTSignTool.toRequestString(respMap, "UTF-8", false, true);
					logger.info("订单号："+orderNo+"响应的待签名原文：" + vOriData);
					logger.info("订单号："+orderNo+"开始验签......");
					boolean v = DTSignTool.verify("RSA2", vOriData, rspSign, payment.getPayPublicKey(), "UTF-8");
					if (v) {
						logger.info("订单号："+orderNo+"验签成功");
						String resultCode = respMap.get("resultCode");
						if (resultCode != null && resultCode.equals("SUCCESS")) {
							String tradeState = respMap.get("tradeState");
							if (tradeState.equals("SUCCESS")){
								logger.info("订单号："+orderNo+"交易成功");
								transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
								transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
							}else if (tradeState.equals("PAYERROR")){
								logger.info("订单号："+orderNo+"交易失败");
								transCode = PayRespCode.RESP_TRANSFER_FAILURE;
								transMsg = "交易失败";
							}else if (tradeState.equals("FAIL")){
								logger.info("订单号："+orderNo+"下单失败");
								transCode = PayRespCode.RESP_TRANSFER_FAILURE;
								transMsg = "下单失败";
							}else if (tradeState.equals("REFUND")){
								logger.info("订单号："+orderNo+"订单已退款（付款失败）");
								transCode = PayRespCode.RESP_TRANSFER_FAILURE;
								transMsg = "订单已退款（付款失败）";
							}else{
								logger.info("订单号："+orderNo+"交易处理中");
							}
							code = PayRespCode.RESP_SUCCESS;
							message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
						}else {
							String errCode = respMap.get("errCode");
							String errCodeDesc = respMap.get("errCodeDesc");
							logger.info("订单号："+orderNo+"单笔代付查询失败");
							logger.info("订单号："+orderNo+"失败原因："+errCode+"，失败描述："+errCodeDesc);
							code = PayRespCode.RESP_FAILURE;
							message = String.valueOf(errCodeDesc);
						}
					}else{
						logger.info("订单号："+orderNo+"验签失败");
						code = PayRespCode.RESP_UNKNOWN;
						message = "上游验签失败";
					}
				}else {
					logger.info("订单号："+orderNo+"单笔代付查询失败");
					logger.info("订单号："+orderNo+"失败原因："+returnCode+"，失败描述："+returnInfo);
					code = PayRespCode.RESP_FAILURE;
					message = String.valueOf(returnInfo);
				}
			}else{
				code = PayRespCode.RESP_FAILURE;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
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
	public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
		return null;
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory queryParams) {
		return null;
	}


}
