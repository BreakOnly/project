package com.jrmf.taxsettlement.api.security.sign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSignWorker implements SignWorker {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSignWorker.class);

	@Override
	public boolean verifySign(Map<String, Object> mapData, String verificationKey, String sign) throws Exception {
		return verifySign(getSortKVStr(mapData), verificationKey, sign);
	}

	@Override
	public String generateSign(Map<String, Object> mapData, String signGenerationKey) throws Exception {
		return generateSign(getSortKVStr(mapData), signGenerationKey);
	}

	private static String getSortKVStr(Map<String, Object> values) {

		List<String> keys = new ArrayList<String>();
		keys.addAll(values.keySet());

		Collections.sort(keys);

		StringBuffer sb = new StringBuffer();
		int size = keys.size();
		for (int i = 0; i < size; i++) {
			String key = keys.get(i);
			Object value = values.get(key);

			if (value == null) {
				continue;
			}

			if (value instanceof String && "".equals(value))
				continue;

			if (sb.length() > 0)
				sb.append('&');
			sb.append(key).append("=").append(value);
		}

		String sortKVStr = sb.toString();
		logger.debug("sort kv string for sign:{}", sortKVStr);
		return sortKVStr;
	}

	protected abstract boolean verifySign(String sortKVStr, String verifyKey, String sign) throws Exception;

	protected abstract String generateSign(String sortKVStr, String signGenerationKey) throws Exception;
}
