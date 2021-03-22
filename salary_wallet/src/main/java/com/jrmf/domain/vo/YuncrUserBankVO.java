package com.jrmf.domain.vo;

import lombok.Data;

@Data
public class YuncrUserBankVO {

  private Integer id;

  private String bankCardName;

  private String bankCardNumber;

  private String subBankName;

  private String subBankNumber;

  private String createTime;

  private Integer authenticationId;

  private String bankId;
}
