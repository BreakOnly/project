package com.jrmf.payment.execute;

import com.jrmf.controller.constant.PayType;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import com.jrmf.domain.*;
import com.jrmf.service.*;

import com.jrmf.utils.exception.CheckUserNameCertIdCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

/**
 * @author zhanghuan
 */
public class ExecuteBatchGrantOption2 implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ExecuteBatchGrantOption2.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static final String PROCESS = "process";

    private String processId;

    private String batchId;

    private OrderNoUtil orderNoUtil;

    private String operatorName;

    private String customName;

    private String companyName;

    private String fileName;

    private String batchDesc;

    private String batchName;

    private int menuId;

    private String originalId;

    private String menuName;

    private String subAcctNo;

    private CyclicBarrier cb;

    private List<CommissionTemporary> param;

    private UserSerivce userSerivce;

    private UserCommission2Dao userCommissionDao2;

    private ChannelRelated channelRelated;

    private BaseInfo baseInfo;

    private CustomLimitConfService customLimitConfService;

    private CompanyService companyService;

    private CustomLdConfigService customLdConfigService;

    private UserCommissionService userCommissionService;

    public ExecuteBatchGrantOption2(String processId,
                                    CyclicBarrier cb,
                                    OrderNoUtil orderNoUtil,
                                   List<CommissionTemporary> param,
                                    UserCommission2Dao userCommissionDao2,
                                    UserSerivce userSerivce,
                                    ChannelRelated channelRelated,
                                    Map<String, Object> batchData,
                                    BaseInfo baseInfo,
                                    CustomLimitConfService customLimitConfService,
                                    CompanyService companyService,
                                    CustomLdConfigService customLdConfigService,
                                    UserCommissionService userCommissionService
    ) {
        super();
        this.processId = processId;
        this.param = param;
        this.cb = cb;
        this.orderNoUtil = orderNoUtil;
        this.userCommissionDao2 = userCommissionDao2;
        this.userSerivce = userSerivce;
        this.channelRelated = channelRelated;
        this.customLimitConfService = customLimitConfService;
        batchId = (String) batchData.get("batchId");
        operatorName = (String) batchData.get("operatorName");
        fileName = (String) batchData.get("fileName");
        batchDesc = (String) batchData.get("batchDesc");
        batchName = (String) batchData.get("batchName");
        originalId = (String) batchData.get("originalId");
        menuName = (String) batchData.get("menuName");
        customName = (String) batchData.get("customName");
        companyName = (String) batchData.get("companyName");
        menuId = (Integer) batchData.get("menuId");
        subAcctNo = (String) batchData.get("subAcctNo");
        this.baseInfo = baseInfo;
        this.companyService=companyService;
        this.customLdConfigService=customLdConfigService;
        this.userCommissionService=userCommissionService;
    }

    @Override
    public void run() {
        try {
            MDC.put(PROCESS, processId);
            logger.info("-----------------佣金下发开始---处理数目:"+param.size()+"---------");
            List<UserCommission> commissionBatch = new ArrayList<>();
            //临时下发明细<-->正式下发明细模型转换
            for (CommissionTemporary commission: param) {
                try {
                    //支付宝下发未填写身份证不生成用户
                    if (!commission.getBankCardNo().equals(commission.getIdCard())){
                        Map<String, Object> addRespMap = userSerivce.addUserInfo(commission.getUserName(),
                                commission.getDocumentType(),
                                commission.getIdCard(),
                                null,
                                null,
                                originalId,
                                null, "");
                        commission.setUserId((int)addRespMap.get("userId"));

                        logger.info("-----addRespMap:{}-----" , addRespMap);
                    }
                    //校验失败明细数据--直接置为失败订单
                    if(commission.getStatus()==2){
                        createErrorCommion(commission,commissionBatch);
                        continue;
                    }else if(commission.getStatus()==1){
                        createSuccessUserCommission(commission, commissionBatch);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    commission.setStatusDesc("下发失败，系统异常！");
                    createErrorCommion(commission,commissionBatch);
                }
            }

            //正式下发明细持久化
            if(commissionBatch.size() != 0){
                int result = userCommissionDao2.addUserCommissionBatch(commissionBatch);
                logger.info("----->同步下发佣金--上送条数:" + commissionBatch.size() + "\r\n----->生成明细--插入条数:" + result);
            }

            //逐条下发
            for (UserCommission userCommission : commissionBatch) {
                //1.判断是否为联动交易
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customkey", originalId);
                params.put("companyId", userCommission.getCompanyId());
                CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
                logger.info("订单号："+userCommission.getOrderNo()+",非联动下发，使用原有模式下发");
                //原有模式下发
                orderLssued(userCommission);
            }
            logger.info("------------------佣金下发结束------------");
            cb.await();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }finally{
            MDC.remove(PROCESS);
        }
    }

    /**
     * 原有模式下发
     * @param userCommission
     */
    private void orderLssued(UserCommission userCommission) {
        try {
            if (userCommission.getStatus() != 0) {
                logger.error("交易订单:{}下发前状态验证失败，中断下发", userCommission.getOrderNo());
                return;
            }
            //查询支付通道和密钥参数
            PaymentConfig paymentConfig = companyService.getPaymentConfigInfoPlus(String.valueOf(userCommission.getPayType()), originalId, userCommission.getCompanyId(), userCommission.getRealCompanyId());
            if(paymentConfig != null){
                userCommission.setPathNo(paymentConfig.getPathNo());
                userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
                //兼容老通道模式，如果appid没在新路由中存放及赋值老路由的参数
                if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
                    paymentConfig.setAppIdAyg(channelRelated.getAppIdAyg());
                }
                Company realCompany = companyService.getCompanyByUserId(Integer.parseInt(userCommission.getRealCompanyId()));
                if (realCompany != null && realCompany.getServiceCompanyId()!=null){
                    paymentConfig.setServiceCompanyId(realCompany.getServiceCompanyId());
                }
            }else{
                logger.error("下发上送过程异常：未配置商户下发通道路由");
                logger.error("查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + userCommission.getOriginalId() + "--服务公司ID："+ userCommission.getRealCompanyId() +  "-订单号：" + userCommission.getOrderNo());

                userCommission.setStatus(2);
                userCommission.setStatusDesc("失败-未配置商户下发通道路由");
                userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
                return;
            }


            logger.info("佣金下发开始------通道配置信息------paymentConfig.toString()：" + paymentConfig.toString());

            String failMessage ;
            PaymentReturn<String> paymentReturn ;
            String orderNo = userCommission.getOrderNo();
            //挡板有效
            String transferBaffleSwitch = "1";
            if(transferBaffleSwitch.equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals	(originalId)){
                String retCode ;

                String amount = userCommission.getAmount();
                amount = ArithmeticUtil.mulStr(amount, "100");
                int length = amount.length();
                //偶数
                if(Integer.parseInt(amount.substring(length - 1))%2 == 0){
                    retCode = PayRespCode.RESP_SUCCESS;
                    failMessage = "交易成功";
                }else{
                    retCode = PayRespCode.RESP_FAILURE;
                    failMessage = "交易失败";
                }
                paymentReturn = new PaymentReturn(retCode, failMessage, orderNo);
            }else{
                userCommission.setPathNo(paymentConfig.getPathNo());
                //上送支付通道进行下发
                paymentReturn = userCommissionService.getPaymentReturn(userCommission, paymentConfig);

            }
            logger.info("1----下发完成，返回流水号："+paymentReturn.toString()+"-------");

            if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())
                    || PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
                // 银行交易流水号
                String bankOrderNo = paymentReturn.getAttachment();
                logger.info("2---下发完成，返回流水号："+bankOrderNo+"-------");
                if(bankOrderNo.length()>100){
                    bankOrderNo = bankOrderNo.substring(0, 50);
                }
                userCommission.setStatus(3);
                userCommission.setAygOrderNo(bankOrderNo);
                userCommission.setStatusDesc("处理中");

                //更新下发累计金额
                customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(),
                        userCommission.getOriginalId(),
                        userCommission.getCertId(),
                        userCommission.getSourceAmount(),
                        true);
                logger.info("更新累计金额：本次累计{}元", userCommission.getSourceAmount());
            } else {
                userCommission.setStatus(2);
                failMessage = paymentReturn.getFailMessage();
                if (failMessage.length() > 200) {
                    failMessage = failMessage.substring(0, 200);
                } else if (failMessage.length() == 0) {
                    failMessage = RespCode.CONNECTION_ERROR;
                }
                userCommission.setStatusDesc(failMessage);
            }
            userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
        } catch (Exception e) {
            logger.error("下发上送过程异常： 明细流水号 ："+userCommission.getOrderNo());
            logger.error(e.getMessage(),e);

            //更新下发累计金额
            customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(),
                    userCommission.getOriginalId(),
                    userCommission.getCertId(),
                    userCommission.getSourceAmount(),
                    true);
            logger.info("更新累计金额：本次累计{}元", userCommission.getSourceAmount());

            userCommission.setStatus(3);
            userCommission.setStatusDesc("处理中");
            userCommissionDao2.updateUserCommissionByOrderNo(userCommission);
        }
    }


    private void createErrorCommion(CommissionTemporary temporary,List<UserCommission> commissionBatch){
        UserCommission commission = new UserCommission();
        commission.setAmount(temporary.getAmount());
        commission.setSourceAmount(temporary.getSourceAmount());
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId()+"");
        commission.setStatus(2);
        commission.setStatusDesc(temporary.getStatusDesc());
        commission.setRemark(temporary.getRemark());
        commission.setSourceRemark(temporary.getRemark());
        commission.setBatchId(batchId);
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setOrderNo(temporary.getOrderNo());

        commission.setSumFee("0");
        commission.setSupplementFee("0");
        commission.setSupplementAmount("0");
        commission.setCalculationRates("0");
        commission.setProfilt("0");
        commission.setProfiltFree("0");

        commission.setOriginalId(channelRelated.getOriginalId());
        commission.setMerchantId(channelRelated.getMerchantId());
        commission.setOperatorName(temporary.getOperatorName());
        commission.setPayUserName(operatorName);
        commission.setPayType(temporary.getPayType());
        commission.setAccount(temporary.getBankCardNo());
        //未开票
        commission.setInvoiceStatus(2);

        commission.setUserName(temporary.getUserName());
        commission.setAccount(temporary.getBankCardNo());
        commission.setBankNo(temporary.getBankNo());
        commission.setBankName(temporary.getBankName());
        commission.setContentName(menuName);
        commission.setMenuId(menuId);
        commission.setBatchFileName(fileName);
        commission.setBatchDesc(batchDesc);
        commission.setBatchName(batchName);
        commission.setDocumentType(temporary.getDocumentType());
        commission.setCertId(temporary.getIdCard());
        commission.setBankNo(temporary.getBankNo());
        commission.setCustomName(customName);
        commission.setCompanyName(companyName);
        commission.setFeeRuleType(temporary.getFeeRuleType());
        commission.setPhoneNo(temporary.getPhoneNo());

        commission.setBusinessManager(temporary.getBusinessManager());
        commission.setOperationsManager(temporary.getOperationsManager());
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commissionBatch.add(commission);
    }

    private void createSuccessUserCommission(CommissionTemporary temporary, List<UserCommission> commissionBatch){
        String amount  = temporary.getAmount();

        UserCommission commission = new UserCommission();

        commission.setAmount(amount);
        commission.setSourceAmount(temporary.getSourceAmount());
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId() + "");
        commission.setStatus(0);
        commission.setAccount(temporary.getBankCardNo());
        commission.setBatchId(batchId);
        commission.setOriginalId(originalId);
        commission.setMerchantId(channelRelated.getMerchantId());
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setOrderNo(temporary.getOrderNo());
        commission.setOperatorName(temporary.getOperatorName());
        commission.setPayUserName(operatorName);
        commission.setStatusDesc("已提交，等待下发");

        commission.setSumFee(temporary.getSumFee());
        commission.setSupplementFee(temporary.getSupplementFee());
        commission.setSupplementAmount(temporary.getSupplementAmount());
        commission.setCalculationRates(temporary.getCalculationRates());
        commission.setProfilt(temporary.getProfiltRates());
        commission.setProfiltFree(temporary.getProfilt());

        commission.setPayType(temporary.getPayType());
        //未开票
        commission.setInvoiceStatus(2);
        commission.setContentName(menuName);
        commission.setMenuId(menuId);
        commission.setBatchFileName(fileName);
        commission.setBatchDesc(batchDesc);
        commission.setBatchName(batchName);
        commission.setAccountDate(sdf.format(new Date()));

        commission.setCreatetime(DateUtils.getNowDate());
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setUserName(temporary.getUserName());
        commission.setBankNo(temporary.getBankNo());
        commission.setBankName(temporary.getBankName());
        commission.setDocumentType(temporary.getDocumentType());
        commission.setCertId(temporary.getIdCard());
        commission.setBankNo(temporary.getBankNo());
        commission.setReceiptNo(orderNoUtil.getReceiptNo());
        commission.setRemark(commission.getReceiptNo()+temporary.getRemark());
        commission.setSourceRemark(temporary.getRemark());
        commission.setCompanyName(companyName);
        commission.setCustomName(customName);
        commission.setRepeatcheck(temporary.getRepeatcheck());
        commission.setFeeRuleType(temporary.getFeeRuleType());
        commission.setPhoneNo(temporary.getPhoneNo());

        commission.setBusinessManager(temporary.getBusinessManager());
        commission.setOperationsManager(temporary.getOperationsManager());
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commissionBatch.add(commission);
    }

}
