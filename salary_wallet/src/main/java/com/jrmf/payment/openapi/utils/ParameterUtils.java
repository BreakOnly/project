/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrmf.payment.openapi.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jrmf.payment.openapi.exception.AygOpenApiException;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年4月25日
 */
@SuppressWarnings({ "unchecked"})
public class ParameterUtils {

	public final static String CONTACT_STR = "&";
	public final static String EQUALS_STR = "=";
	public final static String SPLIT_STR = ",";
	
	public static final String JSON_SUFFIX = "}";
	public static final String JSON_PREFIX = "{";
	
	public static final String BRACKET_PREFIX = "[";
	public static final String BRACKET_SUFFIX = "]";
	
	public final static String PARAM_SIGN = "sign";
	public final static String PARAM_SIGN_TYPE = "signType";
	public final static String PARAM_DATA = "data";
	
	public static Map<String, Object> queryParamsToMap(String queryParams){
		Map<String, Object>  map = new HashMap<String, Object>();
		String[] paramSegs = StringUtils.split(queryParams, CONTACT_STR);
		String[] kv;
		for (String param : paramSegs) {
			kv = StringUtils.split(param,EQUALS_STR);
			if(kv.length == 1 || StringUtils.isBlank(kv[1]))continue;
			map.put(kv[0].trim(), kv[1].trim());
		}
		return map;
	}
	
	
	public static String mapToQueryParams(Map<String, Object> map,boolean sort){
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<>(map.keySet());
		if(sort){
			Collections.sort(keys);
		}
		for (String key : keys) {
			sb.append(key).append(EQUALS_STR).append(map.get(key)).append(CONTACT_STR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static String objectToSignContent(Object param){
		Map<String, Object> map = beanToMap(param);
		return mapToSignContent(map);
	}
	
	
	public static String mapToSignContent(Map<String, Object> param){

		if(param == null || param.isEmpty())return null;
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<>(param.keySet());
		Collections.sort(keys);
		Object value;
		for (String key : keys) {
			if(PARAM_SIGN_TYPE.equals(key) || PARAM_SIGN.equals(key))continue;
			value = param.get(key);
			if(value == null || StringUtils.isBlank(value.toString()))continue;
			if(value instanceof Map){
				value = mapToSignContent((Map<String, Object>) value);
				if(value != null){
					value = JSON_PREFIX + value + JSON_SUFFIX;
				}
			}else if(value instanceof Iterable) {
        		StringBuilder sb1 = new StringBuilder();
        		sb1.append(BRACKET_PREFIX);
                Iterator<?> it = ((Iterable<?>) value).iterator();
                while (it.hasNext()) {
                	Object object = it.next();
                	if(isSimpleDataType(object)){
                		sb1.append(object).append(SPLIT_STR);
                	}else{                		
                		sb1.append(JSON_PREFIX).append(objectToSignContent(object)).append(JSON_SUFFIX).append(SPLIT_STR);
                	}
                }
                if(sb1.length() == 1){
                	value = null;
                } else if(sb1.length() > 0){
                	sb1.deleteCharAt(sb1.length() - 1);
                	sb1.append(BRACKET_SUFFIX);
                	value = sb1.toString();
                }
            }else if(value instanceof Date) {
            	value = ((Date)value).getTime();
            }
			if(value != null){
				sb.append(key).append(EQUALS_STR).append(value).append(CONTACT_STR);	
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	
	private static Map<String, Object> beanToMap(Object bean) {
		if(bean instanceof Map)return (Map<String, Object>) bean;
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
        	BeanInfo srcBeanInfo = Introspector.getBeanInfo(bean.getClass());

    		PropertyDescriptor[] descriptors = srcBeanInfo.getPropertyDescriptors();
    		for (PropertyDescriptor descriptor : descriptors) {
                String propertyName = descriptor.getName();
                if("class".equalsIgnoreCase(propertyName))continue;
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                	if(isSimpleDataType(result) || result instanceof Iterable){                		
                		returnMap.put(propertyName, result);
                	}else{
                		returnMap.put(propertyName, beanToMap(result));
                	}
                }
            }
        } catch (Exception e) {
        	throw new AygOpenApiException("签名数据打包失败", e);
        }


        return returnMap;

    }
	
	private static boolean isSimpleDataType(Object o) {   
		   Class<? extends Object> clazz = o.getClass();
	       return 
	       (   
	           clazz.equals(String.class) ||   
	           clazz.equals(Integer.class)||   
	           clazz.equals(Byte.class) ||   
	           clazz.equals(Long.class) ||   
	           clazz.equals(Double.class) ||   
	           clazz.equals(Float.class) ||   
	           clazz.equals(Character.class) ||   
	           clazz.equals(Short.class) ||   
	           clazz.equals(BigDecimal.class) ||     
	           clazz.equals(Boolean.class) ||   
	           clazz.equals(Date.class) ||   
	           clazz.isPrimitive()   
	       );   
	   }
	
	public static void main(String[] args) {
		Map<String, Object> params = new HashMap<>();
		params.put("aaa", "AAA");
		params.put("ddd", new Date());
		System.out.println(mapToSignContent(params));
	}

}
