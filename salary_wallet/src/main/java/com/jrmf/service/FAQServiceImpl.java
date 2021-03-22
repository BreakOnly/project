package com.jrmf.service;

import com.jrmf.domain.FAQ;
import com.jrmf.persistence.FAQDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FAQServiceImpl implements FAQService {
    @Autowired
    private FAQDao faqDao;

    @Override
    public boolean addFAQ(FAQ faq) {
        return faqDao.insertFAQ(faq) == 1;
    }

    @Override
    public boolean updateFAQ(FAQ faq) {
        return faqDao.updateFAQ(faq) == 1;
    }

    @Override
    public boolean deleteFAQ(Integer id) {
        return faqDao.deleteFAQ(id) == 1;
    }

    @Override
    public List<FAQ> listFAQ(Map<String, Object> params) {
        return faqDao.listFAQ(params);
    }
}
