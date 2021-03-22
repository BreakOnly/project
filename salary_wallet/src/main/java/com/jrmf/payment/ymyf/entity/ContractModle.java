package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;

/**
 * 签约实体类
 *
 * @author Admin
 */
public class ContractModle implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 姓名
   */
  private String name;

  /**
   * 卡号
   */
  private String cardNo;

  /**
   * 身份证号
   */
  private String idCard;

  /**
   * 手机号
   */
  private String mobile;

  /**
   * 签约类型 0：接口签约，1：公众号签约
   */
  private int signType;

  /**
   * 备注
   */
  private String memo;

  /**
   * 状态 0：未签约 1：已签约 2:未检索到soho信息
   */
  private int state;

  /**
   * 签约透传数据
   */
  private String otherParam;

  /**
   * 通道ID
   */
  private Long levyId;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCardNo() {
    return cardNo;
  }

  public void setCardNo(String cardNo) {
    this.cardNo = cardNo;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public int getSignType() {
    return signType;
  }

  public void setSignType(int signType) {
    this.signType = signType;
  }

  public String getMemo() {
    return memo;
  }

  public void setMemo(String memo) {
    this.memo = memo;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getOtherParam() {
    return otherParam;
  }

  public void setOtherParam(String otherParam) {
    this.otherParam = otherParam;
  }

  public Long getLevyId() {
    return levyId;
  }

  public void setLevyId(Long levyId) {
    this.levyId = levyId;
  }

}
