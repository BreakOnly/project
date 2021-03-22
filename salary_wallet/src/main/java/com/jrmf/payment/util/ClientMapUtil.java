package com.jrmf.payment.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jrmf.payment.openapi.OpenApiClient;

public class ClientMapUtil {

	//爱员工连接client
	public static Map<String, OpenApiClient> httpClient = new ConcurrentHashMap<String, OpenApiClient>();

	//独立日连接client
	public static final Map<String, com.duliday.openapi.OpenApiClient> dulidayHttpClient = new ConcurrentHashMap<>();

	public static void main(String[] args){
//		httpClient.putIfAbsent("", null);
//		OpenApiClient sss = httpClient.get(null);
	}
	
}
