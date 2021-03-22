package com.jrmf.splitorder.service;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.SplitOrderConf;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.SplitOrderConfDao;
import com.jrmf.splitorder.controller.SplitOrderController;
import com.jrmf.splitorder.domain.BaseOrderInfo;
import com.jrmf.splitorder.domain.CustomSplitOrder;
import com.jrmf.splitorder.domain.ReturnCode;
import com.jrmf.splitorder.domain.SplitFailOrder;
import com.jrmf.splitorder.thread.ExportExcel;
import com.jrmf.splitorder.thread.SplitOrderResultWorker;
import com.jrmf.splitorder.thread.SplitOrderWorker;
import com.jrmf.splitorder.util.ExcelExportUtil;
import com.jrmf.splitorder.util.ReadExcelUtil;
import com.jrmf.splitorder.util.SplitOrderThreadPool;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.FutureTask;

@Service
public class SplitOrderServiceImpl extends BaseController implements SplitOrderService {
    private static final Logger logger = LoggerFactory.getLogger(SplitOrderController.class);

    @Autowired
    private ReadExcelUtil readExcelUtil;
    @Autowired
    private SplitOrderCustomLimitConfService customLimitConfServiceImpl;
    @Autowired
    private SplitOrderConfDao splitOrderConfDao;
    @Autowired
    private CustomService customService;
    @Autowired
    private CustomSplitOrderService customSplitOrderService;
    @Autowired
    private CustomSplitSuccessOrderService customSplitSuccessOrderService;

    @Override
    public Map<String, Object> splitOrder(Workbook workBook, CustomSplitOrder customSplitOrder, Integer templateNo) {

        try {
            Map<String, Object> result = readExcelUtil.readExcelData(workBook, customSplitOrder.getCustomKey(), templateNo);
            List<UserCommission> userCommissions = (List<UserCommission>) result.get("attachment_0");
            logger.info("excel数据读取获取到 ======> {}条", userCommissions.size());


            //上传拆单文件总金额
            String totalAmount = "0.0";
            for (UserCommission userCommission : userCommissions) {
                totalAmount = ArithmeticUtil.addStr(totalAmount, userCommission.getAmount());
            }

            customSplitOrder.setTotalNumber(userCommissions.size());
            customSplitOrder.setTotalAmount(totalAmount);

            String splitOrderLimit = customService.getCustomByCustomkey(customSplitOrder.getCustomKey()).getSplitOrderLimit();
            if (splitOrderLimit != null && !"".equals(splitOrderLimit)) {
                //查询今天该商户已经拆单成功的金额
                String toDayAmount = customSplitOrderService.selectToDayAmountByCustomKey(customSplitOrder.getCustomKey());
                //商户日限额剩余金额
                String remainSplitOrderLimit = ArithmeticUtil.subStr(splitOrderLimit, toDayAmount);

                //商户日限额剩余金额小于该次拆单金额，直接置为失败订单
                if (ArithmeticUtil.compareTod(remainSplitOrderLimit, totalAmount) < 0) {
                    SplitFailOrder splitFailOrder = new SplitFailOrder();
                    splitFailOrder.setCustomKey(customSplitOrder.getCustomKey());
                    splitFailOrder.setData(userCommissions);
                    String failFileUrl = ExcelExportUtil.exportFail(splitFailOrder, customSplitOrder.getSplitOrderNo(), templateNo);
                    customSplitOrder.setFailNumber(userCommissions.size());
                    customSplitOrder.setFailAmount(totalAmount);
                    customSplitOrder.setFailFileName(customSplitOrder.getSplitOrderName() + "未拆单失败文件.xlsx");
                    customSplitOrder.setFailFileUrl(failFileUrl);
                    customSplitOrder.setStatus(2);
                    customSplitOrder.setStatusDesc("商户日限额不足,今日已拆成功" + toDayAmount + ",当前商户日限额" + splitOrderLimit);
                    customSplitOrderService.updateBySplitOrderNo(customSplitOrder);
                    return getReturnMap(ReturnCode.PARAM_ERROR);
                }
            }

            // 将导入的数据分割成若干份。并行处理。设置同步点，统一导出。
            List<List<UserCommission>> lists = StringUtil.averageAssign2(userCommissions, CommonString.CONCURRENTLIMIT);
            // 查询到这个商户配置的所有服务公司月下发额配置信息
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("customKey", customSplitOrder.getCustomKey());
            List<SplitOrderConf> splitOrderConfs = splitOrderConfDao.listSplitOrderConf(queryParams);

            //用于身份证重复时，累计限额计算
            Map<String, String> splitAmountSum = new ConcurrentHashMap<>();
            // 阻塞主线程，直到多有拆单线程执行完之后。将数据导出excel
            CyclicBarrier cb = new CyclicBarrier(lists.size(), new ExportExcel(splitOrderConfs, customService, templateNo, splitOrderConfDao, customSplitOrder, customSplitOrderService, customSplitSuccessOrderService));
            for (List<UserCommission> originalData : lists) {
                FutureTask<List<BaseOrderInfo>> splitTask = new FutureTask<>(new SplitOrderWorker(originalData, splitOrderConfs, customLimitConfServiceImpl,companyService, splitAmountSum));
                // 执行拆单
                SplitOrderThreadPool.cashThreadPool.execute(splitTask);
                // 异步等待结果以及结果的处理
                SplitOrderThreadPool.cashThreadPool.execute(new SplitOrderResultWorker(splitTask, customSplitOrder, customSplitOrderService, cb));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            customSplitOrder.setStatus(2);
            customSplitOrder.setStatusDesc("拆单异常,请联系工作人员");
            customSplitOrderService.updateBySplitOrderNo(customSplitOrder);
        }

        return getReturnMap(ReturnCode.SUCCESS);
    }

}
