package com.jrmf.payment.yeepay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.error.YopSubError;


public class YeepayTest {
	private static Logger logger = LoggerFactory.getLogger(YeepayTest.class);

	public static void main(String[] args) {
//		payTest();
//		queryTest();
		queryBalanceTest();
    }

	
	public static void payTest(){
    	YeepayTest yeepayService = new YeepayTest();
		Map<String, Object> params = new HashMap<>();

		params.put("batchNo", yeepayService.getRandom(17));
		params.put("orderId", yeepayService.getRandom(10));
		params.put("amount", "0.01");
		params.put("product", "WTJS");
		params.put("urgency", "1");
		params.put("accountName", "张桓");
		params.put("accountNumber", "6227001251210611761");
		params.put("bankCode", "CCB");
//		params.put("bankName", "建设银行");
		params.put("bankBranchName", "");
		params.put("provinceCode", "");
		params.put("cityCode", "");
		params.put("feeType", "");
		params.put("desc", "");
		params.put("leaveWord", "test");
		params.put("abstractInfo", "");
    	
    	try {
    		yeepayService.yeepayYOP(params, "/rest/v1.0/balance/transfer_send");
		} catch (IOException e) {
				logger.error(e.getMessage(),e);
		}
	}
	
	public static void queryTest(){
		
    	YeepayTest yeepayService = new YeepayTest();
		Map<String, Object> params = new HashMap<>();
		
		params.put("batchNo", "2019041200010193");
		params.put("pageSize", "1");
		
    	try {
    		Map<String, Object> jsonMap = yeepayService.yeepayYOP(params, "/rest/v1.0/balance/transfer_query");

    		String errorCode = (String) jsonMap.get("errorCode");
			String errorMsg = (String) jsonMap.get("errorMsg");
			logger.info("易宝通道--代付代发--查询--errorCode：" + errorCode + "-" + errorMsg);
			if("BAC000048".equals(errorCode)){//无此记录
				logger.info("易宝通道--代付代发-无-失败");
			}else{
				
				JSONArray jsonArray = (JSONArray) jsonMap.get("list");
				logger.info("易宝通道--代付代发--查询--jsonArray：" + jsonArray);
				if(jsonArray != null){
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					logger.info("易宝通道--代付代发--查询--jsonObject：" + jsonObject.toString());
					String bankTrxStatusCode = (String) jsonObject.get("bankTrxStatusCode");
					String transferStatusCode = (String) jsonObject.get("transferStatusCode");
					
					if("BAC001".equals(errorCode)
							&& "0026".equals(transferStatusCode)
							&& "S".equals(bankTrxStatusCode)){
						logger.info("易宝通道--代付代发--成功");
					}else if ("BAC001".equals(errorCode)
							&& "0026".equals(bankTrxStatusCode)
							&& "F".equals(transferStatusCode)){
						logger.info("易宝通道--代付代发--失败");
					}else{
						logger.info("易宝通道--代付代发--处理中");
					}
				}else{
					logger.info("易宝通道--代付代发--处理中");
				}
			}

		
    		
		} catch (IOException e) {
				logger.error(e.getMessage(),e);
		}
	}
	
