package com.jrmf.payment.openapi.model.request;

public interface IBaseParam<RES> {

	String requestURI();
	String methodName();
	String version();
	Class<?> respDataClass();
}
