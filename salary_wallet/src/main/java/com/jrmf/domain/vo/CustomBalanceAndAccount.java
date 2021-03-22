package com.jrmf.domain.vo;

import java.io.Serializable;

/**
 * @author: YJY
 * @date: 2020/12/8 14:00
 * @description:
 */
public class CustomBalanceAndAccount implements Serializable {

  /**
  * @Description 商户key
  **/
 private String originalId;
  /**
   * @Description 服务公司key
   **/
 private String companyId;
  /**
   * @Description 付款方式
   **/
 private int payType;
  /**
   * @Description 总金额
   **/
 private String handleAmount;
  /**
   * @Description 交易成功数
   **/
 private int passNum;
  /**
   * @Description 实际下发服务公司
   **/
 private String realCompanyId;
  /**
   * @Description 金额 不包含服务费
   **/
 private String amount;
  /**
   * @Description 订单号
   **/
 private String orderNo;
  /**
   * @Description 操作者
   **/
 private String operator;
  /**
   * @Description 交易类型
   **/
 private int tradeType;

  public String getOriginalId() {
    return originalId;
  }

  public CustomBalanceAndAccount setOriginalId(String originalId) {
    this.originalId = originalId;
    return this;
  }

  public String getCompanyId() {
    return companyId;
  }

  public CustomBalanceAndAccount setCompanyId(String companyId) {
    this.companyId = companyId;
    return this;
  }

  public int getPayType() {
    return payType;
  }

  public CustomBalanceAndAccount setPayType(int payType) {
    this.payType = payType;
    return this;
  }

  public String getHandleAmount() {
    return handleAmount;
  }

  public CustomBalanceAndAccount setHandleAmount(String handleAmount) {
    this.handleAmount = handleAmount;
    return this;
  }

  public int getPassNum() {
    return passNum;
  }

  public CustomBalanceAndAccount setPassNum(int passNum) {
    this.passNum = passNum;
    return this;
  }

  public String getRealCompanyId() {
    return realCompanyId;
  }

  public CustomBalanceAndAccount setRealCompanyId(String realCompanyId) {
    this.realCompanyId = realCompanyId;
    return this;
  }

  public String getAmount() {
    return amount;
  }

  public CustomBalanceAndAccount setAmount(String amount) {
    this.amount = amount;
    return this;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public CustomBalanceAndAccount setOrderNo(String orderNo) {
    this.orderNo = orderNo;
    return this;
  }

  public String getOperator() {
    return operator;
  }

  public CustomBalanceAndAccount setOperator(String operator) {
    this.operator = operator;
    return this;
  }

  public int getTradeType() {
    return tradeType;
  }

  public CustomBalanceAndAccount setTradeType(int tradeType) {
    this.tradeType = tradeType;
    return this;
  }
}
