package com.jrmf.domain.dto;

import lombok.Data;

@Data
public class YuncrUserBankDTO {

  /**
   * 银行id
   */
  private String bankId;

  /**
   * 银行支行id
   */
  private String subBankId;

  /**
   * 用户银行卡号
   */
  private String accountNo;

  /**
   * 个体户编号/自然人编号
   */
  private String firmId;

  /**
   * 账户类型
   */
  private String accountType;
}
