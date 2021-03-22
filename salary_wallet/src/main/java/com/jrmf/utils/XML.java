package com.jrmf.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.NodeList;

import com.jrmf.payment.zjpay.ZjConfig;



/**
 * XML通用类
 * 基于dom4j实现
 * @since 2013.01.15
 * @version 1.0.0_1
 * 
 */
public class XML {
	/**
	 * XML格式数据解析为Map<String, String>
	 * 
	 * @param xmlData XML数据
	 * @param unique 元素是否重名
	 * @param charset 字符集
	 * @return Map<String, String>
	 * @throws Exception
	 */
	public static Map<String, String> parse(String xmlData, boolean unique, String charset) throws Exception {
		Map<String, String> map = new HashMap<String, String>();

		InputStream is = new ByteArrayInputStream(xmlData.getBytes(charset));
		SAXReader sax = new SAXReader(false);
		Document document = sax.read(is);

		Element rootElement = document.getRootElement();
		map = parseElements(map, rootElement, unique ? null : rootElement.getName().toLowerCase());

		return map;
	}

	/**
	 * XML节点元素数据解析为Map<String, String>
	 * 
	 * @param map 返回map
	 * @param element 节点元素
	 * @param name 节点元素名
	 * @return Map<String, String>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, String> parseElements(Map<String, String> map, Element element, String name) throws Exception {
		List<Element> elementList = element.elements();

		if(elementList.isEmpty())
		{
			map.put(StringUtil.isEmpty(name) ? element.getName().toLowerCase() : name, element.getText());
		}
		else
		{
			for(Element _element : elementList)
			{
				map = parseElements(map, _element, StringUtil.isEmpty(name) ? null : (name + "." +_element.getName().toLowerCase()));
			}
		}

		return map;
	}

	/**
	 * 生成XML数据
	 * 
	 * @param rootName 根元素名
	 * @param elements 元素数据
	 * @param charset 字符集
	 * @return XML数据
	 * @throws Exception
	 */
	public static String create(String rootName,  Map<String, String> elements, String charset) throws Exception {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding(charset);
		Element rootElement = document.addElement(rootName);

		Set<Entry<String, String>> elementsSet = elements.entrySet();
		Iterator<Entry<String, String>> elementsIterator = elementsSet.iterator();
		while(elementsIterator.hasNext())
		{
			Entry<String, String> elementEntry = elementsIterator.next();
			String elementName = elementEntry.getKey();
			String elementValue = elementEntry.getValue();
			Element element = rootElement.addElement(elementName);
			element.addText(elementValue);
		}

		return document.asXML();
	}

	/**
	 * 生成XML数据
	 * 
	 * @param rootName 根元素名
	 * @param rootAttributes 根元素数据
	 * @param elements 元素数据
	 * @param charset 字符集
	 * @return XML数据
	 * @throws Exception
	 */
	public static String create(String rootName,  Map<String, Object> rootAttributes, Map<String, List<Map<String, Object>>> elements, String charset) throws Exception {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding(charset);
		Element rootElement = document.addElement(rootName);

		if(rootAttributes != null && !rootAttributes.isEmpty())
		{
			Set<Entry<String, Object>> rootAttributesSet = rootAttributes.entrySet();
			Iterator<Entry<String, Object>> rootAttributesIterator = rootAttributesSet.iterator();
			while(rootAttributesIterator.hasNext())
			{
				Entry<String, Object> rootAttributeEntry = rootAttributesIterator.next();
				rootElement.addAttribute(rootAttributeEntry.getKey(), rootAttributeEntry.getValue().toString());
			}
		}

		Set<Entry<String, List<Map<String, Object>>>> elementsSet = elements.entrySet();
		Iterator<Entry<String, List<Map<String, Object>>>> elementsIterator = elementsSet.iterator();
		while(elementsIterator.hasNext())
		{
			Entry<String, List<Map<String, Object>>> elementEntry = elementsIterator.next();
			String elementName = elementEntry.getKey();
			List<Map<String, Object>> elementAttributes = elementEntry.getValue();
			Element element = rootElement.addElement(elementName + "s");

			for(Map<String, Object> attributes : elementAttributes)
			{
				Element subElement = element.addElement(elementName);
				Set<Entry<String, Object>> attributesSet = attributes.entrySet();
				Iterator<Entry<String, Object>> attributesIterator = attributesSet.iterator();
				while(attributesIterator.hasNext())
				{
					Entry<String, Object> attributeEntry = attributesIterator.next();
					subElement.addAttribute(attributeEntry.getKey(), attributeEntry.getValue().toString());
				}
			}
		}

		return document.asXML();
	}

