package com.jrmf.signContract;

import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.signContract.channel.AYGChannel;
import com.jrmf.signContract.channel.YMChannel;
import com.jrmf.utils.SpringContextUtil;

public class SignContractChannelFactory {
	
	/**
	 * 创建签约通道
	 * @return
	 */
	public static SignContractChannel createChannel(AgreementTemplate agreementTemplate, PaymentConfig paymentConfig) {
		switch (agreementTemplate.getChannelType()) {
		case 1:
			//爱员工
			AYGChannel aygChannel = SpringContextUtil.getBean(AYGChannel.class);
			aygChannel.setAgreementTemplate(agreementTemplate);
			aygChannel.setPaymentConfig(paymentConfig);
			return aygChannel;
		case 2:
			//溢美优付
			YMChannel ymChannel = SpringContextUtil.getBean(YMChannel.class);
			ymChannel.setAgreementTemplate(agreementTemplate);
			ymChannel.setPaymentConfig(paymentConfig);
			return ymChannel;
		}
		return null;
	}

	
}
