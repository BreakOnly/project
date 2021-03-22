package com.jrmf.api;

import com.jrmf.bankapi.SubmitTransferParams;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.*;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.service.*;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.transaction.TransactionRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.CollectionUtils;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途： 作者：郭桐宁 时间：2018/12/3 20:51 Version:1.0
 */
@Service("paymentApiImpl")
public class PaymentApiImpl extends BasePayment<SubmitTransferParams> implements PaymentApi {

    private Logger logger = LoggerFactory.getLogger(PaymentApiImpl.class);

    private static Map<String, PaymentConfig> paymentConfigCacheTable = new ConcurrentHashMap<String, PaymentConfig>();

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserCommissionDao userCommissionDao;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    private JmsTemplate providerJmsTemplate;

    @Autowired
    private Destination transferDealRequestDestination;
    @Autowired
    private CustomLimitConfService customLimitConfService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private CustomBalanceService customBalanceService;
    @Autowired
    private ForwardCompanyAccountService forwardCompanyAccountService;
    @Autowired
    private CustomCompanyRateConfService rateConfService;

    /**
     * Author Nicholas-Ning Description //TODO 交易接口 Date 10:03 2018/12/4 Param
     * [transferParam] return
     * com.jrmf.payment.util.PaymentReturn<java.lang.String>
     **/
    @Override
    public PaymentReturn<String> transfer(UserCommission transferParam, ChannelRelated related,
                                          boolean isBatchPayment) {
        PaymentReturn<String> paymentReturn = null;

        try {

            if (ArithmeticUtil.compareTod(transferParam.getAmount(), "0") != 1 || ArithmeticUtil
                .compareTod(transferParam.getAmount(), transferParam.getSourceAmount()) == 1) {
                return new PaymentReturn<>(ApiReturnCode.FAILURE.getCode(),
                    RespCode.DEDUCT_AMOUNT_ERROR);
            }

            Company company = companyService.getCompanyByUserId(Integer.parseInt(related.getCompanyId()));
        	//真实下发公司id
        	String realCompanyId = company.getRealCompanyId();
            if(StringUtil.isEmpty(realCompanyId)){
        		realCompanyId = related.getCompanyId();
        	}
            boolean forwardCompany = ((1 == company.getCompanyType()) && !String.valueOf(company.getUserId()).equals(company.getRealCompanyId()));
            CustomCompanyRateConf customCompanyMinRate = null;
            if (forwardCompany){//判断记账户是否失效
                ForwardCompanyAccount queryCompanyAccount = new ForwardCompanyAccount(transferParam.getOriginalId(),String.valueOf(company.getUserId()),realCompanyId);
                List<ForwardCompanyAccount> forwardCompanyAccounts = forwardCompanyAccountService.findBalanceByCondition(queryCompanyAccount);
                ForwardCompanyAccount forwardCompanyAccount = forwardCompanyAccounts.get(0);
                int companyAccountStatus = forwardCompanyAccount.getStatus();
                if (2 == companyAccountStatus){
                    return new PaymentReturn<>(ApiReturnCode.REAL_COMPANY_ACCOUNT_INVALID.getCode(),
                            ApiReturnCode.REAL_COMPANY_ACCOUNT_INVALID.getDesc());
                }
                //判断是否配置转包费率
                customCompanyMinRate = rateConfService.getCustomCompanyMinRate(String.valueOf(company.getUserId()), realCompanyId);
                if (customCompanyMinRate == null){
                    return new PaymentReturn<>(ApiReturnCode.FAILURE.getCode(), RespCode.COMPANY_NOT_RATE_CONFIG);
                }
            }

            PaymentConfig paymentConfig = getPaymentConfig(String.valueOf(transferParam.getPayType()),
                    transferParam.getOriginalId(), related.getCompanyId() , realCompanyId);
            logger.info("related {} paymentConfig {}", related, paymentConfig);
            transferParam.setRealCompanyId(realCompanyId);
            if (paymentConfig != null && !PaymentFactory.YMFWSPAY.equals(paymentConfig.getPathNo())) {

                String subAcctNo = null;
                if (paymentConfig.getIsSubAccount() != null && paymentConfig.getIsSubAccount() == 1 && PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo())) {
                    CustomReceiveConfig receiveConfig = customReceiveConfigService.getCustomReceiveConfig(transferParam.getOriginalId(), transferParam.getCompanyId(), transferParam.getPayType());
                    if (receiveConfig != null) {
                        subAcctNo = receiveConfig.getReceiveAccount();
                        logger.info("------子账号下发开始：{}", subAcctNo);
                    }
                }

                ChannelCustom custom = customService.getBusinessInfoByCustomkey(transferParam.getOriginalId());
                transferParam.setBusinessManager(custom.getBusinessManager());
                transferParam.setBusinessPlatform(custom.getBusinessPlatform());
                transferParam.setBusinessChannel(custom.getBusinessChannel());
                transferParam.setBusinessChannelKey(custom.getBusinessChannelKey());
                transferParam.setCustomLabel(custom.getCustomLabel());
                transferParam.setSubAcctNo(subAcctNo);

                if (transactionRunner.runTransaction(TransactionDefinition.PROPAGATION_REQUIRED, null, paramContext -> {
                    userCommissionDao.addUserCommission(transferParam);
                    // 批次号为空则是api传过来的参数，需要在这里扣减余额。
                    if (!isBatchPayment) {
                        customBalanceService.updateCustomBalance(CommonString.DEDUCTION,
                            new CustomBalanceHistory(transferParam.getOriginalId(),
                                transferParam.getCompanyId(), transferParam.getPayType(),
                                ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2),
                                1, TradeType.APIPAYMENT.getCode(),transferParam.getOrderNo(),"api"));
                        logger.info("余额扣减成功！单号：{}", transferParam.getOrderNo());

                        if (forwardCompany){//转包服务公司
                            logger.info("转包服务公司扣减记账户余额，单号：{}", transferParam.getOrderNo());
                            //更新转包服务公司在实际服务公司记账户余额
                            CompanyAccountVo companyAccountVo = new CompanyAccountVo();
                            //String changeAmount = ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2);
                            companyAccountVo.setBalance(transferParam.getAmount());
                            companyAccountVo.setCustomKey(transferParam.getOriginalId());
                            companyAccountVo.setCompanyId(transferParam.getCompanyId());
                            companyAccountVo.setRealCompanyId(transferParam.getRealCompanyId());
                            companyAccountVo.setTradeType(TradeType.APIPAYMENT.getCode());
                            companyAccountVo.setRelateOrderNo(transferParam.getOrderNo());
                            companyAccountVo.setOperator("api");
                            companyAccountVo.setAmount(1);
                            companyAccountVo.setOperating(CommonString.DEDUCTION);
                            companyAccountVo.setOperator("api");
                            APIResponse apiResponse = forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);
                            if(apiResponse.getState() != 1){
                                throw new RuntimeException("记账户扣款失败");
                            }
                        }
                    }

                    //1.判断是否为联动交易
                    customLimitConfService.updateCustomPaymentTotalAmount(transferParam.getCompanyId(),
                            transferParam.getOriginalId(),
                            transferParam.getCertId(),
                            transferParam.getSourceAmount(),
                            true);
                    logger.info("订单号："+transferParam.getOrderNo()+"更新累计金额：本次累计{}元", transferParam.getSourceAmount());
                })) {
                    logger.info("------------扣减余额成功------------");
                } else {
                    logger.error("余额扣减失败！单号：{}", transferParam.getOrderNo());
                    return new PaymentReturn<>(ApiReturnCode.DEDUCTION_FAILURE.getCode(),
                            ApiReturnCode.DEDUCTION_FAILURE.getDesc());
                }

