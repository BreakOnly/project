package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 种路路
 * @create 2020-03-16 10:24
 * @desc 清结算 业务经理统计  mingxi
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticBusinessManagerDetail extends StatisticBusinessManager {
    /**
     * 代理商名称
     */
    private String businessChannel;
    /**
     * 商户名称
     */
    private String customName;

    public static double getDoubleAmount(StatisticBusinessManagerDetail statisticBusinessManager) {
        return Double.parseDouble(statisticBusinessManager.amount);
    }
}
