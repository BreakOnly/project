package com.jrmf.domain.vo;

import lombok.Data;

@Data
public class FundSummaryVO {
    private String customName;
    private String companyName;
    private String rechargeAmount;
    private String rechargeTimes;
    private String commissionAmount;
    private String commissionTimes;
    private String fee;

}
