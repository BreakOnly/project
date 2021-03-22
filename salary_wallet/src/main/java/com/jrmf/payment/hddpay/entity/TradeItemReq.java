package com.jrmf.payment.hddpay.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradeItemReq implements Serializable {

    private String name;

    private String idCard;

    private String cardNo;

    private String mobile;

    private String amount;

    private String remark;

}
