package com.jrmf.api.task;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.util.HashMap;
import org.apache.commons.collections.CollectionUtils;
import org.ehcache.core.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/4 11:07
 * Version:1.0
 *
 * @author guoto
 */
public class ApiTaskImpl implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ApiTaskImpl.class);

    private CountDownLatch countDownLatch;

    private String processId;

    private BaseInfo baseInfo;

    private List<UserCommission> batchData;

    private UserCommissionService commissionService;

    private ChannelRelatedDao channelRelatedDao;

    private CompanyService companyService;

    private UserSerivce userSerivce;

    private UtilCacheManager utilCacheManager;

    private TransferDealStatusNotifier transferDealStatusNotifier;

    private CustomLimitConfService customLimitConfService;

    private CustomBalanceService customBalanceService;

    private ForwardCompanyAccountService forwardCompanyAccountService;

    private UsersAgreementService usersAgreementService;

    ApiTaskImpl(CountDownLatch countDownLatch, String processId,
    		BaseInfo baseInfo, List<UserCommission> batchData,
    		UserCommissionService commissionService,ChannelRelatedDao channelRelatedDao,
    		CompanyService companyService, UserSerivce userSerivce,
    		UtilCacheManager utilCacheManager, TransferDealStatusNotifier transferDealStatusNotifier,
                CustomLimitConfService customLimitConfService, CustomBalanceService customBalanceService,
                ForwardCompanyAccountService forwardCompanyAccountService,
        UsersAgreementService usersAgreementService) {
        this.countDownLatch = countDownLatch;
        this.processId = processId;
        this.baseInfo = baseInfo;
        this.batchData = batchData;
        this.commissionService = commissionService;
        this.channelRelatedDao = channelRelatedDao;
        this.companyService = companyService;
        this.userSerivce = userSerivce;
        this.utilCacheManager = utilCacheManager;
        this.customLimitConfService = customLimitConfService;
        this.transferDealStatusNotifier = transferDealStatusNotifier;
        this.customBalanceService = customBalanceService;
        this.forwardCompanyAccountService = forwardCompanyAccountService;
        this.usersAgreementService =usersAgreementService;
    }

    @Override
    public void run() {
        MDC.put(CommonString.PROCESS, processId);
        try{
            for (UserCommission batchDatum : batchData) {

                ChannelRelated channelRelated = channelRelatedDao.getRelatedByCompAndOrig(batchDatum.getOriginalId(), batchDatum.getCompanyId());
                PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(String.valueOf(batchDatum.getPayType()), batchDatum.getOriginalId(), batchDatum.getCompanyId(), batchDatum.getRealCompanyId(),batchDatum.getPathNo());
                if (paymentConfig != null) {
                    //兼容原有appid存储地址
                    if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())){
                        paymentConfig.setAppIdAyg(channelRelated.getAppIdAyg());
                    }
                } else {
                    logger.error("查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + batchDatum.getOriginalId() + "--服务公司ID：" + batchDatum.getCompanyId() + "-订单号：" + batchDatum.getOrderNo());
                    continue;
                }
                PaymentReturn<TransStatus> transferResult;
                if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(batchDatum.getOriginalId())) {//挡板有效
                    String failMessage = "交易失败";
                    String resultCode = PayRespCode.RESP_TRANSFER_FAILURE ;
                    String resultMsg = "交易失败";
                    String retCode = PayRespCode.RESP_SUCCESS;

                    String amount = batchDatum.getAmount();
                    amount = ArithmeticUtil.mulStr(amount, "100");
                    //偶数
                    if (Integer.parseInt(amount) % 2 == 0) {
                        resultCode = PayRespCode.RESP_TRANSFER_SUCCESS;
                        retCode = PayRespCode.RESP_TRANSFER_SUCCESS;
                        failMessage = "交易成功";
                        resultMsg = "交易成功";
                    }

                    TransStatus transStatusSandBox = new TransStatus(batchDatum.getOrderNo(), resultCode, resultMsg);
                    transferResult = new PaymentReturn(retCode, failMessage, transStatusSandBox);
                } else {

                    paymentConfig.setSubAcctNo(batchDatum.getSubAcctNo());

                    //调用支付通道工厂模式
                    Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
                    PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
                    Payment proxy = paymentProxy.getProxy();
                    transferResult = proxy.queryTransferResult(batchDatum.getOrderNo());
                }
                logger.info("查询下发操作执行结果----------：" + transferResult.toString());
                if (PayRespCode.RESP_SUCCESS.equals(transferResult.getRetCode())) {
                    TransStatus transStatus = transferResult.getAttachment();
                    logger.debug("查询结果---------orderNO:" + transStatus.getOrderNo() + "--resultCode:" + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
                    if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
                        batchDatum.setStatus(1);
                        batchDatum.setStatusDesc("成功");
                        Map<String, Object> stringObjectMap = userSerivce.addUserInfo(
                                batchDatum.getUserName(),
                                batchDatum.getDocumentType(),
                                batchDatum.getCertId(),
                                batchDatum.getUserNo(),
                                batchDatum.getPhoneNo(),
                                batchDatum.getOriginalId(),
                                batchDatum.getMerchantId(), "");
                        batchDatum.setUserId(stringObjectMap.get("userId") + "");
                        updateUserCommission(batchDatum);
                        transferDealStatusNotifier.notify(batchDatum.getOrderNo(), TransferStatus.TRANSFER_DONE, CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());
                        logger.info("交易成功！系统内部回调成功！");

                        if (batchDatum.getPayType() == PayType.PINGAN_BANK.getCode() && PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo()) && !StringUtil.isEmpty(batchDatum.getSubAcctNo()) && ArithmeticUtil.compareTod(batchDatum.getSumFee(), "0") > 0) {
                            logger.info("------------api下发成功联动扣收平安子账户余额,单号：{}---------------", batchDatum.getOrderNo());

                            //联动调账平安子账号余额,是否成功此处不做处理，只在跨行快付时使用，网商下发时已经联动上送服务费字段进行扣收
                            ThreadUtil.subAccountThreadPool.execute(() -> customBalanceService.updateSubAccountBalance(batchDatum.getOriginalId(), batchDatum.getCompanyId(), batchDatum.getPayType(), batchDatum.getSumFee(), CommonString.DEDUCTION, TradeType.SERVICEFEE, "api"));
                        }

                    } else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
                        batchDatum.setStatus(2);
                        String errorMsg = transStatus.getResultMsg();
                        if (errorMsg.contains("余额")) {
                            batchDatum.setStatusDesc("网络异常，请联系管理员");
                        }else if ("结算用户未在平台签约".equals(errorMsg)) {
                            String idNo = batchDatum.getCertId();
                            String companyId = batchDatum.getRealCompanyId();
                            String customKey = batchDatum.getOriginalId();
                            Map<String, Object> params = new HashMap<>();
                            params.put("originalId", customKey);
                            params.put("companyId", companyId);
                            params.put("certId", idNo);
                            params.put("signStatus", 4);
                            List<UsersAgreement> usersAgreements = usersAgreementService
                                .getUsersAgreementsByParams(params);
                            if (usersAgreements != null && usersAgreements.size() == 1) {
                                batchDatum.setStatusDesc(usersAgreements.get(0).getSignStatusDes());
                            } else {
                                batchDatum.setStatusDesc("验证通道异常");
                            }
                        } else {
                            String statusDesc = transStatus.getResultMsg();
                            if (statusDesc.length() > 200) {
                                statusDesc = statusDesc.substring(0, 200);
                            }
                            batchDatum.setStatusDesc(statusDesc);
                        }
                        int updateCount = updateUserCommission(batchDatum);
                        if (updateCount == 1) {
                            customBalanceService.updateCustomBalance(CommonString.ADDITION,
                                new CustomBalanceHistory(batchDatum.getOriginalId(),
                                    batchDatum.getCompanyId(), batchDatum.getPayType(),
                                    ArithmeticUtil.addStr(batchDatum.getAmount(), batchDatum.getSumFee(), 2),
                                    1, TradeType.PAYMENTREFUND.getCode(),
                                        batchDatum.getOrderNo(),"apiTaskJob"));
                            logger.info("退款成功！订单号{}", batchDatum.getOrderNo());

                            customLimitConfService.updateCustomPaymentTotalAmount(batchDatum.getCompanyId(),
                                    batchDatum.getOriginalId(),
                                    batchDatum.getCertId(),
                                    batchDatum.getSourceAmount(),
                                    false);
                            logger.info("更新累计金额：减去失败下发{}元", batchDatum.getSourceAmount());

                            String realCompanyId = batchDatum.getRealCompanyId();
                            if (!StringUtil.isEmpty(realCompanyId) && !batchDatum.getCompanyId().equals(realCompanyId)){//转包服务公司
                                //退转包服务公司在实际服务公司的记账户余额
                                CompanyAccountVo companyAccountVo = new CompanyAccountVo();
                                //String changeAmount = ArithmeticUtil.addStr(batchDatum.getAmount(), batchDatum.getSumFee(), 2);
                                companyAccountVo.setBalance(batchDatum.getAmount());
                                companyAccountVo.setCustomKey(batchDatum.getOriginalId());
                                companyAccountVo.setCompanyId(batchDatum.getCompanyId());
                                companyAccountVo.setRealCompanyId(batchDatum.getRealCompanyId());
                                companyAccountVo.setTradeType(TradeType.PAYMENTREFUND.getCode());
                                companyAccountVo.setRelateOrderNo(batchDatum.getOrderNo());
                                companyAccountVo.setOperator("apiTaskJob");
                                companyAccountVo.setAmount(1);
                                companyAccountVo.setOperating(CommonString.ADDITION);
                                forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);

                                //退转包服务公司在实际服务公司的余额
                                try{
                                    CustomBalanceHistory queryBalanceHistory = new CustomBalanceHistory();
                                    queryBalanceHistory.setTradeType(TradeType.APIPAYMENT.getCode());
                                    queryBalanceHistory.setRelateOrderNo(batchDatum.getOrderNo());
                                    queryBalanceHistory.setCustomKey(batchDatum.getCompanyId());
                                    queryBalanceHistory.setCompanyId(batchDatum.getRealCompanyId());
                                    List<CustomBalanceHistory> customBalanceHistories = customBalanceService.listCustomBalanceHistory(queryBalanceHistory);
                                    if (!CollectionUtils.isEmpty(customBalanceHistories)){
                                        String tradeAmount = customBalanceHistories.get(0).getTradeAmount();
                                        CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(batchDatum.getCompanyId(),
                                                batchDatum.getRealCompanyId(), batchDatum.getPayType(), tradeAmount,
                                                1, TradeType.PAYMENTREFUND.getCode(),batchDatum.getOrderNo(),
                                                "apiTaskJob");
                                        customBalanceService.updateCustomBalance(CommonString.ADDITION, customBalanceHistory);
                                    }
                                }catch (Exception e){
                                    logger.error("退回转包服务公司在实际下发公司余额异常"+e.getMessage(), e);
                                }
                            }
                        } else {
                            logger.error("退款失败！订单号{}", batchDatum.getOrderNo());
                        }
                        transferDealStatusNotifier.notify(batchDatum.getOrderNo(), TransferStatus.TRANSFER_FAILED, CommonRetCodes.UNCATCH_ERROR.getCode(), transStatus.getResultMsg());
                        logger.info("交易失败！系统内部回调成功");
                    } else {
                        logger.error("交易未完成---------" + transferResult.getRetCode() + "------" + transStatus.getResultMsg());
                    }
                } else {
                    logger.error("查询失败---------" + transferResult.getRetCode() + transferResult.getFailMessage());
                }
            }

        }catch (Exception e){
            logger.error("查询异常：{}",e.getMessage());
        }finally {
            countDownLatch.countDown();
            MDC.remove(CommonString.PROCESS);
        }

    }

    private int updateUserCommission(UserCommission batchDatum) {
        batchDatum.setPaymentTime(DateUtils.getNowDate());
        return commissionService.updateUserCommissionById(batchDatum);
    }

}
