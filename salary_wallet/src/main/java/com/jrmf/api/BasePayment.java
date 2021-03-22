package com.jrmf.api;

import com.jrmf.domain.UserCommission;

/**
 * Author Nicholas-Ning
 * Description //TODO 下发api实现抽象父类，封装下发实现类内部调用的方法
 * Date 20:51 2018/12/3
 * Param 
 * return 
 **/
public abstract class BasePayment<T> {
    /**
     * Author Nicholas-Ning
     * Description //TODO 获取下发参数模板（根据参数获取对应的下发通道的参数对象并封装）
     * Date 20:21 2018/12/3
     * Param [userCommission]
     * return TransferTemplate
     **/
    protected T getTransferTemplate(UserCommission userCommission){

        return null;
    }
}
