package com.jrmf.taxsettlement.util.cache;

import java.util.HashMap;
import java.util.Map;

public abstract class InertnessDataCache {
	
	private Map<String, Object> constantKVs = new HashMap<String, Object>();
	
	private Map<String, Map<String, Object>> domainConstantKVs = new HashMap<String, Map<String, Object>>();
	
	final protected void addConstantKV(String key, Object value) {
		constantKVs.put(key, value);
	}
	
	final protected Object getValue(String key) {
		return constantKVs.get(key);
	}
	
	final protected void addConstantDomain(String domain) {
		domainConstantKVs.put(domain, new HashMap<String, Object>());
	}
	
	final protected void addConstantKV(String domain, String key, Object value) {
		domainConstantKVs.get(domain).put(key, value);
	}
	
	final protected Object getValue(String domain, String key) {
		return domainConstantKVs.get(domain).get(key);
	}
}
