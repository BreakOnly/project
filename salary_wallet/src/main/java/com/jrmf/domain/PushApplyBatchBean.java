package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * @author: YJY
 * @date: 2021/1/8 14:04
 * @description:
 */
@Data
@ToString
public class PushApplyBatchBean implements Serializable {

  private int id;

  private String invoiceMoney;

  private String individualName;

  private String phone;

  private String address;

  private String businessLicenseNumber;

  private String governmentAuditDate;

  private String firmId;

  private String bidNo;

  private String idCard;

  private String inAccountNo;

  private Integer step;

  private String stepStatus;

  private String platsrl;

  private String tradeMonth;

  private String taskName;

  private String taskDesc;

  //结算单系统流水号
  private String settlementSerialNumber;
  //云控合同流水号
  private String contractSerialNumber;
  //云控合同编号
  private String contractFileNo;

  //结算系统流水号
  private String pushSettlementSerialNumber;
}
