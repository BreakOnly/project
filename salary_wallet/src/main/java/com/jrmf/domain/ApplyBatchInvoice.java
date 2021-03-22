package com.jrmf.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author: YJY
 * @date: 2021/1/5 15:47
 * @description:
 */
@ApiModel
@Data
public class ApplyBatchInvoice implements Serializable {

  private Integer id;

  @ApiModelProperty(name = "tradeMonth", value = "交易月份", required = false)
  private String tradeMonth;

  @ApiModelProperty(name = "companyName", value = "商户名称", required = false)
  private String companyName;

  @ApiModelProperty(name = "individualName", value = "个体户名称", required = false)
  private String individualName;

  @ApiModelProperty(name = "tradeMoney", value = "交易金额", required = false)
  private String tradeMoney;

  @ApiModelProperty(name = "tradeMoney", value = "发票金额(结算金额)", required = false)
  private String invoiceMoney;

  @ApiModelProperty(name = "tradeNumber", value = "交易笔数", required = false)
  private String tradeNumber;

  @ApiModelProperty(name = "idCard", value = "证件号", required = false)
  private String  idCard;

  @ApiModelProperty(name = "applyBatchRemark", value = "申请批次备注", required = false)
  private String applyBatchRemark;

  @ApiModelProperty(name = "invoiceStatus", value = "开票状态  1:已开票 2:未开票", required = false)
  private Integer invoiceStatus;

  @ApiModelProperty(name = "invoiceStatusDescribe", value = "开票状态描述", required = false)
  private String invoiceStatusDescribe;

  @ApiModelProperty(name = "step", value = "执行步骤 1:推送合同 2:推送结算 3:上传结算单 4:上传发票 5:全部完成", required = false)
  private Integer step;

  @ApiModelProperty(name = "stepStatus", value = "步骤状态 1 成功 2 处理中 3 失败", required = false)
  private String stepStatus;

  @ApiModelProperty(name = "customFirmName", value = "发包商公司名称", required = false)
  private String customFirmName;

  //发布的项目ID
  private Integer channelTaskId;
  //合同url
  private String contractUrl;
  //结算单url
  private String finalStatementUrl;
  //发票url
  private String invoiceUrl;
  //发票票面号
  private String invoiceNumber;
  //推送结算
  private String settlementCard;

  @ApiModelProperty(name = "inAccountNo", value = "银行卡号", required = false)
  private String inAccountNo;
  //发票序列号
  private String invoiceSerialNumber;
  //结算系统流水号
  private String settlementSerialNumber;
  //云控合同流水号
  private String contractSerialNumber;
  //云控合同编号
  private String contractFileNo;

  //实发服务公司ID
  private Integer companyId;
  // 创建时间
  private String  createTime;
  //更新时间
  private String updateTime;
  private String contractName;
  private String finalStatementName;
  private String invoiceName;
  private int updateInvoiceInfo;

  //  非数据库字段
  private String taskName;//项目名称
  private String fullEcoCateName;//项目类型
  private String fullInvoiceCategoryName;//开票信息
  private String invoiceType;//发票类型

}
