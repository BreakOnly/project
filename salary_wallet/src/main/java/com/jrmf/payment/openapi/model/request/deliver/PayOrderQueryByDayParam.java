package com.jrmf.payment.openapi.model.request.deliver;

import java.util.List;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.deliver.PayOrderQueryByDayResult;

public class PayOrderQueryByDayParam implements IBaseParam<List<PayOrderQueryByDayResult>> {

	private String beginDay;	// 格式：yyyy-MM-dd
    private String endDay;		// 格式：yyyy-MM-dd
    
    public String getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(String beginDay) {
        this.beginDay = beginDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }
	
	@Override
	public String requestURI() {
		return "/deliver/dlvopenapi/api/app/pay/query-by-day";
	}

	@Override
	public String methodName() {
		return "ayg.salary.queryByDay";
	}

	@Override
	public String version() {
		return "1.0";
	}

	@Override
	public Class<?> respDataClass() {
		return PayOrderQueryByDayResult.class;
	}

}
