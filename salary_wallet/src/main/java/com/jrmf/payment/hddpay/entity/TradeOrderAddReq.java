package com.jrmf.payment.hddpay.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TradeOrderAddReq implements Serializable {

    private String outTradeNo;

    private String accountType;

    private String remark;

    private List<TradeItemReq> items;

}
