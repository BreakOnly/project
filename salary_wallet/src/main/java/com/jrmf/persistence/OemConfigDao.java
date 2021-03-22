package com.jrmf.persistence;

import com.jrmf.domain.OemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-03-12 16:26
 * @desc
 **/
@Mapper
public interface OemConfigDao {
    /**
     * 根据条件查询oem配置信息；
     * @param map 查询条件
     * @return
     */
    List<OemConfig> getOemByParam(Map<String, Object> map);

    /**
     * 查询OEM信息
     * @param paramMap
     * @return
     */
//    @Select({
//            "<script>",
//            "SELECT * FROM oem_config",
//            "where 1=1" +
//            "<if test='portalDomain != null and portalDomain != '' '>" +
//            "and portal_domain = #{portalDomain}" +
//            "</if>" +
//            "<script>"
//    })
    List<OemConfig> getOemConfig(Map<String, Object> paramMap);

    /**
     * 根据ID查询OEM信息
     * @param id
     * @return
     */
    OemConfig getOemConfigById(@Param("id") String id);

    /**
     * 根据ID删除OEM信息
     * @param id
     */
    void deleteOemConfig(@Param("id") String id);

    /**
     * 修改OEM配置
     * @param oemConfig
     */
    void updateOemConfig(OemConfig oemConfig);

    /**
     * 新增OEM配置
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
