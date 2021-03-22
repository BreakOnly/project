package com.jrmf.service;


import com.jrmf.domain.JunkInfo;

public interface JunkInfoService {
    int deleteByPrimaryKey(Integer id);

    int insert(JunkInfo record);

    JunkInfo selectByPrimaryKey(Integer id);
}