package com.jrmf.taxsettlement.api;

import com.jrmf.taxsettlement.util.cache.InertnessDataCache;
import com.jrmf.taxsettlement.util.cache.UnitDefinition;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TaxSettlementInertnessDataCache extends InertnessDataCache {

	private static final String ERROR_CODE = "error-code";

	private static final String ID_CODE = "id-code";

	@Autowired
	private UtilDao utilMapper;
    @Autowired
    private UtilCacheManager cacheManager;

	@PostConstruct
	public void init() {
        loadErrorDefinitions();
        loadIdNames();
    }

	private void loadErrorDefinitions() {
        for(UnitDefinition errorUnit : utilMapper.getErrors()) {
            cacheManager.setMapValue(ERROR_CODE, errorUnit.getCode(), errorUnit.getName());
		}
	}

	private void loadIdNames() {
		for(UnitDefinition idUnit : utilMapper.getIdNames()) {
            cacheManager.setMapValue(ID_CODE, idUnit.getCode(), idUnit.getName());
		}
	}

	public String getErrorMsg(String errorCode) {
        return cacheManager.getMapValue(ERROR_CODE, errorCode);
	}

	public String getNameOfId(String id) {
        return cacheManager.getMapValue(ID_CODE, id);
	}
}
