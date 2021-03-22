package com.jrmf.splitorder.thread;

import com.jrmf.domain.Company;
import com.jrmf.domain.CustomPaymentTotalAmount;
import com.jrmf.domain.SplitOrderConf;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.splitorder.controller.SplitOrderController;
import com.jrmf.splitorder.domain.*;
import com.jrmf.splitorder.service.SplitOrderCustomLimitConfService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;

public class SplitOrderWorker implements Callable<List<BaseOrderInfo>> {

  private static final Logger logger = LoggerFactory.getLogger(SplitOrderController.class);
  private List<UserCommission> originalData;
  private List<SplitOrderConf> splitOrderConfs;
  private SplitOrderCustomLimitConfService customLimitConfServiceImpl;
  private CompanyService companyService;
  private List<UserCommission> splitFailOrderData = new ArrayList<>();
  private List<UserCommission> splitSuccessOrderData = new ArrayList<>();
  private List<UserCommission> splitLaveOrderData = new ArrayList<>();
  private Map<String, String> splitAmountSum;


  public SplitOrderWorker(List<UserCommission> originalData, List<SplitOrderConf> splitOrderConfs,
      SplitOrderCustomLimitConfService customLimitConfServiceImpl,CompanyService companyService,
      Map<String, String> splitAmountSum) {
    this.originalData = originalData;
    this.splitOrderConfs = splitOrderConfs;
    this.customLimitConfServiceImpl = customLimitConfServiceImpl;
    this.companyService = companyService;
    this.splitAmountSum = splitAmountSum;
  }

  @Override
  public List<BaseOrderInfo> call() {
    List<BaseOrderInfo> result = new ArrayList<>();
    if (originalData.size() == 0) {
      return result;
    }
    logger.info("{} 开始拆单=======>", Thread.currentThread().getName());
    // 从关联关系中去掉当前已选择的关联关系。并返回当前关联关系配置信息。
    SplitFailOrder splitFailOrder = new SplitFailOrder();
    SplitSuccessOrder splitSuccessOrder = new SplitSuccessOrder();
    SplitLaveOrder splitLaveOrder = new SplitLaveOrder();
    Map<String,Map<String,Integer>> companyAgeMap = new HashMap<>();
    //拆单逻辑  循环上传的打款明细，该明细为拆分过的总上传明细
    for (UserCommission originalDatum : originalData) {
//            if (SplitStatus.FAIL.getState().equals(originalDatum.getStatus())) {
//                // 身份证号重复的直接失败。
//                originalDatum.setStatusDesc("身份证重复");
//                splitFailOrderData.add(getNewSameOne(originalDatum));
//                continue;
//            }
      if (SplitStatus.AMOUNT_NOT.getState().equals(originalDatum.getStatus())) {
        // 金额为空直接失败。
        originalDatum.setRemark("金额不能为空");
        splitFailOrderData.add(getNewSameOne(originalDatum));
        continue;
      }

      if (!StringUtil.checkCertId(originalDatum.getCertId())){
        originalDatum.setRemark("身份证格式校验未通过");
        splitFailOrderData.add(getNewSameOne(originalDatum));
        continue;
      }

      UserCommission lave = originalDatum;
      //查询该身份证是否还有可打款金额,没有直接失败放入splitFailOrderData
      if (isFailed(originalDatum, splitOrderConfs)) {
        continue;
      }

      //拆单前该明细的金额
      String preSplitAmount = originalDatum.getAmount();
      //该身份证打款金额 > 0
      if (ArithmeticUtil.compareTod(lave.getAmount(), "0") == 1) {
        //循环服务公司拆单配置
        for (SplitOrderConf orderConf : splitOrderConfs) {
          //拆单年龄从服务公司获取
          String companyId = orderConf.getCompanyId();
          if (!companyAgeMap.containsKey(companyId)){
            Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
            Integer minAge = company.getMinAge();
            Integer maxAge = company.getMaxAge();
            Map<String,Integer> ageMap = new HashMap<>();
            ageMap.put("minAge",minAge);
            ageMap.put("maxAge",maxAge);
            companyAgeMap.put(companyId,ageMap);
          }
          Map<String, Integer> ageMap = companyAgeMap.get(companyId);
          Integer minAge = ageMap.get("minAge");
          Integer maxAge = ageMap.get("maxAge");
          String msg = StringUtil.checkAge(originalDatum.getCertId(), minAge, maxAge);
          if (!StringUtil.isEmpty(msg)) {
//            身份证年龄校验失败直接失败
//            originalDatum.setStatusDesc(msg);
//            splitFailOrderData.add(getNewSameOne(originalDatum));
            continue;
          }
          //循环拆单操作，传入打款明细，拆单成功的数据置入splitSuccessOrderData，拆到剩余金额为0
          lave = splitOrder(orderConf, lave);
          //剩余下发余额 == 0   结束该循环拆单
          if (ArithmeticUtil.compareTod(lave.getAmount(), "0") == 0) {
            break;
          }
        }
        if (ArithmeticUtil.compareTod(lave.getAmount(), "0") == 1) {
          if (ArithmeticUtil.compareTod(lave.getAmount(), preSplitAmount) == 0) {
            logger.error("用户{}{},拆单后金额{}与拆单前{}一致,计为未拆单失败", lave.getAmount(), preSplitAmount,
                originalDatum.getUserName(),
                originalDatum.getCertId());
            splitFailOrderData.add(getNewSameOne(originalDatum));
          } else {
            // 拆单没拆完的，置为剩余
            logger.error("用户{}{},拆单后金额{}与拆单前{}不一致,计为拆单部分失败", lave.getAmount(), preSplitAmount,
                originalDatum.getUserName(),
                originalDatum.getCertId());
            splitLaveOrderData.add(getNewSameOne(lave));
          }
        }
      }
    }
    splitFailOrder.setData(splitFailOrderData);
    splitSuccessOrder.setData(splitSuccessOrderData);
    splitLaveOrder.setData(splitLaveOrderData);
    if (splitFailOrderData.size() != 0) {
      result.add(splitFailOrder);
    }
    if (splitSuccessOrderData.size() != 0) {
      result.add(splitSuccessOrder);
    }
    if (splitLaveOrderData.size() != 0) {
      result.add(splitLaveOrder);
    }
    return result;
  }

