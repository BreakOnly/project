package com.jrmf.persistence;

import com.jrmf.domain.PaymentChannel;
import com.jrmf.domain.PaymentChannelRoute;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelRouteDao
 * @Description:
 * @create 2020/3/23 16:34
 */
@Mapper
public interface ChannelRouteDao {

    List<PaymentChannel> getChannelRouteBaseQuery(Map<String, Object> map);

    void insertChannel(PaymentChannel channelRoute);

    String getchannelRouteByPathNo(String pathNo, Integer id);

    String getChannelRouteByPathName(String pathName, Integer id);

    void updateChannel(PaymentChannel channelRoute);

    void deleteChannel(String id);

    List<Map<String, Object>> getChannelRouteAndLinkageBaseByPathNo(String pathNo);

    List<Map<String, Object>> getChannelRouteRelationByPathNo(String pathNo);

    List<PaymentChannelRoute> getServiceCompanyChannelRoute(Map<String, Object> map);

    void insertBusinessPaymentRoute(PaymentChannelRoute paymentChannelRoute);

    void insertCompanyPaymentRelation(int companyId, String paymentType, String implementor);

    PaymentChannelRoute getCompanyPaychannelRelationByCompanyIdAndPathNo(String pathNo, int companyId);

    void insertCompanyPaychannelRelation(PaymentChannelRoute paymentChannelRoute);

    void updateBusinessPaymentRoute(PaymentChannelRoute paymentChannelRoute);

    void updateCompanyPaychannelRelation(PaymentChannelRoute paymentChannelRoute);

    void updateCompanyPaymentRelation(int companyId, String type, String implementor);

    PaymentChannelRoute getCompanyPaychannelRelationByCompanyIdAndPathNoAndId(String pathNo, int companyId, int payChannelId);

    List<PaymentChannelRoute> getPaymentRouteByParam(Map<String, Object> param);

    void deleteCompanyPaymentRelation(String companyId, String paymentType);

    void deleteCompanyPaychannelRelation(String companyId, String pathNo);

    void deleteBusinessPaymentRoute(String companyId, String paymentType, String pathNo, String customKey, String isDefault);

    PaymentChannelRoute getCompanyPaymentRelationByCompanyIdAndType(int companyId, String type);

    List<PaymentChannelRoute> getBusinessPaymentRouteByCompanyIdAndPathNo(String companyId, String pathNo);

    List<PaymentChannelRoute> getBusinessPaymentRouteByCompanyIdAndType(String companyId, String paymentType);

    PaymentChannelRoute getBusinessPaymentRouteById(String id);

    PaymentChannel getChannelRouteById(Integer id);
}
