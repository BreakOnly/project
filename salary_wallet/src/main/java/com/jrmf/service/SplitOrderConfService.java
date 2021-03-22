package com.jrmf.service;

import com.jrmf.domain.SplitOrderConf;

import java.util.List;
import java.util.Map;

public interface SplitOrderConfService {

    List<SplitOrderConf> getConfByCustomKey(Map<String, Object> params);

    boolean deleteSplitOrderConf(String customKey, String companyId);

    SplitOrderConf getConfByCustomKeyAndCompanyId(String customKey, String companyId);

    boolean addSplitOrderConf(SplitOrderConf splitOrderConf);

    boolean updateSplitOrderConf(SplitOrderConf splitOrderConf);
}
