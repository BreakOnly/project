package com.jrmf.taxsettlement.api.security.sign;

import java.util.HashMap;
import java.util.Map;

public class SignWorkers {
	
	private Map<String, SignWorker> signWorkers = new HashMap<String, SignWorker>();
	
	public SignWorkers(Map<String, SignWorker> signWorkers) {
		this.signWorkers.putAll(signWorkers);
	}

	public SignWorker get(String signType) {
		return signWorkers.get(signType);
	}

}
