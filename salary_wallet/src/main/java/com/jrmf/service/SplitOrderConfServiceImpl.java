package com.jrmf.service;

import com.jrmf.domain.SplitOrderConf;
import com.jrmf.persistence.SplitOrderConfDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SplitOrderConfServiceImpl implements SplitOrderConfService {
    @Autowired
    private SplitOrderConfDao splitOrderConfDao;

    @Override
    public List<SplitOrderConf> getConfByCustomKey(Map<String, Object> params) {
        List<SplitOrderConf> confByCustomKey = splitOrderConfDao.getConfByCustomKey(params);
        return confByCustomKey == null ? new ArrayList<>() : confByCustomKey;
    }

    @Override
    public boolean deleteSplitOrderConf(String customKey, String companyId) {
        return splitOrderConfDao.deleteSplitOrderConf(customKey, companyId) == 1;
    }

    @Override
    public SplitOrderConf getConfByCustomKeyAndCompanyId(String customKey, String companyId) {
        return splitOrderConfDao.getConfByCustomKeyAndCompanyId(customKey, companyId);
    }

    @Override
    public boolean addSplitOrderConf(SplitOrderConf splitOrderConf) {
        return splitOrderConfDao.addSplitOrderConf(splitOrderConf) == 1;
    }

    @Override
    public boolean updateSplitOrderConf(SplitOrderConf splitOrderConf) {
        return splitOrderConfDao.updateSplitOrderConf(splitOrderConf) == 1;
    }
}
