package com.jrmf.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2021/1/5 16:16
 * @description:
 */
@ApiModel
@Data
public class InvoiceCommissionDTO implements Serializable {

  private  int id;

  @ApiModelProperty(name = "companyId", value = "服务公司ID", required = false)
  private String companyId;

  @ApiModelProperty(name = "companyName", value = "商户名称", required = false)
  private String companyName;

  @ApiModelProperty(name = "customKey", value = "商户key", required = false)
  private String customKey;

  @ApiModelProperty(name = "receiptStatus", value = "回单状态 0 无回单 1有回单 ", required = false)
  private Integer receiptStatus;

  @ApiModelProperty(name = "invoiceStatus", value = "发票状态", required = false)
  private String invoiceStatus;

  @ApiModelProperty(name = "individualName", value = "个体户名称", required = false)
  private String individualName;

  private String accountTime;

  private String  amount;

  private String fee;

  @ApiModelProperty(name = "inAccountNo", value = "收款账号", required = false)
  private String inAccountNo;

  private String inAccountName;

  private String firmId;

  @ApiModelProperty(name = "contractCompanyName", value = "发包商", required = false)
  private String contractCompanyName;

  @ApiModelProperty(name = "certId", value = "证件号", required = false)
  private String certId;

  private String receiptUrl;
  @ApiModelProperty(name = "accountStartDate", value = "查询起始时间", required = false)
  private String accountStartDate;
  @ApiModelProperty(name = "accountEndDate", value = "查询结束时间", required = false)
  private String accountEndDate;
  @ApiModelProperty(name = "amountStart", value = "起始金额", required = false)
  private String amountStart;
  @ApiModelProperty(name = "amountEnd", value = "结束金额", required = false)
  private String amountEnd;

  private String accountTimeForMonth;
  @ApiModelProperty(name = "ids", value = "用户选择的数据ID(逗号分割,只有用户选择时需要)", required = false)
  private String ids;

  private  String commissionId;
  @ApiModelProperty(name = "remark", value = "备注", required = false)
  private  String remark;
  @ApiModelProperty(name = "taskId", value = "任务ID(提交开票的时候需要传)", required = false)
  private  Integer taskId;

  private String listNumber;
  @ApiModelProperty(name = "pageNo", value = "页数", required = false)
  private Integer pageNo = 1;
  @ApiModelProperty(name = "pageSize", value = "每页数量", required = false)
  private Integer pageSize = 10;


  private List listId;

  private List invoiceStatusList;

}
