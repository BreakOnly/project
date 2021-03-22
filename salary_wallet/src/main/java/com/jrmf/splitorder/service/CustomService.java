package com.jrmf.splitorder.service;

import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.splitorder.domain.Custom;

import java.util.List;

public interface CustomService {

    List<Custom> listCustomInfo();

    ChannelRelated getRelatedByCusAndCom(String customKey, String companyId);

    String getNameByCustomKey(String customKey);

    ChannelCustom getCustomByCustomkey(String customkey);

    List<ChannelRelated> getRelatedsByCustomKey(String customkey);

  List<String> getCompanyUserIdByBusinessPlatformId(Integer businessPlatformId);

    List<String> getCustomKeyByBusinessPlatformId(Integer platformId);
}
