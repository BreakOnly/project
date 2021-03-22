package com.jrmf.taxsettlement.api.gateway.restful;

import java.util.HashMap;
import java.util.Map;

import com.jrmf.taxsettlement.api.gateway.APIKeyMapper;

public class URLAPIKeyMapper implements APIKeyMapper {

	private Map<String, String> urlApiKeyMapping = new HashMap<String, String>();
	
	public URLAPIKeyMapper(Map<String, String> urlApiKeyMapping) {
		super();
		this.urlApiKeyMapping = urlApiKeyMapping;
	}

	@Override
	public String map(String url) {
		return urlApiKeyMapping.get(url);
	}
}
