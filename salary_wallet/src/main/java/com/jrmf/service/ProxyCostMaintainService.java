package com.jrmf.service;

import com.jrmf.domain.ProxyCostMaintain;

import java.util.List;
import java.util.Map;

/**
 * @Title: ProxyCostMaintainService
 * @Description:
 * @create 2019/10/31 10:13
 */
public interface ProxyCostMaintainService {
    /**
     * 根据条件查询代理商成本信息
     * @param paramMap
     * @return
     */
    List<ProxyCostMaintain> getProxyCostMaintainList(Map<String, Object> paramMap);

    /**
     * 配置代理商成本信息
     * @param proxyCostMaintain
     */
    void configAgentCostMaintain(ProxyCostMaintain proxyCostMaintain);

    /**
     * 通过id查询代理商成本信息
     * @param id
     * @return
     */
    ProxyCostMaintain queryProxyCostMaintainById(int id);

    /**
     * 通过id删除代理商成本信息
     * @param id
     */
    void deleteProxyCostMaintainById(int id);

    /**
     * 根据代理商customkey 服务公司companyId  金额标签gearLabel 档位ID 查询代理商信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @param netfileId
     * @return
     */
    int getProxyCostMaintainByCustomkeyCompanyIdGearLabel(String customkey, int companyId, int gearLabel, int netfileId);

    /**
     * 根据id查询的代理商成本维护信息表
     * @param id
     * @return
     */
    ProxyCostMaintain getProxyCostMaintainById(Integer id);

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    List<ProxyCostMaintain> getProxyCostMaintainByCustomkeyCompanyIdNetfileId(String customkey, int companyId, int gearLabel);

    /**
     * 通过netfileId 查询代理商成本信息
     * @param id
     * @return
     */
    boolean getProxyCostMaintainByNetfileId(String id);

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询不是这些条件的费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    List<ProxyCostMaintain> getNoProxyCostMaintainByCustomkeyCompanyIdGearLabel(String customkey, int companyId, int gearLabel, int netfileId);
}
