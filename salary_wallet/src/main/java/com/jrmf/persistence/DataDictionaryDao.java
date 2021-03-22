package com.jrmf.persistence;


import com.jrmf.domain.DataDictionary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataDictionaryDao {

	List<DataDictionary> getListByDictType(String dictType, String dictKey);

	DataDictionary getByDictTypeAndKey(String dictType, String dictKey);

}
