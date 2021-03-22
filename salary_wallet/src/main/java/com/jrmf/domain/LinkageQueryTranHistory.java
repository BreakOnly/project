package com.jrmf.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Title: LinkageQueryTranHistory
 * @Description:
 * @create 2020/3/12 11:15
 */
@Getter
@Setter
public class LinkageQueryTranHistory {

    private String startDate;

    private String endDate;

    private int pageNo;

    private String acctNo;

    private String orderMode;

    private int pageSize;

    private String mainAccount;

    private String opFlag;

    private String reqSubAccount;

    private String reqLastSubAccNo;

    private String reqLastDate;

    private String reqLastJNo;

    private String reqLastSeq;

}
