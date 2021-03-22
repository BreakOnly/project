package com.jrmf.taxsettlement.api.gateway.paramseditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.AbstractTransferServiceParams;

public class TiananInsuranceServiceParamsEditor implements ServiceParamsEditor {

	private static final Logger logger = LoggerFactory.getLogger(TiananInsuranceServiceParamsEditor.class);

	private Map<String, String[]> merchantMatchTransferCorpIdTable = new HashMap<String, String[]>();

	public TiananInsuranceServiceParamsEditor(Map<String, String> merchantMatchTransferCorpIdTable) {
		super();
		for(Entry<String, String> entry : merchantMatchTransferCorpIdTable.entrySet()) {
			this.merchantMatchTransferCorpIdTable.put(entry.getKey(), entry.getValue().split(","));
		}
	}

	@Override
	public void edit(AbstractTransferServiceParams transferServiceParams) {
		String companyName = transferServiceParams.getTransferCorpId();
		String[] matchConfig = merchantMatchTransferCorpIdTable.get(companyName);
		if (matchConfig == null) {
			logger.error("no match transfer corp config found with[{}]", companyName);
			throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(),
					"companyName[" + companyName + "] is not registered");
		} else {
			transferServiceParams.setPartnerId(transferServiceParams.getMerchantId());
			transferServiceParams.setMerchantId(matchConfig[0]);
			transferServiceParams.setTransferCorpId(matchConfig[1]);
		}
	}

}
