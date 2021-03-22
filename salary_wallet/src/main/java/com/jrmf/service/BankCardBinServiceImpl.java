package com.jrmf.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.jrmf.domain.BankCard;
import com.jrmf.domain.BankName;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.TransferBankDao;

@Service("bankCardBinService")
public class BankCardBinServiceImpl implements BankCardBinService{

	private static Logger logger = LoggerFactory.getLogger(BankCardBinServiceImpl.class);

	private static final String URL = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json";
	private static final String PARAM = "_input_charset=utf-8&cardBinCheck=true";
	@Autowired
	private TransferBankDao bankDao;
	@Autowired
	private ChannelCustomDao customDao;
	
	public static void main(String[] args) {
		long statr = System.currentTimeMillis();
		String sendGet = sendGet(URL,"6214831000235801");
		System.out.println(sendGet);
		long end = System.currentTimeMillis();
		System.out.println((end-statr)+"毫秒");
	}

	/**
	 * 获取银行图片接口 https://apimg.alipay.com/combo.png?d=cashier&t=CCB
	 */
	@Override
	public BankName getBankName(String cardNo) {
		@SuppressWarnings("unchecked")
		String aliResponse = sendGet(URL, cardNo);
		Map<String, String> maps = (Map<String, String>) JSON.parse(aliResponse);
		
		BankName bankName = new BankName();
//		String validated = maps.get("validated");
		String validated = String.valueOf(maps.get("validated"));
		
		if("true".equals(validated)){
			bankName = customDao.getBankName(maps.get("bank"));
			if(bankName != null){
				bankName.setValidated(validated);
			}else{
				bankName = new BankName();
				bankName.setValidated("unknow");
			}
		}else{
			bankName.setValidated(validated);
		}
		
		return bankName;
	}

	private static String sendGet(String url, String cardNo) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + PARAM + "&cardNo=" + cardNo;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setConnectTimeout(10000);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
//			for (String key : map.keySet()) {
//				System.out.println(key + "--->" + map.get(key));
//			}
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送GET请求出现异常！",e);
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	
//	public static void main(String args[]){
////		System.out.println(sendGet(URL, "7200089980110378169"));
//		String aliResponse = sendGet(URL, "112");
//		System.out.println(aliResponse);
//		Map<String, String> maps = (Map<String, String>) JSON.parse(sendGet(URL, "7200089980110378169"));
//		String validated = String.valueOf(maps.get("validated"));
//		System.out.println("------------------"+validated);
//        if("true".equals(validated)){
//        	System.out.println(2);
//        }else if("false".equals(validated)){
//        	System.out.println(3);
//        }
//	}

	@Override
	public List<BankCard> getbankcardAll(){
		return bankDao.getbankcardAll();
	};
}
