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
public class StatisticCompany extends StatisticBase {
    /**
     * 平台名称
     */
    private String plantForm;
    /**
     * 服务公司名称
     */
    private String companyName;
    /**
     * 服务公司id
     */
    private String companyId;

}
