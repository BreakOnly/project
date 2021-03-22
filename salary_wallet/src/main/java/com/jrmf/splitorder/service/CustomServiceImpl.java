package com.jrmf.splitorder.service;

import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.CustomDao;
import com.jrmf.splitorder.domain.Custom;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomServiceImpl implements CustomService {
    @Autowired
    private CustomDao customDao;
    @Autowired
    private ChannelRelatedDao channelRelatedDao;
    @Autowired
    private ChannelCustomDao channelCustomDao;

    @Override
    public List<Custom> listCustomInfo() {
        return customDao.listCustomInfo();
    }

    @Override
    public ChannelRelated getRelatedByCusAndCom(String customKey, String companyId) {
        return channelRelatedDao.getRelatedByCustomKeyAndCompanyId(customKey, companyId);
    }

    @Override
    public String getNameByCustomKey(String customKey) {
        return channelRelatedDao.getNameByCustomKey(customKey);
    }

    @Override
    public ChannelCustom getCustomByCustomkey(String customkey) {
        return channelCustomDao.getCustomByCustomkey(customkey,null);
    }

    @Override
    public List<ChannelRelated> getRelatedsByCustomKey(String customkey) {
        return channelRelatedDao.getRelatedList(customkey);
    }

    @Override
    public List<String> getCompanyUserIdByBusinessPlatformId(Integer businessPlatformId) {
        return channelCustomDao.getCompanyUserIdByBusinessPlatformId(businessPlatformId);
    }

    @Override
    public List<String> getCustomKeyByBusinessPlatformId(Integer platformId) {
        return channelCustomDao.getCustomKeyByBusinessPlatformId(platformId);
    }

}
