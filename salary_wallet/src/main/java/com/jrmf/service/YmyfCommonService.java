package com.jrmf.service;

import java.util.Map;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.UserCommission;

public interface YmyfCommonService {
	
	//溢美优付短信下发
    Map<String, Object> smsPay(ChannelInterimBatch interimBatch, String smsNo);
    //溢美优付预下单
    Map<String, String> prePay(ChannelInterimBatch interimBatch, String phone);
    //溢美优付订单查询
    Map<String, Object> smsPayResultQuery(ChannelInterimBatch interimBatch,String orderNo);
    //溢美优付批次查询
    Map<String, Object> smsPayResultBatchQuery(ChannelInterimBatch interimBatch);

}
