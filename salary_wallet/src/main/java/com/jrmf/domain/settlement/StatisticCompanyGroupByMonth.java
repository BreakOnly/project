package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 种路路
 * @create 2020-03-16 10:24
 * @desc 清结算 下发公司统计
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticCompanyGroupByMonth extends StatisticCompany {
    /**
     * 月份
     */
    private String month;
    /**
     * 小额实发金额
     */
    private String smallAmount;
    /**
     * 大额实发金额
     */
    private String bigAmount;

}
