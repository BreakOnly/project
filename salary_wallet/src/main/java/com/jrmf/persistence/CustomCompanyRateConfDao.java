package com.jrmf.persistence;

import com.jrmf.domain.CompanyRateConf;
import com.jrmf.domain.CustomCompanyRateConf;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomCompanyRateConfDao {

    int insertCustomCompanyRateConf(CustomCompanyRateConf customCompnayRateConf);

    int updateCustomCompanyRateConf(CustomCompanyRateConf customCompnayRateConf);

    int deleteCustomCompanyRateConf(Integer id);

    List<CustomCompanyRateConf> listCustomCompanyRateConf(Map<String, Object> params);

    List<Map<String,Object>> realCompanyList(Map<String, Object> params);

    CustomCompanyRateConf getCustomCompanyRateConf(Map<String, Object> params);

    CustomCompanyRateConf getById(Integer id);

    int queryByCustomKeyAndRateConfId(@Param("customkey") String customkey,@Param("rateConfId")  Integer rateConfId);

    List<CustomCompanyRateConf> getConfByCustomKey(String customkey);

    List<CustomCompanyRateConf> getConfsByCustomKeyAndCompanyId(String customkey,String companyId);

    CustomCompanyRateConf getConfByCustomKeyAndCompanyId(@Param("customkey") String customkey, @Param("companyId") String companyId);

    /**
     * 根据  customKey  和  CompanyRateConfId  查询
     * @param originalId customKey
     * @param id         CompanyRateConfId
     * @return CustomCompanyRateConf
     */
    CustomCompanyRateConf getConfByCustomKeyAndCompanyRateConfId(@Param("customkey") String originalId, @Param("id") Integer id);

    List<Integer> getServiceTypeGroup(CustomCompanyRateConf customCompanyRateConf);

	Map<String,Object> getCustomRateConf(String customkey,String companyId);

	List<Map<String,Object>> getCustomRateConfList(@Param("customkey") String customkey,@Param("companyId") String companyId);

    List<CompanyRateConf> getCustomRateIntervalList(String customKey, Integer companyId,Integer rateConfId);

    String getCustomMonthLimit(String customKey, Integer companyId);

    List<CustomCompanyRateConf> getCustomRateConfByKeyAndId(String customkey, Integer companyId, Integer id);

    CustomCompanyRateConf getCustomCompanyMinRate(String customkey, String companyId);

}
