package com.jrmf.controller.notify;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.zxpay.util.RSA;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.UserCommissionService;

@RestController
public class ZXNotifyController {
	
	private static Logger logger = LoggerFactory.getLogger(ZXNotifyController.class);
	@Autowired
	private UserCommissionService userCommissionService;
	@Autowired 
	private AgreementTemplateService agreementTemplateService;
	@Autowired
	private CompanyService companyService;
	
	@RequestMapping("/zxNotify")
	public String zxNotify(@RequestBody String content,HttpServletRequest request){
        logger.info("众薪交易回调请求地址：{} 请求参数：{}", request.getRemoteAddr(), content);
        try {
            if (StringUtils.isBlank(content)) {
                throw new Exception();
            }
			UserCommission userCommission = userCommissionService.getUserCommission(request.getParameter("orderNo"));
			if(userCommission!=null){
				PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), userCommission.getRealCompanyId(),userCommission.getPathNo());
				JSONObject contentJson = JSONObject.parseObject(content);
				String jsonStr = RSA.decryptPri(String.valueOf(contentJson.get("sign")) , paymentConfig.getPayPrivateKey());
				JSONObject respInfo = JSONObject.parseObject(jsonStr);
				logger.info("订单号："+request.getParameter("orderNo")+"解密信息为："+respInfo); 
				JSONObject jsonRespInfo = JSONObject.parseObject(jsonStr);
				return "success";
			}else{
		        return "success";
			}
        } catch (Exception e) {
            logger.error("解密失败e：{}", e);
            return "failed";
        }
	}
}
