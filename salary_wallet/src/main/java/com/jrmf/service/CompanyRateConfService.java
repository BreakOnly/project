package com.jrmf.service;

import com.jrmf.domain.CompanyRateConf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CompanyRateConfService {

    List<CompanyRateConf> listGearPosition(Integer companyId);

    CompanyRateConf getById(Integer id);

    /**
     * 根据参数查询结果集，参数可添加
     * @param map 参数
     * @return CompanyRateConf
     */
    List<CompanyRateConf> getCompanyRateConfByParam(HashMap<String, Object> map);

    /**
     * 根据id修改档位
     * @param companyRateConf
     */
    void updateGear(CompanyRateConf companyRateConf);

    /**
     * 根据ID 删除档位
     * @param gearId
     * @return
     */
    void removeGeraByGearId(String gearId);

    /**
     * 根据档位ID 查询档位信息
     * @param gearId
     * @return
     */
    boolean queryGearInfoByGearId(String gearId);

    /**
     * 新增档位
     * @param companyRateConf
     */
    void insertCompanyRateConf(CompanyRateConf companyRateConf);

    /**
     * 查询下发公司档位信息
     * @return
     */
    List<CompanyRateConf> queryCompanyRateConf(Map<String, Object> paramMap);

    /**
     * 查询商户 服务公司 配置档位信息
     * @param id
     * @return
     */
    boolean queryCustomCompanyConfig(String id);

    /**
     * 通过的档位ID查询费率配置
     * @param id
     * @return
     */
    CompanyRateConf getCompanyRateConfById(Integer id);

    /**
     * 通过服务公司ID查询服务公司档位信息
     * @param userId
     * @return
     */
    List<CompanyRateConf> getCompanyRateConfByCompanyId(int userId);

    /**
     * 通过报税id查询档位信息表
     * @param id
     * @return
     */
    boolean queryCompanyRateConfByNetfileId(String id);

    /**
     * 查找不是该id的下发公司信息
     * @param id
     * @param companyId
     * @return
     */
    List<CompanyRateConf> getCompanyRateConfByNoIdAndCompanyId(int id, String companyId);

    /**
     * 通过id查询该档位有无商户绑定
     * @param id
     * @return
     */
    boolean queryCustomCompanyRateConfById(String id);

    /**
     * 查询下发公司档位总数
     * @param paramMap
     * @return
     */
    int queryCompanyRateConfCount(Map<String, Object> paramMap);

    void updateCompanyRateConfByCompanyId(String merchantId, int userId);
}
