package com.jrmf.taxsettlement.util.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonDataTransformer implements JsonDataTransformer {

	private static final Logger logger = LoggerFactory.getLogger(FastJsonDataTransformer.class);

	@Override
	public Map<String, Object> transformIn(String jsonStr) {
		JSONObject root = (JSONObject) JSON.parse(jsonStr);
		return transformJsonObject(root);
	}

	private Map<String, Object> transformJsonObject(JSONObject jsonObj) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : jsonObj.getInnerMap().entrySet()) {
			String key = entry.getKey();
			Object entryValue = entry.getValue();
			if (entryValue == null) {
				retMap.put(key, entryValue);
			} else {
				Class<?> valueClass = entryValue.getClass();
				if (isPrimitiveObject(valueClass)) {
					retMap.put(key, entryValue);
				} else if (JSONObject.class.equals(valueClass)) {
					retMap.put(key, transformJsonObject((JSONObject) entryValue));
				} else if (JSONArray.class.equals(valueClass)) {
					retMap.put(key, transformJsonArray((JSONArray) entryValue));
				} else {
					logger.warn("untransform json field[{}] for type[{}]", key, valueClass.getName());
				}
			}
		}

		return retMap;
	}

	private List<Object> transformJsonArray(JSONArray array) {
		List<Object> retList = new ArrayList<Object>(array.size());
		int arraySize = array.size();
		for (int i = 0; i < arraySize; i++) {
			Object elementValue = array.get(i);
			if (elementValue == null) {
				retList.add(null);
			} else {
				Class<?> valueClass = elementValue.getClass();
				if (isPrimitiveObject(valueClass)) {
					retList.add(elementValue);
				} else if (JSONObject.class.equals(valueClass)) {
					retList.add(transformJsonObject((JSONObject) elementValue));
				} else if (JSONArray.class.equals(valueClass)) {
					retList.add(transformJsonArray((JSONArray) elementValue));
				} else {
					logger.warn("untransform json element[{}] for type[{}]", i, valueClass.getName());
				}
			}

		}

		return retList;
	}

	private boolean isPrimitiveObject(Class<?> valueClass) {
		return String.class.equals(valueClass) || valueClass.isPrimitive() || Integer.class.equals(valueClass)
				|| Boolean.class.equals(valueClass) || Short.class.equals(valueClass)
				|| Character.class.equals(valueClass) || Byte.class.equals(valueClass) || Long.class.equals(valueClass)
				|| Double.class.equals(valueClass) || Float.class.equals(valueClass);
	}
}
