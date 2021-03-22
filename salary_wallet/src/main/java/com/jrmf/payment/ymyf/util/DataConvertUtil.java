/**
 * 
 */
package com.jrmf.payment.ymyf.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
//import com.google.common.collect.Maps;


/**
 * <p>数据转换类</p>
 * @author fjl
 * @version $Id: ParamCharsetConvert.java, v 0.1 2013-11-12 下午1:01:09 fjl Exp $
 */
public class DataConvertUtil {

    /**
     * 请求原始数据转换成Map
     * @param data
     * @param charset
     * @return
     */
    public static Map<String, String> paramCharsetConvert(Map<?, ?> data, String charset){

        Map<String, String> formattedParameters = new HashMap<String, String>(data.size());
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            if (entry.getValue() == null || Array.getLength(entry.getValue()) == 0) {
                formattedParameters.put((String) entry.getKey(), null);
            } else {
                String str = (String) Array.get(entry.getValue(), 0);
                if(StringUtils.isNotBlank(str)) {
                	formattedParameters.put((String) entry.getKey(),str);
                } 
            }
        }

        return formattedParameters;
    }

    /**
     * json转map
     * @param jsonObj
     * @return
     */
    public static Map<String, Object> jsonToMap(String jsonObj) {
        JSONObject jsonObject = JSONObject.parseObject(jsonObj);
        Map<String, Object> map = (Map)jsonObject;
        return map;
    }

    /**
     * json转javaBean
     * @param jsonObj
     * @return
     */
    public static <T> T jsonToPojo(String jsonStr, Class<T> clazz) {
        return JSONObject.parseObject(jsonStr, clazz);
    }
    
    /**
     * javaBean转javaBean
     * @param from
     * @param to
     * @return
     */
    public static <T> T convertBeanToBean(T from, T to) {   
        try {   
            BeanInfo beanInfo = Introspector.getBeanInfo(to.getClass());   
            PropertyDescriptor[] ps = beanInfo.getPropertyDescriptors();   
  
            for (PropertyDescriptor p : ps) {   
               Method getMethod = p.getReadMethod();   
               Method setMethod = p.getWriteMethod();   
               if (getMethod != null && setMethod != null) {   
                   try {   
                      Object result = getMethod.invoke(from);   
                      setMethod.invoke(to, result);   
                   } catch (Exception e) {   
                      continue;   
                   }   
               }   
            }   
        } catch (Exception e) {   
           e.printStackTrace();   
        }   
        return to;   
    }
    /**
     * 将对象装换为map
     *
     * @param bean
     * @return
     */
    /*public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }*/

}
