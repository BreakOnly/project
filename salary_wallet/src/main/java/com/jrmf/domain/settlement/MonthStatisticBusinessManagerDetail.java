package com.jrmf.domain.settlement;

import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class MonthStatisticBusinessManagerDetail extends MonthStatisticBusinessManager {
    /**
     * 代理商名称
     */
    private String businessChannel;
    /**
     * 商户名称
     */
    private String customName;

}
