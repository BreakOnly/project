package com.jrmf.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/20 11:06
 * @description: 商户记账户历史记录
 */
@ApiModel
@Data
public class ForwardCompanyAccountHistoryRequestDTO implements Serializable {

  /**
  * @Description 查询起始时间
  **/
  @ApiModelProperty(name = "startDate", value = "查询起始时间", required = false)
  String startDate;

  /**
  * @Description 查询结束时间
  **/
  @ApiModelProperty(name = "endDate", value = "查询结束时间", required = false)
  String endDate;

  /**
  * @Description 每页数量
  **/
  @ApiModelProperty(name = "pageSize", value = "每页数量 默认 10", required = false)
  int pageSize = 10;

  /**
  * @Description 页数
  **/
  @ApiModelProperty(name = "pageNo", value = "页数 默认 1", required = false)
  int pageNo = 1;

  /**
  * @Description 转包服务公司Key
  **/
  @ApiModelProperty(name = "companyId", value = "转包服务公司Key", required = false)
  String companyId;

  /**
  * @Description 完税服务公司Key
  **/
  @ApiModelProperty(name = "realCompanyId", value = "完税(实际)服务公司Key", required = false)
  String realCompanyId;

  /**
   * @Description 商户Key
   **/
  @ApiModelProperty(name = "customKey", value = "商户Key", required = false)
  String customKey;
  /**
   * @Description 最小交易金额
   **/
  @ApiModelProperty(name = "minBalance", value = "最小交易金额", required = false)
  Integer minBalance;

  /**
   * @Description 最大交易金额
   **/
  @ApiModelProperty(name = "maxBalance", value = "最大交易金额", required = false)
  Integer maxBalance;
  /**
  * @Description 所属记账户ID
  **/
  @ApiModelProperty(name = "accountId", value = "记账户ID", required = false)
  Integer accountId;

  /**
  * @Description 商户名称
  **/
  @ApiModelProperty(name = "merchantName", value = "商户名称", required = false)
  String merchantName;

  @ApiModelProperty(name = "tradeType", value = "交易类型 1.充值 2.下发 3.增额 4.减额 5.切入完税公司 6.切出完税公司 7.补登记API资金下发 8.充值退款", required = false)
  Integer tradeType;
}
