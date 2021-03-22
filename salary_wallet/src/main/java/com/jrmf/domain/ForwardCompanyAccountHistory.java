package com.jrmf.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/20 10:06
 * @description: 商户记账户历史记录
 */
@ApiModel
@Data
public class ForwardCompanyAccountHistory implements Serializable {


  private int id;

  /**
  * @Description 商户Key
  **/
  @ApiModelProperty(name = "customKey", value = "商户Key", required = false)
  private String customKey;

  /**
  * @Description 服务公司Key
  **/
  @ApiModelProperty(name = "companyId", value = "服务公司Key", required = false)
  private String companyId;

  /**
  * @Description 实际下发服务公司Key
  **/
  @ApiModelProperty(name = "realCompanyId", value = "实际下发服务公司Key", required = false)
  private String realCompanyId;

  /**
  * @Description 交易类型 1.充值 2.下发 3.增额 4.减额 5.切入完税公司 6.切出完税公司 7.补登记API资金下发 8.充值退款
  **/
  @ApiModelProperty(name = "tradeType", value = "交易类型 1.充值 2.下发 3.增额 4.减额 5.切入完税公司 6.切出完税公司 7.补登记API资金下发 8.充值退款", required = false)
  private int tradeType;

  /**
  * @Description 交易金额
  **/
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @ApiModelProperty(name = "tradeMoney", value = "交易金额", required = false)
  private BigDecimal tradeMoney;

  /**
  * @Description 交易前金额
  **/
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @ApiModelProperty(name = "preTradeMoney", value = "交易前金额", required = false)
  private BigDecimal preTradeMoney;

  /**
  * @Description 交易后金额
  **/
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @ApiModelProperty(name = "afterTradeMoney", value = "交易后金额", required = false)
  private BigDecimal afterTradeMoney;

  /**
  * @Description 发生的交易笔数
  **/
  @ApiModelProperty(name = "amount", value = "发生的交易笔数", required = false)
  private int amount;

  /**
  * @Description 备注
  **/
  @ApiModelProperty(name = "remark", value = "备注", required = false)
  private String remark;

  /**
   * @Description 创建时间
   **/
  private String  createTIme;

  /**
  * @Description 商户名称
  **/
  @ApiModelProperty(name = "merchantName", value = "商户名称", required = false)
  private String merchantName;
  /**
   * @Description 服务公司名称
   **/
  @ApiModelProperty(name = "companyName", value = "服务公司名称", required = false)
  private String companyName;

  /**
   * @Description 实际下发公司名称
   **/
  @ApiModelProperty(name = "realCompanyName", value = "实际下发公司名称", required = false)
  private String realCompanyName;

  /**
   * @Description 关联的记账户ID
   **/
  @ApiModelProperty(name = "accountId", value = "关联的记账户ID", required = false)
  private int accountId;

  /**
  * @Description 流水号
  **/
  private String relateOrderNo;

  /**
  * @Description 操作者 登陆者名称
  **/
  private String operator;

}
