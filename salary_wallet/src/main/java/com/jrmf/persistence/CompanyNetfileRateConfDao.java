package com.jrmf.persistence;

import com.jrmf.domain.CompanyNetfileRateConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Title: CompanyNetfileRateConfDao
 * @Description: 服务公司报税档位信息
 * @create 2019/10/29 17:34
 */
@Mapper
public interface CompanyNetfileRateConfDao {

    /**
     * 查询报税金额信息
     * @param companyId
     * @return
     */
    List<CompanyNetfileRateConf> queryNetfileGearPosition(Map<String, Object> paramMap);

    /**
     * 通过id获取服务公司报税金额信息
     * @param id
     * @return
     */
    CompanyNetfileRateConf getCompanyNetfileRateConfById(@Param("id") int id);

    /**
     * 修改报税金额信息
     * @param companyNetfileRateConf
     */
    void updateNetfileGearPosition(CompanyNetfileRateConf companyNetfileRateConf);

    /**
     * 新增报税金额信息
     * @param companyNetfileRateConf
     */
    void insertNetfileGearPosition(CompanyNetfileRateConf companyNetfileRateConf);

    /**
     * 查询该档位是否关联商户
     * @param id
     * @return
     */
    int queryCustomCompanyConfig(@Param("id") String id);

    /**
     * 删除报税金额档位信息
     * @param id
     */
    void removeGeraByGearId(@Param("id") String id);

    /**
     * 通过服务公司id和档位编号查询报税档位信息
     * @param companyId
     * @return
     */
    CompanyNetfileRateConf getCompanyNetfileRateConfByCompanyIdAndGearPosition(@Param("companyId") int companyId, @Param("gearPosition") int gearPosition);

    /**
     * 通过服务公司id和档位编号查询不属于该档位编号的信息
     * @param companyId
     * @param gearPosition
     * @return
     */
    List<CompanyNetfileRateConf> getNoGearPositionByCompanyIdAndGearPosition(@Param("companyId") int companyId, @Param("gearPosition") int gearPosition);

    /**
     * 查询不为当前ID 的报税金额信息
     * @param id
     * @return
     */
    List<CompanyNetfileRateConf> getCompanyNetfileRateConfByNoIdAndCompanyId(@Param("id") Integer id, @Param("companyId") int companyId);

    List<CompanyNetfileRateConf> queryNetfileGearPositionByMin(@Param("companyId") int companyId);

    List<CompanyNetfileRateConf> queryNetfileGearPositionByMax(@Param("companyId") int companyId);

    CompanyNetfileRateConf getMinAmountStartByCompanyId(int companyId);

    CompanyNetfileRateConf getMaxAmountStartByCompanyId(int companyId);

    Map<String, Object> getJudgeAmountStartAndAmountEnd(String amountStart, String amountEnd, String minAmountStart, String maxAmountEnd, int companyId);

    /**
     * 查询报税金额信息总条数
     * @param paramMap
     * @return
     */
    int queryNetfileGearPositionCount(Map<String, Object> paramMap);

    List<CompanyNetfileRateConf> getCompanyNetfileRateConfByCompanyId(int userId);

    void updateCompanyNetfileRateConfByCompanyId(String merchantId, int userId);
}
