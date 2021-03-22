package com.jrmf.payment.gsbpay.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class TradePayRsp implements Serializable {

  private String code;
  private String msg;
  private TradeQueryRsp data;

}
