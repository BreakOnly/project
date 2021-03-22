package com.jrmf.payment.openapi.model.request.prepare;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.prepare.QueryCertificationResult;

/**
 *查询认证结果
 */
public class QueryCertificationParam implements IBaseParam<QueryCertificationResult>{

	private String cerRequestId;	// 查询条件
	private String requestId;	//  请求唯一标识

	public String getCerRequestId() {
		return cerRequestId;
	}

	public void setCerRequestId(String cerRequestId) {
		this.cerRequestId = cerRequestId;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String requestURI() {
		return "/prepare/certification/result";
	}
	
	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}

	@Override
	public Class<?> respDataClass() {
		return QueryCertificationResult.class;
	}
	
}
