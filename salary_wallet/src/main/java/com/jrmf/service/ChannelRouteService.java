package com.jrmf.service;

import com.jrmf.domain.PaymentChannel;
import com.jrmf.domain.PaymentChannelRoute;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelRouteService
 * @Description:
 * @create 2020/3/23 16:33
 */
@Service
public interface ChannelRouteService {

    /**
     * 查询通道基础信息
     * @param map
     * @return
     */
    List<PaymentChannel> getChannelRouteBaseQuery(Map<String, Object> map);

    /**
     * 新增通道基础信息
     * @param channelRoute
     */
    void insertChannel(PaymentChannel channelRoute);

    String getChannelRouteByPathNo(String pathNo, Integer id);

    String getChannelRouteByPathName(String name, Integer id);

    /**
     * 修改通道基础信息
     * @param channelRoute
     */
    void updateChannel(PaymentChannel channelRoute);

    /**
     * 删除通道
     * @param id
     */
    void deleteChannel(String id);

    /**
     * 查询通道有没有跟资金联动基础表做关联
     * @param pathNo
     * @return
     */
    List<Map<String, Object>> getChannelRouteAndLinkageBaseByPathNo(String pathNo);

    /**
     * 查询通道有没有和下发公司做关联
     * @param pathNo
     * @return
     */
    List<Map<String, Object>> getChannelRouteRelationByPathNo(String pathNo);

    /**
     * 查询下发公司配置通道路由信息
     * @param map
     * @return
     */
    List<PaymentChannelRoute> getServiceCompanyChannelRoute(Map<String, Object> map);

    /**
     * 新增/修改下发公司通道路由信息
     * @param paymentChannelRoute
     * @return
     */
    Map<String, Object> serviceCompanyChannelRouteConfig(PaymentChannelRoute paymentChannelRoute) throws Exception;

    /**
     * 删除服务公司通道路由
     * @param
     */
    Map<String, Object>  deleteServiceCompanyChannelRoute(String companyId, String paymentType, String pathNo, String customKey, String isDefault);

    PaymentChannel getChannelRouteById(Integer id);
}
