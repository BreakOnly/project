package com.jrmf.taxsettlement.api.service.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.api.ApiReturnCode;
import com.jrmf.api.PaymentApi;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.APITransferBatchDO;
import com.jrmf.taxsettlement.api.APITransferRecordDO;
import com.jrmf.taxsettlement.api.MerchantAPITransferBatchDao;
import com.jrmf.taxsettlement.api.MerchantAPITransferRecordDao;
import com.jrmf.taxsettlement.api.gateway.batch.TransferBatchStatus;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;

@ActionConfig(name = "商户批次下发单状态查询")
public class QueryTransferBatchService
		implements Action<QueryTransferBatchServiceParams, QueryTransferBatchServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(QueryTransferBatchService.class);

	@Autowired
	private MerchantAPITransferBatchDao transferBatchDao;

	@Override
	public String getActionType() {
		return APIDefinition.QUERY_TRANSFER_BATCH.name();
	}

	@Override
	public ActionResult<QueryTransferBatchServiceAttachment> execute(QueryTransferBatchServiceParams actionParams) {

		String batchNo = actionParams.getBatchNo();
		if (batchNo == null || "".equals(batchNo)) {
			throw new APIDockingException(APIDockingRetCodes.BATCH_NO_NOT_EXISTED.getCode(), "null");
		}

		String merchantId = actionParams.getMerchantId();

		APITransferBatchDO batchInfo = transferBatchDao.getDealBatch(merchantId, batchNo);
		if (batchInfo == null) {
			throw new APIDockingException(APIDockingRetCodes.BATCH_NO_NOT_EXISTED.getCode(), batchNo);
		}

		QueryTransferBatchServiceAttachment attachment = new QueryTransferBatchServiceAttachment();
		attachment.setBatchNo(batchNo);
		attachment.setStatus(batchInfo.getStatus());
		attachment.setStatusDesc(TransferBatchStatus.codeOf(batchInfo.getStatus()).getDesc());
		attachment.setCreateTime(batchInfo.getCreateTime());
		attachment.setTotalRequestCount(batchInfo.getTotalRequestCount());
		attachment.setAcceptRequestCount(batchInfo.getAcceptRequestCount());
		attachment.setUnacceptRequestCount(batchInfo.getUnacceptRequestCount());
		attachment.setTransferDoneRequestCount(batchInfo.getTransferDoneRequestCount());
		attachment.setTransferFailRequestCount(batchInfo.getTransferFailRequestCount());
		return new ActionResult<QueryTransferBatchServiceAttachment>(attachment);
	}

}
