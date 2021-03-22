package com.jrmf.taxsettlement.api.gateway.batch;

import com.jrmf.taxsettlement.api.MerchantAPITransferBatchDao;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;

public interface BatchTransferRequestDealer {

	BatchDealResult batchSubmit(String merchantId, String partnerId, String notifyUrl, String batchNo,
			String transferCorpId, byte[] fileBytes, Action<ActionParams, ActionAttachment> dockingService,
			MerchantAPITransferBatchDao apiTransferBatchDao, TransferDealStatusNotifier statusNotifier)
			throws Exception;
}
