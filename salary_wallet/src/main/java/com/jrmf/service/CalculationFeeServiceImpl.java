package com.jrmf.service;

import com.jrmf.controller.constant.TempStatus;
import com.jrmf.utils.RespCode;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.domain.*;
import com.jrmf.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.persistence.CompanyRateConfDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.utils.ArithmeticUtil;

@Service
public class CalculationFeeServiceImpl implements CalculationFeeService {

    private Logger logger = LoggerFactory.getLogger(CalculationFeeServiceImpl.class);
    public static final String PROCESS = "process";

    @Autowired
    private CompanyRateConfDao companyRateConfDao;
    @Autowired
    private CommissionTemporary2Dao temporaryDao2;
    @Autowired
    private UserCommission2Dao userCommissionDao2;
    @Autowired
    private CustomCompanyRateConfService customCompanyRateConfService;
    @Autowired
    private CustomLimitConfService customLimitConfService;

    @Autowired
    private UserRelatedService userRelatedService;
    @Autowired
    private ChannelCustomService channelCustomService;
    @Autowired
    private UserCommissionService userCommissionService;

    @Override
    public Map<String, String> calculationFeeInfo(String callType,
                                                  String originalId,
                                                  String companyId,
                                                  String batchId,
                                                  Set<String> validateSet,
                                                  String certId,
                                                  String amountTemp,
                                                  boolean autoSupplement,
                                                  CommissionTemporary temporary,
                                                  Integer serviceFeeType) {

        String sumFee = "0.00";
        String profiltRates = "0.00";
        String profitAmount = "0.00";
        String supplementFee = "0.00";
        String supplementAmount = "0.00";
        String calculationRates = "0.00";

        String ruleType = "1";
        String ratesAll = "0.0";
        String ratesMonth = "0.0";
        String profiltRateAll = "0.0";
        String profiltRateMonth = "0.0";
        Integer rateConfAllId = null;
        Integer rateConfMonthId = null;
        String rateInterval = "";

        BigDecimal initValueDecimal = new BigDecimal("1");
        Map<String, String> commissionFeeInfoMap = new HashMap<String, String>();

        if ("web".equals(callType)) {
            certId = temporary.getIdCard();
            amountTemp = temporary.getSourceAmount();

            String mounthSumAmonut =userCommissionDao2.getSumAmountOfMonthByCertId(certId, originalId, companyId);
            customLimitConfService.queryCustomPaymentTotalAmount(companyId,
                    originalId,
                    certId);
//            CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId,
//                    originalId,
//                    certId);
//            BigDecimal currentMonthTotal = new BigDecimal(customPaymentTotalAmount.getCurrentMonthTotal());
//            String mounthSumAmonut = currentMonthTotal.divide(new BigDecimal(100)).toString();
            logger.info("------------计算佣金费用------下发金额-amountTemp：" + amountTemp + "----月下发金额-mounthSumAmonut：" + mounthSumAmonut);

            CustomCompanyRateConf rateConfMonth = null;
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("originalId", originalId);
            params.put("companyId", companyId);
            //月累计下发不为0才可能会补款
            boolean mounthSumCompareFlag = ArithmeticUtil.compareTod(mounthSumAmonut, "0") > 0;
            if (mounthSumCompareFlag) {
                params.put("sumAmount", mounthSumAmonut);
                rateConfMonth = customCompanyRateConfService.getCustomCompanyRateConf(params);
            }

            params.clear();
            String batchSumAmount = temporaryDao2.getSumAmountOfBatchByCertId(certId, batchId);
            logger.info("------------计算佣金费用---此用户的当前批次总金额-batchSumAmount：" + batchSumAmount);
            BigDecimal batchSumAmountDecimal = new BigDecimal(batchSumAmount);
            BigDecimal mounthSumAmonutDecimal = new BigDecimal(mounthSumAmonut);
            BigDecimal mounthAndBatchTempSumDecimal = mounthSumAmonutDecimal.add(batchSumAmountDecimal);
            String mounthAndBatchTempSum = mounthAndBatchTempSumDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            logger.info("------------计算佣金费用-----用户当月+当前批次总金额-mounthAndBatchTempSum：" + mounthAndBatchTempSum);

            params.put("originalId", originalId);
            params.put("companyId", companyId);
            params.put("sumAmount", mounthAndBatchTempSum);
            CustomCompanyRateConf rateConfAll = customCompanyRateConfService.getCustomCompanyRateConf(params);

            boolean rateConfFlag = false;
            boolean ratesCompareResult = false;
            boolean isSupplementProfilt = false;

            ratesAll = rateConfAll.getCustomRate();
            profiltRateAll = rateConfAll.getMfIncomeRate();
            rateConfAllId = rateConfAll.getId();
            Integer ruleTypeInteger = rateConfAll.getFeeRuleType();
            ruleType = String.valueOf(ruleTypeInteger == null ? "1" : ruleTypeInteger);
            rateInterval = rateConfAll.getAmountStart() + rateConfAll.getOperator() + rateConfAll.getAmountEnd();

            if (rateConfMonth != null) {
                ratesMonth = rateConfMonth.getCustomRate();
                profiltRateMonth = rateConfMonth.getMfIncomeRate();
                rateConfMonthId = rateConfMonth.getId();

                rateConfFlag = rateConfAllId.intValue() == rateConfMonthId.intValue();
                ratesCompareResult = ArithmeticUtil.compareTod(ratesAll, ratesMonth) <= 0;
            }
            logger.info("------------计算佣金费用----当前费率ratesAll:" + ratesAll + "--本月累计下发费率ratesMonth：" + ratesMonth);

            if (rateConfFlag
                    || ratesCompareResult
                    || !mounthSumCompareFlag
                    || !autoSupplement) {//不涉及补款 ：同档位;不同档位时&&高档位费率<=低档位费率时；月累计下发为0时;自动补差价==false
                logger.info("------------计算佣金费用--是否补差价--档位是否相同ID-rateConfFlag:" + rateConfFlag
                        + "---费率比较ratesCompareResult：" + ratesCompareResult
                        + "---月下发累计!mounthSumCompareFlag:" + !mounthSumCompareFlag
                        + "---自动补差价标志!autoSupplement：" + !autoSupplement);

                BigDecimal sumFeeDecimal = null;
                calculationRates = ratesAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal calculationRatesDecimal = new BigDecimal(calculationRates);

                if ("1".equals(ruleType)) {
                    sumFeeDecimal = transAmountDecimal.multiply(calculationRatesDecimal);
                }
                if ("2".equals(ruleType)) {
                    sumFeeDecimal = transAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);
                }
                sumFee = sumFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("------------计算佣金费用----当前档位ID:" + rateConfAllId + "---当前费率：" + calculationRates + "--服务费（无补差价）sumFee:" + sumFee);

            } else {//不在一个档位，且高档位费率大于低档位费率，则涉及补差价问题
                calculationRates = ratesAll;
                if (validateSet.add(certId)) {//当前批次中，第一笔下发补差价
                    supplementAmount = mounthSumAmonut;
                    BigDecimal supplementFeeDecimal = null;
                    BigDecimal nowBatchTransFeeDecimal = null;

                    BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                    BigDecimal serviceRatesDecimal = new BigDecimal(ratesMonth);
                    BigDecimal supplementAmountDecimal = new BigDecimal(supplementAmount);
                    BigDecimal calculationRatesDecimal = new BigDecimal(ratesAll);

                    if ("1".equals(ruleType)) {
                        supplementFeeDecimal = supplementAmountDecimal.multiply(calculationRatesDecimal.subtract(serviceRatesDecimal));
                        nowBatchTransFeeDecimal = transAmountDecimal.multiply(calculationRatesDecimal);
                    }
                    if ("2".equals(ruleType)) {
                        nowBatchTransFeeDecimal = transAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);

                        BigDecimal supplementFeeDecimalUpper = supplementAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);
                        BigDecimal supplementFeeDecimaldown = supplementAmountDecimal.divide(initValueDecimal.subtract(serviceRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(serviceRatesDecimal);

                        supplementFeeDecimal = supplementFeeDecimalUpper.subtract(supplementFeeDecimaldown);
                    }

                    String nowBatchTransFee = nowBatchTransFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    supplementFee = supplementFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    sumFee = ArithmeticUtil.addStr(nowBatchTransFee, supplementFee);

                    isSupplementProfilt = true;//补收收益
                } else {
                    supplementFee = "0.00";
                    supplementAmount = "0.00";

                    BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                    BigDecimal calculationRatesDecimal = new BigDecimal(ratesAll);
                    BigDecimal sumFeeDecimal = null;
                    if ("1".equals(ruleType)) {
                        sumFeeDecimal = transAmountDecimal.multiply(calculationRatesDecimal);
                    }
                    if ("2".equals(ruleType)) {
                        sumFeeDecimal = transAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);
                    }
                    sumFee = sumFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                }
                logger.info("------计算佣金费用---当前档位ID:" + rateConfAllId + "--当前费率：" + ratesAll + "----月累计档位：" + profiltRateMonth + "--月累计费率：" + ratesMonth);

                logger.info("---服务费（包含补差价）-sumFee：" + sumFee + "-----补差价服务费-supplementFee：" + supplementFee + "------补差价金额-supplementAmount：" + supplementAmount);
            }

