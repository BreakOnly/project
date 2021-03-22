package com.jrmf.payment.entity;


import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.payment.ympay.*;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class YMPayment implements Payment<Map<String, Object>, Map<String, Object>, String> {

	private Logger logger = LoggerFactory.getLogger(YMPayment.class);

	public PaymentConfig payment;

	public YMPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public Map<String, Object> getTransferTemple(UserCommission userCommission) {
		Map<String, Object> tranDataMap = new HashMap<String, Object>();
		tranDataMap.put("recAcct", userCommission.getAccount());                    //收款账户
		tranDataMap.put("orderAmt", userCommission.getAmount());                    //汇款金额
		tranDataMap.put("accountName", userCommission.getUserName());               //收款客户名称
		tranDataMap.put("bankName", userCommission.getBankName());                  //收款银行
		tranDataMap.put("sonName", "");                                             //支行名称（可空）
		tranDataMap.put("tranFlowNo", "");                                          //原汇款流水号（不填。预留字段）
		tranDataMap.put("remark", "");                                              //摘要（可空）
		tranDataMap.put("userType", "1");                  //收款账户类型（0-单位1-个人，只能选0或1）
		tranDataMap.put("bankflag", "0");                                           //同城异地标志
		//根据账号获取支付联行号
		tranDataMap.put("recAcctBankNo",userCommission.getBankNo());                //总行联行行号
		tranDataMap.put("merFlowNo", userCommission.getOrderNo());                  //商户流水号（不可重复)
		tranDataMap.put("userIp", getLocalIP()==null?"139.219.4.246":getLocalIP()); //必填。访问ip地址
		tranDataMap.put("cardType", "0");                  //网联渠道 必填。（0：借记卡，1：贷记卡，2：准贷记卡/公务员卡）个人卡：0,1,2对公户：9
		return tranDataMap;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为溢美");
		PaymentReturn<String> transferReturn = null;
		try{
			String baseUrl = payment.getPreHost();
			String methodUrl = YmConfig.SINGLE_TRANSFER_URL;
			//请求地址
			String url = baseUrl+methodUrl;
			//请求参数封装
			Map<String, Object> params = getTransferTemple(userCommission);
			//调用服务
			YmService ymService = new YmService(url,payment.getCorporationAccount(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params);
			//请求溢美
			Map<String, Object> respMap = ymService.singleTransFer();
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
			if(respMap.get("errorCode")!=null && respMap.get("errorCode").equals("0")){
				//业务受理成功
				logger.info("请求成功");
				String merSignMsg = String.valueOf(respMap.get("merSignMsg"));
				logger.info("溢美签名信息为："+merSignMsg);
				String tranData = String.valueOf(respMap.get("tranData"));
				Map<String, Object> tranDataMap = new HashMap<String, Object>();
				//base64解码
				String decodeRespInfo = Base64.decodeGbk(tranData);
				//验签
				String publicKey = payment.getPayPublicKey();
				boolean flag = Sign.verfySignByPubKey(decodeRespInfo,merSignMsg,publicKey);
				if(flag){
					try {
						logger.info("验签成功");
						tranDataMap = XmlUtils.xml2map(decodeRespInfo, false);
						if(tranDataMap.get("returnCode")!=null&&tranDataMap.get("returnCode").equals("0000")){
							//受理成功
							logger.info("付款受理成功");
							logger.info("付款受理流水号为："+tranDataMap.get("orderNo"));
							orderNo = String.valueOf(tranDataMap.get("orderNo"));
							code = PayRespCode.RESP_SUCCESS;
							message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
						}else{
							//受理失败
							logger.info("付款受理失败");
							code = PayRespCode.RESP_CHECK_FAIL;
							message = PayRespCode.codeMaps.get(PayRespCode.RESP_CHECK_FAIL);
						}
					} catch (DocumentException e) {
						logger.error("xml转map异常",e);
						code = PayRespCode.RESP_UNKNOWN;
						message = "xml转map异常";
					}
				}else{
					logger.info("验签失败");
					code = PayRespCode.RESP_UNKNOWN;
					message = "上游验签失败";
				}
			}else{
				//付款受理失败
				logger.info("付款受理失败");
				logger.info("失败原因："+respMap.get("errorCode")+"，失败描述："+respMap.get("errorMsg"));
				code = PayRespCode.RESP_CHECK_FAIL;
				message = String.valueOf(respMap.get("errorMsg"));
			}
		} catch (Exception e) {
			logger.error("base64解码异常",e);
			code = PayRespCode.RESP_UNKNOWN;
			message = "base64解码异常";
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
			String methodUrl = YmConfig.SINGLE_TRANSFER_QUERY_URL;
			//请求地址
			String url = baseUrl+methodUrl;
			//封装请求参数
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("merchantId", payment.getCorporationAccount());   //商户代码
			params.put("orderNo", orderNo);                              //商户流水号
			params.put("random", String.valueOf(new Date().getTime()));              //随机数
			//调用服务
			YmService ymService = new YmService(url,payment.getCorporationAccount(),payment.getPayPrivateKey(),payment.getPayPublicKey(),params);
			//请求溢美
			Map<String, Object> respMap = ymService.transFerQuery();
			if(respMap!=null){
				//查询成功
				logger.info("订单号："+orderNo+"查询成功");
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				//汇款状态（0-待汇款；1-成功；2-失败；其他-汇款处理中）
				Integer tranStat = Integer.parseInt(String.valueOf(respMap.get("tranStat")));
				if(tranStat==1){
					//交易成功
					logger.info("订单号："+orderNo+"交易成功,交易时间为"+respMap.get("tranTime")+"，交易流水号为："+respMap.get("tranSerialNo"));
					transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				}else if(tranStat==2){
					//交易失败
					logger.info("订单号："+orderNo+"交易失败："+respMap.get("remark"));
					transCode = PayRespCode.RESP_TRANSFER_FAILURE;
					transMsg = respMap.get("remark")==null?"":String.valueOf(respMap.get("remark"));
				}else{
					//待付款/汇款处理中
					transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
					transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
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
	public PaymentReturn queryBalanceResult(String type) {
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

	/**
	 * 获取本机ip
	 * @return
	 */
	public String getLocalIP() {
		String ip =null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.error("获取本地ip异常",e);
		}
		return ip;
	}
}

