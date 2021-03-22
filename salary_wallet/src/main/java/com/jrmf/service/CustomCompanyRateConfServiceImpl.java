package com.jrmf.service;

import com.jrmf.domain.CompanyRateConf;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.persistence.CustomCompanyRateConfDao;
import com.jrmf.utils.ArithmeticUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomCompanyRateConfServiceImpl implements CustomCompanyRateConfService {
    private final CustomCompanyRateConfDao customCompanyRateConfDao;

    @Autowired
    public CustomCompanyRateConfServiceImpl(CustomCompanyRateConfDao customCompanyRateConfDao) {
        this.customCompanyRateConfDao = customCompanyRateConfDao;
    }

    @Autowired
    private CompanyRateConfService companyRateConfService;

    @Override
    public boolean addCustomCompanyRateConf(CustomCompanyRateConf customCompanyRateConf) {
        if (isExistedConf(customCompanyRateConf)) return false;
        return customCompanyRateConfDao.insertCustomCompanyRateConf(customCompanyRateConf) != 0;
    }

    private boolean isExistedConf(CustomCompanyRateConf customCompanyRateConf) {
        return customCompanyRateConfDao.queryByCustomKeyAndRateConfId(customCompanyRateConf.getCustomkey(), customCompanyRateConf.getRateConfId()) > 0;
    }

    @Override
    public List<CustomCompanyRateConf> listCustomCompanyRateConf(Map<String, Object> params) {
        return customCompanyRateConfDao.listCustomCompanyRateConf(params);
    }

    @Override
    public List<Map<String,Object>> realCompanyList(Map<String, Object> params) {
        return customCompanyRateConfDao.realCompanyList(params);
    }

    @Override
    public boolean updateCustomCompanyRateConf(CustomCompanyRateConf customCompanyRateConf) {
        return customCompanyRateConfDao.updateCustomCompanyRateConf(customCompanyRateConf) == 1;
    }

    @Override
    public boolean deleteCustomCompanyRateConf(Integer id) {
        return customCompanyRateConfDao.deleteCustomCompanyRateConf(id) == 1;
    }

    @Override
    public CustomCompanyRateConf getCustomCompanyRateConf(Map<String, Object> params) {
        return customCompanyRateConfDao.getCustomCompanyRateConf(params);
    }


    @Override
    public CustomCompanyRateConf getById(Integer id) {
        return customCompanyRateConfDao.getById(id);
    }

    @Override
    public List<CustomCompanyRateConf> getConfByCustomKey(String customkey) {
        return customCompanyRateConfDao.getConfByCustomKey(customkey);
    }

    @Override
    public List<CustomCompanyRateConf> getConfsByCustomKeyAndCompanyId(String customkey,String companyId) {
        return customCompanyRateConfDao.getConfsByCustomKeyAndCompanyId(customkey,companyId);
    }

    @Override
    public CustomCompanyRateConf getConfByCustomKeyAndCompanyId(String customkey, String companyId) {
        return customCompanyRateConfDao.getConfByCustomKeyAndCompanyId(customkey, companyId);
    }

    /**
     * 根据  customKey  和  CompanyRateConfId  查询
     *
     * @param originalId customKey
     * @param id         CompanyRateConfId
     * @return CustomCompanyRateConf
     */
    @Override
    public CustomCompanyRateConf getConfByCustomKeyAndCompanyRateConfId(String originalId, Integer id) {
        return customCompanyRateConfDao.getConfByCustomKeyAndCompanyRateConfId(originalId, id);
    }

    @Override
    public List<Integer> getServiceTypeGroup(CustomCompanyRateConf customCompanyRateConf) {
        return customCompanyRateConfDao.getServiceTypeGroup(customCompanyRateConf);
    }

    @Override
    public Map<String, Object> getCustomRateConf(String customkey, String companyId) {
        return customCompanyRateConfDao.getCustomRateConf(customkey, companyId);
    }

    @Override
    public List<Map<String, Object>> getCustomRateConfList(String customkey, String companyId) {
        return customCompanyRateConfDao.getCustomRateConfList(customkey, companyId);
    }


    /**
     * 校验商户配置的档位是否有重叠区间
     *
     * @date 2019/11/5
     */
    @Override
    public boolean checkOverlap(String customKey, Integer companyId, Integer customCompanyRateId, Integer rateConfId) {

        Integer currentRateConfId = null;
        if (customCompanyRateId != null) {
            CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfDao.getById(customCompanyRateId);
            currentRateConfId = customCompanyRateConf.getRateConfId();
        }

        List<CompanyRateConf> rateConfList = customCompanyRateConfDao.getCustomRateIntervalList(customKey, companyId, currentRateConfId);
        //当前该商户没有配置别的档位,肯定不会重叠
        if (rateConfList == null) {
            return false;
        }

        CompanyRateConf rateConf = companyRateConfService.getById(rateConfId);
        if (rateConf != null) {
            rateConfList.add(rateConf);
        }

        List<String> amountIntervalList = new ArrayList<>();
        for (CompanyRateConf item : rateConfList) {
            amountIntervalList.add(item.getAmountStart() + "-" + item.getAmountEnd());
        }

        return ArithmeticUtil.checkOverlap(amountIntervalList);
    }

    @Override
    public String getCustomMonthLimit(String customKey, Integer companyId) {
        return customCompanyRateConfDao.getCustomMonthLimit(customKey, companyId);
    }

    @Override
    public List<CustomCompanyRateConf> getCustomRateConfByKeyAndId(String customkey, Integer companyId, Integer id) {
        return customCompanyRateConfDao.getCustomRateConfByKeyAndId(customkey, companyId, id);
    }

    @Override
    public CustomCompanyRateConf getCustomCompanyMinRate(String customkey, String companyId) {
        return customCompanyRateConfDao.getCustomCompanyMinRate(customkey, companyId);
    }
}
