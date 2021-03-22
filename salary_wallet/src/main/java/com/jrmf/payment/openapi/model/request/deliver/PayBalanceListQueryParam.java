package com.jrmf.payment.openapi.model.request.deliver;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.deliver.PayBalanceListQueryResult;

import java.util.List;

public class PayBalanceListQueryParam implements IBaseParam<List<PayBalanceListQueryResult>> {

    @Override
    public String requestURI() {
        return "/deliver/dlvopenapi/api/app/account/query-balance-list";
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
        return List.class;
    }

}
