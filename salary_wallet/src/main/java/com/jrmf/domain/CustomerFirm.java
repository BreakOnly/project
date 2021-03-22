package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class CustomerFirm implements Serializable {

  private Integer id;

  /**
   * 商户名称
   */
  private String customName;

  /**
   * 商户唯一标识
   */
  private String customKey;

  /**
   * 用工企业编号
   */
  private String firmId;

  /**
   * 法人
   */
  private String legalPerson;

  /**
   * 法人手机号
   */
  private String legalPersonPhone;

  /**
   * 法人邮箱
   */
  private String legalPersonEmail;

  /**
   * 性别(0男1女)
   */
  private Integer sex;

  /**
   * 身份证号
   */
  private String identityCard;

  /**
   * 经济分类编号
   */
  private Integer categoryId;

  /**
   * 省id
   */
  private Integer provinceId;

  /**
   * 市id
   */
  private Integer cityId;

  /**
   * 区id
   */
  private Integer areaId;

  /**
   * 地址
   */
  private String address;

  /**
   * 联系人姓名
   */
  private String contactsName;

  /**
   * 法人手机号
   */
  private String contactsPhone;

  /**
   * 法人邮箱
   */
  private String contactsEmail;

  /**
   * 操作账号
   */
  private String addUser;

  /**
   * 创建时间
   */
  private String createTime;

  /**
   * 更新时间
   */
  private String updateTime;

  /**
   * 状态（1：成功，2：失败，3：处理中）
   */
  private Integer status;

  /**
   * 状态描述
   */
  private String statusDesc;

  /**
   * 系统流水号
   */
  private String platsrl;

  /**
   * 统一社会信用代码
   */
  private String creditCode;

  /**
   * 经济分类编号名称
   */
  private String label;

  /**
   * 省名称
   */
  private String provinceName;

  /**
   * 市名称
   */
  private String cityName;

  /**
   * 区名称
   */
  private String areaName;

}
