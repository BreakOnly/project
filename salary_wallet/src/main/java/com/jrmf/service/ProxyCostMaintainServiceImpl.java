package com.jrmf.service;

import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomProxy;
import com.jrmf.domain.ProxyCostMaintain;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.persistence.ProxyCostMaintainDao;
import com.jrmf.utils.DateUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Title: ProxyCostMaintainServiceImpl
 * @Description:
 * @create 2019/10/31 10:13
 */
@Service("proxyCostMaintainService")
public class ProxyCostMaintainServiceImpl implements  ProxyCostMaintainService{

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProxyCostMaintainServiceImpl.class);

    @Autowired
    private ProxyCostMaintainDao proxyCostMaintainDao;

    @Autowired
    private CustomProxyDao customProxyDao;

    @Autowired
    private ChannelCustomDao customDao;

    @Autowired
    private CompanyDao companyDao;


    /**
     * 根据条件查询代理商成本信息
     * @param paramMap
     * @return
     */
    @Override
    public List<ProxyCostMaintain> getProxyCostMaintainList(Map<String, Object> paramMap) {
        return proxyCostMaintainDao.getProxyCostMaintainList(paramMap);
    }

    /**
     * 配置代理商成本信息
     * @param proxyCostMaintain
     */
    @Override
    public void configAgentCostMaintain(ProxyCostMaintain proxyCostMaintain) {

        CustomProxy customProxy = customProxyDao.getProxyIdByCustomkey(proxyCostMaintain.getCustomkey());
        Company company = companyDao.getCompanyByUserId(proxyCostMaintain.getCompanyId());
        proxyCostMaintain.setCompanyName(company.getCompanyName());
        if (customProxy != null) {
            proxyCostMaintain.setProxyLevel(customProxy.getContentLevel());
            if (customProxy.getParentId() != 0) {
                String customkey = customProxyDao.getProxyById(customProxy.getParentId());
                proxyCostMaintain.setMasterCustomkey(customkey);
                ChannelCustom customByCustomkey = customDao.getCustomByCustomkey(customkey,null);
                proxyCostMaintain.setMasterName(customByCustomkey.getCompanyName());
            }
        }
        logger.info("配置代理商成本信息{}", proxyCostMaintain);
        if (proxyCostMaintain.getId() != null) {
            proxyCostMaintain.setUpdateTime(DateUtils.getNowDate());
            proxyCostMaintainDao.updateProxyCostMaintain(proxyCostMaintain);
        } else {
            proxyCostMaintainDao.insertProxyCostMaintain(proxyCostMaintain);
        }

    }

    /**
     * 通过id查询代理商成本信息
     * @param id
     * @return
     */
    @Override
    public ProxyCostMaintain queryProxyCostMaintainById(int id) {
        return proxyCostMaintainDao.queryProxyCostMaintainById(id);
    }

    /**
     * 通过id删除代理商成本信息
     * @param id
     */
    @Override
    public void deleteProxyCostMaintainById(int id) {
        proxyCostMaintainDao.deleteProxyCostMaintainById(id);
    }

    /**
     * 根据代理商customkey 服务公司companyId  金额标签gearLabel 档位ID 查询代理商信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @param netfileId
     * @return
     */
    @Override
    public int getProxyCostMaintainByCustomkeyCompanyIdGearLabel(String customkey, int companyId, int gearLabel, int netfileId) {
        return proxyCostMaintainDao.getProxyCostMaintainByCustomkeyCompanyIdGearLabel(customkey, companyId, gearLabel, netfileId);
    }

    /**
     * 根据id查询的代理商成本维护信息表
     * @param id
     * @return
     */
    @Override
    public ProxyCostMaintain getProxyCostMaintainById(Integer id) {
        return proxyCostMaintainDao.getProxyCostMaintainById(id);
    }

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    @Override
    public List<ProxyCostMaintain> getProxyCostMaintainByCustomkeyCompanyIdNetfileId(String customkey, int companyId, int gearLabel) {
        return proxyCostMaintainDao.getProxyCostMaintainByCustomkeyCompanyIdNetfileId(customkey, companyId, gearLabel);
    }

    /**
     * 通过netfileId 查询代理商成本信息
     * @param netfileId
     * @return
     */
    @Override
    public boolean getProxyCostMaintainByNetfileId(String netfileId) {
        return proxyCostMaintainDao.getProxyCostMaintainByNetfileId(netfileId) > 0;
    }

    /**
     * 根据代理商customkey 服务公司id， 报税档位id 查询不是这些条件的费率信息
     * @param customkey
     * @param companyId
     * @param gearLabel
     * @return
     */
    @Override
    public List<ProxyCostMaintain> getNoProxyCostMaintainByCustomkeyCompanyIdGearLabel(String customkey, int companyId, int gearLabel, int netfileId) {
        return proxyCostMaintainDao.getNoProxyCostMaintainByCustomkeyCompanyIdGearLabel(customkey, companyId, gearLabel, netfileId);
    }
}
