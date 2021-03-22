package com.jrmf.taxsettlement.api.service.verify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.constants.CertificationType;
import com.jrmf.payment.openapi.model.request.prepare.SyncCertificationParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.prepare.AsynCertificationResult;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;

@ActionConfig(name = "三要素校验")
public class Verify3FactorsService implements Action<Verify3FactorsServiceParams, Verify3FactorsServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(Verify3FactorsService.class);

	private static final String ACCOUNT_TYPE = "BANK_CARDNO";

	private static final String VERIFY_SUCCESS_RESULT = "1";

	private static OpenApiClient client;

	static {
		String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";
		client = new OpenApiClient.Builder().appId("bjylmfay").privateKey(priKeyString).build();
	}

	@Override
	public String getActionType() {
		return APIDefinition.VERIFY_3_FACTORS.name();
	}

	@Override
	public ActionResult<Verify3FactorsServiceAttachment> execute(Verify3FactorsServiceParams actionParams) {

		SyncCertificationParam param = new SyncCertificationParam();
		param.setName(actionParams.getName());
		param.setIdcard(actionParams.getCertificateNo());
		param.setPayAccount(actionParams.getBankCardNo());
		param.setPayAccountType(ACCOUNT_TYPE);
		param.setValidType(CertificationType._3element);

		OpenApiBaseResponse<AsynCertificationResult> response = client.execute(param);
		if (response.successed()) {
			Verify3FactorsServiceAttachment attachment = new Verify3FactorsServiceAttachment();
			attachment.setPassing(VERIFY_SUCCESS_RESULT.equals(response.getData().getCertResult()));
			return new ActionResult<Verify3FactorsServiceAttachment>(attachment);
		}

		throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(),
				new StringBuilder(response.getCode()).append(":").append(response.getMsg()).toString());
	}

}
