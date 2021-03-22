package com.jrmf.persistence;

import com.jrmf.domain.ProxyCostMaintain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Title: ProxyCostMaintainDao
 * @Description: 代理商成本维护管理
 * @create 2019/10/31 10:16
 */
@Mapper
public interface ProxyCostMaintainDao {

    /**
     * 获取代理商成本维护信息
     * @param paramMap
     * @return
     */
    List<ProxyCostMaintain> getProxyCostMaintainList(Map<String, Object> paramMap);

    /**
     * 新增代理商成本维护信息
     * @param proxyCostMaintain
     */
    void insertProxyCostMaintain(ProxyCostMaintain proxyCostMaintain);

    /**
     * 修改代理商成本维护信息
     * @param proxyCostMaintain
     */
    void updateProxyCostMaintain(ProxyCostMaintain proxyCostMaintain);

    /**
     * 通过id查询代理商成本信息
     * @param id
     * @return
     */
    ProxyCostMaintain queryProxyCostMaintainById(@Param("id") int id);

    /**
     * 通过id删除代理商成本信息
     * @param id
     */
    void deleteProxyCostMaintainById(@Param("id") int id);

    /**
     * 根据代理商customkey 服务公司companyId  金额标签gearLabel 档位ID 查询代理商信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @param netfileId
     * @return
     */
    int getProxyCostMaintainByCustomkeyCompanyIdGearLabel(@Param("customkey") String customkey, @Param("companyId") int companyId, @Param("gearLabel") int gearLabel, @Param("netfileId") int netfileId);

    /**
     * 根据id查询的代理商成本维护信息表
     * @param id
     * @return
     */
    ProxyCostMaintain getProxyCostMaintainById(@Param("id") Integer id);

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    List<ProxyCostMaintain> getProxyCostMaintainByCustomkeyCompanyIdNetfileId(@Param("customkey") String customkey, @Param("companyId") int companyId, @Param("gearLabel") int gearLabel);

    /**
     * 通过netfileId 查询代理商成本信息
     * @param netfileId
     * @return
     */
    int getProxyCostMaintainByNetfileId(@Param("netfileId") String netfileId);

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询不是这些条件的费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    List<ProxyCostMaintain> getNoProxyCostMaintainByCustomkeyCompanyIdGearLabel(@Param("customkey") String customkey, @Param("companyId") int companyId, @Param("gearLabel") int gearLabel, @Param("netfileId") int netfileId);
}
