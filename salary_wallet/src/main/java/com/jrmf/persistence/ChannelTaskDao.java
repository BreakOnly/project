package com.jrmf.persistence;


import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.ChannelTask;
import com.jrmf.domain.ChannelTaskType;
import com.jrmf.interceptor.InterceptPlatformPermissionAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChannelTaskDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ChannelTask record);

    int insertSelective(ChannelTask record);

    ChannelTask selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(ChannelTask record);

    int updateByPrimaryKeySelective(ChannelTask record);

    List<ChannelTask> selectAll(Map<String, Object> paramMap);

    int updateConfirmMatchTask(@Param("certIds") String certIds);

    List<String> selectResourceCustom(@Param("customKey") String customKey);

    List<ChannelTask> selectCustomAll(Map<String, Object> paramMap);

    List<ChannelTaskType> selectAllType();

    @InterceptPlatformPermissionAnnotation(aliasName = "cc.businessPlatformId")
    List<ChannelTask> selectResourceAll(Map<String, Object> paramMap);

    List<ChannelAreas> selectByParentCode(@Param("parentCode") String parentCode);

    int updateTaskStatus(Integer id, Integer status);

    List<ChannelTask> selectAutogenerateTaskAll(Map<String, Object> paramMap);

    int selectCustomAllCount(Map<String, Object> paramMap);
}