package com.jrmf.service;

import com.jrmf.domain.OemConfig;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-03-12 16:11
 * @desc
 **/
public interface OemConfigService {
    /**
     * 根据条件查询oem配置信息；
     * @param map 查询条件
     * @return
     */
    OemConfig getOemByParam(Map<String, Object> map);

    /**
     * 重新加载缓存
     */
    void reloadCache();

    /**
     * 查询OEM信息
     * @param paramMap
     * @return
     */
    List<OemConfig> getOemConfig(Map<String, Object> paramMap);

    /**
     * 根据ID查询OEM信息
     * @param id
     * @return
     */
    OemConfig getOemConfigById(String id);

    /**
     * 根据ID删除OEM信息
     * @param id
     */
    void deleteOemConfig(String id);

    /**
     * 修改OEM信息
     * @param oemConfig
     */
    void updateOemConfig(OemConfig oemConfig);

    /**
     * 新增OEM信息
     * @param oemConfig
     */
    void insertOemConfig(OemConfig oemConfig);

    /**
     * 根据ID修改OEM的图片为空
     * @param oc
     */
    void updateOemConfigIsNull(OemConfig oc);

    /**
     * 通过customkey获取OEM信息
     * @param customkey
     * @return
     */
    int getOemConfigByCustomkey(String customkey);
}
