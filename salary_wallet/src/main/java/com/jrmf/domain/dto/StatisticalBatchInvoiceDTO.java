package com.jrmf.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2021/1/5 17:51
 * @description:
 */
@ApiModel
@Data
public class StatisticalBatchInvoiceDTO implements Serializable {

  @ApiModelProperty(name = "发票总金额",value = "invoiceMoney")
  private String invoiceMoney;

  @ApiModelProperty(name = "交易总金额",value = "tradeMoney")
  private String tradeMoney;

  @ApiModelProperty(name = "商户数量",value = "companyNameCount")
  private int companyNameCount;

  @ApiModelProperty(name = "开票笔数",value = "tradeNumber")
  private int tradeNumber;

  @ApiModelProperty(name = "发包商(公司名称)",value = "contractCompanyName")
  private String contractCompanyName;

  private int  contractCompanyNameCount;
  @ApiModelProperty(name = "个体工商户数",value = "certIdCount")
  private int certIdCount;
}
