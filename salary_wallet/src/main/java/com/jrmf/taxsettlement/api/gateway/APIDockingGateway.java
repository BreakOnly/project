package com.jrmf.taxsettlement.api.gateway;

import java.util.Map;

import com.jrmf.taxsettlement.api.APIDockingException;

public interface APIDockingGateway {

	Map<String, Object> apiHandle(APIDockingAccesserProfile accesserBrief, Map<String, Object> inData)
			throws APIDockingException;

	Map<String, Object> signagreementApiHandle(APIDockingAccesserProfile accesserBrief, Map<String, Object> inData)
			throws APIDockingException;

	Map<String, Object> batchHandle(APIDockingAccesserProfile accesserBrief, Map<String, Object> inData)
			throws APIDockingException;

}
