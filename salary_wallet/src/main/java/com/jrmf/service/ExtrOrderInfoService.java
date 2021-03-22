package com.jrmf.service;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ExtrOrderInfo;

/** 
* @author 种路路 
* @version 创建时间：2018年1月11日 上午10:20:46 
* 类说明 
*/
//@Service
public interface ExtrOrderInfoService {

	ExtrOrderInfo getOrderInfoByUserId(int id);

	void changeSignType(int id, int status);

	void addExtrOrderInfo(int id, String channelSerialno, int i, String customkey);

	void updateExtrOrderInfo(ExtrOrderInfo orderInfoByUserId);

}
 