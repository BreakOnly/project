package com.jrmf.splitorder.thread;

import com.jrmf.domain.UserCommission;
import com.jrmf.splitorder.domain.BaseOrderInfo;
import com.jrmf.splitorder.domain.CustomSplitOrder;
import com.jrmf.splitorder.domain.SplitFailOrder;
import com.jrmf.splitorder.domain.SplitSuccessOrder;
import com.jrmf.splitorder.service.CustomSplitOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class SplitOrderResultWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SplitOrderResultWorker.class);
    private FutureTask<List<BaseOrderInfo>> task;
    private CyclicBarrier cb;
    private CustomSplitOrder customSplitOrder;
    private CustomSplitOrderService customSplitOrderService;

    public SplitOrderResultWorker(FutureTask<List<BaseOrderInfo>> task, CustomSplitOrder customSplitOrder, CustomSplitOrderService customSplitOrderService, CyclicBarrier cb) {
        this.task = task;
        this.customSplitOrder = customSplitOrder;
        this.cb = cb;
        this.customSplitOrderService = customSplitOrderService;
    }

    public static void main(String[] args) {
        List<BaseOrderInfo> list = new ArrayList<>();
        UserCommission userCommission1 = new UserCommission();
        userCommission1.setAmount("1");
        UserCommission userCommission2 = new UserCommission();
        userCommission2.setAmount("2");

        List<UserCommission> dataSuccess = new ArrayList<>();
        dataSuccess.add(userCommission1);
        dataSuccess.add(userCommission2);
        List<UserCommission> dataFail = new ArrayList<>();
        dataFail.add(userCommission1);
        dataFail.add(userCommission2);

        SplitSuccessOrder splitSuccessOrder = new SplitSuccessOrder();
        splitSuccessOrder.setData(dataSuccess);

        SplitFailOrder splitFailOrder = new SplitFailOrder();
        splitFailOrder.setData(dataFail);
        list.add(splitSuccessOrder);
        list.add(splitFailOrder);

        CacheOriginalData.put("123", list);
        CacheOriginalData.put("123", list);
        List<BaseOrderInfo> baseOrderInfos = CacheOriginalData.get("123");
        for (BaseOrderInfo baseOrderInfo : baseOrderInfos) {
            if (baseOrderInfo instanceof SplitSuccessOrder) {
                System.out.println("??????????????????");
                SplitSuccessOrder a = (SplitSuccessOrder) baseOrderInfo;
                List<UserCommission> data = a.getData();
                for (UserCommission datum : data) {
                    System.out.println(datum.getAmount());
                }
            } else if (baseOrderInfo instanceof SplitFailOrder) {
                System.out.println("??????????????????");
                SplitFailOrder a = (SplitFailOrder) baseOrderInfo;
                List<UserCommission> data = a.getData();
                for (UserCommission datum : data) {
                    System.out.println(datum.getAmount());
                }
            }

        }
    }

    @Override
    public void run() {
        try {
            //????????????????????????
            List<BaseOrderInfo> baseOrderInfos = task.get(60, TimeUnit.SECONDS);
            boolean put = false;
            if (baseOrderInfos != null && baseOrderInfos.size() != 0) {
                //???????????????????????????  serialNo???key???
                put = CacheOriginalData.put(customSplitOrder.getSplitOrderNo(), baseOrderInfos);
            }
            logger.info("???????????? {} ??????????????????????????? {} ???,?????????????????????{}", Thread.currentThread().getName(), baseOrderInfos.size(), put ? "??????" : "??????");
            //??????CyclicBarrier???????????????
            cb.await();

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            customSplitOrder.setStatus(2);
            customSplitOrder.setStatusDesc("????????????????????????,???????????????");
            customSplitOrderService.updateBySplitOrderNo(customSplitOrder);
        }
    }
}
