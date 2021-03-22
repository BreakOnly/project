package com.jrmf.payment.openapi.model.request.deliver;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;

/**
 * Created by ThinkPad on 2017/6/15.
 */
public class ReceiptRequestParam implements IBaseParam<BaseResponseResult<String>> {

    private String reqNo;
    private String outOrderNo;
    private String notifyUrl;
    private String attach;

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

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#requestURI()
	 */
	@Override
	public String requestURI() {
		return "/deliver/dlvopenapi/api/app/pay/request-receipt";
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#methodName()
	 */
	@Override
	public String methodName() {
		return "ayg.salary.requestReceipt";
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#version()
	 */
	@Override
	public String version() {
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#respDataClass()
	 */
	@Override
	public Class<?> respDataClass() {
		return BaseResponseResult.class;
	}
}
