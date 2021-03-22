package com.jrmf.payment.gsbpay.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class TradeQueryItemRsp implements Serializable {

  private String respDesc;
  private String tradeStatus;
  private String platformSeqNo;

}
