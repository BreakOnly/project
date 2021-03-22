package com.jrmf.persistence;

import com.jrmf.domain.TaskBaseConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskBaseConfigDao {

    TaskBaseConfig getConfigByOsId(String osId);

}