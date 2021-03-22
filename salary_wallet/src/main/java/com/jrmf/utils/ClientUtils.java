/**
 * 
 */
package com.jrmf.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

/**
 * filename：com.jrmf.utils.ClientUtils.java
 * 
 * @author: linan
 * @time: 2015-9-16 下午3:44:57
 */
public class ClientUtils {
	/**
	 * 获得客户端ip
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientip(HttpServletRequest request) {
		String fromip = request.getHeader("x-forwarded-for");
		if (StringUtil.hasNullStr(fromip) || "unknown".equalsIgnoreCase(fromip)) {
			fromip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtil.hasNullStr(fromip) || "unknown".equalsIgnoreCase(fromip)) {
			fromip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtil.hasNullStr(fromip) || "unknown".equalsIgnoreCase(fromip)) {
			fromip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtil.hasNullStr(fromip) || "unknown".equalsIgnoreCase(fromip)) {
			fromip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtil.hasNullStr(fromip) || "unknown".equalsIgnoreCase(fromip)) {
			fromip = request.getRemoteAddr();
		}
		// 多级代理
		if (fromip != null && fromip.length() > 15) { // "***.***.***.***".length()
			if (fromip.indexOf(",") > 0) {
				fromip = fromip.substring(0, fromip.indexOf(","));
			}
		}
		return fromip;
	}
	
	/**
	 * 获取本机ip
	 * @return
	 * @throws UnknownHostException 
	 */
	public static String getLocalIP() throws UnknownHostException {
		String ip =null;
	    ip = InetAddress.getLocalHost().getHostAddress();
	    return ip;
	}
}
