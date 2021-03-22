package com.jrmf.splitorder.thread;

import com.jrmf.common.CommonString;
import com.jrmf.domain.SplitOrderConf;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.SplitOrderConfDao;
import com.jrmf.service.SplitOrderConfService;
import com.jrmf.splitorder.domain.*;
import com.jrmf.splitorder.service.CustomService;
import com.jrmf.splitorder.service.CustomSplitOrderService;
import com.jrmf.splitorder.service.CustomSplitSuccessOrderService;
import com.jrmf.splitorder.util.ExcelExportUtil;
import com.jrmf.splitorder.util.FileUtil;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportExcel implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExportExcel.class);
    private List<SplitOrderConf> splitOrderConfs;
    private CustomService customService;
    private Integer templateNo;
    private SplitOrderConfDao splitOrderConfDao;
    private CustomSplitOrder customSplitOrder;
    private CustomSplitOrderService customSplitOrderService;
    private CustomSplitSuccessOrderService customSplitSuccessOrderService;

    public ExportExcel(List<SplitOrderConf> splitOrderConfs, CustomService customService, Integer templateNo, SplitOrderConfDao splitOrderConfDao, CustomSplitOrder customSplitOrder, CustomSplitOrderService customSplitOrderService, CustomSplitSuccessOrderService customSplitSuccessOrderService) {
        this.splitOrderConfs = splitOrderConfs;
        this.customService = customService;
        this.templateNo = templateNo;
        this.splitOrderConfDao = splitOrderConfDao;
        this.customSplitOrder = customSplitOrder;
        this.customSplitOrderService = customSplitOrderService;
        this.customSplitSuccessOrderService = customSplitSuccessOrderService;
    }

    @Override
    public void run() {

        //更新拆单限额
        for (SplitOrderConf splitOrderConf : splitOrderConfs) {
            if (!StringUtil.isEmpty(splitOrderConf.getSplitOrderBalance())) {
                String splitOrderBalance = ArithmeticUtil.subZeroAndDot(splitOrderConf.getSplitOrderBalance());
                splitOrderConf.setSplitOrderBalance(splitOrderBalance);
            }
            splitOrderConfDao.updateSplitOrderConf(splitOrderConf);
            logger.info("服务公司 {} 剩余限额 {}", splitOrderConf.getCompanyId(), splitOrderConf.getSplitOrderBalance());
        }

        logger.info("Excel导出开始==========>");
        // 获取执行的结果
        List<BaseOrderInfo> baseOrderInfos = CacheOriginalData.get(customSplitOrder.getSplitOrderNo());
        // 将结果按照 商户+服务公司 分类
        Map<String, Object> result = sortResult(baseOrderInfos, splitOrderConfs);

        Integer totalSuccessNumber = 0;
        String totalSuccessAmount = "0.0";
        //将分类后的数据传入 导出工具类，进行导出
        for (Map.Entry<String, Object> stringObjectEntry : result.entrySet()) {
            Object value = stringObjectEntry.getValue();
            if (value instanceof SplitSuccessOrder) {
                SplitSuccessOrder successOrder = (SplitSuccessOrder) value;
                successOrder.setCustomName(customService.getNameByCustomKey(successOrder.getCustomKey()));
                successOrder.setCompanyName(customService.getNameByCustomKey(successOrder.getCompanyId()));
                String fileUrl = ExcelExportUtil.exportSuccess(successOrder, customSplitOrder.getSplitOrderNo(), templateNo);

                List<UserCommission> successData = successOrder.getData();
                if (successData != null && successData.size() != 0) {
                    CustomSplitSuccessOrder customSplitSuccessOrder = new CustomSplitSuccessOrder();
                    customSplitSuccessOrder.setSplitOrderNo(customSplitOrder.getSplitOrderNo());
                    customSplitSuccessOrder.setSplitOrderName(customSplitOrder.getSplitOrderName());
                    customSplitSuccessOrder.setCustomKey(successOrder.getCustomKey());
                    customSplitSuccessOrder.setCompanyId(successOrder.getCompanyId());

                    customSplitSuccessOrder.setTotalNumber(successData.size());
                    String successAmount = "0.0";
                    for (UserCommission userCommission : successData) {
                        successAmount = ArithmeticUtil.addStr(successAmount, userCommission.getAmount());
                    }
                    customSplitSuccessOrder.setTotalAmount(successAmount);

                    totalSuccessNumber += successData.size();
                    totalSuccessAmount = ArithmeticUtil.addStr(totalSuccessAmount, successAmount);

                    customSplitSuccessOrder.setFileName(customSplitOrder.getSplitOrderName() + successOrder.getCustomName() + "_" + successOrder.getCompanyName() + "拆单成功文件.xlsx");
                    customSplitSuccessOrder.setFileUrl(fileUrl);

                    customSplitSuccessOrderService.insert(customSplitSuccessOrder);
                }

            } else if (value instanceof SplitLaveOrder) {
                SplitLaveOrder laveOrder = (SplitLaveOrder) value;
                List<UserCommission> laveData = laveOrder.getData();
                if (laveData != null && laveData.size() != 0) {
                    String laveFileUrl = ExcelExportUtil.exportlave(laveOrder, customSplitOrder.getSplitOrderNo(), templateNo);

                    customSplitOrder.setLaveNumber(laveData.size());
                    String laveAmount = "0.0";
                    for (UserCommission userCommission : laveData) {
                        laveAmount = ArithmeticUtil.addStr(laveAmount, userCommission.getAmount());
                    }
                    customSplitOrder.setLaveAmount(laveAmount);

                    customSplitOrder.setLaveFileName(customSplitOrder.getSplitOrderName() + "拆单部分失败文件.xlsx");
                    customSplitOrder.setLaveFileUrl(laveFileUrl);
                }

            } else if (value instanceof SplitFailOrder) {
                SplitFailOrder failOrder = (SplitFailOrder) value;
                List<UserCommission> failData = failOrder.getData();
                if (failData != null && failData.size() != 0) {
                    String failFileUrl = ExcelExportUtil.exportFail(failOrder, customSplitOrder.getSplitOrderNo(), templateNo);

                    customSplitOrder.setFailNumber(failData.size());
                    String failAmount = "0.0";
                    for (UserCommission userCommission : failData) {
                        failAmount = ArithmeticUtil.addStr(failAmount, userCommission.getAmount());
                    }
                    customSplitOrder.setFailAmount(failAmount);

                    customSplitOrder.setFailFileName(customSplitOrder.getSplitOrderName() + "未拆单失败文件.xlsx");
                    customSplitOrder.setFailFileUrl(failFileUrl);
                }

            }
        }

        customSplitOrder.setSuccessNumber(totalSuccessNumber);
        customSplitOrder.setSuccessAmount(totalSuccessAmount);
        if (customSplitOrder.getFailNumber() == 0 && customSplitOrder.getLaveNumber() == 0) {
            customSplitOrder.setStatus(1);
            customSplitOrder.setStatusDesc("拆单成功");
        } else {
            customSplitOrder.setStatus(5);
            customSplitOrder.setStatusDesc("拆单部分成功");
        }

        customSplitOrderService.updateBySplitOrderNo(customSplitOrder);
        // 导出结束后清除掉本次请求所产生的数据
        CacheOriginalData.clear(customSplitOrder.getSplitOrderNo());
    }

    // 按照 customKey + companyId 将数据分类
    private Map<String, Object> sortResult(List<BaseOrderInfo> baseOrderInfos, List<SplitOrderConf> splitOrderConfs) {
        Map<String, Object> result = new HashMap<>();
        //循环服务公司拆单配置，创建用于存储拆单成功的各个SplitSuccessOrder对象，并存入result中，key值为customKey + companyId
        for (SplitOrderConf splitOrderConf : splitOrderConfs) {
            SplitSuccessOrder splitSuccessOrder = new SplitSuccessOrder();
            splitSuccessOrder.setCustomKey(splitOrderConf.getCustomKey());
            splitSuccessOrder.setCompanyId(splitOrderConf.getCompanyId());
            splitSuccessOrder.setData(new ArrayList<>());
            result.put(splitSuccessOrder.getCustomKey() + "-" + splitSuccessOrder.getCompanyId(), splitSuccessOrder);
        }

        //创建用于存储拆单失败的SplitFailOrder对象
        SplitFailOrder splitFailOrder = new SplitFailOrder();
        splitFailOrder.setCustomKey(splitOrderConfs.get(0).getCustomKey());
        splitFailOrder.setData(new ArrayList<>());

        //创建用于存储拆单不足的SplitLaveOrder对象
        SplitLaveOrder splitLaveOrder = new SplitLaveOrder();
        splitLaveOrder.setCustomKey(splitOrderConfs.get(0).getCustomKey());
        splitLaveOrder.setData(new ArrayList<>());

        result.put("fail", splitFailOrder);
        result.put("lave", splitLaveOrder);

        //循环拆单完的订单信息
        for (int i = 0; i < baseOrderInfos.size(); i++) {
            SplitSuccessOrder successData;
            SplitFailOrder failData;
            SplitLaveOrder laveData;
            BaseOrderInfo info = baseOrderInfos.get(i);
            //拆单成功订单
            if (info instanceof SplitSuccessOrder) {
                successData = (SplitSuccessOrder) info;
                //获取已存储在baseOrderInfos中的成功拆单数据
                List<UserCommission> data = successData.getData();
                //循环成功拆单数据
                for (UserCommission datum : data) {
                    String key = datum.getOriginalId() + "-" + datum.getCompanyId();
                    //获取result中已存储的key值为customKey + companyId的SplitSuccessOrder
                    SplitSuccessOrder splitSuccessOrder = (SplitSuccessOrder) result.get(key);
                    //把成功拆单数据插入SplitSuccessOrder
                    splitSuccessOrder.getData().add(datum);
                }
                //拆单不足订单
            } else if (info instanceof SplitLaveOrder) {
                laveData = (SplitLaveOrder) info;
                //获取已存储在baseOrderInfos中的拆单不足订单数据
                List<UserCommission> data = laveData.getData();
                //循环拆单不足订单数据
                for (UserCommission datum : data) {
                    //获取result中已存储的key值为lave 的splitLaveOrder
                    SplitLaveOrder lave = (SplitLaveOrder) result.get("lave");
                    //把拆单不足订单数据插入splitLaveOrder
                    lave.getData().add(datum);
                }
                //拆单失败订单
            } else if (info instanceof SplitFailOrder) {
                //获取已存储在baseOrderInfos中的拆单失败数据
                failData = (SplitFailOrder) info;
                //循环拆单失败的数据
                List<UserCommission> data = failData.getData();
                for (UserCommission datum : data) {
                    //获取result中已存储的key值为fail 的splitFailOrder
                    SplitFailOrder fail = (SplitFailOrder) result.get("fail");
                    //把拆单失败的数据插入splitFailOrder
                    fail.getData().add(datum);
                }
            }
        }
        return result;
    }
}