  /**
   * 查询该身份证是否还有可打款金额
   *
   * @param originalDatum   打款明细
   * @param splitOrderConfs 该商户配置的所有服务公司打款配置
   * @return
   */
  private boolean isFailed(UserCommission originalDatum, List<SplitOrderConf> splitOrderConfs) {
    String ableUse = "0.00";
    //获取所有给该身份证打款的服务公司本月所有剩余可打款额度
    ableUse = getLaveUseAmount(originalDatum, splitOrderConfs, ableUse);
    //给该身份证打款的服务公司本月所有剩余可打款额度 < 0.00
    if (ArithmeticUtil.compareTod(ableUse, "0.00") == 0) {
      // 无法拆单的，没有可拆余额的 置为失败
      originalDatum.setStatusDesc("可拆金额为零");
      splitFailOrderData.add(getNewSameOne(originalDatum));
      splitOrderFail(originalDatum);
      return true;
    }
    return false;
  }

  /**
   * 获取所有给该身份证打款的服务公司本月所有剩余可打款额度
   *
   * @param originalDatum   打款明细
   * @param splitOrderConfs 该商户配置的所有服务公司打款配置
   * @param ableUse         可支配金额
   * @return
   */
  private String getLaveUseAmount(UserCommission originalDatum,
      List<SplitOrderConf> splitOrderConfs, String ableUse) {
    //循环服务公司拆单配置
    for (SplitOrderConf orderConf : splitOrderConfs) {
      //根据服务公司companyId、商户的customKey、个人身份证号  查询个人下发累计(custom_payment_totalamount),如果没有该信息则创建一条昨天、今天、上月、当月累计都为0的数据并返回
      CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfServiceImpl
          .queryCustomPaymentTotalAmount(orderConf.getCompanyId(), orderConf.getCustomKey(),
              originalDatum.getCertId());
      // 该服务公司配置的人/月可下发金额 - 该身份证该服务公司当月累计下发的总额 = 该服务公司对该身份证当月的剩余可下发额度
      String laveAbleUseAmount = ArithmeticUtil
          .subStr(orderConf.getAmountLimit(), customPaymentTotalAmount.getCurrentMonthTotalStr());
      //该服务公司对该身份证当月的剩余可下发额度 < 0.00
      if (ArithmeticUtil.compareTod(laveAbleUseAmount, "0.00") != 1) {
        laveAbleUseAmount = "0.00";
      }
      // 可下发额度 + 该服务公司对该身份证当月的剩余可下发额度
      ableUse = ArithmeticUtil.addStr(ableUse, laveAbleUseAmount);
    }
    return ableUse;
  }

