package com.jrmf.service;


import com.jrmf.domain.DataDictionary;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DataDictionaryService {

    List<DataDictionary> getListByDictType(String dictType, String dictKey);

    DataDictionary getByDictTypeAndKey(String dictType, String dictKey);

}
