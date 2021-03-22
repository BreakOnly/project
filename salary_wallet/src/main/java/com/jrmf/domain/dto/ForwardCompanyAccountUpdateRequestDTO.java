package com.jrmf.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/11/20 17:28
 * @description: 修改商户记账户余额或状态
 */
@ApiModel
@Data
public class ForwardCompanyAccountUpdateRequestDTO implements Serializable {

  /**
  * @Description 数据ID
  **/
  private int id;

  /**
   * @Description 状态  1:正常 2:失效
   **/
  @ApiModelProperty(name = "status", value = "状态  1:正常 2:失效", required = false)
  private int status;

  /**
   * @Description  增减 余额
   **/
  @ApiModelProperty(name = "balance", value = "增减金额", required = false)
  private Double balance;

  /**
   * @Description 交易密码
   **/
  @ApiModelProperty(name = "password", value = "交易密码", required = false)
  private String password;

  /**
   * @Description 增减标识
   **/
  @ApiModelProperty(name = "type", value = "1:增额 2:减额", required = false)
  private Integer type;

  /**
   * @Description 备注
   **/
  @ApiModelProperty(name = "remark", value = "备注", required = false)
  private String remark;

  /**
   * @Description 转包公司ID  业务代码
   **/
  private String companyId;

  /**
   * @Description 操作者  业务代码
   **/
  private String operator;
}
