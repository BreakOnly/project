package com.jrmf.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Title: PaymentChannelRoute
 * @Description:
 * @create 2020/3/25 10:10
 */
@Getter
@Setter
public class PaymentChannelRoute {

    private int id;

    private int payChannelId;

    private int companyId;

    private String paymentType;

    private String pathNo;

    private String customKey;

    private int isDefault;

    private String createTime;

    private String updateTime;

    private String isSubAccount;

    private String keyWords;

    private String containKeyWords;

    private String shadowAcctNo;

    private String corporationAccount;

    private String corporationAccountName;

    private String corpToBankStandardCode;

    private String corporationName;

    private String preHost;

    private String remotePort;

    private String readTimeOut;

    private int status;

    private String payPublicKey;

    private String payPrivateKey;

    private String parameter1;

    private String parameter2;

    private String parameter3;

    private String parameter4;

    private String parameter5;

    private String parameter6;

    private String parameter7;

    private String parameter8;

    private String parameter9;

    private String pathName;

    private String customName;

    private String implementor;

    private String ids;

    private String apiKey;

    private String connectTimeOut;

    private String companyName;

    private String paymentName;

    private Integer pathType;

    private Integer pathKeyType;
}
