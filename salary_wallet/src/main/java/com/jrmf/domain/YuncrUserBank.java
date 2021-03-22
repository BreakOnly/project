package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/9/11 15:41
 * @description: 用户银行卡 信息
 */
@Data
public class YuncrUserBank implements Serializable {

  private Integer id;

  private Integer authenticationId;

  private String bankCardNumber;

  private String subBankId;

  private String bankCardPhone;

  private Integer type;

  private String bankId;

  private Integer isBind;

}
