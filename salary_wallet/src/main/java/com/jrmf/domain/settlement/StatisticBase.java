package com.jrmf.domain.settlement;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-03-16 10:24
 * @desc 清结算 基础统计
 **/
@Data
public class StatisticBase {
    /**
     * 交易总金额
     */
    String amount;
    /**
     * 统计交易开始时间
     */
    private String startTime;
    /**
     * 统计交易结束时间
     */
    private String endTime;


}
