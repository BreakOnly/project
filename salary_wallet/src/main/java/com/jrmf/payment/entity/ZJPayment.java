package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.payment.zjpay.Base64;
import com.jrmf.payment.zjpay.Sign;
import com.jrmf.payment.zjpay.ZjService;
import com.jrmf.utils.AmountConvertUtil;
import com.jrmf.utils.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ZJPayment implements Payment<Map<String, Object>, Map<String, Object>, String> {

	private Logger logger = LoggerFactory.getLogger(ZJPayment.class);
	public PaymentConfig payment;

	public ZJPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public Map<String, Object> getTransferTemple(UserCommission userCommission) {
		Map<String, Object> tranDataMap = new HashMap<String, Object>();
		tranDataMap.put("AccountType", "11");
		tranDataMap.put("BankID", userCommission.getBankNo());
		tranDataMap.put("BankAccountName", userCommission.getUserName());
		tranDataMap.put("BankAccountNumber", userCommission.getAccount());
		tranDataMap.put("PhoneNumber", userCommission.getPhoneNo());
		tranDataMap.put("Amount", AmountConvertUtil.changeY2F(userCommission.getAmount()));
		tranDataMap.put("Remark", userCommission.getRemark());
		tranDataMap.put("TxSN", userCommission.getOrderNo());
		tranDataMap.put("PaymentFlag", "1");
		return tranDataMap;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为中金");
		PaymentReturn<String> transferReturn = null;
		try{
			//请求地址
			String url = payment.getPreHost();
			//请求参数封装
			Map<String, Object> params = getTransferTemple(userCommission);
			//调用服务
			ZjService zjService = new ZjService(url,payment.getParameter1(),payment.getCorporationAccount(),payment.getCorporationAccountName(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params,payment.getParameter2(),payment.getParameter3());
			//请求中金
			Map<String, Object> respMap = zjService.singleTransFer();
			transferReturn = getTransferResult(respMap);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			transferReturn = new PaymentReturn<String>(PayRespCode.RESP_UNKNOWN,PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),userCommission.getOrderNo());
		}
		return transferReturn;
	}

	@Override
	public PaymentReturn<String> getTransferResult(Map<String, Object> respMap) {
		String code = "";
		String message = "";
		String orderNo = "";
		try {
			//处理响应
			if(respMap!=null||!respMap.isEmpty()){
				//获取响应信息
				String responseMessage = String.valueOf(respMap.get("responseMessage"));
				//base64解码
				byte[] data = Base64.decode(responseMessage);
				//解码后信息
				String responseMessageDecode = new String(data, "UTF-8");
				logger.info("解码后信息为"+responseMessageDecode);
				//响应签名
				String responseSignature = String.valueOf(respMap.get("responseSignature"));
				//验证签名
				boolean signFlag = Sign.verfySignByPubKey(responseMessageDecode, responseSignature, payment.getPayPublicKey());
				if (signFlag) {
					logger.info("验签成功");
					Document document = XML.createDocument(responseMessageDecode);
					String upCode = XML.getNodeText(document, "Code");
					String upMessage = XML.getNodeText(document, "Message");
					if("2000".equals(upCode)){
						String txSN = XML.getNodeText(document, "TxSN");
						//10=未处理 20=正在处理 30=代付成功 40=代付失败 50=已撤销
						int status = Integer.parseInt(XML.getNodeText(document, "Status"));
						//受理成功
						logger.info("订单号："+orderNo+"付款受理状态为:"+	statusDesc(status));
						logger.info("付款受理流水号为："+txSN);
						orderNo = txSN;
						code = PayRespCode.RESP_SUCCESS;
						message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
					}else{
						//付款受理失败
						logger.info("付款受理失败");
						logger.info("失败原因："+upCode+"，失败描述："+upMessage);
						code = PayRespCode.RESP_CHECK_FAIL;
						message = String.valueOf(upMessage);
					}
				}else{
					logger.info("验签失败");
					code = PayRespCode.RESP_UNKNOWN;
					message = "上游验签失败";
				}
			}else{
				logger.info("中金响应信息为空");
				code = PayRespCode.RESP_UNKNOWN;
				message = "中金响应信息为空";
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			logger.error("中金请求异常",e);
			code = PayRespCode.RESP_UNKNOWN;
			message = "中金请求异常";
		}
		PaymentReturn<String> transferReturn = new PaymentReturn<String>(code,message,orderNo);
		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo){
		String code = "";
		String message = "";
		String transCode = "";
		String transMsg = "";
		PaymentReturn<TransStatus> paymentReturn = null;
		try{
			//请求地址
			String url = payment.getPreHost();
			//封装请求参数
			Map<String, Object> params = new HashMap<String, Object>();
			//商户流水号
			params.put("TxSN", orderNo);
			//调用服务
			ZjService zjService = new ZjService(url,payment.getParameter1(),payment.getCorporationAccount(),payment.getCorporationAccountName(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params,payment.getParameter2(),payment.getParameter3());
			//请求中金
			Map<String, Object> respMap = zjService.transFerQuery();
			//处理响应
			if(respMap!=null||!respMap.isEmpty()){
				//获取响应信息
				String responseMessage = String.valueOf(respMap.get("responseMessage"));
				//base64解码
				byte[] data = Base64.decode(responseMessage);
				//解码后信息
				String responseMessageDecode = new String(data, "UTF-8");
				logger.info("订单号："+orderNo+"解码后信息为"+responseMessageDecode);
				//响应签名
				String responseSignature = String.valueOf(respMap.get("responseSignature"));
				//验证签名
				boolean signFlag = Sign.verfySignByPubKey(responseMessageDecode, responseSignature, payment.getPayPublicKey());
				if (signFlag) {
					logger.info("订单号："+orderNo+"验签成功");
					Document document = XML.createDocument(responseMessageDecode);
					String upCode = XML.getNodeText(document, "Code");
					String upMessage = XML.getNodeText(document, "Message");
					if("2000".equals(upCode)){
						String institutionID = XML.getNodeText(document, "InstitutionID");
						String txSN = XML.getNodeText(document, "TxSN");
						String paymentFlag = XML.getNodeText(document, "PaymentFlag");
						//10=未处理 20=正在处理 30=代付成功 40=代付失败 50=已撤销
						int status = Integer.parseInt(XML.getNodeText(document, "Status"));
						String bankResponseCode = XML.getNodeText(document, "BankResponseCode");
						String bankResponseMessage = XML.getNodeText(document, "BankResponseMessage");
						Long amount = Long.parseLong(XML.getNodeText(document, "Amount"));
						String bankTxTime = XML.getNodeText(document, "BankTxTime");
						logger.info("付款状态为:"+statusDesc(status));
						if(status==30){
							//代付成功
							logger.info("订单号："+orderNo+"交易成功,交易时间为"+bankTxTime+"，流水号为："+txSN+",金额："+AmountConvertUtil.changeF2Y(amount)+"元,"
							+"代付标识:"+paymentFlag+"银行响应码："+bankResponseCode+",银行响应信息："+bankResponseMessage);
							transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
							transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
						}else if(status==40){
							//代付失败
							logger.info("订单号："+orderNo+"交易失败："+upMessage);
							transCode = PayRespCode.RESP_TRANSFER_FAILURE;
							transMsg = bankResponseMessage;
						}else if(status==50){
							//已撤销
							logger.info("订单号："+orderNo+"交易失败,已撤销："+upMessage);
							transCode = PayRespCode.RESP_TRANSFER_FAILURE;
							transMsg = bankResponseMessage;
						}
						code = PayRespCode.RESP_SUCCESS;
						message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
					}else{
						//付款受理失败
						logger.info("订单号："+orderNo+"单笔代付查询失败");
						logger.info("订单号："+orderNo+"失败原因："+upCode+"，失败描述："+upMessage);
						code = PayRespCode.RESP_FAILURE;
						message = String.valueOf(upMessage);
					}
				}else{
					logger.info("订单号："+orderNo+"验签失败");
					code = PayRespCode.RESP_UNKNOWN;
					message = "上游验签失败";
				}
			}else{
				logger.info("订单号："+orderNo+"中金响应信息为空");
				code = PayRespCode.RESP_UNKNOWN;
				message = "中金响应信息为空";
			}
			TransStatus transStatus = new TransStatus(orderNo,transCode,transMsg);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,transStatus);
		}catch(Exception e){
			code = PayRespCode.RESP_FAILURE;
			message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,null);
			logger.info("订单号："+orderNo+"请求中金异常",e);
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

	public String statusDesc(int status){
		String statusDesc="";
		switch (status) {
		case 10:
			statusDesc="未处理";
			break;
		case 20:
			statusDesc="正在处理 ";
			break;
		case 30:
			statusDesc="代付成功";
			break;
		case 40:
			statusDesc="代付失败";
			break;
		case 50:
			statusDesc="已撤销 ";
			break;
		default:
			break;
		}
		return statusDesc;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String a="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+CjxSZXNwb25zZT4KPEhlYWQ+CjxDb2RlPjIwMDA8L0NvZGU+CjxNZXNzYWdlPk9LLjwvTWVzc2FnZT4KPC9IZWFkPgo8Qm9keT4KPEluc3RpdHV0aW9uSUQ+MDA1MjAzPC9JbnN0aXR1dGlvbklEPgo8VHhTTj4yMDE5MDgyMTAwMDAwMDMxPC9UeFNOPgo8U3RhdHVzPjMwPC9TdGF0dXM+CjxCYW5rUmVzcG9uc2VDb2RlPjAwPC9CYW5rUmVzcG9uc2VDb2RlPgo8QmFua1Jlc3BvbnNlTWVzc2FnZT7miJDlip9bMDAwMDAwMF08L0JhbmtSZXNwb25zZU1lc3NhZ2U+CjwvQm9keT4KPC9SZXNwb25zZT4=";
		//base64解码
		byte[] data = Base64.decode(a);
		//解码后信息
		String responseMessageDecode = new String(data, "UTF-8");
		System.out.println(responseMessageDecode);
	}

}
