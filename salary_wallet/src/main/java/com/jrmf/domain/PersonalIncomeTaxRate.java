package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/7/15 10:56
 * @description: 服务公司个税配置
 */
@Data
public class PersonalIncomeTaxRate implements Serializable {

  /**
   * @Description  主键ID
   **/
  private Integer id;
  /**
   * @Description  下发公司Id
   **/
  private Integer companyId;
  /**
   * @Description  个税金额起始
   **/
  private String amountStart;
  /**
   * @Description  个税金额结束
   **/
  private String amountEnd;
  /**
   * @Description  操作符
   **/
  private String operator;
  /**
   * @Description  税率
   **/
  private String taxRate;
  /**
   * @Description  创建时间
   **/
  private String createTime;
  /**
   * @Description  最后更新时间
   **/
  private String lastUpdateTime;
  /**
   * @Description  备注
   **/
  private String remark;

}
