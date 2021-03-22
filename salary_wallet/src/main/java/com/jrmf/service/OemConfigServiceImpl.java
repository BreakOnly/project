package com.jrmf.service;

import com.alibaba.fastjson.JSON;
import com.jrmf.common.CommonString;
import com.jrmf.domain.OemConfig;
import com.jrmf.persistence.OemConfigDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-03-12 16:24
 * @desc
 **/
@Service("oemConfigService")
public class OemConfigServiceImpl implements OemConfigService {

    private final OemConfigDao oemConfigDao;
    private UtilCacheManager cacheManager;

    @Autowired
    public OemConfigServiceImpl(OemConfigDao oemConfigDao, UtilCacheManager cacheManager) {
        this.oemConfigDao = oemConfigDao;
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void loadAndInit() {
        Map<String, Object> params = new HashMap<>(2);
        params.put("status",1);
        for (OemConfig oemConfig : oemConfigDao.getOemByParam(params)) {
            cacheManager.put(oemConfig.getPortalDomain(), JSON.toJSONString(oemConfig), -1);
            cacheManager.put(oemConfig.getClientDomain(), JSON.toJSONString(oemConfig), -1);
        }
    }

    /**
     * 根据条件查询oem配置信息；
     *
     * @param map 查询条件
     * @return oem
     */
    @Override
    public OemConfig getOemByParam(Map<String, Object> map) {
        Object portalDomain = cacheManager.get(map.get("portalDomain").toString());
        if(portalDomain == null){
            map.put("status",1);
            List<OemConfig> list = oemConfigDao.getOemByParam(map);
            if(list.isEmpty()){
                portalDomain = cacheManager.get(CommonString.PORTAL_DOMAIN);
            }else{
                for (OemConfig oemConfig : list) {
                    cacheManager.put(oemConfig.getPortalDomain(), JSON.toJSONString(oemConfig), -1);
                    cacheManager.put(oemConfig.getClientDomain(), JSON.toJSONString(oemConfig), -1);
                }
                return list.get(0);
            }
        }
        return JSON.parseObject(portalDomain.toString(), OemConfig.class);
    }

    /**
     * 重新加载缓存
     */
    @Override
    public void reloadCache() {
        cacheManager.remove("jrmf_task_instance_01");
        cacheManager.remove("jrmf_task_instance_02");

        Map<String, Object> map = new HashMap<>(2);
        map.put("status",1);
        for (OemConfig oemConfig : oemConfigDao.getOemByParam(map)) {
            cacheManager.put(oemConfig.getPortalDomain(), JSON.toJSONString(oemConfig), -1);
            cacheManager.put(oemConfig.getClientDomain(), JSON.toJSONString(oemConfig), -1);
        }
    }

    /**
     * 查询OEM信息
     * @param paramMap
     * @return
     */
    @Override
    public List<OemConfig> getOemConfig(Map<String, Object> paramMap) {
        return oemConfigDao.getOemConfig(paramMap);
    }

    /**
     * 根据ID查询OEM信息
     * @param id
     * @return
     */
    @Override
    public OemConfig getOemConfigById(String id) {
        return oemConfigDao.getOemConfigById(id);
    }

    /**
     * 根据ID删除OEM信息
     * @param id
     */
    @Override
    public void deleteOemConfig(String id) {
        oemConfigDao.deleteOemConfig(id);
    }

    /**
     * 修改OEM配置
     * @param oemConfig
     */
    @Override
    public void updateOemConfig(OemConfig oemConfig) {
        oemConfig.setUpdateTime(DateUtils.getNowDate());
        oemConfigDao.updateOemConfig(oemConfig);
    }

    /**
     * 新增OEM配置
     * @param oemConfig
     */
    @Override
    public void insertOemConfig(OemConfig oemConfig) {
        oemConfigDao.insertOemConfig(oemConfig);
    }

    /**
     * 根据ID修改OEM的图片为空
     * @param oc
     */
    @Override
    public void updateOemConfigIsNull(OemConfig oc) {
        oemConfigDao.updateOemConfigIsNull(oc);
    }

    /**
     * 通过customkey获取OEM信息
     * @param customkey
     * @return
     */
    @Override
    public int getOemConfigByCustomkey(String customkey) {
        return oemConfigDao.getOemConfigByCustomkey(customkey);
    }
}
