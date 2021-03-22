package com.jrmf.api;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;

/**
 * Author Nicholas-Ning
 * Description //TODO 下发API 
 * Date 20:20 201/12/3
 **/
public interface PaymentApi {
    /**
     * Author Nicholas-Ning
     * Description //TODO 上送交易参数（扣款--上送--返回）
     * Date 20:31 2018/12/3
     * Param [userCommission]
     * return ReturnParam
     * @param channelWithContract 
     * @param isBatchPayment 
     **/
    PaymentReturn transfer(UserCommission transferParam, ChannelRelated channelWithContract, boolean isBatchPayment);
    /**
     * Author Nicholas-Ning
     * Description //TODO 获取本地交易记录明细状态
     * Date 13:47 2018/12/4
     * Param [transferParam]
     * return R
     **/
    PaymentReturn getLocalResult(String orderNo);

}
