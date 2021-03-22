package com.jrmf.taxsettlement.api.task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.api.ApiReturnCode;
import com.jrmf.api.PaymentApi;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.taxsettlement.api.APIDockingRepositoryConstants;
import com.jrmf.taxsettlement.api.APITransferRecordDO;
import com.jrmf.taxsettlement.api.MerchantAPITransferRecordDao;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;

public class MerchantMonthReportGenerator extends AbstractMerchantDataFileGenerator {

	private static final Logger logger = LoggerFactory.getLogger(MerchantMonthReportGenerator.class);

	private static final String MONTH_REPORT_FILE_NAME_PREFIX = "MR_";

	@Autowired
	private MerchantAPITransferRecordDao transferRecordDao;

	@Autowired
	private PaymentApi paymentApiImpl;

	@Override
	protected String getNewFileName(Map<String, Object> params) {
		return new StringBuilder(MONTH_REPORT_FILE_NAME_PREFIX)
				.append((String) params.get(APIDefinitionConstants.CFN_MERCHANT_ID)).append("_")
				.append((String) params.get(APIDefinitionConstants.CFN_ACCOUNT_MONTH)).toString();
	}

	@Override
	protected List<String[]> getDataRows(Map<String, Object> params) {
		String merchantId = (String) params.get(APIDefinitionConstants.CFN_MERCHANT_ID);
		String accountMonth = (String) params.get(APIDefinitionConstants.CFN_ACCOUNT_MONTH);

		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		queryParams.put(APIDockingRepositoryConstants.ACCOUNT_MONTH,
				new StringBuilder(accountMonth).insert(4, "-").toString());

		List<APITransferRecordDO> apiRecordList = transferRecordDao.listDealRecord(queryParams);
		List<String[]> dataLines = new ArrayList<String[]>();

		ReportLineData totalReportLineData = new ReportLineData();
		totalReportLineData.transferCorpId = "*";
		Map<String, ReportLineData> reportLineDataInTransferCorp = new HashMap<String, ReportLineData>();

		for (APITransferRecordDO apiRecord : apiRecordList) {

			String apiRecordStatus = apiRecord.getStatus();
			if (!TransferStatus.TRANSFERING.getCode().equals(apiRecordStatus)
					&& !TransferStatus.TRANSFER_DONE.getCode().equals(apiRecordStatus)
					&& !TransferStatus.TRANSFER_FAILED.getCode().equals(apiRecordStatus)
					&& !TransferStatus.TRANSFER_ROLLBACK.getCode().equals(apiRecordStatus)) {
				continue;
			}

			String dealNo = apiRecord.getDealNo();

			PaymentReturn<UserCommission> ret = paymentApiImpl.getLocalResult(dealNo);
			String retCode = ret.getRetCode();
			if (!ApiReturnCode.SUCCESS.getCode().equals(retCode)) {
				logger.error("error occured in get payment record[{}] for error code[{}]", dealNo, retCode);
				continue;
			}

			UserCommission commission = ret.getAttachment();
			totalReportLineData.users.add(commission.getCertId());
			totalReportLineData.accounts.add(commission.getAccount());
			BigDecimal amount = new BigDecimal(commission.getAmount());
			totalReportLineData.totalAmount = totalReportLineData.totalAmount.add(amount);

			String transferCorpId = commission.getCompanyId();
			ReportLineData transferCorpData = reportLineDataInTransferCorp.get(transferCorpId);
			if (transferCorpData == null) {
				transferCorpData = new ReportLineData();
				transferCorpData.transferCorpId = transferCorpId;
				reportLineDataInTransferCorp.put(transferCorpId, transferCorpData);
			}

			transferCorpData.users.add(commission.getCertId());
			transferCorpData.accounts.add(commission.getAccount());
			transferCorpData.totalAmount = transferCorpData.totalAmount.add(amount);

			if (commission.getStatus() == 1) {
				BigDecimal feeAmount = new BigDecimal(commission.getSumFee());

				totalReportLineData.totalDoneAmount = totalReportLineData.totalDoneAmount.add(amount);
				totalReportLineData.totalDoneCount++;
				totalReportLineData.totalFeeAmount = totalReportLineData.totalFeeAmount.add(feeAmount);

				transferCorpData.totalDoneAmount = transferCorpData.totalDoneAmount.add(amount);
				transferCorpData.totalDoneCount++;
				transferCorpData.totalFeeAmount = transferCorpData.totalFeeAmount.add(feeAmount);
			} else {
				totalReportLineData.totalFailAmount = totalReportLineData.totalFailAmount.add(amount);
				totalReportLineData.totalFailCount++;

				transferCorpData.totalFailAmount = transferCorpData.totalFailAmount.add(amount);
				transferCorpData.totalFailCount++;
			}
		}

		dataLines.add(totalReportLineData.toDataLine());
		for (ReportLineData lineData : reportLineDataInTransferCorp.values()) {
			dataLines.add(lineData.toDataLine());
		}

		return dataLines;
	}

	private static class ReportLineData {

		String transferCorpId;

		Set<String> users = new HashSet<String>();

		Set<String> accounts = new HashSet<String>();

		BigDecimal totalAmount = new BigDecimal(0);

		BigDecimal totalDoneAmount = new BigDecimal(0);

		BigDecimal totalFailAmount = new BigDecimal(0);

		BigDecimal totalFeeAmount = new BigDecimal(0);

		int totalDoneCount = 0;

		int totalFailCount = 0;

		public String[] toDataLine() {
			int totalCount = totalDoneCount + totalFailCount;
			return new String[] { transferCorpId, String.valueOf(users.size()), String.valueOf(accounts.size()),
					String.valueOf(totalDoneCount + totalFailCount),
					totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), String.valueOf(totalDoneCount),
					totalDoneAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), String.valueOf(totalFailCount),
					totalFailAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
					totalCount == 0 ? "-"
							: new BigDecimal(totalDoneCount)
									.divide(new BigDecimal(totalCount), 4, BigDecimal.ROUND_HALF_UP)
									.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%",
					totalCount == 0 ? "-"
							: totalDoneAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
									.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
									+ "%",
					totalFeeAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() };
		}
	}
}