  private UserCommission getNewSameOne(UserCommission originalDatum) {
    UserCommission userCommission = new UserCommission();
    userCommission.setCompanyId(originalDatum.getCompanyId());
    userCommission.setUserName(originalDatum.getUserName());
    if (!StringUtil.isEmpty(originalDatum.getAmount())) {
      userCommission.setAmount(ArithmeticUtil.subZeroAndDot(originalDatum.getAmount()));
    }
    userCommission.setAccount(originalDatum.getAccount());
    userCommission.setStatus(originalDatum.getStatus());
    userCommission.setPhoneNo(originalDatum.getPhoneNo());
    userCommission.setCertId(originalDatum.getCertId());
    userCommission.setDocumentType(originalDatum.getDocumentType());
    userCommission.setOriginalId(originalDatum.getOriginalId());
    userCommission.setMerchantId(originalDatum.getMerchantId());
    userCommission.setPayType(originalDatum.getPayType());
    userCommission.setStatusDesc(originalDatum.getStatusDesc());
    userCommission.setRemark(originalDatum.getRemark());
    return userCommission;
  }

  public static void main(String[] args) {
//        String s = ArithmeticUtil.subStr("100", "47");
    //(数据类型)(最小值+Math.random()*(最大值-最小值+1))
    String str = "0." + (int) (9500 + Math.random() * (9999 - 9500 + 1));
    String s1 = ArithmeticUtil.mulStr("123.56", str, 2);
    System.out.println(s1);
  }


  /**
   * 拆分打款账单
   *
   * @param orderConf     服务公司拆单配置
   * @param originalDatum 打款明细
   * @return
   */
  private UserCommission splitOrder(SplitOrderConf orderConf, UserCommission originalDatum) {
    originalDatum.setOriginalId(orderConf.getCustomKey());
    originalDatum.setCompanyId(orderConf.getCompanyId());
    // 最大可拆金额
    String amountLimit = orderConf.getAmountLimit();
    // 当前剩余下发金额
    String amount = originalDatum.getAmount();
    //根据服务公司companyId、商户的customKey、个人身份证号  查询个人下发累计(custom_payment_totalamount),如果没有该信息则创建一条昨天、今天、上月、当月累计都为0的数据并返回
    CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfServiceImpl
        .queryCustomPaymentTotalAmount(orderConf.getCompanyId(), orderConf.getCustomKey(),
            originalDatum.getCertId());
    // 月累计已下发金额
    String currentMonthTotalStr = customPaymentTotalAmount.getCurrentMonthTotalStr();

    synchronized (splitAmountSum) {
      String key = originalDatum.getCertId() + orderConf.getCompanyId();
      if (splitAmountSum.containsKey(key)) {
        String amountSum = splitAmountSum.get(key);
        currentMonthTotalStr = ArithmeticUtil.addStr(currentMonthTotalStr, amountSum);
      }

      // 月累计下发小于等于最大可拆金额   该服务公司月累计下发 < 剩余拆单金额
      if (ArithmeticUtil.compareTod(currentMonthTotalStr, amountLimit) == -1) {

        // 可支配金额
        String ableUse;
        //*为整数拆分
        if ("*".equals(orderConf.getFilePath())) {
          //整数拆单 剩余拆单金额 - 该身份证该服务公司月累计已下发金额 = 该服务公司当月剩余可为该身份证打款的额度
          ableUse = ArithmeticUtil.subStr(amountLimit, currentMonthTotalStr);
        } else {
          //随机数拆单 该服务公司当月剩余可为该身份证打款的额度
          ableUse = getAbleUse(amountLimit, currentMonthTotalStr, orderConf);
        }

        boolean canNext = false;
        // 可支配金额 大于 等于 代下发   |   该服务公司当月剩余可为该身份证打款的额度 >= 当前剩余下发金额
        if (ArithmeticUtil.compareTod(ableUse, amount) != -1) {
          if (orderConf.getSplitOrderBalance() != null && !""
              .equals(orderConf.getSplitOrderBalance())) {
            if (orderConf.deductAmount(amount)) {
              canNext = true;
              logger.info("------------下发用户 {} 服务公司 {} 扣款 {} 成功,剩余 {} ------------",
                  originalDatum.getUserName(), orderConf.getCompanyId(), amount,
                  orderConf.getSplitOrderBalance());
            } else {
              logger.info("------------下发用户 {} 服务公司 {} 扣款 {} 失败,剩余 {} ------------",
                  originalDatum.getUserName(), orderConf.getCompanyId(), amount,
                  orderConf.getSplitOrderBalance());
            }
          } else {
            canNext = true;
          }

          if (canNext) {
            while (ArithmeticUtil.compareTod(originalDatum.getAmount(), "50000") == 1) {
              String laveAmount = ArithmeticUtil.subStr(originalDatum.getAmount(), "50000");
              originalDatum.setAmount("50000");
              splitSuccessOrderData.add(getNewSameOne(originalDatum));
              originalDatum.setAmount(laveAmount);
            }

            //无拆单剩余金额，添加当前明细到splitSuccessOrderData，且把当前剩余下发金额设置为0，该明细拆分完成
            splitSuccessOrderData.add(getNewSameOne(originalDatum));
            originalDatum.setAmount("0");

            if (splitAmountSum.containsKey(key)) {
              String amountSum = splitAmountSum.get(key);
              splitAmountSum.put(key, ArithmeticUtil.addStr(amount, amountSum));
            } else {
              splitAmountSum.put(key, amount);
            }
          }

        } else {

          if (orderConf.getSplitOrderBalance() != null && !""
              .equals(orderConf.getSplitOrderBalance())) {
            if (orderConf.deductAmount(ableUse)) {
              canNext = true;
              logger.info("------------下发用户 {} 服务公司 {} 扣款 {} 成功,剩余 {} ------------",
                  originalDatum.getUserName(), orderConf.getCompanyId(), ableUse,
                  orderConf.getSplitOrderBalance());
            } else {
              logger.info("------------下发用户 {} 服务公司 {} 扣款 {} 失败,剩余 {} ------------",
                  originalDatum.getUserName(), orderConf.getCompanyId(), amount,
                  orderConf.getSplitOrderBalance());
            }
          } else {
            canNext = true;
          }

          if (canNext) {
            // 可支配 小于代下发。
            // 拆单剩余金额    |    当前剩余下发金额 - 该服务公司当月剩余可为该身份证打款的额度 = 拆单剩余金额
            String lave = ArithmeticUtil.subStr(amount, ableUse);
            // 设置当前订单金额为可支配金额，然后返回携带剩余金额订单
            originalDatum.setAmount(ableUse);

            while (ArithmeticUtil.compareTod(originalDatum.getAmount(), "50000") == 1) {
              String laveAmount = ArithmeticUtil.subStr(originalDatum.getAmount(), "50000");
              originalDatum.setAmount("50000");
              splitSuccessOrderData.add(getNewSameOne(originalDatum));
              originalDatum.setAmount(laveAmount);
            }
            //拆出该服务公司最多能为该身份证下发的金额 添加到splitSuccessOrderData
            splitSuccessOrderData.add(getNewSameOne(originalDatum));
            //设置剩余的拆单剩余金额，等待进入下一次循环拆分
            originalDatum.setAmount(lave);

            if (splitAmountSum.containsKey(key)) {
              String amountSum = splitAmountSum.get(key);
              splitAmountSum.put(key, ArithmeticUtil.addStr(ableUse, amountSum));
            } else {
              splitAmountSum.put(key, ableUse);
            }
          }
        }

        return originalDatum;

      }

      return originalDatum;

    }
  }

