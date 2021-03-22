package com.jrmf.service;

import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.jrmf.utils.alipay.AliPayConfigLast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝转账接口（资金下发）
 * 
 * @author guoto
 */
@Scope("prototype")
@Service("alipayServiceImpl")
public class AlipayServiceImpl implements AlipayService {

	private static Logger logger = LoggerFactory.getLogger(AlipayServiceImpl.class);

	@Autowired
	public UserCommissionService userCommissionServiceImpl;

	// 支付宝单笔佣金下发
	@Override
	public String singleTransferAccounts(Map<String, Object> map) {
		/**
		 * ALIPAY_USERID 支付宝账号对应的支付宝唯一用户号。以2088开头的16位纯数字组成。
		 *  ALIPAY_LOGONID 支付宝登录号，支持邮箱和手机号格式。
		 **/
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
		request.setBizContent("{" + "\"out_biz_no\":\"" + map.get("out_biz_no") + "\"," +
				"\"payee_type\":\"" + map.get("payee_type") + "\"," +
				"\"payee_account\":\"" + map.get("payee_account")+ "\"," +
				"\"amount\":\"" + map.get("amount") + "\"," +
				"\"payer_show_name\":\"" + map.get("payer_show_name") + "\"," +
				"\"payee_real_name\":\"" + map.get("payee_real_name") + "\"," +
				"\"remark\":\"" + map.get("remark") + "\"" +
				"}");
		AlipayFundTransToaccountTransferResponse response;
		String result = null;
		try {
			response = AliPayConfigLast.alipayClient.execute(request);
			if (response != null){
                result = response.getBody();
            }
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.error("订单出错！单号:" + map.get("out_biz_no"));
		}
		return result;
	}
}
