package com.jrmf.taxsettlement.api.service.transfer;

public interface TransferDealStatusNotifier {

	void notify(String dealNo, TransferStatus status, String retCode, String retMsg);

	void registerBatch(String merchantId, boolean proxyMode, String batchNo, int acceptCount, String notifyUrl);
}
