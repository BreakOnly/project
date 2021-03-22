package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.Parameter;

/**
 * filename：com.jrmf.persistence.ParameterDao.java
 * 
 * @author: zhangyong
 * @time: 2013-10-11下午2:58:37
 */

@Mapper
public interface ParameterDao {
	/**
	 * 添加
	 * 
	 * @param parameter
	 */
	void saveParameter(Parameter parameter);

	/**
	 * 设置为无效
	 * 
	 * @param paramName
	 * @param paramFlag
	 */
	void deleteParameter(@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag);
	
	List<Parameter> getParamByCondition(Map<String,Object> param);

	/**
	 * 根据参数名,参数值，参数类型查询
	 * 
	 * @param paramName
	 * @param paramValue
	 * @param paramFlag
	 * @return
	 */
	Parameter getParameterByValue(@Param("paramName") String paramName,
			@Param("paramValue") String paramValue,
			@Param("paramFlag") String paramFlag);

	/***
	 * 根据参数名称查参数值
	 * 
	 * @param paramName
	 * @param paramFlag
	 * @return
	 */
	String findParamValueByParamName(
			@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag);

	Parameter getParameterByValueDate(
			@Param("paramName") String paramName,
			@Param("paramValue") String paramValue,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate);

	int findParamCount(@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate);

	int findParamCountByFromip(@Param("fromip") String fromip,
			@Param("paramDate") String paramDate);

	void updateVailCount(@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate);

	int findParamCountByCondition(@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate,
			@Param("deviceUUID") String deviceUUID,
			@Param("fromip") String fromip);

	Parameter getParameBySerialID(@Param("serialID") String serialID,
			@Param("paramDate") String paramDate);

	void deleteParamBySerialID(@Param("serialID") String serialID,
			@Param("paramName") String paramName);

	Parameter getParamByNameFlagAndDate(
			@Param("paramName") String paramName,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate, @Param("isVoice") int isVoice);

	Parameter valiCodeBySerialId(@Param("serialID") String serialID,
			@Param("paramValue") String paramValue,
			@Param("paramFlag") String paramFlag,
			@Param("paramDate") String paramDate);

	void deleteParamBySerialID2(@Param("serialID") String serialID);
	
	int countOneMinute(Map<String,String> param);

    List<Parameter> getLastByLimit(@Param("limit") int limit);

}
