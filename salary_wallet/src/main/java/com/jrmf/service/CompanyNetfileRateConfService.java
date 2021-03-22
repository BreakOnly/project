package com.jrmf.service;

import com.jrmf.domain.CompanyNetfileRateConf;

import java.util.List;
import java.util.Map;

/**
 * @Title: CompanyNetfileRateConfService
 * @Description: 服务公司报税档位信息
 * @create 2019/10/29 17:20
 */
public interface CompanyNetfileRateConfService{
    /**
     * 查询报税金额信息
     * @param paramMap
     * @return
     */
    List<CompanyNetfileRateConf> queryNetfileGearPosition(Map<String, Object> paramMap);

    /**
     * 配置报税档位金额信息
     * @param
     * @return
     */
    Map<String, Object> configNetfileGearPosition( CompanyNetfileRateConf companyNetfileRateConf);

    /**
     * 查询该档位是否关联商户
     * @param id
     * @return
     */
    boolean queryCustomCompanyConfig(String id);

    /**
     * 删除报税金额档位信息
     * @param id
     */
    void removeGeraByGearId(String id);

    /**
     * 通过id 查询报税金额信息
     * @param netfileId
     * @return
     */
    CompanyNetfileRateConf getCompanyNetfileRateConfById(int netfileId);

    /**
     * 通过服务公司id和档位编号查询报税档位信息
     * @param companyId
     * @return
     */
    CompanyNetfileRateConf getCompanyNetfileRateConfByCompanyIdAndGearPosition(int companyId, int gearPosition);

    /**
     * 通过服务公司id和档位编号查询不属于该档位编号的信息
     * @param companyId
     * @param gearPosition
     * @return
     */
    List<CompanyNetfileRateConf> getNoGearPositionByCompanyIdAndGearPosition(int companyId, Integer gearPosition);

    /**
     * 查询不为当前ID 的下发公司报税金额信息
     * @param id
     * @return
     */
    List<CompanyNetfileRateConf> getCompanyNetfileRateConfByNoIdAndCompanyId(Integer id, int companyId);

    List<CompanyNetfileRateConf> queryNetfileGearPositionByMin(int companyId);

    List<CompanyNetfileRateConf> queryNetfileGearPositionByMax(int companyId);

    CompanyNetfileRateConf getMinAmountStartByCompanyId(int companyId);

    CompanyNetfileRateConf getMaxAmountStartByCompanyId(int companyId);

    Map<String, Object> getJudgeAmountStartAndAmountEnd(String amountStart, String amountEnd, String minAmountStart, String maxAmountEnd,int companyId);

    /**
     * 查询报税金额信息总条数
     * @param paramMap
     * @return
     */
    int queryNetfileGearPositionCount(Map<String, Object> paramMap);

    List<CompanyNetfileRateConf> getCompanyNetfileRateConfByCompanyId(int userId);

    void updateCompanyNetfileRateConfByCompanyId(String merchantId, int userId);
}
