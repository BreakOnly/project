package com.jrmf.domain.dto;

import lombok.Data;

@Data
public class YuncrPushCustomerFirmDTO {
  /**
   * 法人手机号
   */
  private String firmName;

  /**
   * 组织机构代码（统一社会信用代码）
   */
  private String zuzhjgdm;

  /**
   * 法人
   */
  private String farnName;

  /**
   * 性别(0男1女)
   */
  private String farnxngb;

  /**
   * 身份证(A代表身份证)
   */
  private String farnzjlx;

  /**
   * 身份证号
   */
  private String farnzjno;

  /**
   * 经济分类编号（1级）
   */
  private String categoryId;

  /**
   * 法人手机号
   */
  private String farnshji;
  /**
   * 法人邮箱
   */
  private String farndzyx;

  /**
   * 省份id
   */
  private String farnhkprovId;

  /**
   * 城市id
   */
  private String farnhkcityId;

  /**
   * 区域id
   */
  private String farnhkregiId;

  /**
   * 地址
   */
  private String farnjzdz;

  /**
   * 联系人姓名
   */
  private String lxrnName;

  /**
   * 联系人手机号
   */
  private String lxrnshji;

  /**
   * 联系人邮箱
   */
  private String lxrndzyx;
}
