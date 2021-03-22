package com.jrmf.service;

import com.jrmf.domain.SettleWithRate;
import com.jrmf.persistence.SettleWithRateDao;
import com.jrmf.utils.RespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: SettleWithRateServiceImpl
 * @Description:
 * @create 2020/2/14 17:51
 */
@Service("SettleWithRateService")
public class SettleWithRateServiceImpl implements SettleWithRateService{

    private static final Logger logger = LoggerFactory.getLogger(SettleWithRateServiceImpl.class);

    @Autowired
    private SettleWithRateDao settleWithRateDao;

    /**
     * 新增变更费率
     * @param settleWithRate
     * @return
     */
    @Override
    public Map<String, Object> insertSettleWithRate(SettleWithRate settleWithRate) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        try {
            settleWithRateDao.insertSettleWithRate(settleWithRate);
        } catch (Exception e) {
            logger.error("新增变更费率失败{}",e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "新增失败请联系管理员");
            return result;
        }
        return result;
    }

    /**
     * 查询变更费率
     * @param paramMap
     * @return
     */
    @Override
    public List<SettleWithRate> querySettleWithRate(Map<String, Object> paramMap) {
        return settleWithRateDao.querySettleWithRate(paramMap);
    }

    /**
     * 查询变更费率数量
     * @param paramMap
     * @return
     */
    @Override
    public int querySettleWithRateCount(Map<String, Object> paramMap) {
        return settleWithRateDao.querySettleWithRateCount(paramMap);
    }

    /**
     * 根据customkey查询代理商相关信息
     * @param map
     * @return
     */
    @Override
    public List<Map<String, Object>> queryProxyInfo(Map<String, Object> map) {
        return settleWithRateDao.queryProxyInfo(map);
    }

    /**
     * 根据customkey查询商户相关信息
     * @param map
     * @return
     */
    @Override
    public List<Map<String, Object>> queryMerchantInfo(Map<String, Object> map) {
        return settleWithRateDao.queryMerchantInfo(map);
    }

    @Override
    public List<Map<String, Object>> queryProxyInfoAndCompanyId(Map<String, Object> param) {
        return settleWithRateDao.queryProxyInfoAndCompanyId(param);
    }

    @Override
    public List<Map<String, Object>> queryMerchantInfoAndCompanyId(Map<String, Object> param) {
        return settleWithRateDao.queryMerchantInfoAndCompanyId(param);
    }

    @Override
    public int querySettleWithRateByParamCount(Map<String, Object> map) {
        return settleWithRateDao.querySettleWithRateByParamCount(map);
    }

    /**
     * 查询所有商户费率
     * @param param
     * @return
     */
    @Override
    public List<Map<String, String>> queryMerchantRateByParam(Map<String, String> param) {
        return settleWithRateDao.queryMerchantRateByParam(param);
    }

}
