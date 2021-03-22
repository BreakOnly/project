package com.jrmf.controller.systemrole.merchant.payment;

import com.jrmf.domain.UserCommission;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/11/27 15:50
 * Version:1.0
 * @author guoto
 */
public class PaymentProxy implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(PaymentProxy.class);

    private Payment payment;

    private int maxSubmitTime;

    private UtilCacheManager utilCacheManager;

    public PaymentProxy(Payment payment, int maxSubmitTime, UtilCacheManager utilCacheManager) {
        this.payment = payment;
        this.maxSubmitTime = maxSubmitTime;
        this.utilCacheManager = utilCacheManager;
        logger.info("下发代理初始化---------->maxSubmitTime{}",maxSubmitTime);
    }

    public Payment getProxy() {
        return (Payment) Proxy.newProxyInstance(payment.getClass().getClassLoader(),
                payment.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (method.getName().equals("queryTransferResult")) {
            String key2 = (String) args[0];
            result = method.invoke(payment, args);
            @SuppressWarnings("unchecked")
            PaymentReturn<TransStatus> returnResult = (PaymentReturn<TransStatus>) result;
            String retCode = returnResult.getRetCode();
            if(PayRespCode.RESP_SUCCESS.equals(retCode)){
                TransStatus attachment = returnResult.getAttachment();
                if (PayRespCode.RESP_TRANSFER_FAILURE.equals(attachment.getResultCode())) {
                    String key1 =(String) utilCacheManager.get(key2);
                    if (key1 != null) {
                        boolean removeKey1 = utilCacheManager.remove(key1) != null;
                        boolean removeKey2 = utilCacheManager.remove(key2) != null;
                        logger.info("------------------------>交易失败! 删除redis key[{}-{},{}-{}] ", key1,removeKey1, key2,removeKey2);
                    }
                }
            }
        }
        if (method.getName().equals("paymentTransfer")) {
            UserCommission params = (UserCommission) args[0];
            String transferInAccountName = params.getUserName();
            String transferAmount = params.getAmount();
            String transferInAccountNo = params.getAccount();
            String customKey = params.getOriginalId();
            String companyId = params.getCompanyId();
            String transferSerialNo = params.getOrderNo();
            /**
             * 这个值表示这笔交易是否需要进行防重复下发拦截 true:需要，false:不需要
             **/
            boolean couldRepeated = params.getRepeatcheck() == 1;
            logger.info("------------------------>下发参数上送前校验 ,验证{},参数[amount={},userName={},account={},customKey={},companyId={}]",
                    couldRepeated,transferAmount, transferInAccountName, transferInAccountNo, customKey, companyId);
            String key1 = transferInAccountName + "," + transferAmount + "," + transferInAccountNo + "," + customKey + "," + companyId;
            String key2 = transferSerialNo;
            /**
             * 设置一个姓名+金额+卡号+渠道key的redis key
             **/
            boolean setPayInfoSuccess = utilCacheManager.putIfAbsent(key1, "1",maxSubmitTime);
            if(couldRepeated){
                if(!setPayInfoSuccess){
                    long lastLife = utilCacheManager.getCacheLife(key1)/60;
                    logger.info("------------------------>重复下发,单号{}.剩余时间(分钟):[{}]",transferSerialNo,lastLife);
                    PaymentReturn<String> transferReturn = new PaymentReturn<>(PayRespCode.RESP_FAILURE,
                            (maxSubmitTime/60)+"分钟内，禁止重复下发。剩余:"+lastLife+"分钟",
                            transferInAccountNo );
                    return transferReturn;
                }
                logger.info("------------------------下发重复检测：通过--key1" + key1);
            }
            /**
             * 由于每次的订单号不一样，所以这里不可能失败。 true = utilCacheManager.putIfAbsent(key2, key1, maxSubmitTime) == 1;
             **/
            utilCacheManager.putIfAbsent(key2, key1, maxSubmitTime);
            result = method.invoke(payment, args);
            PaymentReturn<String> returnResult = (PaymentReturn<String>) result;
            if (!returnResult.getRetCode().equals(PayRespCode.RESP_SUCCESS)) {
                boolean delSuccess1 = utilCacheManager.remove(key1) != null;
                boolean delSuccess2 = utilCacheManager.remove(key2) != null;
                logger.info("------------------------>交易上送失败! 删除redis key[{}-{},{}-{}] ", key1, delSuccess1, key2, delSuccess2);
            }
        } else {
            result = method.invoke(payment, args);
        }
        return result;
    }
}
