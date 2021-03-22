package com.jrmf.taxsettlement.api.gateway.batch;

import java.util.HashMap;
import java.util.Map;

public class BatchTransferRequestDealers {

	private static final String DEFAULT_REQUEST_DEALER_KEY = "*";
	
	private Map<String, BatchTransferRequestDealer> requestDealers = new HashMap<String, BatchTransferRequestDealer>();
	
	public BatchTransferRequestDealers(Map<String, BatchTransferRequestDealer> requestDealers) {
		super();
		this.requestDealers = requestDealers;
	}	
	
	public BatchTransferRequestDealer getDealerFor(String merchantId, String partnerId) {
		
		String specifiedKey = new StringBuilder(partnerId).append("-").append(merchantId).toString();
		BatchTransferRequestDealer dealer = requestDealers.get(specifiedKey);
		if(dealer != null) {
			return dealer;
		}
		
		specifiedKey = new StringBuilder(partnerId).append("-").append(DEFAULT_REQUEST_DEALER_KEY).toString();
		dealer = requestDealers.get(specifiedKey);
		if(dealer != null) {
			return dealer;
		}		
		return requestDealers.get(DEFAULT_REQUEST_DEALER_KEY);
	}

}
