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
import com.jrmf.taxsettlement.api.APITransferRecordDO;
import com.jrmf.taxsettlement.api.MerchantAPITransferRecordDao;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;

@ActionConfig(name = "商户单笔下发单状态查询")
public class QueryTransferDealService
		implements Action<QueryTransferDealServiceParams, QueryTransferDealServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(QueryTransferDealService.class);

	@Autowired
	private PaymentApi paymentApiImpl;

	@Autowired
	private MerchantAPITransferRecordDao transferRecordDao;

	@Override
	public String getActionType() {
		return APIDefinition.QUERY_TRANSFER_DEAL.name();
	}

	@Override
	public ActionResult<QueryTransferDealServiceAttachment> execute(QueryTransferDealServiceParams actionParams) {

		String dealNo = actionParams.getDealNo();
		String requestNo = actionParams.getRequestNo();
		if (dealNo == null || "".equals(dealNo)) {
			if (requestNo == null || "".equals(requestNo)) {
				throw new APIDockingException(APIDockingRetCodes.DEAL_NO_NOT_EXISTED.getCode(), "null");
			}
			dealNo = transferRecordDao.matchDealNo(actionParams.getMerchantId(), requestNo);
			if (dealNo == null) {
				throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_NOT_EXISTED.getCode(), requestNo);
			}
		}
		
		APITransferRecordDO recordDo = transferRecordDao.getDealRecord(dealNo);
		QueryTransferDealServiceAttachment attachment = new QueryTransferDealServiceAttachment();
		attachment.setDealNo(dealNo);
		attachment.setRequestNo(recordDo.getRequestNo());

		if(TransferStatus.FAIL_TO_ACCEPT.getCode().equals(recordDo.getStatus())) {
			attachment.setDealStatus(TransferStatus.FAIL_TO_ACCEPT.getCode());
			attachment.setDealStatusMsg(TransferStatus.FAIL_TO_ACCEPT.getDesc());
			return new ActionResult<QueryTransferDealServiceAttachment>(attachment);			
		}

		PaymentReturn<UserCommission> ret = paymentApiImpl.getLocalResult(dealNo);
		String retCode = ret.getRetCode();
		if (ApiReturnCode.SUCCESS.getCode().equals(retCode)) {
			UserCommission userCommission = ret.getAttachment();
			attachment.setAccountDate(recordDo.getAccountDate());

			TransferStatus status = null;
			switch (userCommission.getStatus()) {
			case 0:
				status = TransferStatus.ACCEPTED;
				break;
			case 1:
				status = TransferStatus.TRANSFER_DONE;
				break;
			case 2:
				status = TransferStatus.TRANSFER_FAILED;
				break;
			case 3:
				status = TransferStatus.TRANSFERING;
				break;
			}

			attachment.setDealStatus(status.getCode());
			attachment.setDealStatusMsg(status.getDesc());
			attachment.setFee(userCommission.getSumFee());
			return new ActionResult<QueryTransferDealServiceAttachment>(attachment);
		} else if (ApiReturnCode.NOT_SUCH_ORDER.getCode().equals(retCode)) {
			throw new APIDockingException(APIDockingRetCodes.DEAL_NO_NOT_EXISTED.getCode(), dealNo);
		} else {
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), ret.getFailMessage());
		}
	}

}