  /**
   * 计算该服务公司可为该身份证打款的额度
   *
   * @param amountLimit          剩余拆单金额
   * @param currentMonthTotalStr 该身份证该服务公司月累计已下发金额
   * @param splitOrderConf       服务公司拆单配置
   * @return
   */
  private String getAbleUse(String amountLimit, String currentMonthTotalStr,
      SplitOrderConf splitOrderConf) {
    if (StringUtil.isEmpty(splitOrderConf.getInterval())) {
      logger.error("拆单随机数未配置");
    }
    //剩余拆单金额 - 该身份证该服务公司月累计已下发金额 = 该服务公司当月剩余可为该身份证打款的额度
    String ableUse = ArithmeticUtil.subStr(amountLimit, currentMonthTotalStr);
    //获取随机数拆单的随机区间
    String[] str = splitOrderConf.getInterval().split(",");
    Integer start = Integer.parseInt(str[0]);
    Integer end = Integer.parseInt(str[1]);
    //组装拆单随机数
    String str1 = "0." + (int) (start + Math.random() * (end - start + 1));
    //该服务公司当月剩余可为该身份证打款的额度 * 拆单随机数 = 该服务公司可为该身份证打款的额度
    ableUse = ArithmeticUtil.mulStr(ableUse, str1, 2);
    return ableUse;
  }

  private void splitOrderFail(UserCommission originalDatum) {
    logger.info("拆单失败！姓名{} 金额{} 账号{}", originalDatum.getUserName(), originalDatum.getAmount(),
        originalDatum.getAccount());
  }

}
