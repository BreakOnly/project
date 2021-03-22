package com.jrmf.service;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.payment.AccountSystemFactory;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.AccountSystem;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.utils.*;
import com.jrmf.utils.exception.BalanceException;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/11/19 10:35
 * Version:1.0
 *
 * @author guoto
 */
@Service("customBalanceService")
public class CustomBalanceServiceImpl implements CustomBalanceService {
    private static Logger logger = LoggerFactory.getLogger(CustomBalanceServiceImpl.class);
    @Autowired
    private CustomBalanceDao customBalanceDao;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private CustomTransferRecordService customTransferRecordService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private  DataDictionaryService dataDictionaryService;
    @Autowired
    private CustomBalanceHistoryService customBalanceHistoryService;
    @Autowired
    ForwardCompanyAccountService forwardCompanyAccountService;


    @Override
    public Integer queryBalance(Map<String, Object> param) {
        return customBalanceDao.queryBalance(param);
    }

    @Override
    public void initCustomBalance(Map<String, Object> param) {
        try {
            customBalanceDao.initCustomBalance(param);
        } catch (Exception e) {
            logger.error("插入错误：{}", e);
        }
    }

    @Override
    public List<CompanyAccount> queryCompanyAccount(Map<String, Object> param) {
        return customBalanceDao.queryCompanyAccount(param);
    }

