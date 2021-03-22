package com.jrmf.service;

import com.jrmf.domain.PaymentChannel;
import com.jrmf.domain.PaymentChannelRoute;
import com.jrmf.persistence.ChannelRouteDao;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.ChannelRouteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: ChannelRouteServiceImpl
 * @Description:
 * @create 2020/3/23 16:33
 */
@Service("channelRouteService")
public class ChannelRouteServiceImpl implements ChannelRouteService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelRouteServiceImpl.class);

    @Autowired
    private ChannelRouteDao channelRouteDao;

    /**
     * 获取通道路由基础配置信息
     *
     * @param map
     * @return
     */
    @Override
    public List<PaymentChannel> getChannelRouteBaseQuery(Map<String, Object> map) {
        return channelRouteDao.getChannelRouteBaseQuery(map);
    }

    @Override
    public void insertChannel(PaymentChannel channelRoute) {
        channelRouteDao.insertChannel(channelRoute);
    }

    @Override
    public String getChannelRouteByPathNo(String pathNo, Integer id) {
        return channelRouteDao.getchannelRouteByPathNo(pathNo, id);
    }

    @Override
    public String getChannelRouteByPathName(String name, Integer id) {
        return channelRouteDao.getChannelRouteByPathName(name, id);
    }

    /**
     * 修改通道基础信息
     *
     * @param channelRoute
     */
    @Override
    public void updateChannel(PaymentChannel channelRoute) {
        channelRouteDao.updateChannel(channelRoute);
    }

    /**
     * 删除通道
     *
     * @param id
     */
    @Override
    public void deleteChannel(String id) {
        channelRouteDao.deleteChannel(id);
    }

    /**
     * 查询通道有没有跟资金联动基础表做关联
     *
     * @param pathNo
     * @return
     */
    @Override
    public List<Map<String, Object>> getChannelRouteAndLinkageBaseByPathNo(String pathNo) {
        return channelRouteDao.getChannelRouteAndLinkageBaseByPathNo(pathNo);
    }

    /**
     * 查询通道有没有和下发公司做关联
     *
     * @param pathNo
     * @return
     */
    @Override
    public List<Map<String, Object>> getChannelRouteRelationByPathNo(String pathNo) {
        return channelRouteDao.getChannelRouteRelationByPathNo(pathNo);
    }

    /**
     * 查询下发公司配置通道路由信息
     *
     * @param map
     * @return
     */
    @Override
    public List<PaymentChannelRoute> getServiceCompanyChannelRoute(Map<String, Object> map) {
        return channelRouteDao.getServiceCompanyChannelRoute(map);
    }

    /**
     * 新增/修改下发公司通道路由信息
     *
     * @param paymentChannelRoute
     * @return
     */
    @Override
    public Map<String, Object> serviceCompanyChannelRouteConfig(
        PaymentChannelRoute paymentChannelRoute) throws Exception {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        if (StringUtil.isEmpty(paymentChannelRoute.getIsSubAccount())) {
            paymentChannelRoute.setIsSubAccount("0");
        }

        // 付款类型
        String[] paymentType = paymentChannelRoute.getPaymentType().split(",");
        List<PaymentChannelRoute> channelRoute;

        if (!StringUtil.isEmpty(paymentChannelRoute.getIds())) {
            PaymentChannelRoute routeIfExist = channelRouteDao
                .getCompanyPaychannelRelationByCompanyIdAndPathNoAndId(
                    paymentChannelRoute.getPathNo(), paymentChannelRoute.getCompanyId(),
                    paymentChannelRoute.getPayChannelId());
            if (routeIfExist != null) {
                result.put(RespCode.RESP_STAT, RespCode.error101);
                result.put(RespCode.RESP_MSG, "通道路由已存在");
                return result;
            }

            for (String type : paymentType) {
                Map<String, Object> param = new HashMap<>(9);
                param.put("companyId", paymentChannelRoute.getCompanyId());
                param.put("paymentType", type);
                param.put("isDefault", paymentChannelRoute.getIsDefault());
                int isDefault = paymentChannelRoute.getIsDefault();
                if (isDefault == 0) {
                    param.put("pathNo", paymentChannelRoute.getPathNo());
                }
                if (!StringUtil.isEmpty(paymentChannelRoute.getCustomKey())) {
                    param.put("customKey", paymentChannelRoute.getCustomKey());
                    channelRoute = channelRouteDao.getPaymentRouteByParam(param);
                } else {
                    channelRoute = channelRouteDao.getPaymentRouteByParam(param);
                }

                String[] ids = paymentChannelRoute.getIds().split(",");
                if (!channelRoute.isEmpty()) {
                    int count = 0;
                    for (String id : ids) {
                        if (!id.equals(channelRoute.get(0).getId() + "")) {
                            count++;
                        }
                    }
                    if (ids.length == count) {
                        logger.info("---------已配置路由---------");
                        throw new ChannelRouteException("已配置路由");
                    }

                    for (String id : ids) {
                        if (id.equals(channelRoute.get(0).getId() + "")) {
                            PaymentChannelRoute route = new PaymentChannelRoute();
                            BeanUtils.copyProperties(paymentChannelRoute, route);
                            route.setId(channelRoute.get(0).getId());
                            route.setPaymentType(type);
                            channelRouteDao.updateBusinessPaymentRoute(route);
                        }
                    }
                } else {
                    for (String id : ids) {
                        PaymentChannelRoute route = channelRouteDao.getBusinessPaymentRouteById(id);
                        if (route != null && route.getPaymentType().equals(type)) {
                            PaymentChannelRoute r = new PaymentChannelRoute();
                            BeanUtils.copyProperties(paymentChannelRoute, r);
                            r.setId(Integer.parseInt(id));
                            r.setPaymentType(type);
                            channelRouteDao.updateBusinessPaymentRoute(r);
                        }
                    }
                }
                channelRouteDao.updateCompanyPaychannelRelation(paymentChannelRoute);
            }
            return result;
        }
        for (String type : paymentType) {
            Map<String, Object> param = new HashMap<>(8);
            param.put("companyId", paymentChannelRoute.getCompanyId());
            param.put("paymentType", type);
            param.put("isDefault", paymentChannelRoute.getIsDefault());
            int isDefault = paymentChannelRoute.getIsDefault();
            if (isDefault == 0) {
                param.put("pathNo", paymentChannelRoute.getPathNo());
            }
            if (!StringUtil.isEmpty(paymentChannelRoute.getCustomKey())) {
                param.put("customKey", paymentChannelRoute.getCustomKey());
                channelRoute = channelRouteDao.getPaymentRouteByParam(param);
            } else {
                channelRoute = channelRouteDao.getPaymentRouteByParam(param);
            }

            if (!channelRoute.isEmpty()) {
                logger.info("---------已配置路由---------");
                throw new ChannelRouteException("已配置路由");
            }

            paymentChannelRoute.setPaymentType(type);
            channelRouteDao.insertBusinessPaymentRoute(paymentChannelRoute);

            PaymentChannelRoute route = channelRouteDao
                .getCompanyPaymentRelationByCompanyIdAndType(paymentChannelRoute.getCompanyId(),
                    type);
            if (route == null) {
                channelRouteDao
                    .insertCompanyPaymentRelation(paymentChannelRoute.getCompanyId(), type,
                        paymentChannelRoute.getImplementor());
            }

            PaymentChannelRoute channel = channelRouteDao
                .getCompanyPaychannelRelationByCompanyIdAndPathNo(paymentChannelRoute.getPathNo(),
                    paymentChannelRoute.getCompanyId());
            if (channel == null) {
                channelRouteDao.insertCompanyPaychannelRelation(paymentChannelRoute);
            } else {
                paymentChannelRoute.setPayChannelId(channel.getId());
                channelRouteDao.updateCompanyPaychannelRelation(paymentChannelRoute);
            }

        }
        return result;
    }

    /**
     * 删除下发公司通道路由信息
     *
     * @return
     */
    @Override
    public Map<String, Object> deleteServiceCompanyChannelRoute(String companyId, String paymentType, String pathNo, String customKey, String isDefault) {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        String[] splitType = paymentType.split(",");
        for (String type : splitType) {
            channelRouteDao.deleteBusinessPaymentRoute(companyId, type, pathNo, customKey, isDefault);
            List<PaymentChannelRoute> routeList = channelRouteDao.getBusinessPaymentRouteByCompanyIdAndPathNo(companyId, pathNo);
            if (routeList.isEmpty() && routeList.size() <= 0) {
                channelRouteDao.deleteCompanyPaychannelRelation(companyId, pathNo);
            }
            List<PaymentChannelRoute> list = channelRouteDao.getBusinessPaymentRouteByCompanyIdAndType(companyId, type);
            if (list.isEmpty() && list.size() <= 0) {
                channelRouteDao.deleteCompanyPaymentRelation(companyId, type);
            }
        }
        return result;
    }

    @Override
    public PaymentChannel getChannelRouteById(Integer id) {
        return channelRouteDao.getChannelRouteById(id);
    }
}
