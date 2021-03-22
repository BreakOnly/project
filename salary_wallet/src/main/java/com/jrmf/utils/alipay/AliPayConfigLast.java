package com.jrmf.utils.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransOrderQueryModel;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.jrmf.domain.UserCommission;
import com.jrmf.utils.AlipayConfigUtil;

public class AliPayConfigLast {

//	public static final AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfigUtil.getURL(),
//			AlipayConfigUtil.getAppID(), AlipayConfigUtil.getPrivateKey(), AlipayConfigUtil.getTYPE(),
//			AlipayConfigUtil.getCharset(), AlipayConfigUtil.getPublicKey(), AlipayConfigUtil.getSecreat());

	
	public static final AlipayClient alipayClient = new DefaultAlipayClient(ALiPayConfig.URL,
			ALiPayConfig.APP_ID, ALiPayConfig.APP_PRIVATE_KEY, ALiPayConfig.TYPE,
			ALiPayConfig.CHARSET, ALiPayConfig.ALIPAY_PUBLIC_KEY, ALiPayConfig.SECREAT);
	
	public static void main(String args[]) throws AlipayApiException{
		
//		UserCommission userCommission = new UserCommission();
//		userCommission.setOrderNo("2018120500001");
//		userCommission.setAccount("17701393451");
//		userCommission.setAmount("0.10");
////		userCommission.setCertId(certId);
//		userCommission.setUserName("张桓");
//		userCommission.setRemark("test");
//		
//		
//		AlipayFundTransToaccountTransferResponse queryResult = null;
//
//		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
//		request.setBizContent("{" 
//				+ "\"out_biz_no\":\"" 
//				+ userCommission.getOrderNo() + "\","
//				+"\"payee_type\":\"" 
//				+ "ALIPAY_LOGONID" + "\"," //支付宝登录账号
//				+ "\"payee_account\":\"" 
//				+ userCommission.getAccount() + "\","
//				+"\"amount\":\"" 
//				+ userCommission.getAmount() + "\"," 
//				+ "\"payer_show_name\":\"" 
//				+ userCommission.getCompanyName() + "\","
//				+ "\"payee_real_name\":\""
//				+ userCommission.getUserName() + "\","
//				+ "\"remark\":\"" 
//				+ userCommission.getRemark() + "\""
//				+"}");
//		
//		
		
		AlipayFundTransOrderQueryResponse queryResult = null;
		AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
		model.setOutBizNo("2018120737011033333");
		AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
      request.setBizModel(model);
		queryResult = alipayClient.execute(request);
		String accessToken = "";

//		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
//		request.setCode("2e4248c2f50b4653bf18ecee3466UC18");
//		request.setGrantType("authorization_code");
//		try {
//		    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
//		    System.out.println(oauthTokenResponse.getAccessToken());
//		    accessToken = oauthTokenResponse.getAccessToken();
//		} catch (AlipayApiException e) {
//		    //处理异常
//		    e.printStackTrace();
//		}
		
//		AlipayUserInfoShareRequest request2 = new AlipayUserInfoShareRequest();
//		AlipayUserInfoShareResponse response = alipayClient.execute(request2, accessToken);
//		if(response.isSuccess()){
//		System.out.println("调用成功");
//		} else {
//		System.out.println("调用失败");
//		}
//			
//		System.out.println(response.getMsg());
	}
}
