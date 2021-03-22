package com.jrmf.persistence;

import com.jrmf.domain.CustomProxySubCommission;
import com.jrmf.domain.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-10-31 16:33
 * @desc
 **/
@Mapper
public interface ProxyCustomDao {
    /**
     * 根据参数查询条数
     * @param page 参数
     * @return int
     */
    int countByPage(Page page);

    /**
     * 根据参数查询列表
     * @param page 参数
     * @return list
     */
    List<CustomProxySubCommission> listByPage(Page page);
    /**
     * 根据参数查询列表
     * @param page 参数
     * @return list
     */
    List<CustomProxySubCommission> listByNoPage(Page page);

    /**
     * 根据参数删除列表
     * @param map 参数
     */
    void deleteByParam(Map<Object, Object> map);

    /**
     * 根据参数查询直接代理商 数据
     * @param map 参数
     * @return list
     */
    List<CustomProxySubCommission> calculateByParam(Map<Object, Object> map);

    /**
     * 批量添加
     * @param list 批量列表
     */
    void addCustomProxySubCommission(@Param("customProxySubCommissions") List<CustomProxySubCommission> list);
}
