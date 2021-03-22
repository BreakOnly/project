package com.jrmf.taxsettlement.api.task;

import java.util.HashMap;
import java.util.Map;

public class MerchantDataFileGenerators {

	private Map<String, MerchantDataFileGenerator> dataFileGenerators = new HashMap<String, MerchantDataFileGenerator>();
	
	public MerchantDataFileGenerators(Map<String, MerchantDataFileGenerator> dataFileGenerators) {
		this.dataFileGenerators.putAll(dataFileGenerators);
	}

	public MerchantDataFileGenerator get(String dataFileType) {
		return dataFileGenerators.get(dataFileType);
	}
}
