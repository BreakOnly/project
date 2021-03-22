package com.jrmf.persistence;

import com.jrmf.domain.CompanyRateConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface CompanyRateConfDao {

    int insertCompanyRateConf(CompanyRateConf companyRateConf);

    List<CompanyRateConf> listGearPosition(Integer companyId);

    /**
     * 根据参数查询结果集，参数可添加
     *
     * @param map 参数
     * @return CompanyRateConf
     */
    List<CompanyRateConf> getCompanyRateConfByParam(HashMap<String, Object> map);

    CompanyRateConf getById(Integer id);

    /**
     * 根据id修改档位
     * @param companyRateConf
     */
    void updateGear(CompanyRateConf companyRateConf);

    /**
     * 根据id删除档位
     * @param gearId
     * @return
     */
    int removeGeraByGearId(@Param("gearId") String gearId);

    /**
     * 根据档位ID 查询档位信息
     * @param gearId
     * @return
     */
    int queryGearInfoByGearId(@Param("gearId") String gearId);

    /**
     * 根据下发公司ID 档位级别 档位组 查询档位信息
     * @param companyId
     * @param gearPosition
     * @param gearGroup
     * @return
     */
    int queryGearById(@Param("companyId") Integer companyId, @Param("gearPosition")Integer gearPosition, @Param("gearGroup") String gearGroup);

    /**
     * 新增档位信息
     * @param companyRateConf
     */
    void addCompanyRateConf(CompanyRateConf companyRateConf);

    /**
     * 查询档位信息
     * @return
     */
    List<CompanyRateConf> queryCompanyRateConf(Map<String, Object> paramMap);

    /**
     * 查询商户 服务公司 配置档位信息
     * @param id
     * @return
     */
    int queryCustomCompanyConfig(@Param("id") String id);

    /**
     * 通过档位ID查询费率配置
     * @param id
     * @return
     */
    CompanyRateConf getCompanyRateConfById(@Param("id") Integer id);

    /**
     * 通过服务公司ID查询服务公司档位信息
     * @param userId
     * @return
     */
    List<CompanyRateConf> getCompanyRateConfByCompanyId(@Param("userId") int userId);

    /**
     * 通过报税id查询档位信息表
     * @param id
     * @return
     */
    int queryCompanyRateConfByNetfileId(@Param("netfileId") String id);

    /**
     * 查找不是该id的下发公司信息
     * @param id
     * @param companyId
     * @return
     */
    List<CompanyRateConf> getCompanyRateConfByNoIdAndCompanyId(@Param("id") int id, @Param("companyId") String companyId);

    /**
     * 通过id查询该档位有无商户绑定
     * @param id
     * @return
     */
    int queryCustomCompanyRateConfById(@Param("id") String id);

    /**
     * 查询下发公司档位总数
     * @param paramMap
     * @return
     */
    int queryCompanyRateConfCount(Map<String, Object> paramMap);

    void updateCompanyRateConfByCompanyId(String merchantId, int userId);
}
