package com.jrmf.taxsettlement.api;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.taxsettlement.util.cache.UnitDefinition;

@Mapper
public interface UtilDao {

	List<UnitDefinition> getErrors();

	List<UnitDefinition> getIdNames();
}
