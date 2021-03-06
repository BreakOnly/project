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
        logger.info("------------????????????????????????-------------------");

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

                //????????????
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
                        //????????????????????????????????????????????????????????????????????? calculationFeeInfo????????????
                        amount = temporary.getAmount();
                    }
                }

                //????????????????????????????????????????????????????????????????????? ??????????????????????????????????????????????????? ???????????????????????????
                if (ArithmeticUtil.compareTod(amount, "0") != 1) {
                    failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
                    failedNum++;
                } else {
                    amountSum = ArithmeticUtil.addStr(amountSum, amount);//???????????????
                    serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum, sumFee);//??????????????????
                    profitAmountSum = ArithmeticUtil.addStr(profitAmountSum, profitAmount);//???????????????
                    supplementServiceFeeSum = ArithmeticUtil.addStr(supplementServiceFeeSum, supplementFee);//???????????????????????????
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
        batch.setServiceFee(serviceFeeSum);//?????????
        batch.setSupplementServiceFee(supplementServiceFeeSum);//??????????????????
        batch.setMfkjServiceFee(profitAmountSum);
        batch.setPassNum(passNum);
        batch.setFailedNum(failedNum);
        batch.setHandleAmount(handleAmount);
        batch.setFailedAmount(failedAmountSum);

        if (failedNum > 0) {
            if (passNum == 0) {
                batch.setStatus(2);//????????????
            } else {
                batch.setStatus(3);//????????????
            }
        } else if (failedNum == 0) {
            if (passNum == 0) {
                batch.setStatus(2);//????????????
            } else {
                batch.setStatus(1);//????????????
            }
        }
        interimBatchDao2.updateChannelInterimBatch(batch);
        logger.info("------------????????????????????????-------------------");

        MDC.remove(PROCESS);
    }

}
