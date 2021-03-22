package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2019-10-31 15:12
 * @desc 代理商分佣
 **/
@Data
public class CustomProxySubCommission {
    /**
     * 主键
     */
    private int id;
    /**
     * 时间yyyy-MM
     */
    private String time;
    /**
     * 代理商户key
     */
    private String proxyCustomKey;
    /**
     * 代理商户名称
     */
    private String proxyCustomName;
    /**
     * 商户key
     */
    private String customKey;
    /**
     * 商户名称
     */
    private String customName;
    /**
     * 商户类型
     1,直接商户
     2.间接商户
     */
    private int proxyType;
    /**
     * 服务公司id
     */
    private String companyId;
    /**
     * 服务公司名称
     */
    private String companyName;
    /**
     * 商户下发额
     */
    private String amount;
    /**
     * 返佣金额
     */
    private String returnCommissionAmount;
    /**
     * 商户承担费率
     */
    private String calculationRates;
    /**
     * 商户承担服务费
     */
    private String customServiceFee;
    /**
     * 代理商成本费率
     */
    private String proxyFeeRate;
    /**
     * 下发公司费率id，company_netfile_rate_conf  id
     */
    private int companyNetFileRateConfId;
    /**
     * 直接代理商key
     */
    private String directProxyCustomKey;
    /**
     * 金额档位标签
     */
    private int gearLabel;
    /**
     * 档位最小金额
     */
    private String amountStart;
    /**
     * 金额范围运算符
     */
    private String operator;
    /**
     * 档位最大金额
     */
    private String amountEnd;
    /**
     * 直接代理商名称
     */
    private String directProxyCustomName;
    /**
     * 统计计算方式：
     1.本级代理商直接成本统计
     2.下级代理差额成本统计
     */
    private int countType;
    /**
     * 下级代理差额收益率
     */
    private String nextLevelProxyDiffEarnRate;
    /**
     * 父级代理id  对应custom_proxy id
     */
    private String parentId;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 变更费率
     */
    private String modifyRate;
    /**
     * 变更后成本服务费
     */
    private String modifyProxyFee;
    /**
     * 变更费率开始时间
     */
    private String modifyEffectStartTime;
    /**
     * 变更费率结束时间
     */
    private String modifyEffectEndTime;
    /**
     * 变更费率操作人
     */
    private String modifyAddUser;


}
