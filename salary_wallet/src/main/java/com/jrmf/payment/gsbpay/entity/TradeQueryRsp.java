package com.jrmf.payment.gsbpay.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class TradeQueryRsp implements Serializable {

  private String outTradeNo;
  private List<TradeQueryItemRsp> bizContent;


}