                if (forwardCompany){
                    logger.info("转包服务公司扣减余额2，单号：{}", transferParam.getOrderNo());
                    //更新转包服务公司在实际服务公司的余额
                    try{
                        String serviceFee = "0";
                        if (ServiceFeeType.ISSUE.getCode() == customCompanyMinRate.getServiceFeeType() || ServiceFeeType.PERSON.getCode() == customCompanyMinRate.getServiceFeeType()){
                            String customRate = customCompanyMinRate.getCustomRate();
                            serviceFee = ArithmeticUtil.mulStr(transferParam.getAmount(),customRate);
                        }
                        CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(transferParam.getCompanyId(),
                                transferParam.getRealCompanyId(), transferParam.getPayType(),
                                ArithmeticUtil.addStr(transferParam.getAmount(), serviceFee, 2),
                                1, TradeType.APIPAYMENT.getCode(),transferParam.getOrderNo(),
                                 "api");
                        customBalanceService.updateCustomBalance(CommonString.DEDUCTION, customBalanceHistory);
                    }catch (Exception e){
                        logger.error("扣减转包服务公司在实际下发公司余额异常"+e.getMessage(), e);
                    }
                }

                //兼容老appid模式
                if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())){
                    paymentConfig.setAppIdAyg(related.getAppIdAyg());
                }

                Company realCompany = companyService.getCompanyByUserId(
                    Integer.parseInt(realCompanyId));
                if (realCompany != null && realCompany.getServiceCompanyId() != null) {
                    paymentConfig.setServiceCompanyId(realCompany.getServiceCompanyId());
                }
                paymentReturn = new PaymentReturn<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getDesc(),
                        transferParam.getOrderNo());

                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setPaymentConfig(paymentConfig);
                paymentRequest.setUserCommission(transferParam);

                providerJmsTemplate.send(transferDealRequestDestination,
                    session -> session.createObjectMessage(paymentRequest));

            } else {
                logger.error("配置存在异常，无法上送交易。customKey{}", transferParam.getOriginalId());
                paymentReturn = new PaymentReturn<>(ApiReturnCode.FAILURE.getCode(), ApiReturnCode.FAILURE.getDesc());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return paymentReturn;
    }

    private PaymentConfig getPaymentConfig(String payType, String originalId, String companyId, String realCompanyId) {

//		String key = new StringBuilder(originalId).append(companyId).append(payType).toString();
//		PaymentConfig paymentConfig = paymentConfigCacheTable.get(key);
//		if (paymentConfig == null) {
//			synchronized (paymentConfigCacheTable) {
//				paymentConfig = paymentConfigCacheTable.get(key);
//				if (paymentConfig == null) {
//					paymentConfig = companyService.getPaymentConfigInfo(payType, originalId, companyId);
//					paymentConfigCacheTable.put(key, paymentConfig);
//				}
//			}
//		}
        PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(payType, originalId, companyId, realCompanyId);//后台切换通道时手动改库，暂时取消缓存

        return paymentConfig;
    }

    @Override
    public PaymentReturn<UserCommission> getLocalResult(String orderNo) {
        UserCommission commByOrderNo = userCommissionDao.getCommByOrderNo(orderNo);
        PaymentReturn<UserCommission> paymentReturn = null;
        try {
            if (commByOrderNo == null) {
                // 无该笔订单
                paymentReturn = new PaymentReturn<>(ApiReturnCode.NOT_SUCH_ORDER.getCode(),
                        ApiReturnCode.NOT_SUCH_ORDER.getDesc());
            } else {
                paymentReturn = new PaymentReturn<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getDesc());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            paymentReturn = new PaymentReturn<>(ApiReturnCode.FAILURE.getCode(), ApiReturnCode.FAILURE.getDesc());
        } finally {
            paymentReturn.setAttachment(commByOrderNo);
        }
        return paymentReturn;
    }

}