    public static Map<String, Object> yeepayYOP(Map<String, Object> map, String uri) throws IOException {

        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, YopSubError> erresult = new HashMap<String, YopSubError>();
        
		String baseUrl = "https://openapi.yeepay.com/yop-center";
		String priavateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDdCQ72/uAGAl0xU8u0QcqMq40Yln4nCbDnSvT75DNwnGTqGlkqtQCHhgCWLRsiCz3SIaDvuS1bPiP1w+0ixsl/DASRNfd9q3+z8HYKdsGOF3aYLjZNaCqKo7qVXOswMzfV2N9bs9er1A4U4CK1612iH+/Urw1YMggtqW8V44k7hh61mzpl6A95cbn+WPC8Th9ZKBo31J4M5IBJ9FSbkFfYa9qj2hrL+JPHW8TJFp3KtoxoNzetg1OeyZLbDUL1qLvSg+p1oqX4rJrukGJ3ndsFOVuJlbxDsQIiutIW/HEYnnUMSXnLbvPDbVbiOZKRwkjOuRyWftxPdBRXuUQmUWcVAgMBAAECggEAdTDHUWeC6Uy5klymRTUYqgqOrXmk+xelRow0ZZTxPQR+cKx1mDINTM1cZEMX/q3NIisYQ+F/TcAdmsE9alHRJV3+0eq2BFg4RfP48cVTC/4er8LYoaPX4le6plR0kcKmGOh6TAmHEKBGswS/Aor+wuWB59bEEX29XlBWCFnyOa7/Hm+UduHUnURWxFvxyvlpu+PFrxgVmFaHXk69Kw7zDmwhzOVPG9cUioFoVwWQAR6mWEnLQhvcN/4aV03Vb0FSvBrIcyGUMuY5xCjOqvxJE3DAhDQV/6nlq7ZFV52Jl5xYs7YT4AN9+p09pCjju0ENi1gpA2trd5aNaDMiFGaWIQKBgQD1UuRCEeuSAMUoj3j7/NY2cQLV3Cal4KCTjH/blu/mVENqxgaVwcCP8vHApm8YD8bJi4Gk+kjuQjHrYPLmaHEHauuPHXi8VP/eKsp8FzFJ/KH4msp3zWEojbE3+V+S6SYUa4sNytKTj+NlKYKgWgxhtRkbW1oJpDiewRyc5WIcyQKBgQDmp5LxiCOSP0Q8b50b9WW07TQOi+yHjpHNRkT6YP4kmo3MSOXfSKoqM/TwjoBOBOqm7MT9QfGjmK4BP1pX25wrKK/vrP5PIY+SXJgAWyvfJ1R7bD58p//X4/lKNUka05rTZiikjhKDrsCDg8/P+sZzo/IISs+4pYtcQNAbL3Q57QKBgQCokVSWsYmUwUK4JB2VyJ3Ius6f9K35vOeZeyk/0e717WCFtZxAuul+AAltvu8HMY/+XQWwPupuDL4QZoWBHLLNzgIeTLnxbtaB6hVIPwd3N0h0cqg8ubrRCsuDEq9BW0L3NQ5PY9Zd3rj5ghRn6ngnizC9Q4ASr+zX6QmoJVXNQQKBgQCiCEBzCpcqiuVNq37sACU1x5jVSzibNe15Dg8w8Og4IJbSVreddreLtDoyr3i01kFdiGEOJMAyk/RRDgdSRwVbUFbgAWAZiNMKF5/0SBf2yPNRTQZMndxd9lnO5sX3YYWXUmHkz6Vz+lRlTziyu1GkNRy29I8eALV41lMbdeepNQKBgQC3TqhXuHY8WRuSBMdtVktK+LVLMrsmQspioM7385lRld0UdUBGzfFpYexhxXxEFS0cKlzt8SSpKSBPs26kQ4JcRhMeZ1ywMoT2q0iFrrXck7qMXZX4FIPiVssOxUy1fx6ALuH4JgWkIFEL0RCVRSW2iUxU6bkEbIwdDXn0kL+eKQ==";
		String merchantNo = "OPR:10027488981";
		YopRequest yoprequest = new YopRequest(merchantNo, priavateKey, baseUrl);

//        YopRequest yoprequest = new YopRequest("OPR:" + getMerchantNo());
//        yoprequest.setSignAlg("RSA");

        Set<Entry<String, Object>> entry = map.entrySet();
        for (Entry<String, Object> s : entry) {
        	System.out.println("key--value:" + s.getKey() + "--" +  s.getValue());
            yoprequest.addParam(s.getKey(), s.getValue());
        }
        System.out.println("yoprequest:" + yoprequest.getParams());

//        YopResponse yopresponse = YopRsaClient.post(Uri, yoprequest);

		YopResponse yopresponse = YopClient3.postRsa(uri, yoprequest);
		System.out.println("请求YOP之后的结果head：state" + yopresponse.getState());
		if(yopresponse.getError() != null){
			System.out.println("请求YOP之后的结果head：state" + yopresponse.getError().getCode());
		}
        System.out.println("请求YOP之后的结果Result：" + yopresponse.getStringResult());

//        	对结果进行处理
        if ("FAILURE".equals(yopresponse.getState())) {
            if (yopresponse.getError() != null) {
                result.put("errorcode", yopresponse.getError().getCode());
                result.put("errormsg", yopresponse.getError().getMessage());

                System.err.println("错误明细：" + yopresponse.toString());
                result.putAll(erresult);
                System.out.println("系统处理异常结果：" + result);
            }

            return result;
        }
        //成功则进行相关处理
        if (yopresponse.getStringResult() != null) {
            result = parseResponse(yopresponse.getStringResult());
        }

        return result;
    }

	public static void queryBalanceTest() {

		YeepayTest yeepayService = new YeepayTest();
		Map<String, Object> params = new HashMap<>();

		try {
			Map<String, Object> jsonMap = yeepayService.yeepayYOP(params,
					"/rest/v1.0/balance/query_customer_amount");

			String errorCode = (String) jsonMap.get("errorCode");
			String errorMsg = (String) jsonMap.get("errorMsg");
			logger.info("易宝通道--代付代发--查询--errorCode：" + errorCode + "-"
					+ errorMsg);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
    	
    	
    	
    //将获取到的response转换成json格式
    public static Map<String, Object> parseResponse(String yopresponse) {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap = JSON.parseObject(yopresponse,
                new TypeReference<TreeMap<String, Object>>() {
                });
        System.out.println("将response转化为map格式之后: " + jsonMap);
        return jsonMap;
    }

    public static String getRandom(int length) {
        Random random = new Random();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < length; i++) {
            ret.append(Integer.toString(random.nextInt(10)));
        }
        System.out.println("ret.toString(): " + ret.toString());
        return ret.toString();
    }

}

        	

        

