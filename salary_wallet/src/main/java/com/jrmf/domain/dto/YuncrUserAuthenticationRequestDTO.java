package com.jrmf.domain.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/9/25 9:49
 * @description: 个体户注册审核入参
 */
@Data
public class YuncrUserAuthenticationRequestDTO implements Serializable {

  /**
  * @Description 商户名称
  **/
  private String customName;
  /**
   * @Description 是否白名单  1:是 2:不是
   **/
  private Integer isWhiteList;
  /**
   * @Description 姓名
   **/
  private String name;
  /**
   * @Description 身份证号码
   **/
  private String idCard;
  /**
  * @Description 手机号
  **/
  private String phone;
  /**
   * @Description 注册方式 1微信注册 2手机号注册
   **/
  private Integer registerType;
  /**
   * @Description 审核状态  6:待用户提交 2:待企业审核 3:政府审核中 4:审核成功 5:审核失败
   **/
  private Integer rechargeType;

  /**
   * @Description 身份证号码集合
   **/
  private List idCardList;

  /**
   * @Description 页数
   **/
  private Integer pageNo;

  /**
   * @Description 每页数量
   **/
  private Integer pageSize;
  /**
   * @Description 起始时间
   **/
  private String startTime;
  /**
   * @Description 结束时间
   **/
  private String endTime;
}
