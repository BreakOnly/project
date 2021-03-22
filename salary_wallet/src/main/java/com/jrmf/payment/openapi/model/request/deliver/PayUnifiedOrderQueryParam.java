package com.jrmf.payment.openapi.model.request.deliver;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderQueryResult;

public class PayUnifiedOrderQueryParam implements IBaseParam<PayUnifiedOrderQueryResult> {

	private String reqNo;
    private String outOrderNo;

    public String getReqNo() {
        return reqNo;
    }

    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }
	
	@Override
	public String requestURI() {
		return "/deliver/dlvopenapi/api/app/pay/query";
	}

	@Override
	public String methodName() {
		return "ayg.salary.payQuery";
	}

	@Override
	public String version() {
		return "1.0";
	}

	@Override
	public Class<?> respDataClass() {
		return PayUnifiedOrderQueryResult.class;
	}

}
