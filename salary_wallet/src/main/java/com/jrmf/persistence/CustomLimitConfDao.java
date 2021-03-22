package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.CustomLimitConf;

@Mapper
public interface CustomLimitConfDao {
    /**
     * 添加配置
     *
     * @param customLimitConf 新增配置
     */
    int insertCustomLimitConf(CustomLimitConf customLimitConf);

    CustomLimitConf getCustomLimitConf(Map<String, Object> param);


    /**
     * 获取商户配置限额列表
     *
     * @param hashMap 入参
     *                pageNo 页码
     *                pageSize 单页条数
     * @return list
     */
    List<CustomLimitConf> listLimitConfByParams(Map<String, Object> hashMap);

    /**
     * 删除配置
     *
     * @param id 配置id
     */
    void deleteConfig(String id);

    /**
     * 修改配置
     *
     * @param customLimitConf 修改配置
     */
    void updateConfig(CustomLimitConf customLimitConf);

}
