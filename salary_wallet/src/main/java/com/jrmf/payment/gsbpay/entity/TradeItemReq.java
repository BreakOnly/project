package com.jrmf.payment.gsbpay.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class TradeItemReq implements Serializable {

  private String accType; //账号类型,00银⾏01⽀付宝02微信03国美04富⺠银⾏

  private String accNo; //账号

  private String amt; //⾦额（单位分）

  private String idType; //证件类型,00=身份证

  private String idNo; //证件号

  private String idName; //证件对应名称

  private String mobile;//手机号

  private String note; //打款备注

  private String seqNo; //序列号

}
