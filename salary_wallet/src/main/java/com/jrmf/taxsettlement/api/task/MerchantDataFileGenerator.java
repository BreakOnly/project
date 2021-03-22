package com.jrmf.taxsettlement.api.task;

import java.util.Map;

public interface MerchantDataFileGenerator {

	void generateDataFile(Map<String, Object> params) throws Exception;

}
