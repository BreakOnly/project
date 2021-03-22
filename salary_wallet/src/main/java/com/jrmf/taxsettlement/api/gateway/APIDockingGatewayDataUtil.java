package com.jrmf.taxsettlement.api.gateway;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jrmf.utils.ArithmeticUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;

public class APIDockingGatewayDataUtil {

	private static final Logger logger = LoggerFactory.getLogger(APIDockingGatewayDataUtil.class);

	private static final String GATEWAY_FIELD_DIV = "_";

	private static final String AMOUNT_REGEX = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";

	public static Map<String, Object> parseAndTransform(Object data) {
		try {
			Class<?> thisClass = data.getClass();
			Map<String, Object> outData = new HashMap<String, Object>();

			do {
				for (Field field : thisClass.getDeclaredFields()) {
					int mod = field.getModifiers();
					if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
						continue;
					}
					boolean originalAccessable = field.isAccessible();
					String outDataKey = getAPIDataKey(field.getName());

					Class<?> fieldType = field.getType();
					field.setAccessible(true);
					try {
						Object value = field.get(data);
						if (value == null || fieldType.isPrimitive() || String.class.equals(fieldType)) {
							outData.put(outDataKey, value);
						} else if (Map.class.isAssignableFrom(fieldType)) {
							outData.put(outDataKey, parseAndTransformMap((Map<?, ?>) value));
						} else if (Collection.class.isAssignableFrom(fieldType)) {
							outData.put(outDataKey, parseAndTransformCollection((Collection) value));
						} else {
							outData.put(outDataKey, parseAndTransform(value));
						}
					} finally {
						field.setAccessible(originalAccessable);
					}
				}

				thisClass = thisClass.getSuperclass();
			} while (!thisClass.equals(Object.class));

			return outData;
		} catch (Exception e) {
			logger.error("error occured in data parse and transform", e);
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
		}

	}

	private static List<Object> parseAndTransformCollection(Collection<?> collectionObj) {

		List<Object> outData = new ArrayList<Object>();

		Iterator<?> it = collectionObj.iterator();
		while (it.hasNext()) {
			Object elementValue = it.next();
			if (elementValue == null) {
				outData.add(null);
			} else {
				Class<?> valueClass = elementValue.getClass();
				if (valueClass.isPrimitive() || String.class.equals(valueClass)) {
					outData.add(elementValue);
				} else if (Map.class.isAssignableFrom(valueClass)) {
					outData.add(parseAndTransformMap((Map<?, ?>) elementValue));
				} else if (Collection.class.isAssignableFrom(valueClass)) {
					outData.add(parseAndTransformCollection((Collection) elementValue));
				} else {
					outData.add(parseAndTransform(elementValue));
				}
			}
		}

		return outData;
	}

	private static Map<String, Object> parseAndTransformMap(Map<?, ?> mapObj) {
		Map<String, Object> outData = new HashMap<String, Object>();

		for (Entry<?, ?> entry : mapObj.entrySet()) {
			String outDataKey = getAPIDataKey(entry.toString());

			Object value = entry.getValue();

			if (value == null) {
				outData.put(outDataKey, value);
			} else {
				Class<?> valueClass = value.getClass();
				if (valueClass.isPrimitive() || String.class.equals(valueClass)) {
					outData.put(outDataKey, value);
				} else if (Map.class.isAssignableFrom(valueClass)) {
					outData.put(outDataKey, parseAndTransformMap((Map<?, ?>) value));
				} else if (Collection.class.isAssignableFrom(valueClass)) {
					outData.put(outDataKey, parseAndTransformCollection((Collection) value));
				} else {
					outData.put(outDataKey, parseAndTransform(value));
				}
			}
		}

		return outData;
	}

	static Object checkAndTransform(Map<String, Object> inData, Class<?> exactualParamClass) {
		try {
			Object newParams = exactualParamClass.newInstance();
			Class<?> thisClass = exactualParamClass;
			do {
				for (Field field : thisClass.getDeclaredFields()) {
					int mod = field.getModifiers();
					if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
						continue;
					}
					boolean originalAccessable = field.isAccessible();
					String inDataKey = getAPIDataKey(field.getName());
					Object inValue = inData.get(inDataKey);

					NotNull notNull = field.getAnnotation(NotNull.class);
					if (notNull != null && (inValue == null || "".equals(inValue))) {
						throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), field.getName());
					}

					Amount amount = field.getAnnotation(Amount.class);
					if (amount != null) {
						if (inValue == null || "".equals(inValue)) {
							throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), field.getName());
						}

						if (!((String) inValue).matches(AMOUNT_REGEX) || ArithmeticUtil.compareTod(((String) inValue), "0") <= 0) {
							throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(),
									inValue.toString());
						}
					}

					field.setAccessible(true);
					try {
						field.set(newParams, inValue);
					} finally {
						field.setAccessible(originalAccessable);
					}
				}

				thisClass = thisClass.getSuperclass();
			} while (!thisClass.equals(Object.class));

			return newParams;
		} catch (APIDockingException e) {
			throw e;
		} catch (Exception e) {
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
		}
	}

	private static String getAPIDataKey(String fieldName) {

		StringBuilder apiDataKey = new StringBuilder();
		for (char c : fieldName.toCharArray()) {
			if (Character.isUpperCase(c)) {
				apiDataKey.append(GATEWAY_FIELD_DIV).append(Character.toLowerCase(c));
			} else {
				apiDataKey.append(c);
			}
		}

		return apiDataKey.toString();
	}

	public static Map<String, Object> toSignMap(Map<String, Object> inMap) {
		return transformMap(null, inMap);
	}

	private static Map<String, Object> transformMap(String prefix, Map<String, Object> mapObj) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : mapObj.entrySet()) {
			String key = entry.getKey();
			key = prefix == null ? key : new StringBuilder(prefix).append(".").append(key).toString();
			Object entryValue = entry.getValue();
			if (entryValue == null) {
				retMap.put(key, entryValue);
			} else {
				Class<?> valueClass = entryValue.getClass();
				if (isPrimitiveObject(valueClass)) {
					retMap.put(key, entryValue);
				} else if (Map.class.isAssignableFrom(valueClass)) {
					retMap.putAll(transformMap(key, (Map<String, Object>) entryValue));
				} else if (List.class.isAssignableFrom(valueClass)) {
					retMap.putAll(transformList(key, (List<Object>) entryValue));
				} else {
					retMap.putAll(transformObject(key, entryValue));
				}
			}
		}

		return retMap;
	}

	private static Map<String, Object> transformObject(String prefix, Object obj) {
		Map<String, Object> retMap = new HashMap<String, Object>();

		Class<?> thisClass = obj.getClass();
		do {
			for (Field field : thisClass.getDeclaredFields()) {

				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers))
					continue;

				boolean fieldAccessable = field.isAccessible();
				field.setAccessible(true);
				String fieldName = field.getName();
				try {
					Object fieldValue;
					try {
						fieldValue = field.get(obj);
					} catch (Exception e) {
						logger.error("error occured in get value of field[" + fieldName + "]", e);
						continue;
					}

					String key = new StringBuilder(prefix).append(".").append(fieldName).toString();
					if (fieldValue == null) {
						retMap.put(key, fieldValue);
					} else {
						Class<?> valueClass = fieldValue.getClass();
						if (isPrimitiveObject(valueClass)) {
							retMap.put(key, fieldValue);
						} else if (Map.class.isAssignableFrom(valueClass)) {
							retMap.putAll(transformMap(key, (Map<String, Object>) fieldValue));
						} else if (List.class.isAssignableFrom(valueClass)) {
							retMap.putAll(transformList(key, (List<Object>) fieldValue));
						} else {
							retMap.putAll(transformObject(key, fieldValue));
						}
					}
				} finally {
					field.setAccessible(fieldAccessable);
				}
			}
			thisClass = thisClass.getSuperclass();
		} while (!thisClass.equals(Object.class));

		return retMap;
	}

	private static Map<String, Object> transformList(String prefix, List<Object> list) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		int arraySize = list.size();
		for (int i = 0; i < arraySize; i++) {
			Object elementValue = list.get(i);
			String key = new StringBuilder(prefix).append("[").append(String.valueOf(i)).append("]").toString();
			if (elementValue == null) {
				retMap.put(key, elementValue);
			} else {
				Class<?> valueClass = elementValue.getClass();
				if (isPrimitiveObject(valueClass)) {
					retMap.put(key, elementValue);
				} else if (Map.class.isAssignableFrom(valueClass)) {
					retMap.putAll(transformMap(key, (Map<String, Object>) elementValue));
				} else if (List.class.isAssignableFrom(valueClass)) {
					retMap.putAll(transformList(key, (List<Object>) elementValue));
				} else {
					retMap.putAll(transformObject(key, elementValue));
				}
			}
		}

		return retMap;
	}

	private static boolean isPrimitiveObject(Class<?> valueClass) {
		return String.class.equals(valueClass) || valueClass.isPrimitive() || Integer.class.equals(valueClass)
				|| Boolean.class.equals(valueClass) || Short.class.equals(valueClass)
				|| Character.class.equals(valueClass) || Byte.class.equals(valueClass) || Long.class.equals(valueClass)
				|| Double.class.equals(valueClass) || Float.class.equals(valueClass);
	}
}
