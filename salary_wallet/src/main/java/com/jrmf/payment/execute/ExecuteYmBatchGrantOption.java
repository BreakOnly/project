package com.jrmf.payment.execute;

import com.jrmf.domain.*;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;


public class ExecuteYmBatchGrantOption implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ExecuteYmBatchGrantOption.class);

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


    public ExecuteYmBatchGrantOption(String processId,
                                     CyclicBarrier cb,
                                     OrderNoUtil orderNoUtil,
                                     List<CommissionTemporary> param,
                                     UserCommission2Dao userCommissionDao2,
                                     UserSerivce userSerivce,
                                     ChannelRelated channelRelated,
                                     Map<String, Object> batchData
    ) {
        super();
        this.processId = processId;
        this.param = param;
        this.cb = cb;
        this.orderNoUtil = orderNoUtil;
        this.userCommissionDao2 = userCommissionDao2;
        this.userSerivce = userSerivce;
        this.channelRelated = channelRelated;
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
    }

    @Override
    public void run() {
        try {
            MDC.put(PROCESS, processId);
            logger.info("-----------------溢美服务商模式校验验证码成功执行插入明细---处理数目:" + param.size() + "---------");
            List<UserCommission> commissionBatch = new ArrayList<>();
            for (CommissionTemporary commission : param) {
                try {
                    Map<String, Object> addRespMap = userSerivce.addUserInfo(commission.getUserName(),
                            commission.getDocumentType(),
                            commission.getIdCard(),
                            null,
                            null,
                            originalId,
                            null, "");
                    commission.setUserId((int) addRespMap.get("userId"));
                    logger.info("-----addRespMap:{}-----", addRespMap);
                    //校验失败明细数据--直接置为失败订单
                    if (commission.getStatus() == 2) {
                        createErrorCommion(commission, commissionBatch);
                        continue;
                    } else if (commission.getStatus() == 1) {
                        createSuccessUserCommission(commission, commissionBatch);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    commission.setStatusDesc("下发失败，系统异常！");
                    createErrorCommion(commission, commissionBatch);
                }
            }

            //同步临时数据到正式数据
            if (commissionBatch.size() != 0) {
                int result = userCommissionDao2.addUserCommissionBatch(commissionBatch);
                logger.info("----->溢美服务商模式校验验证码成功执行插入--执行条数:" + commissionBatch.size() + "\r\n----->生成明细--插入条数:" + result);
            }


            logger.info("------------------溢美服务商模式校验验证码成功执行插入明细结束------------");
            cb.await();
        } catch (Exception e) {
            logger.error("------------------溢美服务商模式校验验证码成功执行插入失败------------");
            logger.error(e.getMessage(), e);
        } finally {
            MDC.remove(PROCESS);
        }
    }


    private void createErrorCommion(CommissionTemporary temporary, List<UserCommission> commissionBatch) {
        UserCommission commission = new UserCommission();
        commission.setAmount(temporary.getAmount());
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId() + "");
        commission.setStatus(2);
        commission.setStatusDesc(temporary.getStatusDesc());
        commission.setRemark(temporary.getRemark());
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
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commission.setPathNo(PaymentFactory.YMFWSPAY);
        commissionBatch.add(commission);
    }

    private void createSuccessUserCommission(CommissionTemporary temporary, List<UserCommission> commissionBatch) {
        String amount = temporary.getAmount();

        UserCommission commission = new UserCommission();

        commission.setAmount(amount);
        commission.setCreatetime(DateUtils.getNowDate());
        commission.setUserId(temporary.getUserId() + "");
        commission.setStatus(3);
        commission.setAccount(temporary.getBankCardNo());
        commission.setBatchId(batchId);
        commission.setOriginalId(originalId);
        commission.setMerchantId(channelRelated.getMerchantId());
        commission.setCompanyId(channelRelated.getCompanyId());
        commission.setOrderNo(temporary.getOrderNo());
        commission.setOperatorName(temporary.getOperatorName());
        commission.setPayUserName(operatorName);
        commission.setStatusDesc("处理中");

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
        commission.setRemark(commission.getReceiptNo() + temporary.getRemark());
        commission.setCompanyName(companyName);
        commission.setCustomName(customName);
        commission.setRepeatcheck(temporary.getRepeatcheck());
        commission.setFeeRuleType(temporary.getFeeRuleType());
        commission.setPhoneNo(temporary.getPhoneNo());

        commission.setBusinessManager(temporary.getBusinessManager());
        commission.setBusinessPlatform(temporary.getBusinessPlatform());
        commission.setBusinessChannel(temporary.getBusinessChannel());
        commission.setBusinessChannelKey(temporary.getBusinessChannelKey());
        commission.setCustomLabel(temporary.getCustomLabel());
        commission.setSubAcctNo(subAcctNo);
        commission.setRateInterval(temporary.getRateInterval());
        commission.setRealCompanyId(temporary.getRealCompanyId());
        commission.setPathNo(PaymentFactory.YMFWSPAY);
        commissionBatch.add(commission);
    }

}
