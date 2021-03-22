package com.jrmf.service;

import com.jrmf.domain.CustomCompanyRateConf;

import java.util.List;
import java.util.Map;

public interface CustomCompanyRateConfService {

    boolean addCustomCompanyRateConf(CustomCompanyRateConf customCompnayRateConf);

    List<CustomCompanyRateConf> listCustomCompanyRateConf(Map<String, Object> params);

    List<Map<String,Object>> realCompanyList(Map<String, Object> params);

    boolean updateCustomCompanyRateConf(CustomCompanyRateConf customCompnayRateConf);

    boolean deleteCustomCompanyRateConf(Integer id);

    CustomCompanyRateConf getCustomCompanyRateConf(Map<String, Object> params);

    CustomCompanyRateConf getById(Integer id);

    List<CustomCompanyRateConf> getConfByCustomKey(String customkey);

    List<CustomCompanyRateConf> getConfsByCustomKeyAndCompanyId(String customkey,String companyId);

    CustomCompanyRateConf getConfByCustomKeyAndCompanyId(String customkey, String companyId);

    /**
     * 根据  customKey  和  CompanyRateConfId  查询
     * @param originalId  customKey
     * @param id  CompanyRateConfId
     * @return  CustomCompanyRateConf
     */
    CustomCompanyRateConf getConfByCustomKeyAndCompanyRateConfId(String originalId, Integer id);

	List<Integer> getServiceTypeGroup(CustomCompanyRateConf customCompanyRateConf);

    Map<String, Object> getCustomRateConf(String customkey,String companyId);

    List<Map<String,Object>> getCustomRateConfList(String customkey,String companyId);

    boolean checkOverlap(String customKey, Integer companyId, Integer customCompanyRateId, Integer rateConfId);

    String getCustomMonthLimit(String customKey, Integer companyId);

    List<CustomCompanyRateConf> getCustomRateConfByKeyAndId(String customkey, Integer companyId, Integer id);

    CustomCompanyRateConf getCustomCompanyMinRate(String customkey, String companyId);
}
