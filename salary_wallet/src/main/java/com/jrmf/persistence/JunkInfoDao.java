package com.jrmf.persistence;


import com.jrmf.domain.JunkInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JunkInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(JunkInfo record);

    JunkInfo selectByPrimaryKey(Integer id);
}