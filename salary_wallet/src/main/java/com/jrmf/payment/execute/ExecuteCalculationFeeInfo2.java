package com.jrmf.payment.execute;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jrmf.controller.constant.ServiceFeeType;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.persistence.ChannelInterimBatch2Dao;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.service.CalculationFeeService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;

public class ExecuteCalculationFeeInfo2 implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ExecuteCalculationFeeInfo2.class);
    public static final String PROCESS = "process";

    private String processId;
    private ChannelRelated related;
    private Set<String> validateSet;
    private Map<String, String> batchData;
    private CommissionTemporary2Dao temporaryDao2;
    private ChannelInterimBatch2Dao interimBatchDao2;
    private boolean autoSupplement;
    private CalculationFeeService calculationFeeService;
    private Integer serviceFeeType;

    public ExecuteCalculationFeeInfo2(String processId,
                                      CommissionTemporary2Dao temporaryDao2,
                                      ChannelInterimBatch2Dao interimBatchDao2,
                                      ChannelRelated related,
                                      Set<String> validateSet,
                                      Map<String, String> batchData,
                                      boolean autoSupplement,
                                      CalculationFeeService calculationFeeService,
                                      Integer serviceFeeType) {
        super();
        this.related = related;
        this.batchData = batchData;
        this.processId = processId;
        this.validateSet = validateSet;
        this.temporaryDao2 = temporaryDao2;
        this.autoSupplement = autoSupplement;
        this.interimBatchDao2 = interimBatchDao2;
        this.calculationFeeService = calculationFeeService;
        this.serviceFeeType = serviceFeeType;
    }

    @Override
    public void run() {

        MDC.put(PROCESS, processId);
        logger.info("------------计算佣金费用开始-------------------");

        int passNum = 0;
        int failedNum = 0;
        String amountSum = "0.00";
        String batchAmount = "0.00";
        String handleAmount = "0.00";
        String serviceFeeSum = "0.00";
        String profitAmountSum = "0.00";
        String failedAmountSum = "0.00";
        String supplementServiceFeeSum = "0.00";
        String batchId = batchData.get("batchId");
        String originalId = related.getOriginalId();


        List<CommissionTemporary> commissionsByBatchId = temporaryDao2.getCommissionsByBatchId(batchId, originalId);
        for (CommissionTemporary temporary : commissionsByBatchId) {
            String amount = temporary.getSourceAmount();
            if (temporary.getStatus() == 1) {

                String sumFee = "0";
                String profitAmount = "0";
                String supplementFee = "0";
//				String ruleType = temporary.getFeeRuleType();
//				ruleType = StringUtil.isEmpty(ruleType)? "1" : ruleType;

                //调用服务
                Map<String, String> commissionFeeInfoMap = calculationFeeService.calculationFeeInfo("web",
                        originalId,
                        temporary.getCompanyId(),
                        batchId,
                        validateSet,
                        "",
                        "",
                        autoSupplement,
                        temporary,
                        serviceFeeType);

                if (serviceFeeType == ServiceFeeType.ISSUE.getCode()
                    || serviceFeeType == ServiceFeeType.PERSON.getCode()) {

                    sumFee = commissionFeeInfoMap.get("sumFee");
                    profitAmount = commissionFeeInfoMap.get("profitAmount");
                    supplementFee = commissionFeeInfoMap.get("supplementFee");
                    if (serviceFeeType == ServiceFeeType.PERSON.getCode()) {
                        //下发实时扣税个人承担时，这里的实发金额已被上面 calculationFeeInfo方法修改
                        amount = temporary.getAmount();
                    }
                }

                //增加下发实时扣税个人承担后可能出现跨档补差价后 本次下发金额不足以抵扣补服务费金额 导致到账金额为负数
                if (ArithmeticUtil.compareTod(amount, "0") != 1) {
                    failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
                    failedNum++;
                } else {
                    amountSum = ArithmeticUtil.addStr(amountSum, amount);//批次总金额
                    serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum, sumFee);//批次总服务费
                    profitAmountSum = ArithmeticUtil.addStr(profitAmountSum, profitAmount);//批次总利润
                    supplementServiceFeeSum = ArithmeticUtil.addStr(supplementServiceFeeSum, supplementFee);//批次补差价总服务费
                    passNum++;
                }

            } else if (temporary.getStatus() == 2) {
                failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
                failedNum++;
            }
            batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
        }
//TODO
//        temporaryDao2.updateCommissionTemporarys(commissionsByBatchId);

        validateSet.clear();
        handleAmount = ArithmeticUtil.addStr(amountSum, serviceFeeSum);

        ChannelInterimBatch batch = interimBatchDao2.getChannelInterimBatchByOrderno(batchId, originalId);
        batch.setOrderno(batchId);
        batch.setAmount(amountSum);
        batch.setBatchAmount(batchAmount);
        batch.setServiceFee(serviceFeeSum);//服务费
        batch.setSupplementServiceFee(supplementServiceFeeSum);//服务费补差价
        batch.setMfkjServiceFee(profitAmountSum);
        batch.setPassNum(passNum);
        batch.setFailedNum(failedNum);
        batch.setHandleAmount(handleAmount);
        batch.setFailedAmount(failedAmountSum);

        if (failedNum > 0) {
            if (passNum == 0) {
                batch.setStatus(2);//全部失败
            } else {
                batch.setStatus(3);//部分失败
            }
        } else if (failedNum == 0) {
            if (passNum == 0) {
                batch.setStatus(2);//全部失败
            } else {
                batch.setStatus(1);//全部成功
            }
        }
        interimBatchDao2.updateChannelInterimBatch(batch);
        logger.info("------------计算佣金费用结束-------------------");

        MDC.remove(PROCESS);
    }

}
