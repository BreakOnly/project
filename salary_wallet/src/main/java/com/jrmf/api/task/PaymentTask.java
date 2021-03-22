package com.jrmf.api.task;

import static com.xxl.job.core.biz.model.ReturnT.FAIL_CODE;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.LdCommissionBusinessTypeEnum;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/4 10:56
 * Version:1.0
 */
@Component("paymentTask")
public class PaymentTask {

    private Logger logger = LoggerFactory.getLogger(PaymentTask.class);

    private static final Lock lock = new ReentrantLock();

    @Autowired
    private UserCommissionService userCommissionService;
    @Autowired
    private ChannelRelatedDao channelRelatedDao;
    @Autowired
    private UtilCacheManager utilCacheManager;
    @Autowired
    private BaseInfo baseInfo;
    @Autowired
    private UserSerivce userSerivce;
    @Autowired
    private TransferDealStatusNotifier transferDealStatusNotifier;
    @Autowired
    private CustomLimitConfService customLimitConfService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CustomBalanceService customBalanceService;
    @Autowired
    private LdOrderStepService ldOrderStepService;
    @Autowired
    private ForwardCompanyAccountService forwardCompanyAccountService;
    @Autowired
    private UsersAgreementService usersAgreementService;

    public static final String PROCESS = "process";

    /**
     * Author Nicholas-Ning
     * Description //TODO API下发明细结果查询定时（串行化执行）
     * Date 10:57 2018/12/4
     * Param []
     * return void
     **/
    @XxlJob("apiCommissionTask")
    public ReturnT<String> apiCommissionTask(String args) {
        try {
            String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            lock.lock();
            MDC.put(CommonString.PROCESS, processId);
            logger.info("----------------API下发结果查询定时任务开始----------------");
            List<UserCommission> list = userCommissionService.getApiListByTypeAndStatusOnJob(3, "2,3,4");
            if(list.size()>0){
                List<List<UserCommission>> averageAssign = StringUtil.averageAssign(list, 2);
                CountDownLatch countDownLatch = new CountDownLatch(averageAssign.size());
                for (int i = 0; i < averageAssign.size(); i++) {
                    ThreadUtil.cashThreadPool.execute(new ApiTaskImpl(countDownLatch, processId, baseInfo, averageAssign.get(i),
                            userCommissionService,channelRelatedDao, companyService, userSerivce, utilCacheManager,
                            transferDealStatusNotifier, customLimitConfService,customBalanceService,forwardCompanyAccountService,usersAgreementService));
                }
                countDownLatch.await();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return new ReturnT<>(FAIL_CODE, e.toString());
        }finally {
            logger.info("----------------API下发结果查询定时任务结束----------------");
            MDC.remove(CommonString.PROCESS);
            lock.unlock();
        }

        return ReturnT.SUCCESS;
    }

    /**
     * 联动拆单明细维护
     */
    @XxlJob("apiSplitBankPayInitLdStatusTask")
    public ReturnT<String> apiSplitBankPayInitLdStatusTask(String args) {
        try {
            String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            MDC.put(PROCESS, processId);

            logger.info("api拆单联动交易-明细维护--状态定时任务开始");
            lock.lock();
            List<UserCommission> list = userCommissionService
                .getLdListByTypeAndStatusAndBusinessTypeOnJob(3, "2,3,4",
                    LdCommissionBusinessTypeEnum.B2CSPLIT.getCode());
            logger.info("api拆单联动交易--明细z维护--状态定时任务开始---数量：" + list.size());
            if (list.size() > 0) {
                List<List<UserCommission>> averageAssign = StringUtil.averageAssign(list, 2);
                for (int i = 0; i < averageAssign.size(); i++) {
                    String subProcessId = processId + "--" + i;
                    ThreadUtil.cashThreadPool.execute(
                        new ExecuteSplitOrderLdQuery(subProcessId, averageAssign.get(i), userCommissionService,
                            companyService, utilCacheManager, customLimitConfService, ldOrderStepService, customBalanceService, baseInfo,
                            transferDealStatusNotifier,userSerivce,forwardCompanyAccountService));
                }
            }
            logger.info("api拆单联动交易--明细--状态定时任务结束");
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            return new ReturnT<>(FAIL_CODE, e.toString());
        } finally {
            MDC.remove(PROCESS);
            lock.unlock();
        }

        return ReturnT.SUCCESS;
    }
}
