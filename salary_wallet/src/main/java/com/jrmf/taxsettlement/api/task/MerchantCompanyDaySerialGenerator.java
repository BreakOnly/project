package com.jrmf.taxsettlement.api.task;

import com.jrmf.api.ApiReturnCode;
import com.jrmf.api.PaymentApi;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.taxsettlement.api.APIDockingRepositoryConstants;
import com.jrmf.taxsettlement.api.APITransferRecordDO;
import com.jrmf.taxsettlement.api.MerchantAPITransferRecordDao;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MerchantCompanyDaySerialGenerator extends AbstractMerchantDataFileGenerator {

	private static final Logger logger = LoggerFactory.getLogger(MerchantCompanyDaySerialGenerator.class);

	private static final String DAY_SERIAL_FILE_NAME_PREFIX = "DS_";

	private static final String BANK_ACCOUNT_PAYMENT = "银行电子户下发";

	private static final String ALIPAY_ACCOUNT_PAYMENT = "支付宝账户下发";

	private static final String WECHAT_ACCOUNT_PAYMENT = "微信账户下发";

	private static final String BANK_CARD_PAYMENT = "银行卡下发";

	private static final String OTHER_PAYMENT = "其他下发";

	@Autowired
	private MerchantAPITransferRecordDao transferRecordDao;

	@Autowired
	private PaymentApi paymentApiImpl;

	private String getPayTypeDesc(int payType) {

		switch (payType) {
			case 1:
				return BANK_ACCOUNT_PAYMENT;
			case 2:
				return ALIPAY_ACCOUNT_PAYMENT;
			case 3:
				return WECHAT_ACCOUNT_PAYMENT;
			case 4:
				return BANK_CARD_PAYMENT;
			default:
				return OTHER_PAYMENT;
		}

	}

	@Override
	protected String getNewFileName(Map<String, Object> params) {
		return new StringBuilder(DAY_SERIAL_FILE_NAME_PREFIX)
				.append((String) params.get(APIDefinitionConstants.CFN_MERCHANT_ID)).append("_")
				.append((String) params.get(APIDefinitionConstants.CFN_TRANSFER_CORP_ID)).append("_")
				.append((String) params.get(APIDefinitionConstants.CFN_ACCOUNT_DATE)).toString();
	}

	@Override
	protected List<String[]> getDataRows(Map<String, Object> params) {
		String merchantId = (String) params.get(APIDefinitionConstants.CFN_MERCHANT_ID);
		String companyId =  (String) params.get(APIDefinitionConstants.CFN_TRANSFER_CORP_ID);
		String accountDate = (String) params.get(APIDefinitionConstants.CFN_ACCOUNT_DATE);

		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		queryParams.put(APIDockingRepositoryConstants.ACCOUNT_DATE, accountDate);
		queryParams.put(APIDockingRepositoryConstants.TRANSFER_CORP_ID, companyId);
		queryParams.put(APIDockingRepositoryConstants.STATUS, TransferStatus.TRANSFER_DONE.getCode());

		List<APITransferRecordDO> apiRecordList = transferRecordDao.listDealRecord(queryParams);
		List<String[]> dataLines = new ArrayList<String[]>(apiRecordList.size());
		for (APITransferRecordDO apiRecord : apiRecordList) {

			String dealNo = apiRecord.getDealNo();

			PaymentReturn<UserCommission> ret = paymentApiImpl.getLocalResult(dealNo);
			String retCode = ret.getRetCode();
			if (!ApiReturnCode.SUCCESS.getCode().equals(retCode)) {
				logger.error("error occured in get payment record[{}] for error code[{}]", dealNo, retCode);
				continue;
			}

			UserCommission commission = ret.getAttachment();
			dataLines.add(new String[] { apiRecord.getRequestNo(), apiRecord.getDealNo(),
					getPayTypeDesc(commission.getPayType()), apiRecord.getPartnerId(), apiRecord.getTransferCorpId(),
					commission.getAmount(), commission.getUserName(), commission.getAccount(), commission.getSumFee(),
					apiRecord.getAccountDate(), commission.getUpdatetime(), commission.getRemark(), "" });
		}

		return dataLines;
	}

}