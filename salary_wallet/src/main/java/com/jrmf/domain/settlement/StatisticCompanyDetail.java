package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 种路路
 * @create 2020-03-16 10:28
 * @desc 清结算 下发公司统计明细
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticCompanyDetail extends StatisticCompany {
    /**
     * 商户名称
     */
    private String customName;
}
