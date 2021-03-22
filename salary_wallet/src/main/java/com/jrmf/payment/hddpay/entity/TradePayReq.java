package com.jrmf.payment.hddpay.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradePayReq implements Serializable {
    private String outTradeNo;
    private String cardNo;
    private String remark;
}