            boolean profiltRateCompareResult = ArithmeticUtil.compareTod(profiltRateAll, profiltRateMonth) > 0;
            if (isSupplementProfilt && profiltRateCompareResult) {
                profiltRates = profiltRateAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal profiltUpperDecimal = new BigDecimal(profiltRateAll);

                BigDecimal supplementAmountDecimal = new BigDecimal(supplementAmount);

                BigDecimal profiltLowerDecimal = new BigDecimal(profiltRateMonth);
                BigDecimal profitAmountDecimal = transAmountDecimal.multiply(profiltUpperDecimal).add(supplementAmountDecimal.multiply(profiltUpperDecimal.subtract(profiltLowerDecimal)));
                profitAmount = profitAmountDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("------------计算佣金费用--当前收益率--profiltRates：" + profiltRates + "--月累计收益率：" + profiltRateMonth + "---收益-profitAmount（包含补差价）:" + profitAmount);
            } else {
                profiltRates = profiltRateAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal profiltUpperDecimal = new BigDecimal(profiltRates);
                BigDecimal profitAmountDecimal = transAmountDecimal.multiply(profiltUpperDecimal);
                profitAmount = profitAmountDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("------------计算佣金费用--收益率-profiltRates：" + profiltRates + "---收益-profitAmount（不包含补差价）:" + profitAmount);
            }

