package com.jrmf.service;

import com.jrmf.domain.CompanyRateConf;
import com.jrmf.persistence.CompanyRateConfDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyRateConfServiceImpl implements CompanyRateConfService {

    @Autowired
    private CompanyRateConfDao companyRateConfDao;

    /**
     * 根据参数查询结果集，参数可添加
     *
     * @param map 参数
     * @return CompanyRateConf
     */
    @Override
    public List<CompanyRateConf> getCompanyRateConfByParam(HashMap<String, Object> map) {
        return companyRateConfDao.getCompanyRateConfByParam(map);
    }

    @Override
    public List<CompanyRateConf> listGearPosition(Integer companyId) {
        return companyRateConfDao.listGearPosition(companyId);
    }

    @Override
    public CompanyRateConf getById(Integer id) {
        return companyRateConfDao.getById(id);
    }


    /**
     * 根据id修改档位
     * @param companyRateConf
     */
    @Override
    public void updateGear(CompanyRateConf companyRateConf) {
        companyRateConfDao.updateGear(companyRateConf);
    }

    /**
     * 根据id删除档位
     * @param gearId
     * @return
     */
    @Override
    public void removeGeraByGearId(String gearId) {
        companyRateConfDao.removeGeraByGearId(gearId);
    }

    /**
     * 根据档位ID 查询档位信息
     * @param gearId
     * @return
     */
    @Override
    public boolean queryGearInfoByGearId(String gearId) {
        int i = companyRateConfDao.queryGearInfoByGearId(gearId);
        return i > 0;
    }

    /**
     * 新增档位
     * @param companyRateConf
     */
    @Override
    public void insertCompanyRateConf(CompanyRateConf companyRateConf) {
        companyRateConfDao.addCompanyRateConf(companyRateConf);
    }

    /**
     * 查询下发公司档位信息
     * @return
     */
    @Override
    public List<CompanyRateConf> queryCompanyRateConf(Map<String, Object> paramMap) {
        return companyRateConfDao.queryCompanyRateConf(paramMap);
    }

    /**
     * 查询商户 服务公司 配置档位信息
     * @param id
     * @return
     */
    @Override
    public boolean queryCustomCompanyConfig(String id) {
        int i = companyRateConfDao.queryCustomCompanyConfig(id);
        return i > 0;
    }

    /**
     * 通过档位ID查询费率配置
     * @param id
     * @return
     */
    @Override
    public CompanyRateConf getCompanyRateConfById(Integer id) {
        return companyRateConfDao.getCompanyRateConfById(id);
    }

    /**
     * 通过服务公司ID查询服务公司档位信息
     * @param userId
     * @return
     */
    @Override
    public List<CompanyRateConf> getCompanyRateConfByCompanyId(int userId) {
        return companyRateConfDao.getCompanyRateConfByCompanyId(userId);
    }

    /**
     * 通过报税id查询档位信息表
     * @param id
     * @return
     */
    @Override
    public boolean queryCompanyRateConfByNetfileId(String id) {
        int i = companyRateConfDao.queryCompanyRateConfByNetfileId(id);
        return i > 0;
    }


    /**
     * 查找不是该id的下发公司信息
     * @param id
     * @param companyId
     * @return
     */
    @Override
    public List<CompanyRateConf> getCompanyRateConfByNoIdAndCompanyId(int id, String companyId) {
        return companyRateConfDao.getCompanyRateConfByNoIdAndCompanyId(id,companyId);
    }

    /**
     * 通过id查询该档位有无商户绑定
     * @param id
     * @return
     */
    @Override
    public boolean queryCustomCompanyRateConfById(String id) {
        int i = companyRateConfDao.queryCustomCompanyRateConfById(id);
        return i > 0;
    }

    /**
     * 查询下发公司档位总数
     * @param paramMap
     * @return
     */
    @Override
    public int queryCompanyRateConfCount(Map<String, Object> paramMap) {
        return companyRateConfDao.queryCompanyRateConfCount(paramMap);
    }

    @Override
    public void updateCompanyRateConfByCompanyId(String merchantId, int userId) {
        companyRateConfDao.updateCompanyRateConfByCompanyId(merchantId, userId);
    }

}
