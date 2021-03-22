package com.jrmf.splitorder.service;

import com.jrmf.domain.Company;
import com.jrmf.persistence.CompanyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyDao companyDao;

    @Override
    public List<Company> listCompanyInfo() {
        return companyDao.listCompanyInfo();
    }
}