            //确认最终的服务费是否正确---v2.9.5
            sumFee = checkSumFee(originalId, sumFee, calculationRates);

            //充值预扣收不在这个更新服务费,只需登记费率
            if (serviceFeeType == ServiceFeeType.ISSUE.getCode()
                || serviceFeeType == ServiceFeeType.PERSON.getCode()) {
                temporary.setSumFee(sumFee);
                temporary.setSupplementFee(supplementFee);
                temporary.setSupplementAmount(supplementAmount);
                temporary.setProfilt(profitAmount);
                temporary.setProfiltRates(profiltRates);
                if (serviceFeeType == ServiceFeeType.PERSON.getCode()) {
                    String amount = ArithmeticUtil.subStr(temporary.getSourceAmount(), sumFee, 2);
                    if (ArithmeticUtil.compareTod(amount, "0") != 1 || ArithmeticUtil
                        .compareTod(amount, temporary.getSourceAmount()) == 1) {
                        temporary.setStatus(TempStatus.FAILURE.getCode());
                        temporary.setStatusDesc(RespCode.DEDUCT_AMOUNT_ERROR);
                    }
                    temporary.setAmount(amount);
                }
            }

            temporary.setCalculationRates(calculationRates);
            temporary.setRateInterval(rateInterval);
            //TODO 打款计算服务费优化点，更改为批量插入
            temporaryDao2.updateCommissionTemporary(temporary);

        }
        if ("api".equals(callType)) {
            CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId,
                    originalId,
                    certId);
            BigDecimal currentMonthTotal = new BigDecimal(customPaymentTotalAmount.getCurrentMonthTotal());
            String mounthSumAmonut = currentMonthTotal.divide(new BigDecimal(100)).toString();
            logger.info("-----api-------计算佣金费用------下发金额-amountTemp：" + amountTemp + "----月下发金额-mounthSumAmonut：" + mounthSumAmonut);

            CustomCompanyRateConf rateConfMonth = null;
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("originalId", originalId);
            params.put("companyId", companyId);
            //月累计下发不为0才可能会补款
            boolean mounthSumCompareFlag = ArithmeticUtil.compareTod(mounthSumAmonut, "0") > 0;
            if (mounthSumCompareFlag) {
                params.put("sumAmount", mounthSumAmonut);
                rateConfMonth = customCompanyRateConfService.getCustomCompanyRateConf(params);
            }

            params.clear();
            BigDecimal amountTempDecimal = new BigDecimal(amountTemp);
            BigDecimal mounthSumAmonutDecimal = new BigDecimal(mounthSumAmonut);
            BigDecimal mounthAndTempSumDecimal = mounthSumAmonutDecimal.add(amountTempDecimal);
            String mounthAndTempSum = mounthAndTempSumDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            params.put("originalId", originalId);
            params.put("companyId", companyId);
            params.put("sumAmount", mounthAndTempSum);
            CustomCompanyRateConf rateConfAll = customCompanyRateConfService.getCustomCompanyRateConf(params);

            ratesAll = rateConfAll.getCustomRate();
            profiltRateAll = rateConfAll.getMfIncomeRate();
            rateConfAllId = rateConfAll.getId();
            Integer ruleTypeInteger = rateConfAll.getFeeRuleType();
            ruleType = String.valueOf(ruleTypeInteger == null ? "1" : ruleTypeInteger);
            rateInterval = rateConfAll.getAmountStart() + rateConfAll.getOperator() + rateConfAll.getAmountEnd();

            boolean rateConfFlag = false;
            boolean ratesCompareResult = false;
            boolean isSupplementProfilt = false;

            if (rateConfMonth != null) {
                ratesMonth = rateConfMonth.getCustomRate();
                profiltRateMonth = rateConfMonth.getMfIncomeRate();
                rateConfMonthId = rateConfMonth.getId();

                rateConfFlag = rateConfAllId.intValue() == rateConfMonthId.intValue();
                ratesCompareResult = ArithmeticUtil.compareTod(ratesAll, ratesMonth) <= 0;
            }

            logger.info("-----api-------计算佣金费用----当前费率ratesAll:" + ratesAll + "--本月累计下发费率ratesMonth：" + ratesMonth);

            if (rateConfFlag
                    || ratesCompareResult
                    || !mounthSumCompareFlag
                    || !StringUtils.isNotEmpty(certId)
                    || !autoSupplement) {//不涉及补款 :同档位;不同档位时&&高档位费率<=低档位费率时;月累计下发为0时;身份证号为空时

                logger.info("-----api-------计算佣金费用--是否补差价--档位是否相同ID-rateConfFlag:" + rateConfFlag
                        + "---费率比较ratesCompareResult：" + ratesCompareResult
                        + "---月下发累计!mounthSumCompareFlag:" + !mounthSumCompareFlag
                        + "---身份证件号为空!StringUtils.isNotEmpty(certId):" + !StringUtils.isNotEmpty(certId)
                        + "---自动补差价标志!autoSupplement：" + !autoSupplement);

                BigDecimal sumFeeDecimal = null;
                calculationRates = ratesAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal calculationRatesDecimal = new BigDecimal(calculationRates);

                if ("1".equals(ruleType)) {
                    sumFeeDecimal = transAmountDecimal.multiply(calculationRatesDecimal);
                }
                if ("2".equals(ruleType)) {
                    sumFeeDecimal = transAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);
                }
                sumFee = sumFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("-----api-------计算佣金费用----当前档位ID:" + rateConfAllId + "---当前费率：" + calculationRates + "--服务费（无补差价）sumFee:" + sumFee);

            } else {//不在一个档位，且高档位费率大于低档位费率，则涉及补差价问题
                calculationRates = ratesAll;

                supplementAmount = mounthSumAmonut;
                BigDecimal supplementFeeDecimal = null;
                BigDecimal nowBatchTransFeeDecimal = null;

                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal serviceRatesDecimal = new BigDecimal(ratesMonth);
                BigDecimal supplementAmountDecimal = new BigDecimal(supplementAmount);
                BigDecimal calculationRatesDecimal = new BigDecimal(ratesAll);

                if ("1".equals(ruleType)) {
                    supplementFeeDecimal = supplementAmountDecimal.multiply(calculationRatesDecimal.subtract(serviceRatesDecimal));
                    nowBatchTransFeeDecimal = transAmountDecimal.multiply(calculationRatesDecimal);
                }
                if ("2".equals(ruleType)) {
                    nowBatchTransFeeDecimal = transAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);

                    BigDecimal supplementFeeDecimalUpper = supplementAmountDecimal.divide(initValueDecimal.subtract(calculationRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(calculationRatesDecimal);
                    BigDecimal supplementFeeDecimaldown = supplementAmountDecimal.divide(initValueDecimal.subtract(serviceRatesDecimal), 9, BigDecimal.ROUND_HALF_UP).multiply(serviceRatesDecimal);

                    supplementFeeDecimal = supplementFeeDecimalUpper.subtract(supplementFeeDecimaldown);
                }

                String nowBatchTransFee = nowBatchTransFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                supplementFee = supplementFeeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                sumFee = ArithmeticUtil.addStr(nowBatchTransFee, supplementFee);

                isSupplementProfilt = true;//补收收益

                logger.info("---api---计算佣金费用---当前档位ID:" + rateConfAllId + "--当前费率：" + ratesAll + "----月累计档位：" + profiltRateMonth + "--月累计费率：" + ratesMonth);
                logger.info("---api---服务费（包含补差价）-sumFee：" + sumFee + "-----补差价服务费-supplementFee：" + supplementFee + "------补差价金额-supplementAmount：" + supplementAmount);
            }

            boolean profiltRateCompareResult = ArithmeticUtil.compareTod(profiltRateAll, profiltRateMonth) > 0;
            if (isSupplementProfilt && profiltRateCompareResult) {
                profiltRates = profiltRateAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal profiltUpperDecimal = new BigDecimal(profiltRateAll);

                BigDecimal supplementAmountDecimal = new BigDecimal(supplementAmount);

                BigDecimal profiltLowerDecimal = new BigDecimal(profiltRateMonth);
                BigDecimal profitAmountDecimal = transAmountDecimal.multiply(profiltUpperDecimal).add(supplementAmountDecimal.multiply(profiltUpperDecimal.subtract(profiltLowerDecimal)));
                profitAmount = profitAmountDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("-----api-------计算佣金费用--当前收益率--profiltRates：" + profiltRates + "--月累计收益率：" + profiltRateMonth + "---收益-profitAmount（包含补差价）:" + profitAmount);
            } else {
                profiltRates = profiltRateAll;
                BigDecimal transAmountDecimal = new BigDecimal(amountTemp);
                BigDecimal profiltUpperDecimal = new BigDecimal(profiltRates);
                BigDecimal profitAmountDecimal = transAmountDecimal.multiply(profiltUpperDecimal);
                profitAmount = profitAmountDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                logger.info("-----api-------计算佣金费用--收益率-profiltRates：" + profiltRates + "---收益-profitAmount（不包含补差价）:" + profitAmount);
            }
        }


        //确认最终的服务费是否正确---v2.9.5
        sumFee = checkSumFee(originalId, sumFee, calculationRates);


        commissionFeeInfoMap.put("sumFee", sumFee);
        commissionFeeInfoMap.put("profitAmount", profitAmount);
        commissionFeeInfoMap.put("supplementFee", supplementFee);
        commissionFeeInfoMap.put("supplementAmount", supplementAmount);
        commissionFeeInfoMap.put("calculationRates", calculationRates);
        commissionFeeInfoMap.put("rateInterval",rateInterval);

        return commissionFeeInfoMap;
    }

    /**
     * 检查服务费
     *
     * @param originalId       商户id
     * @param sumFee           服务费
     * @param calculationRates 费率
     * @return 服务费
     */
    @Override
    public String checkSumFee(String originalId, String sumFee, String calculationRates) {
        ChannelCustom custom = channelCustomService.getCustomByCustomkey(originalId);
        if (StringUtil.isNumber(custom.getMinSumFee())) {
            if (ArithmeticUtil.compareTod(calculationRates, "0") > 0) {
                if (!(ArithmeticUtil.compareTod(sumFee, "0") > 0)) {
                    sumFee = custom.getMinSumFee();
                }
            }
        }
        return sumFee;
    }

    @Override
    public void locationCustomCompanyRateConf(String companyId, String customkey, String batchId) {

        List<CommissionGroup> commissionGroupList = temporaryDao2.getCommissionGroupByCertId(batchId, "1", null);
        Map<String,String> mounthSumAmonuts = userCommissionService.getCommissionMounthSumAmonut(customkey, companyId, batchId, "1");

        for (CommissionGroup commission : commissionGroupList) {

//            UserRelated userRelated = userRelatedService.selectIsWhiteList(customkey, companyId, commission.getCertId());
//            //白名单用户不进行档位校验
//            if (!(userRelated != null && userRelated.getIsWhiteList() == 1)) {

            //计算本批次一个证件号的下发金额之和
            String batchAmout = "0.0";

            for (TempCommission tempCommission : commission.getCommissionList()) {
                batchAmout = ArithmeticUtil.addStr(tempCommission.getSourceAmount(), batchAmout);
            }
            logger.info("-----本批次总金额batchAmout：" + batchAmout);

            //本证件号月累计总额
            String mounthSumAmonut = mounthSumAmonuts.get(commission.getCertId());
//			String mounthSumAmonut = userCommissionDao2.getSumAmountOfMonthByCertId(commission.getCertId(), customkey, companyId);
//            CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId,
//                    customkey,
//                    commission.getCertId());
//            BigDecimal currentMonthTotal = new BigDecimal(customPaymentTotalAmount.getCurrentMonthTotal());
//            String mounthSumAmonut = currentMonthTotal.divide(new BigDecimal(100)).toString();
            //在限额表中没有记录的创建记录
            customLimitConfService.queryCustomPaymentTotalAmount(companyId, customkey, commission.getCertId());
            logger.info("-----单月下发金额mounthSumAmonut：" + mounthSumAmonut);

            String mounthBatchSum = ArithmeticUtil.addStr(mounthSumAmonut, batchAmout);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("originalId", customkey);
            params.put("companyId", companyId);
            params.put("sumAmount", mounthBatchSum);
            CustomCompanyRateConf rateConf = customCompanyRateConfService.getCustomCompanyRateConf(params);
            logger.info("----费率档位判断-------月下发累计金额+本批次总金额：" + mounthBatchSum);

            if (rateConf == null) {//如果无配置档位信息，则更新明细状态
                logger.info("----费率档位判断-----无档位---");
                for (TempCommission tempCommission : commission.getCommissionList()) {
                    CommissionTemporary commissionFail = new CommissionTemporary();
                    commissionFail.setId(tempCommission.getId());
                    commissionFail.setStatus(2);
                    commissionFail.setStatusDesc("无对应费率档位");
                    commissionFail.setSumFee("0.00");
                    commissionFail.setCalculationRates("0.00");
                    commissionFail.setSupplementAmount("0.00");
                    commissionFail.setSupplementFee("0.00");
                    temporaryDao2.updateCommissionTemporary(commissionFail);
                    logger.info("----费率档位判断--无档位--id:" + tempCommission.getId());
                }
            }
//            }
        }

    }
}
