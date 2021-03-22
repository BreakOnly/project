package com.jrmf.service;


import com.jrmf.domain.*;
import com.jrmf.persistence.DataDictionaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataDictionaryServiceImpl implements DataDictionaryService {

	private final DataDictionaryDao dataDictionaryDao;

	@Autowired
	public DataDictionaryServiceImpl(DataDictionaryDao dataDictionaryDao) {
		this.dataDictionaryDao = dataDictionaryDao;
	}

	@Override
	public List<DataDictionary> getListByDictType(String dictType, String dictKey) {
		return dataDictionaryDao.getListByDictType(dictType, dictKey);
	}

	@Override
	public DataDictionary getByDictTypeAndKey(String dictType, String dictKey) {
		return dataDictionaryDao.getByDictTypeAndKey(dictType, dictKey);
	}


}
