package com.jrmf.persistence;

import com.jrmf.domain.SettleWithRate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Title: SettleWithRateDao
 * @Description:
 * @create 2020/2/14 18:16
 */
@Mapper
public interface SettleWithRateDao {

    /**
     * 根据customkey查询商户相关信息
     * @param customkey
     * @return
     */
    List<Map<String, Object>> queryMerchantInfo(Map<String, Object> customkey);

    /**
     * 根据customkey查询代理商相关信息
     * @param customkey
     * @return
     */
    List<Map<String, Object>> queryProxyInfo(Map<String, Object> customkey);

    /**
     * 新增变更费率
     * @param settleWithRate
     */
    void insertSettleWithRate(SettleWithRate settleWithRate);

    /**
     * 查询变更费率
     * @param paramMap
     * @return
     */
    List<SettleWithRate> querySettleWithRate(Map<String, Object> paramMap);

    /**
     * 查询变更费率数量
     * @param paramMap
     * @return
     */
    int querySettleWithRateCount(Map<String, Object> paramMap);

    List<Map<String, Object>> queryProxyInfoAndCompanyId(Map<String, Object> param);

    List<Map<String, Object>> queryMerchantInfoAndCompanyId(Map<String, Object> param);

    int querySettleWithRateByParamCount(Map<String, Object> map);

    List<Map<String, String>> queryMerchantRateByParam(Map<String, String> param);
}