    @Override
    @Transactional
    public boolean confirmBalance(ChannelHistory info, CustomTransferRecord record) {

        try {

            String customName = info.getCustomName().replaceAll("（","(").replaceAll("）",")");
            String oppAccountName = record.getOppAccountName().replaceAll("（","(").replaceAll("）",")");

            PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(info.getPayType()), info.getCustomkey(), info.getRecCustomkey());

            //收款账号 = 平安子账号  付款账号 = 平安对方账号 付款户名 = 对方户名 交易金额相等  自动确认
            if (info.getPayType() == PayType.PINGAN_BANK.getCode() &&
                customName.equals(oppAccountName) &&
                ((PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo()) && info.getInAccountNo()
                    .equals(record.getSubAccount())) ||
                    (PaymentFactory.PAYQZL.equals(paymentConfig.getPathNo()) && paymentConfig
                        .getShadowAcctNo().equals(record.getSubAccount())) ||
                    (PaymentFactory.MYBANK.equals(paymentConfig.getPathNo()) && info
                        .getInAccountNo().equals(record.getSubAccount())))) {

                DataDictionary upAmountDictionary = dataDictionaryService.getByDictTypeAndKey(DataDictionaryDictType.RECHARGE_CONFIRM_AMOUNT.getDictType(), DataDictionaryDictKey.UPAMOUNT.getDictKey());
                DataDictionary downAmountDictionary = dataDictionaryService.getByDictTypeAndKey(DataDictionaryDictType.RECHARGE_CONFIRM_AMOUNT.getDictType(), DataDictionaryDictKey.DOWNAMOUNT.getDictKey());


                String rechargeAmount = info.getRechargeAmount();
                String tranAmount = record.getTranAmount();
                String upAmount = info.getRechargeAmount();
                String downAmount = info.getRechargeAmount();

                if (upAmountDictionary != null && !StringUtil.isEmpty(upAmountDictionary.getDictValue()) && ArithmeticUtil.compareTod(upAmountDictionary.getDictValue(), "0") > 0 && ArithmeticUtil.compareTod(upAmountDictionary.getDictValue(), "1") < 1) {
                    upAmount = ArithmeticUtil.addStr(rechargeAmount, upAmountDictionary.getDictValue(), 2);
                }

                if (downAmountDictionary != null && !StringUtil.isEmpty(downAmountDictionary.getDictValue()) && ArithmeticUtil.compareTod(downAmountDictionary.getDictValue(), "0") > 0 && ArithmeticUtil.compareTod(downAmountDictionary.getDictValue(), "1") < 1) {
                    downAmount = ArithmeticUtil.subStr2(rechargeAmount, downAmountDictionary.getDictValue());
                }

                //用户提交打款金额与实际打款金额偏差金额
                String differentAmount = null;

                boolean amountSuccess = false;
                if (ArithmeticUtil.compareTod(tranAmount, downAmount) >= 0 && ArithmeticUtil.compareTod(upAmount, tranAmount) >= 0) {
                    amountSuccess = true;
                    differentAmount = ArithmeticUtil.subStr2(tranAmount, rechargeAmount);
                }

                if (amountSuccess) {
                    if (paymentConfig != null) {
                        logger.info("-----------订单号:{}与业务流水:{}，关键字{},打款备注{}---------", info.getOrderno(), record.getBizFlowNo(),paymentConfig.getKeyWords(),record.getRemark());
                        if (!StringUtil.isEmpty(paymentConfig.getKeyWords()) && (StringUtil.isEmpty(record.getRemark()) || !paymentConfig.getKeyWords().equals(record.getRemark()))) {
                            return false;
                        }

                        if (!StringUtil.isEmpty(paymentConfig.getContainKeyWords()) && (StringUtil.isEmpty(record.getRemark()) || !record.getRemark().contains(paymentConfig.getContainKeyWords()))) {
                            return false;
                        }
                    }

                    if (differentAmount != null && ArithmeticUtil.compareTod(differentAmount, "0") != 0) {
                        info.setAmount(ArithmeticUtil.addStr(info.getAmount(), differentAmount, 2));
                        info.setRemark("自动更正充值金额(" + differentAmount + ")");
                        info.setRechargeAmount(record.getTranAmount());
                    }
                    ArrayList<CustomTransferRecord> records = new ArrayList<CustomTransferRecord>();
                    records.add(record);
                    return manualConfirmBalance(info, records);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }


    @Transactional
    public int updateCustomBalanceAndSubBalance(String customKey, String companyId, Integer payType, String amount,
                             int operating, TradeType changeType, String operatorName, String refundOrderNo) {
        try {
            //操作商户或者转包服务公司余额
            CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory();
            customBalanceHistory.setCustomKey(customKey);
            customBalanceHistory.setCompanyId(companyId);
            customBalanceHistory.setPayType(payType);
            if (!StringUtil.isEmpty(refundOrderNo)){
                customBalanceHistory.setRemark("充值订单号:" + refundOrderNo + "退款");
            }
            customBalanceHistory.setTradeNumber(1);
            customBalanceHistory.setTradeType(changeType.getCode());
            customBalanceHistory.setTradeAmount(amount);
            if (StringUtil.isEmpty(refundOrderNo)){
                refundOrderNo = orderNoUtil.getChannelSerialno();
            }
            customBalanceHistory.setRelateOrderNo(refundOrderNo);
            customBalanceHistory.setOperator(operatorName);
            updateCustomBalance(operating,customBalanceHistory);

            //联动调账子账号余额,是否成功此处不做处理
            ThreadUtil.subAccountThreadPool.execute(() -> updateSubAccountBalance(customKey, companyId, payType, amount, operating, changeType, operatorName));
        }catch (BalanceException be){
            logger.error("商户{}，服务公司{}，余额更新失败",customKey,companyId);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        return 1;
    }

    @Override
    public String queryCustomBalance(String customKey, String companyId, Integer payType) {
        return customBalanceDao.queryCustomBalance(customKey, companyId, payType);
    }

    @Override
    @Transactional
    public boolean manualConfirmBalance(ChannelHistory info, List<CustomTransferRecord> records) {
        try {
            String customKey = info.getCustomkey();
            String companyId = info.getRecCustomkey();
            int payType = info.getPayType();
            String amount = info.getAmount();
            int count = 0;
            String transAmount = "0";
            StringBuilder transRemark = new StringBuilder();
            for (CustomTransferRecord record : records) {
                record.setIsConfirm(ConfirmStatus.SUCCESS.getCode());
                record.setConfirmOrderNo(info.getOrderno());
                record.setCustomKey(customKey);
                record.setCompanyId(companyId);
                record.setCurrentStatus(ConfirmStatus.FAILURE.getCode());
                count = customTransferRecordService.updateByPrimaryKey(record);

                transAmount = ArithmeticUtil.addStr(transAmount,record.getTranAmount());
                if (!StringUtil.isEmpty(record.getRemark())){
                    transRemark.append(" ").append(record.getRemark());
                }
            }

            String bizFlowNos = records.stream().map(CustomTransferRecord::getBizFlowNo).collect(Collectors.joining());
            if (count < 1) {
                logger.error("-------------异常转账流水,bizFlowNo:{}-------------", bizFlowNos);
                return false;
            }
            logger.info("-----------充值记录订单:{},匹配子账户流水号:{} 确认到账:{}---------", info.getOrderno(), bizFlowNos, transAmount);

            info.setStatus(RechargeStatusType.SUCCESS.getCode());
            info.setRealRechargeAmount(transAmount);
            info.setUnInvoiceAmount(transAmount);
            if (StringUtil.isEmpty(info.getRemark())) {
                info.setRemark(transRemark.toString());
            } else {
                info.setRemark(info.getRemark() + " " + transRemark.toString());
            }
            info.setCurrentStatus(RechargeStatusType.CONFIRMING.getCode());
            count = channelHistoryService.updateChannelHistory(info);
            if (count > 0) {
                if (info.getRechargeType() == RechargeType.AMOUNT.getCode()) {
                    //添加账户余额充值记录
                    CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory();
                    customBalanceHistory.setCustomKey(info.getCustomkey());
                    customBalanceHistory.setCompanyId(info.getRecCustomkey());
                    customBalanceHistory.setPayType(info.getPayType());
                    customBalanceHistory.setRemark(info.getRemark());
                    customBalanceHistory.setTradeNumber(1);
                    customBalanceHistory.setTradeType(TradeType.RECHARGE.getCode());
                    customBalanceHistory.setTradeAmount(info.getAmount());
                    customBalanceHistory.setRelateOrderNo(info.getOrderno());
                    customBalanceHistory.setOperator(info.getCompanyOperatorName());
                    updateCustomBalance(CommonString.ADDITION,customBalanceHistory);
                    logger.info("-----------充值记录订单:{}计入商户余额{}---------", info.getOrderno(), info.getAmount());
                }

                PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(payType), customKey, companyId);
                //增加跨行快付通道判断，切换为银企直联通道后不进行调账
                CustomTransferRecord record = records.get(0);
                if (paymentConfig != null && PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo())
                        && record.getSubAccount().equals(paymentConfig.getShadowAcctNo())
                        && CustomTransferRecordType.SUBACCOUNTINTO.getCode() == record.getTranType()) {
                    if (ArithmeticUtil.compareTod(amount, "0") > 0) {
                        //说明打款到了实体账户,这里从实体账户调账到充值的子账户
                        ThreadUtil.subAccountThreadPool.execute(() -> updateSubAccountBalance(customKey, companyId, payType, amount, CommonString.ADDITION, TradeType.RECHARGEINTO, info.getCompanyOperatorName()));
                    }
                } else {
                    if (StringUtil.isEmpty(info.getCompanyOperatorName())) {
                        info.setCompanyOperatorName("系统自动匹配");
                    }

                    subAccountBalanceDeduct(info);
                }
                return true;
            } else {
                logger.error("-------------异常充值记录,orderNo:{}-------------", info.getOrderno());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                //抛出RuntimeException回滚转账记录表的修改
//                throw new RuntimeException("-------------异常充值记录,orderNo:" + info.getOrderno() + "-------------");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 充值记录退款,更新记录状态,更新系统余额,如果是子账户模式联动更新平安余额
     *
     * @author linsong
     * @date 2019/10/11
     */
    @Override
    @Transactional
    public boolean rechargeRefund(ChannelHistory info, String amount, String operatorName) {

        logger.info("----------------开始进行充值记录退款,订单号：{}----------------------", info.getOrderno());

        info.setCurrentStatus(RechargeStatusType.SUCCESS.getCode());
        int count = channelHistoryService.updateChannelHistory(info);

        if (count > 0) {

            logger.info("---------------充值记录退款——原记录相关字段更新成功,订单号：{}------------------", info.getOrderno());

            count = updateCustomBalanceAndSubBalance(info.getCustomkey(), info.getRecCustomkey(), info.getPayType(), amount, CommonString.DEDUCTION, TradeType.RECHARGEREFUND, operatorName, info.getOrderno());
            if (count < 1) {

                logger.info("------------------充值记录退款——更新系统余额失败,订单号：{}-------------------", info.getOrderno());

                //同步商户余额失败,回滚充值记录
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            logger.info("------------------充值记录退款——更新系统余额成功,订单号：{}-------------------", info.getOrderno());
            return true;
        }

        logger.info("---------------充值记录退款——原记录相关字段更新失败,订单号：{}------------------", info.getOrderno());

        return false;
    }

    @Override
    public void updateSubAccountBalance(String customKey, String companyId, Integer payType, String amount, int operating, TradeType changeType, String operatorName) {
        CustomReceiveConfig receiveConfig = customReceiveConfigService.getCustomReceiveConfig(customKey, companyId, payType);

        if (receiveConfig.getIsSubAccount() == 1) {

            try {

                logger.info("---------------更新商户子账号余额    商户:{}, 下发公司:{}, 下发方式:{} ,操作金额：{}元 ,子账号：{}---------------", customKey, companyId, payType, amount, receiveConfig.getReceiveAccount());


                PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(payType.toString(), customKey, companyId);

                AccountSystem accountSystem = AccountSystemFactory.accountSystemEntity(paymentConfig);

                //银企直联使用子账号体系，但无法进行调账，所以这里进行一次校验防止银企直联使用调账功能
                if (accountSystem == null || PaymentFactory.PAYQZL.equals(paymentConfig.getPathNo())){
                    return;
                }

                CustomTransferRecord transferRecord = new CustomTransferRecord(receiveConfig, paymentConfig);

                CustomTransferRecordType type = CustomTransferRecordType.UNDERFINED;

                if (CommonString.ADDITION == operating) {
                    type = CustomTransferRecordType.ADJUSTMENTINTO;
                } else if (CommonString.DEDUCTION == operating) {
                    type = CustomTransferRecordType.ADJUSTMENTOUT;
                }

                transferRecord.setTranAmount(amount);
                transferRecord.setTranType(type.getCode());
                transferRecord.setFlag(type.getFlag());

                transferRecord.setIsConfirm(ConfirmStatus.Paying.getCode());

                transferRecord.setRemark(changeType.getDesc() + "(" + operatorName + ")");

                String date = DateUtils.formartDate(new Date(), "yyyyMMddHHmmss");

                transferRecord.setTranTime(date);

                ActionReturn<String> res = accountSystem
                    .subAccountSubmitTransfer(orderNoUtil.getChannelSerialno(), transferRecord);
                if (res.isOk()) {
                    String bizFlowNo = res.getAttachment();
                    transferRecord.setBizFlowNo(bizFlowNo);
                    logger.info("----------余额调账请求成功,业务流水号：{}----------", bizFlowNo);
                } else {
                    transferRecord.setIsConfirm(ConfirmStatus.PayFail.getCode());
                    logger.error("----------余额调账请求失败:----------" + res.getFailMessage());
                }

                customTransferRecordService.insert(transferRecord);

            } catch (Exception e) {
                logger.error("----------商户余额调账或退款同步子账号失败----------------");
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 扣减商户余额统一方法
     * 判断余额是否扣减成功需在外部方法catch BalanceException异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomBalance(int operating, CustomBalanceHistory balanceHistory) {
        Map<String, Object> params = new HashMap<>(5);

        if (ArithmeticUtil.compareTod(balanceHistory.getTradeAmount(), "0") < 0) {
            throw new BalanceException();
        }

        BigDecimal magnification = new BigDecimal(operating * 100);
        params.put(CommonString.CUSTOMKEY, balanceHistory.getCustomKey());
        params.put(CommonString.COMPANYID, balanceHistory.getCompanyId());
        params.put(CommonString.PAYTYPE, balanceHistory.getPayType());
        params.put(CommonString.BALANCE,
            new BigDecimal(balanceHistory.getTradeAmount()).multiply(magnification));

        String preTradeBalance = customBalanceDao
            .queryCustomBalanceForUpdate(balanceHistory.getCustomKey(),
                balanceHistory.getCompanyId(), balanceHistory.getPayType());
        balanceHistory.setPreTradeBalance(preTradeBalance);
        int updateCount = customBalanceDao.updateBalance(params);
        if (updateCount == 1) {
            if (CommonString.DEDUCTION == operating) {
                balanceHistory.setAfterTradeBalance(ArithmeticUtil
                    .subStr(preTradeBalance, balanceHistory.getTradeAmount(), 2));
            } else {
                balanceHistory.setAfterTradeBalance(ArithmeticUtil
                    .addStr(preTradeBalance, balanceHistory.getTradeAmount(), 2));
            }
            customBalanceHistoryService.insert(balanceHistory);
            logger.info(
                "----------系统余额变更成功 operating:[{}] {},customKey:{},companyId:{},payType:{},preTradeBalance:{},afterTradeBalance:{}-----------",
                operating, balanceHistory.getTradeAmount(), balanceHistory.getCustomKey(),
                balanceHistory.getCompanyId(), balanceHistory.getPayType(),
                balanceHistory.getPreTradeBalance(), balanceHistory.getAfterTradeBalance());
        } else {
            logger.error(
                "----------系统余额变更失败 operating:[{}] {},customKey:{},companyId:{},payType:{},preTradeBalance:{}-----------",
                operating, balanceHistory.getTradeAmount(), balanceHistory.getCustomKey(),
                balanceHistory.getCompanyId(), balanceHistory.getPayType(),
                balanceHistory.getPreTradeBalance());
            throw new BalanceException();
        }

    }

    private void subAccountBalanceDeduct(ChannelHistory info){

        if ((RechargeType.SERVICEAMOUNT.getCode() == info.getRechargeType() && ArithmeticUtil.compareTod(info.getRealRechargeAmount(), "0") > 0) || (RechargeType.AMOUNT.getCode() == info.getRechargeType() && ServiceFeeType.RECHARGE.getCode() == info.getServiceFeeType() && ArithmeticUtil.compareTod(info.getServiceFee(), "0") > 0)) {

            String changeAmount = info.getServiceFee();
            if (RechargeType.SERVICEAMOUNT.getCode() == info.getRechargeType()) {
                changeAmount = info.getRealRechargeAmount();
            }

            String finalChangeAmount = changeAmount;
            logger.info("-----------------充值订单号:{},开始联动扣收子账号服务费{}元-------------------", info.getOrderno(), finalChangeAmount);

            if (!StringUtil.isEmpty(finalChangeAmount) && ArithmeticUtil.compareTod(finalChangeAmount, "0") > 0){
                //联动调账子账号余额,是否成功此处不做处理
                ThreadUtil.subAccountThreadPool.execute(() -> updateSubAccountBalance(info.getCustomkey(), info.getRecCustomkey(), info.getPayType(), finalChangeAmount, CommonString.DEDUCTION, TradeType.RECHARGESERVICEFEE, info.getCompanyOperatorName()));
            }
        }
    }

    @Override
    public List<CustomBalanceHistory> listCustomBalanceHistory(CustomBalanceHistory balanceHistory) {
        return customBalanceHistoryService.queryCustomBalanceHistory(balanceHistory);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCustomAndCompanyBalance(UserCommission transferParam) {
        updateCustomBalance(CommonString.DEDUCTION,
                new CustomBalanceHistory(transferParam.getOriginalId(),
                        transferParam.getCompanyId(), transferParam.getPayType(),
                        ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2),
                        1, TradeType.APIPAYMENT.getCode(),transferParam.getOrderNo(),transferParam.getOperatorName()));
        logger.info("余额扣减成功！单号：{}", transferParam.getOrderNo());

        String realCompanyId = transferParam.getRealCompanyId();
        String companyId = transferParam.getCompanyId();

        boolean forwardCompany = (!StringUtil.isEmpty(realCompanyId) && !companyId.equals(realCompanyId));

        if (forwardCompany){//转包服务公司
            //更新转包服务公司在实际服务公司记账户余额
            CompanyAccountVo companyAccountVo = new CompanyAccountVo();
            String changeAmount = ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2);
            companyAccountVo.setBalance(changeAmount);
            companyAccountVo.setCustomKey(transferParam.getOriginalId());
            companyAccountVo.setCompanyId(transferParam.getCompanyId());
            companyAccountVo.setRealCompanyId(transferParam.getRealCompanyId());
            companyAccountVo.setTradeType(TradeType.APIPAYMENT.getCode());
            companyAccountVo.setRelateOrderNo(transferParam.getOrderNo());
            companyAccountVo.setOperator(transferParam.getOperatorName());
            companyAccountVo.setAmount(1);
            companyAccountVo.setOperating(CommonString.DEDUCTION);
            APIResponse apiResponse = forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);
            if(apiResponse.getState() != 1){
                throw new RuntimeException("记账户扣款失败");
            }
            //更新转包服务公司在实际服务公司的余额
            try{
                CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(transferParam.getCompanyId(),
                        transferParam.getRealCompanyId(), transferParam.getPayType(),
                        ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2),
                        1, TradeType.APIPAYMENT.getCode(),transferParam.getOrderNo(),
                        transferParam.getOperatorName());
                updateCustomBalance(CommonString.DEDUCTION, customBalanceHistory);
            }catch (Exception e){
                logger.error("扣减转包服务公司在实际下发公司余额异常"+e.getMessage(), e);
            }
        }
    }
}
