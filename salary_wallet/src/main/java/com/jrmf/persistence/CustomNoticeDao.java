package com.jrmf.persistence;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.CustomNotice;

@Mapper
public interface CustomNoticeDao {

	void insertCustomNotice(CustomNotice customNotice); 
}
