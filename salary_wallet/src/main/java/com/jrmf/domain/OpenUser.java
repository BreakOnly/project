package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/9/24 11:29
 * @description: 用户微信信息
 */
@Data
public class OpenUser implements Serializable {

  Integer id;

  /**
   * @Description 微信openId
   **/
  String openId;

  /**
   * @Description 头像
   **/
  String avatar;

  /**
   * @Description 微信绑定的手机号
   **/
  String mobileNo;

  /**
   * @Description 微信昵称
   **/
  String nickName;


  /**
   * @Description 微信昵称 '0 女,1 男'
   **/
  Integer sex;

  /**
   * @Description 所属省份
   **/
  String province;

  /**
   * @Description 所属市
   **/
  String city;

  /**
   * @Description 所属区
   **/
  String district;

}