	/**
	 * 将Map转换为XML,Map可以多层转
	 * @param params 需要转换的map。
	 * @param parentName 就是map的根key,如果map没有根key,就输入转换后的xml根节点。
	 * @return String-->XML
	 */
	@SuppressWarnings("unchecked")
	public static String createXmlByMap(Map<String, Object> map,
			String parentName) {
		//获取map的key对应的value
		Map<String, Object> rootMap=(Map<String, Object>)map.get(parentName);
		if (rootMap==null) {
			rootMap=map;
		}
		Document doc = DocumentHelper.createDocument();
		//设置根节点
		doc.addElement(parentName);
		String xml = iteratorXml(doc.getRootElement(), parentName, rootMap);
		return formatXML(xml);
	}

	/**
	 * 循环遍历params创建xml节点
	 * @param element 根节点
	 * @param parentName 子节点名字
	 * @param params map数据
	 * @return String-->Xml
	 */
	@SuppressWarnings("unchecked")
	public static String iteratorXml(Element element, String parentName,
			Map<String, Object> params) {
		Element e = element.addElement(parentName);
		Set<String> set = params.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (params.get(key) instanceof Map) {
				iteratorXml(e, key, (Map<String, Object>) params.get(key));
			} else if (params.get(key) instanceof List) {
				List<Object> list = (ArrayList<Object>) params.get(key);
				for (int i = 0; i < list.size(); i++) {
					iteratorXml(e, key, (Map<String, Object>) list.get(i));
				}
			} else {
				String value = params.get(key) == null ? "" : params.get(key)
						.toString();
				e.addElement(key).addText(value);
				// e.addElement(key).addCDATA(value);
			}
		}
		return e.asXML();
	}

	/**
	 * 格式化xml
	 * @param xml
	 * @return
	 */
	public static String formatXML(String xml) {
		String requestXML = null;
		StandaloneWriter writer = null;
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			document = reader.read(new StringReader(xml));
			if (document != null) {
				StringWriter stringWriter = new StringWriter();
				writer = new StandaloneWriter();
				writer.setWriter(stringWriter);
				writer.write(document);
				writer.flush();
				requestXML = stringWriter.getBuffer().toString();
			}
			return requestXML;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {

				}
			}
		}
	}

	public static org.w3c.dom.Document createDocument(String xmlString) throws Exception
	{
		DocumentBuilder documentBuilder = createDocumentBuilder();
		return documentBuilder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
	}

	public static DocumentBuilder createDocumentBuilder() throws Exception
	{
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		return documentBuilderFactory.newDocumentBuilder();
	}

	public static String getNodeText(org.w3c.dom.Document document, String nodeName) throws Exception
	{
		return getNodeText(document, nodeName, 0);
	}

	public static String getNodeText(org.w3c.dom.Document document, String nodeName, int index) throws Exception
	{
		NodeList nodeList = (document).getElementsByTagName(nodeName);
		if ((nodeList == null) || (index >= nodeList.getLength())) {
			return null;
		}
		return nodeList.item(index).getTextContent();
	}

	public static void main(String[] args) {
		Map<String, Object> reqMap = new LinkedHashMap<String, Object>();
		Map<String, Object> tranDataMap = new LinkedHashMap<String, Object>();
		Map<String, Object> headMap = new LinkedHashMap<String, Object>();
		//消息头部
		headMap.put("TxCode", ZjConfig.SINGLE_TRANSFER_METHOD);
		headMap.put("InstitutionID", "005203");
		//请求内容
		Map<String, Object> bodyMap = new LinkedHashMap<String, Object>();
		//付款人信息
		Map<String, Object> payerMap = new LinkedHashMap<String, Object>();
		payerMap.put("PaymentAccountName", "海南慧用工服务有限公司");
		payerMap.put("PaymentAccountNumber", "600052030003");
		//收款人信息
		Map<String, Object> payeeMap = new HashMap<String, Object>();
		payeeMap.put("AccountType", "11");
		payeeMap.put("BankID", "700");
		payeeMap.put("BankAccountName", "尹邦凤");
		payeeMap.put("BankAccountNumber", "6222020200002432");
		payeeMap.put("PhoneNumber", "");
		bodyMap.put("TxSN", "");
		bodyMap.put("PaymentFlag", "1");
		bodyMap.put("Payer", payerMap);
		bodyMap.put("Payee", payeeMap);
		bodyMap.put("Amount", "100");
		bodyMap.put("Remark", "");
		tranDataMap.put("Head", headMap);
		tranDataMap.put("Body", bodyMap);
		reqMap.put("Request", tranDataMap);
		System.out.println(createXmlByMap(reqMap,"Request"));
	}
}