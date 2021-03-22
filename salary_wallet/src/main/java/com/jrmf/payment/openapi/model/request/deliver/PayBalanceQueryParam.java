package com.jrmf.payment.openapi.model.request.deliver;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.deliver.PayBalanceDetailQueryResult;

public class PayBalanceQueryParam implements IBaseParam<PayBalanceDetailQueryResult> {

	@Override
	public String requestURI() {
		return "/deliver/dlvopenapi/api/app/account/query-balance-detail";
	}

	@Override
	public String methodName() {
		return "ayg.account.queryBalance";
	}

	@Override
	public String version() {
		return "1.0";
	}

	@Override
	public Class<?> respDataClass() {
		return PayBalanceDetailQueryResult.class;
	}

}
