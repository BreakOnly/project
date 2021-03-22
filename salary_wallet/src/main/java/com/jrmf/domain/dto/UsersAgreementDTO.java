package com.jrmf.domain.dto;

import java.io.File;
import lombok.Data;

@Data
public class UsersAgreementDTO {

  /**
   * 主键
   */
  private int id;
  /**
   * 登记业务类型
   */
  private int regType;
  /**
   * 用户信息表ID
   */
  private String userId;
  /**
   * 签约协议模板表ID
   */
  private String agreementTemplateId;
  /**
   * 签约协议名称
   */
  private String agreementName;
  /**
   * 签约协议模版url
   */
  private String agreementTemplateURL;
  /**
   * 用户姓名
   */
  private String userName;
  /**
   * 用户证件号
   */
  private String certId;
  /**
   * 证件类型
   * 1-身份证
   * 2-港澳台通行证
   * 3-护照
   * 4-军官证
   */
  private String documentType;
  /**
   * 签约用户联系手机号
   */
  private String mobilePhone;
  /**
   * 签约状态
   * 1-创建
   * 2-签约处理中
   * 3-签约待审核
   * 4-签约失败
   * 5-签约成功
   */
  private int signStatus;
  /**
   * 签约状态描述
   */
  private String signStatusDes;
  /**
   * 签约步骤
   * 0-创建
   * 1-签约协议模板成功
   * 2-签约协议模板失败
   */
  private int signStep;
  /**
   * 上传身份证
   * 0-创建
   * 1-上传身份证成功
   * 2-上传身份证失败
   */
  private int documentStep;
  /**
   * 魔方商户号
   */
  private String originalId;
  /**
   * 平台号  （ps：爱员工）
   */
  private String merchantId;
  /**
   * 服务公司编号
   */
  private String companyId;
  /**
   * 签约第三方商户ID (ps : 爱员工appid)
   */
  private String thirdMerchId;
  /**
   * 签约第三方签约模板ID
   */
  private String thirdTemplateId;
  /**
   * 签约方式
   * 1-本地人工审核签约
   * 2-调用第三方接口静默签约
   */
  private String agreementType;
  /**
   * 用户签约协议电子版URL
   */
  private String agreementURL;
  /**
   * 魔方签约协议号
   */
  private String agreementNo;
  /**
   * 第三方通道签约协议号
   */
  private String thirdAgreementNo;
  /**
   * 调用流水号
   */
  private String orderNo;
  /**
   * 三方流水号
   */
  private String thirdNo;
  /**
   * 用户证件影像保存URL地址1
   */
  private String imageURLA;
  /**
   * 用户证件影像保存URL地址2
   */
  private String imageURLB;
  /**
   * 用户证件影像保存URL地址3
   */
  private String imageURLC;
  /**
   * 用户证件影像保存URL地址4
   */
  private String imageURLD;
  /**
   * 创建时间
   */
  private String createTime;
  /**
   * 最后更新时间
   */
  private String lastUpdateTime;
  /**
   * 签约来源
   * 0-H5、1-API、2-迁移签约、3-批次下发共享签约、4-平台发起共享签约、5-后台批量导入签约
   */
  private Integer signSubmitType;

  /**
   * 认证等级， L0:未认证 ， L1:本地认证，L2:二要素认证，L3:三要素认证,L4：四要素认证
   */
  private String checkLevel;

  /**
   * 是否通过证照认证：0-否，1-是
   */
  private Integer checkByPhoto;

  /**
   * 白名单
   * 0，校验
   * 1，不校验
   */
  private int whiteList;
  /**
   * 审批人
   */
  private String approver;

  /**
   * 预留
   */
  private String preparedA;

  /**
   * 预留
   */
  private String preparedB;

  /**
   * 是否上传身份证图片 是：true，否：false
   */
  private boolean uploadIdCard;

  /**
   * 身份证人像面
   */
  private byte[] frontFileByte;

  /**
   * 身份证国徽面
   */
  private byte[] backFileByte;

  /**
   * 身份证人像面
   */
  private File frontFile;

  /**
   * 身份证国徽面
   */
  private File backFile;

  /**
   * 请求流水号
   */
  private String channelSerialno;

}
