package com.jrmf.service;

import com.jrmf.domain.CustomProxySubCommission;
import com.jrmf.domain.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-10-31 16:26
 * @desc
 **/
public interface ProxyCustomService {

    /**
     * 根据参数查询条数
     */
    int countByPage(Page page);

    /**
     * 根据参数查询列表
     */
    List<CustomProxySubCommission> listByPage(Page page);
    /**
     * 根据参数查询列表
     */
    List<CustomProxySubCommission> listByNoPage(Page page);

    /**
     * 计算代理商分佣
     * @param againType 1.全部、2.服务公司、3.代理商、4.商户
     * @param merchantId customkey
     * @param month 月份
     */
    void calculate(int againType, String merchantId, String month, String businessPlatformId);

    /**
     * 根据条件删除数据
     * @param map 参数
     */
    void deleteByParam(Map<Object, Object> map);
}
