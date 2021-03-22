package com.jrmf.taxsettlement.api.service;

import java.util.Map;

public interface ActionInterceptor<AP extends ActionParams, AA extends ActionAttachment> {

	void preHandle(Map<String, Object> paramMap, AP params);

	void postHandle(Map<String, Object> paramMap, AP params, ActionResult<AA> result);
	
	void abortHandle(Map<String, Object> paramMap, AP params, Throwable t);
}
