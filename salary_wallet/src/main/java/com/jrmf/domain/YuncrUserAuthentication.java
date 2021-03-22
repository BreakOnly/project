package com.jrmf.domain;

import com.jrmf.common.YuncrFailNode;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * @author: YJY
 * @date: 2020/9/10 10:05
 * @description: 用户认证数据
 */
@Data
@ToString
public class YuncrUserAuthentication implements Serializable {

  private Integer id;

  private String birthday;

  private String nation;

  private String phone;

  private String sex;

  private String address;

  private String name;

  private String idCard;

  private String issueAuthority;

  private String vaildPriod;

  private String idCardFrontNumber;

  private  String idCardReverseNumber;

  private String liveTestNumber;

  private String trueNameNumber;

  private String liveTestUrl;

  private String signatureUrl;

  private Integer applyType;

  private Integer isWhiteList;

  private Integer processNode;

  private Date createTime;

  private Date lastUpdateTime;

  private Integer  enterpriseAudit;

  private Integer governmentAudit;

  private Date enterpriseAuditDate;

  private Date governmentAuditDate;

  private String enterpriseRefuseReason;

  private String governmentRefuseReason;

  private String businessLicenseNumber;

  private String businessLicenseUrl;

  private String  idCardFrontUrl;

  private  String idCardReverseUrl;

  private String firmId;

  /**
  * @Description 审批账号
  **/
  private String auditName;

  /**
  * @Description 注册方式
  **/
  private Integer registerType;

  /**
  * @Description 所属工商户
  **/
  List<HashMap> customName;


  /**
   * @Description 银行卡信息
   **/
  List<HashMap> bankInfo;

  /**
   * @Description 审核状态 1 待用户提交  2:待企业审核 3:政府审核中 4:审核成功 5:审核失败
   **/
  private Integer auditStatus;

  /**
   * @Description 状态描述
   **/
  private String statusDescription;

  /**
   * @Description 提交云控注册时记录错误节点  1: 身份证正面认证 2: 身份证反面认证
   * 3: 实名认证 4: 视频认证 5: 电子签名并提交注册工商户 6:无错误节点 全部完成注册
   **/
  private Integer yuncrErrNode;
  /**
   * @Description 提交云控注册时记录错误信息
   **/
  private String yuncrErrMessage;

  private String customKey;

  private String applyNumber;

  /**
   * @Description 回调地址
   **/
  private String callbackAddress;

  /**
   * @Description 回调失败次数
   **/
  private int callbackNumber;
  /**
   * @Description 回调状态 0:未进行回调 1:回调成功 2:回调失败
   **/
  private int callbackStatus;

  /**
   * @Description 上传此用户的商户ID
   **/
  private String merchantId;

  /**
  * @Description 错误节点
  **/
  private String failNodes;

  private List<YuncrUserFailNode> failNodeList;

}
