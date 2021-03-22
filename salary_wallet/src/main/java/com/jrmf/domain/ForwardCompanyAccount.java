package com.jrmf.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/20 14:35
 * @description: 商户记账户
 */
@ApiModel
@Data
public class ForwardCompanyAccount implements Serializable {

  public ForwardCompanyAccount() {
  }

  public ForwardCompanyAccount(String customKey, String companyId, String realCompanyId) {
    this.customKey = customKey;
    this.companyId = companyId;
    this.realCompanyId = realCompanyId;
  }

  private int id;

  /**
  * @Description 商户Key
  **/
  @ApiModelProperty(name = "customKey", value = "商户Key", required = false)
  private String  customKey;

  /**
   * @Description 转包公司
   **/
  @ApiModelProperty(name = "companyId", value = "转包公司", required = false)
  private String  companyId;

  /**
  * @Description 完税公司
  **/
  @ApiModelProperty(name = "realCompanyId", value = "完税公司", required = false)
  private String  realCompanyId;

  /**
  * @Description 余额，单位：分
  **/
  @ApiModelProperty(name = "balance", value = "余额", required = false)
  private Integer  balance;


  /**
  * @Description 1:正常 2:失效
  **/
  @ApiModelProperty(name = "status", value = " 状态 1:正常 2:失效", required = false)
  private Integer  status;

  /**
   * @Description 创建时间
   **/
  private String  createTime;

  /**
   * @Description 更新时间
   **/
  private String  updateTime;

  /**
   * @Description 商户名称
   **/
  @ApiModelProperty(name = "merchantName", value = "商户名称", required = false)
  private String merchantName;

  /**
   * @Description 转包公司名称
   **/
  @ApiModelProperty(name = "companyName", value = "转包公司名称", required = false)
  private String companyName;

  /**
   * @Description 实际下发公司名称
   **/
  @ApiModelProperty(name = "realCompanyName", value = "实际下发公司名称", required = false)
  private String realCompanyName;

  /**
  * @Description 业务字段 商户数量
  **/
  @ApiModelProperty(name = "customCount", value = "商户数量", required = false)
  private int customCount;

  /**
   * 交易笔数
   */
  private Integer tradeCount;

  /**
   * @Description 余额，单位：元
   **/
  private String  balanceTwo;
}
