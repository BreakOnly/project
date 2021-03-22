package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 种路路
 * @create 2020-03-16 10:24
 * @desc 清结算 业务经理统计
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticOperationsManager extends StatisticBase {
    /**
     * 运营经理
     */
    private String operationsManager;
    /**
     * 交易商户数
     */
    private int count;

}
