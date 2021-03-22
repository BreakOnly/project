/**
 * Project Name:pay-protocol
 * File Name:Xml.java
 * Package Name:cn.swiftpass.pay.protocol
 * Date:2014-8-10下午10:48:21
 *
 */

package com.jrmf.payment.ympay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
/**
 * ClassName:Xml
 * Function: XML的工具方法
 * Date:     2014-8-10 下午10:48:21 
 * @author    
 */
public class XmlUtils {

	/** <一句话功能简述>
	 * <功能详细描述>request转字符串
	 * @param request
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String parseRequst(HttpServletRequest request){
		String body = "";
		try {
			ServletInputStream inputStream = request.getInputStream(); 
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while(true){
				String info = br.readLine();
				if(info == null){
					break;
				}
				if(body == null || "".equals(body)){
					body = info;
				}else{
					body += info;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}            
		return body;
	}

	public static String parseXML(SortedMap<String, String> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				sb.append("<" + k + ">" + parameters.get(k) + "</" + k + ">\n");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * 从request中获得参数Map，并返回可读的Map
	 * 
	 * @param request
	 * @return
	 */
	public static SortedMap getParameterMap(HttpServletRequest request) {
		// 参数Map
		Map properties = request.getParameterMap();
		// 返回值Map
		SortedMap returnMap = new TreeMap();
		Iterator entries = properties.entrySet().iterator();
		Map.Entry entry;
		String name = "";
		String value = "";
		while (entries.hasNext()) {
			entry = (Map.Entry) entries.next();
			name = (String) entry.getKey();
			Object valueObj = entry.getValue();
			if(null == valueObj){
				value = "";
			}else if(valueObj instanceof String[]){
				String[] values = (String[])valueObj;
				for(int i=0;i<values.length;i++){
					value = values[i] + ",";
				}
				value = value.substring(0, value.length()-1);
			}else{
				value = valueObj.toString();
			}
			returnMap.put(name, value.trim());
		}
		return returnMap;
	}


	/** 
	 * xml转map 不带属性 
	 * @param xmlStr 
	 * @param needRootKey 是否需要在返回的map里加根节点键 
	 * @return 
	 * @throws DocumentException 
	 */  
	public static Map xml2map(String xmlStr, boolean needRootKey) throws org.dom4j.DocumentException {  
		org.dom4j.Document doc = org.dom4j.DocumentHelper.parseText(xmlStr);  
		org.dom4j.Element root = doc.getRootElement();  
		Map<String, Object> map = (Map<String, Object>) xml2map(root);  
		if(root.elements().size()==0 && root.attributes().size()==0){  
			return map;  
		}  
		if(needRootKey){  
			//在返回的map里加根节点键（如果需要）  
			Map<String, Object> rootMap = new HashMap<String, Object>();  
			rootMap.put(root.getName(), map);  
			return rootMap;  
		}  
		return map;  
	}  

	/** 
	 * xml转map 不带属性 
	 * @param e 
	 * @return 
	 */  
	private static Map xml2map(org.dom4j.Element e) {  
		Map map = new LinkedHashMap();  
		List list = e.elements();  
		if (list.size() > 0) {  
			for (int i = 0; i < list.size(); i++) {  
				org.dom4j.Element iter = (org.dom4j.Element) list.get(i);  
				List mapList = new ArrayList();  

				if (iter.elements().size() > 0) {  
					Map m = xml2map(iter);  
					if (map.get(iter.getName()) != null) {  
						Object obj = map.get(iter.getName());  
						if (!(obj instanceof List)) {  
							mapList = new ArrayList();  
							mapList.add(obj);  
							mapList.add(m);  
						}  
						if (obj instanceof List) {  
							mapList = (List) obj;  
							mapList.add(m);  
						}  
						map.put(iter.getName(), mapList);  
					} else  
						map.put(iter.getName(), m);  
				} else {  
					if (map.get(iter.getName()) != null) {  
						Object obj = map.get(iter.getName());  
						if (!(obj instanceof List)) {  
							mapList = new ArrayList();  
							mapList.add(obj);  
							mapList.add(iter.getText());  
						}  
						if (obj instanceof List) {  
							mapList = (List) obj;  
							mapList.add(iter.getText());  
						}  
						map.put(iter.getName(), mapList);  
					} else  
						map.put(iter.getName(), iter.getText());  
				}  
			}  
		} else  
			map.put(e.getName(), e.getText());  
		return map;  
	}  


	public static String toXml(Map<String, Object> params){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?><B2CReq>");
		Set es = params.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String v = (String) entry.getValue();
			String k = (String) entry.getKey();
			sb.append("<" + k + ">" + v + "</" + k + ">");
		}
		sb.append("</B2CReq></xml>");
		return sb.toString();
	}
}

