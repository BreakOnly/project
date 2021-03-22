package com.jrmf.persistence;

import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.Company;
import com.jrmf.domain.SMSChannelConfig;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @version 创建时间：2017年9月8日 下午4:07:57
 * 类说明
 */
@Mapper
public interface CompanyDao {

    void creatCompanyInfo(Company company);

    Company getCompanyByUserId(int id);

    void addPicturePath(Company company);

    void updateCompanyInfo(Company company);

    List<Company> getCompanyList(Map<String, Object> params);

    List<Company> listCompanyInfo();

    List<Company> getCompanyListByProxy(String customKey);

    List<Company> getCompanyByUserIds(String ids);

    /**
     * 查询公司名称
     *
     * @param companyName
     * @return
     */
    int getCompanyName(@Param("companyName") String companyName);

    /**
     * 新增下发公司
     *
     * @param company
     * @return
     */
    void addCompany(Company company);

    /**
     * 查询下发公司邮箱
     *
     * @param email
     * @return
     */
    Company getCompanyEmail(@Param("email") String email);

    /**
     * 条件查询下发公司
     *
     * @param paramMap
     * @return
     */
    List<Company> getCompanyListByParam(Map<String, Object> paramMap);

    /**
     * 通过服务公司名称查询服务公司信息
     *
     * @param companyName
     * @return
     */
    Company getCompanyByCompanyName(String companyName);

    String getMerchantIdByUserId(@Param("companyId") Integer companyId);

    int checkIsExist(ChannelConfig channelConfig);

    /**
     * 条件查询下发公司总数
     *
     * @param paramMap
     * @return
     */
    int getCompanyListByParamCount(Map<String, Object> paramMap);

    SMSChannelConfig getSmsConfig();

    List<Company> getAllCompanyList(@Param("companyType") Integer companyType);

    Company getCompanyById(String id);

    Integer getCompanyCountByIdAndCompanyType(String companyId, int companyType);

    List<Company> listRealityCompany();

    List<Company> selectCompanyByPlatform(Integer customId);

    Company getLikeCompanyByCompanyName(String companyName);

    Map<String, String> getCompanyPayChannelRelation(String companyId, String pathNo);

    List<Company> getIndividualCompanys(@Param("userId") String userId);
}
