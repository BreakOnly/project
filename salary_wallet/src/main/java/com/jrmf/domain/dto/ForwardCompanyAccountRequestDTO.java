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
public class ForwardCompanyAccountRequestDTO implements Serializable {

  /**
  * @Description 查询起始时间
  **/
  @ApiModelProperty(name = "startDate", value = "查询起始时间", required = false)
 private String startDate;

  /**
  * @Description 查询结束时间
  **/
  @ApiModelProperty(name = "endDate", value = "查询结束时间", required = false)
  private String endDate;

  /**
  * @Description 每页数量
  **/
  @ApiModelProperty(name = "pageSize", value = "每页数量 默认 10", required = false)
  private int pageSize = 10;

  /**
  * @Description 页数
  **/
  @ApiModelProperty(name = "pageSize", value = "页数 默认 1", required = false)
  private int pageNo = 1;

  /**
  * @Description 转包服务公司Key
  **/
  @ApiModelProperty(name = "companyId", value = "转包服务公司Key", required = false)
  private String companyId;

  /**
  * @Description 完税服务公司Key
  **/
  @ApiModelProperty(name = "realCompanyId", value = "完税服务公司Key", required = false)
  private String realCompanyId;

  /**
   * @Description 商户Key
   **/
  @ApiModelProperty(name = "customKey", value = "商户Key", required = false)
  private String customKey;

  /**
   * @Description 状态 1:正常 2:失效
   **/
  @ApiModelProperty(name = "status", value = "状态 1:正常 2:失效", required = false)
  private int  status;

  /**
   * @Description 商户名称
   **/
  @ApiModelProperty(name = "merchantName", value = "商户名称", required = false)
  private String  merchantName;

  /**
   * @Description 查询起始余额
   **/
  @ApiModelProperty(name = "minBalance", value = "查询起始余额", required = false)
  private Double minBalance;

  /**
   * @Description 查询结束余额
   **/
  @ApiModelProperty(name = "maxBalance", value = "查询结束余额", required = false)
  private Double maxBalance;


 /**
  * @Description 查询某一条具体数据
  **/
 @ApiModelProperty(name = "id", value = "数据ID", required = false)
 private Integer id;

 /**
 * @Description 排序 1:金额排序 其他 时间排序
 **/
 @ApiModelProperty(name = "order", value = "排序 1:金额排序  其他或默认:时间排序", required = false)
 private Integer order = 2;
}
