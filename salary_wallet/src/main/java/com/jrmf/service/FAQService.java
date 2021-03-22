package com.jrmf.service;

import com.jrmf.domain.FAQ;

import java.util.List;
import java.util.Map;

public interface FAQService {
    boolean addFAQ(FAQ faq);

    boolean updateFAQ(FAQ faq);

    boolean deleteFAQ(Integer id);

    List<FAQ> listFAQ(Map<String, Object> keyWords);
}
