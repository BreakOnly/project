package com.jrmf.utils;

import cn.emay.sdk.client.api.Client;


public class SmsSingletonClient {
	private static Client client = null;

	private SmsSingletonClient() {
	}

	public synchronized static Client getClient(String softwareSerialNo,
			String key) {
		if (client == null) {
			try {
				client = new Client(softwareSerialNo, key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}

	public synchronized static Client getClient() {
		if (client == null) {
			try {
				client = new Client("9SDK-EMY-0999-JBQPO","657300");
//				client = new Client("9SDK-EMY-0999-JBQPO","657300");  //发送验证码
//				client = new Client("3SDK-EMY-0130-JBRPP","217725");  //发送广告

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
}
