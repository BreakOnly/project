package com.jrmf.service;

import com.jrmf.domain.CompanyNetfileRateConf;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.CompanyNetfileRateConfDao;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: CompanyNetfileRateConfServiceImpl
 * @Description: 服务公司报税档位信息
 * @create 2019/10/29 17:21
 */
@Service("companyNetfileRateConfService")
public class CompanyNetfileRateConfServiceImpl implements CompanyNetfileRateConfService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyNetfileRateConfServiceImpl.class);

    @Autowired
    private CompanyNetfileRateConfDao companyNetfileRateConfDao;

    @Autowired
    private CompanyDao companyDao;

    /**
     * 查询报税金额信息
     * @param companyId
     * @return
     */
    @Override
    public List<CompanyNetfileRateConf> queryNetfileGearPosition(Map<String, Object> paramMap) {
        return companyNetfileRateConfDao.queryNetfileGearPosition(paramMap);
    }

    /**
     * 配置报税档位金额信息
     * @param
     * @return
     */
    @Override
    public Map<String, Object> configNetfileGearPosition(CompanyNetfileRateConf companyNetfileRateConf) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            if (companyNetfileRateConf.getId() != null) {
                if ("1".equals(String.valueOf(companyNetfileRateConf.getGearLabel()))) {
                    CompanyNetfileRateConf crc = companyNetfileRateConfDao.getCompanyNetfileRateConfById(companyNetfileRateConf.getId());
                    if (crc != null) {
                        if (!StringUtil.isEmpty(crc.getCostRate())) {
                            if (!companyNetfileRateConf.getCostRate().equals(crc.getCostRate())) {
                                result.put(RespCode.RESP_STAT, RespCode.error101);
                                result.put(RespCode.RESP_MSG, "报税标签为“小金额”则报税成本费率必须一致!");
                                return result;
                            }
                        }
                    }
                }
                companyNetfileRateConf.setUpdateTime(DateUtils.getNowDate());
                companyNetfileRateConfDao.updateNetfileGearPosition(companyNetfileRateConf);
            } else {
                String merchantId = companyDao.getMerchantIdByUserId(companyNetfileRateConf.getCompanyId());
                companyNetfileRateConf.setMerchantId(merchantId);
                companyNetfileRateConfDao.insertNetfileGearPosition(companyNetfileRateConf);
            }
        } catch (Exception e) {
            logger.info("配置报税金额档位信息异常{}", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "配置报税金额档位信息失败，请联系管理员!");
            return result;
        }
        return result;
    }

    /**
     * 查询该档位是否关联商户
     * @param id
     * @return
     */
    @Override
    public boolean queryCustomCompanyConfig(String id) {
        int i = companyNetfileRateConfDao.queryCustomCompanyConfig(id);
        return i > 0;
    }

    /**
     * 删除报税金额档位信息
     * @param id
     */
    @Override
    public void removeGeraByGearId(String id) {
        companyNetfileRateConfDao.removeGeraByGearId(id);
    }

    /**
     * 通过id 查询报税金额信息
     * @param netfileId
     * @return
     */
    @Override
    public CompanyNetfileRateConf getCompanyNetfileRateConfById(int netfileId) {
        return companyNetfileRateConfDao.getCompanyNetfileRateConfById(netfileId);
    }

    /**
     * 通过服务公司id和档位编号查询报税档位信息
     * @param companyId
     * @return
     */
    @Override
    public CompanyNetfileRateConf getCompanyNetfileRateConfByCompanyIdAndGearPosition(int companyId, int gearPosition) {
        return companyNetfileRateConfDao.getCompanyNetfileRateConfByCompanyIdAndGearPosition(companyId, gearPosition);
    }

    /**
     * 通过服务公司id和档位编号查询不属于该档位编号的信息
     * @param companyId
     * @param gearPosition
     * @return
     */
    @Override
    public List<CompanyNetfileRateConf> getNoGearPositionByCompanyIdAndGearPosition(int companyId, Integer gearPosition) {
        return companyNetfileRateConfDao.getNoGearPositionByCompanyIdAndGearPosition(companyId, gearPosition);
    }

    /**
     * 查询不为当前ID 的报税金额信息
     * @param id
     * @return
     */
    @Override
    public List<CompanyNetfileRateConf> getCompanyNetfileRateConfByNoIdAndCompanyId(Integer id, int companyId) {
        return companyNetfileRateConfDao.getCompanyNetfileRateConfByNoIdAndCompanyId(id, companyId);
    }

    @Override
    public List<CompanyNetfileRateConf> queryNetfileGearPositionByMin(int companyId) {
        return companyNetfileRateConfDao.queryNetfileGearPositionByMin(companyId);
    }

    @Override
    public List<CompanyNetfileRateConf> queryNetfileGearPositionByMax(int companyId) {
        return companyNetfileRateConfDao.queryNetfileGearPositionByMax(companyId);
    }

    @Override
    public CompanyNetfileRateConf getMinAmountStartByCompanyId(int companyId) {
        return companyNetfileRateConfDao.getMinAmountStartByCompanyId(companyId);
    }

    @Override
    public CompanyNetfileRateConf getMaxAmountStartByCompanyId(int companyId) {
        return companyNetfileRateConfDao.getMaxAmountStartByCompanyId(companyId);
    }

    @Override
    public Map<String, Object> getJudgeAmountStartAndAmountEnd(String amountStart, String amountEnd, String minAmountStart, String maxAmountEnd, int companyId) {
        return companyNetfileRateConfDao.getJudgeAmountStartAndAmountEnd(amountStart,amountEnd,minAmountStart,maxAmountEnd, companyId);
    }

    /**
     * 查询报税金额信息总条数
     * @param paramMap
     * @return
     */
    @Override
    public int queryNetfileGearPositionCount(Map<String, Object> paramMap) {
        return companyNetfileRateConfDao.queryNetfileGearPositionCount(paramMap);
    }

    @Override
    public List<CompanyNetfileRateConf> getCompanyNetfileRateConfByCompanyId(int userId) {
        return companyNetfileRateConfDao.getCompanyNetfileRateConfByCompanyId(userId);
    }

    @Override
    public void updateCompanyNetfileRateConfByCompanyId(String merchantId, int userId) {
        companyNetfileRateConfDao.updateCompanyNetfileRateConfByCompanyId(merchantId, userId);
    }
}
