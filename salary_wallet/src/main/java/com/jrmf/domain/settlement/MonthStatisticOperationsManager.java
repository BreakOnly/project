package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class MonthStatisticOperationsManager extends StatisticBase {

    /**
     * 月份
     */
    private String month;
    /**
     * 运营经理
     */
    private String operationsManager;
    /**
     * 小额实发金额
     */
    private String smallAmount;
    /**
     * 大额实发金额
     */
    private String bigAmount;
    /**
     * 交易商户数
     */
    private int count;


}
