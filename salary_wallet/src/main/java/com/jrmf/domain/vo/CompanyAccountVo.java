package com.jrmf.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/25 9:14
 * @description: 修改记账户 vo
 */
@ApiModel
@Data
public class CompanyAccountVo implements Serializable {

  /**
  * @Description 记账户表ID
  **/
  @ApiModelProperty(name = "id", value = "记账户表ID", required = false)
 private Integer id;
  /**
  * @Description 交易类型
  **/
  @ApiModelProperty(name = "tradeType", value = "交易类型", required = false)
  private  int tradeType;
  /**
   * @Description 交易金额 单位 元
   **/
  @ApiModelProperty(name = "balance", value = "交易金额 单位 元", required = false)
  private String balance;

  /**
   * @Description 商户key
   **/
  @ApiModelProperty(name = "customKey", value = "商户key", required = false)
  private String customKey;
  /**
   * @Description 服务公司key
   **/
  @ApiModelProperty(name = "companyId", value = "服务公司key", required = false)
  private String companyId;
  /**
   * @Description 实际下发服务公司key
   **/
  @ApiModelProperty(name = "realCompanyId", value = "实际下发服务公司key", required = false)
  private String realCompanyId;

  /**
   * @Description 操作者
   **/
  @ApiModelProperty(name = "operator", value = "操作者", required = false)
  private String operator;

  /**
   * @Description 流水号
   **/
  @ApiModelProperty(name = "relateOrderNo", value = "流水号", required = false)
  private String relateOrderNo;

  /**
   * @Description 交易笔数
   **/
  @ApiModelProperty(name = "amount", value = "交易笔数", required = false)
  private int amount;

  @ApiModelProperty(name = "operating", value = "金额操作标识", required = false)
  private int operating;

}
