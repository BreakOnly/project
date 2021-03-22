package com.jrmf.taxsettlement.util.json;

import java.util.Map;

public interface JsonDataTransformer {

	Map<String, Object> transformIn(String jsonStr);

}
