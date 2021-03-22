package com.jrmf.persistence;

import com.jrmf.domain.SplitOrderConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SplitOrderConfDao {

    List<SplitOrderConf> getConfByCustomKey(Map<String, Object> params);

    int deleteSplitOrderConf(@Param("customKey") String customKey, @Param("companyId") String companyId);

    SplitOrderConf getConfByCustomKeyAndCompanyId(@Param("customKey") String customKey, @Param("companyId") String companyId);

    int addSplitOrderConf(SplitOrderConf splitOrderConf);

    int updateSplitOrderConf(SplitOrderConf splitOrderConf);

    List<SplitOrderConf> listSplitOrderConf(Map<String, Object> params);
}
