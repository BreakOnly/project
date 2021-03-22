package com.jrmf.service;

import com.jrmf.domain.SettleWithRate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Title: SettleWithRateService
 * @Description:
 * @create 2020/2/14 17:51
 */
@Service
public interface SettleWithRateService {
    /**
     * 新增变更费率
     * @param settleWithRate
     * @return
     */
    Map<String, Object> insertSettleWithRate(SettleWithRate settleWithRate);

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

    /**
     * 根据customkey查询代理商相关信息
     * @param map
     * @return
     */
    List<Map<String, Object>> queryProxyInfo(Map<String, Object> map);

    /**
     * 根据customkey查询商户相关信息
     * @param map
     * @return
     */
    List<Map<String, Object>> queryMerchantInfo(Map<String, Object> map);


    List<Map<String, Object>> queryProxyInfoAndCompanyId(Map<String, Object> param);

    List<Map<String, Object>> queryMerchantInfoAndCompanyId(Map<String, Object> param);

    int querySettleWithRateByParamCount(Map<String, Object> map);

    /**
     * 查询所有商户费率
     * @param param
     * @return
     */
    List<Map<String, String>> queryMerchantRateByParam(Map<String, String> param);
}
