package com.jrmf.persistence;

import com.jrmf.domain.FAQ;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FAQDao {

    int insertFAQ(FAQ faq);

    int updateFAQ(FAQ faq);

    int deleteFAQ(Integer id);

    List<FAQ> listFAQ(Map<String, Object> params);
}
