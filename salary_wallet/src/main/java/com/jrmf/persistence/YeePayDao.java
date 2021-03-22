package com.jrmf.persistence;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.YeePayLog;
@Mapper
public interface YeePayDao {
	
	int saveYeePayRequestLog(YeePayLog log);
	
	int updateYeePayResponseLog(YeePayLog log);

}
