package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.math.BigDecimal;

public class TextRowUnit implements SerialFieldDataUnit {

	private String originalLine;
	
	private String[] fieldValues;

	public TextRowUnit(String splitRegex, String lineData) {
		originalLine = lineData;
		fieldValues = lineData.split(splitRegex);
	}

	@Override
	public Object getFieldValue(int index, Class<?> fieldType) {
		
		if(index >= fieldValues.length)
			return null;
		
		String value = fieldValues[index];
		if (value == null || String.class.equals(fieldType))
			return value;

		if (int.class.equals(fieldType)) {
			return Integer.valueOf(value);
		} else if (boolean.class.equals(fieldType)) {
			return Boolean.valueOf(value);
		} else if (long.class.equals(fieldType)) {
			return Long.valueOf(value);
		} else if (short.class.equals(fieldType)) {
			return Short.valueOf(value);
		} else if (float.class.equals(fieldType)) {
			return Float.valueOf(value);
		} else if (double.class.equals(fieldType)) {
			return Double.valueOf(value);
		} else if (BigDecimal.class.equals(fieldType)) {
			return new BigDecimal(value);
		} else {
			throw new RuntimeException("not support field type[{" + fieldType.getName() + "}] transform");
		}
	}

	@Override
	public String getBriefInfo() {
		return originalLine;
	}

}
