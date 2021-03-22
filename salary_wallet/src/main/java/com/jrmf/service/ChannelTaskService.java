package com.jrmf.service;


import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.ChannelTask;
import com.jrmf.domain.ChannelTaskType;

import com.jrmf.domain.TaxCode;
import java.util.List;
import java.util.Map;

public interface ChannelTaskService {

  int deleteByPrimaryKey(Integer id);

  int insert(ChannelTask task);

  int insertSelective(ChannelTask record);

  ChannelTask selectByPrimaryKey(Integer id);

  int updateByPrimaryKey(ChannelTask task);

  int updateByPrimaryKeySelective(ChannelTask task);

  List<ChannelTask> selectAll(Map<String, Object> paramMap);

  int updateConfirmMatchTask(String certIds);

  List<String> selectResourceCustom(String customKeys);

  void autogenerateTask(String customKey, String startTime, String endTime, String startAmount,
      String endAmount, String taskIds, String orderNos);

  List<ChannelTask> selectCustomAll(Map<String, Object> paramMap);

  List<ChannelTaskType> selectAllType();

  List<ChannelTask> selectResourceAll(Map<String, Object> paramMap);

  List<ChannelAreas> selectByParentCode(String parentCode);

  int updateTaskStatus(Integer id, Integer status);

  List<ChannelTask> selectAutogenerateTaskAll(Map<String, Object> paramMap);

  int selectCustomAllCount(Map<String, Object> paramMap);

  List<TaxCode> selectTaxCode(int level, String levelCode);
}
