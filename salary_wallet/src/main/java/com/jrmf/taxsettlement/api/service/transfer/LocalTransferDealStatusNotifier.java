package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.taxsettlement.api.*;
import com.jrmf.taxsettlement.api.gateway.APIDockingGatewayDataUtil;
import com.jrmf.taxsettlement.api.gateway.batch.TransferBatchStatus;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.query.QueryTransferDealServiceAttachment;
import com.jrmf.taxsettlement.api.service.query.QueryTransferDealServiceParams;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

public class LocalTransferDealStatusNotifier implements TransferDealStatusNotifier {

	private static final String DEFAULT_PARTNER_ID = "JRMF";

	private static final Logger logger = LoggerFactory.getLogger(LocalTransferDealStatusNotifier.class);

	private static final String QUERY_API_KEY = "QUERY_TRANSFER_DEAL";

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final String DEFAULT_FIELD_DIV = "|";

	private static final String DEFAULT_ROW_END = "\r\n";

	@Autowired
	private SignWorkers signWorkers;

	@Autowired
	private APIDockingManager apiDockingManager;

	@Autowired
	private MerchantAPITransferRecordDao transferRecordDao;

	@Autowired
	private MerchantAPITransferBatchDao transferBatchDao;

	@Autowired
	private JmsTemplate providerJmsTemplate;

	@Autowired
	private Destination transferDealStatusNoticeDestination;

	private Map<String, BatchTransferDataRuntime> batchRuntimeTable = new ConcurrentHashMap<String, BatchTransferDataRuntime>();

	@Override
	public void notify(String dealNo, TransferStatus status, String retCode, String retMsg) {

		logger.debug("receive deal[{}] transfer status[{}] notify[{}:{}]", dealNo, status, retCode, retMsg);

		recordDealStatus(dealNo, status, retCode, retMsg);

		APITransferRecordDO recordDo = transferRecordDao.getDealRecord(dealNo);
		String batchNo = recordDo.getBatchNo();
		if (batchNo == null) {
			independentlyNotify(recordDo.getMerchantId(), recordDo.getNotifyUrl(), dealNo);
		} else {
			tryToUniformlyNotify(recordDo.getMerchantId(), recordDo.getPartnerId(), recordDo.getNotifyUrl(), batchNo);
		}
	}

	private void tryToUniformlyNotify(String merchantId, String partnerId, String notifyUrl, String batchNo) {
		if (StringUtil.isEmpty(notifyUrl)) {
            return;
        }

		boolean proxyMode = !DEFAULT_PARTNER_ID.equals(partnerId);
		String key = proxyMode ? generateBatchRuntimeKey(partnerId, batchNo)
				: generateBatchRuntimeKey(merchantId, batchNo);
		BatchTransferDataRuntime newRuntime = new BatchTransferDataRuntime();
		BatchTransferDataRuntime existedRuntime = batchRuntimeTable.putIfAbsent(key, newRuntime);
		if (existedRuntime == null) {
            existedRuntime = newRuntime;
        }

		if (existedRuntime.toUniformlyNotifyOnOneMoreResultGot()) {
			try {
				updateBatchStatusAndUniformlyNotify(proxyMode ? partnerId : merchantId, proxyMode, batchNo, notifyUrl);
			} finally {
				batchRuntimeTable.remove(key);
			}
		}
	}

	private void updateBatchStatusAndUniformlyNotify(String merchantId, boolean proxyMode, String batchNo,
			String notifyUrl) {

		Action<QueryTransferDealServiceParams, QueryTransferDealServiceAttachment> action = apiDockingManager
				.getDockingService(QUERY_API_KEY);

		int totalAcceptCount = 0;
		int totalDoneCount = 0;
		int totalFailCount = 0;

		ByteArrayOutputStream detailBytes = new ByteArrayOutputStream();
		try {
			GZIPOutputStream zipOut = new GZIPOutputStream(detailBytes);
			try {
				Map<String, Object> queryParams = new HashMap<String, Object>();

				if (proxyMode) {
					queryParams.put(APIDockingRepositoryConstants.PARTNER_ID, merchantId);
				} else {
					queryParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
				}
				queryParams.put(APIDockingRepositoryConstants.BATCH_NO, batchNo);

				for (APITransferRecordDO transferRecord : transferRecordDao.listDealRecord(queryParams)) {

					totalAcceptCount++;

					String dealNo = transferRecord.getDealNo();
					QueryTransferDealServiceParams params = new QueryTransferDealServiceParams();
					params.setDealNo(dealNo);
					ActionResult<QueryTransferDealServiceAttachment> result = action.execute(params);
					if (!result.isOk()) {
						logger.error("failed to query deal[{}] detail for ret[{}:{}]",
								new Object[] { dealNo, result.getRetCode(), result.getRetMsg() });
						continue;
					}

					QueryTransferDealServiceAttachment attachment = result.getRetData();
					String detailRow = construct(attachment);
					zipOut.write(detailRow.getBytes(DEFAULT_CHARSET));

					if (TransferStatus.TRANSFER_DONE.getCode().equals(attachment.getDealStatus())) {
						totalDoneCount++;
					} else {
						totalFailCount++;
					}
				}
			} finally {
				if (zipOut != null) {
					zipOut.flush();
					zipOut.close();
				}
			}
		} catch (Exception e) {
			logger.error("fatal error occurd in querying detail and abort notify mission", e);
			return;
		}

		Map<String, Object> updateParams = new HashMap<String, Object>();
		updateParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		updateParams.put(APIDockingRepositoryConstants.BATCH_NO, batchNo);
		updateParams.put(APIDockingRepositoryConstants.TRANSFER_DONE_REQUEST_COUNT, totalDoneCount);
		updateParams.put(APIDockingRepositoryConstants.TRANSFER_FAIL_REQUEST_COUNT, totalFailCount);
		updateParams.put(APIDockingRepositoryConstants.STATUS, TransferBatchStatus.BATCH_DONE.getCode());

		try {
			transferBatchDao.updateTransferBatch(updateParams);
		} catch (Exception e) {
			logger.error("error occured in updating batch record", e);
		}

		uniformlyNotify(merchantId, batchNo, notifyUrl, totalAcceptCount, totalDoneCount, totalFailCount,
				detailBytes.toByteArray());
	}

	private void uniformlyNotify(String merchantId, String batchNo, String notifyUrl, int totalAcceptCount,
			int totalDoneCount, int totalFailCount, byte[] detailBytes) {

		MerchantAPIDockingConfig dockingConfig = apiDockingManager.getMerchantAPIDockingConfig(merchantId);
		String signType = dockingConfig.getSignType();

		String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String sign = generateBatchFileSign(merchantId, batchNo, timestamp, detailBytes, signType,
				dockingConfig.getSignGenerationKey());

		Map<String, Object> outData = new HashMap<String, Object>();

		outData.put(APIDefinitionConstants.CFN_BATCH_NO, batchNo);
		outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
		outData.put(APIDefinitionConstants.CFN_TOTAL_ACCEPT_COUNT, String.valueOf(totalAcceptCount));
		outData.put(APIDefinitionConstants.CFN_TOTAL_DONE_COUNT, String.valueOf(totalDoneCount));
		outData.put(APIDefinitionConstants.CFN_TOTAL_FAIL_COUNT, String.valueOf(totalFailCount));
		outData.put(APIDefinitionConstants.CFN_TIMESTAMP, timestamp);
		outData.put(APIDefinitionConstants.CFN_SIGN, sign);
		outData.put(APIDefinitionConstants.CFN_ACCEPT_DEAL_DETAILS, detailBytes);

		final TransferDealStatusNotice notice = new TransferDealStatusNotice(notifyUrl, outData);
		notice.setBatchNotice(true);

		providerJmsTemplate.send(transferDealStatusNoticeDestination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(notice);
			}
		});

	}

	private String construct(QueryTransferDealServiceAttachment attachment) {
		return new StringBuilder(attachment.getRequestNo()).append(DEFAULT_FIELD_DIV).append(attachment.getDealNo())
				.append(DEFAULT_FIELD_DIV).append(attachment.getDealStatus()).append(DEFAULT_FIELD_DIV)
				.append(attachment.getDealStatusMsg()).append(DEFAULT_FIELD_DIV).append(attachment.getAccountDate())
				.append(DEFAULT_FIELD_DIV).append(attachment.getFee()).append(DEFAULT_ROW_END).toString();
	}

	private void independentlyNotify(String merchantId, String notifyUrl, String dealNo) {
		if (StringUtil.isEmpty(notifyUrl)) {
            return;
        }

		Action<QueryTransferDealServiceParams, QueryTransferDealServiceAttachment> action = apiDockingManager
				.getDockingService(QUERY_API_KEY);
		QueryTransferDealServiceParams params = new QueryTransferDealServiceParams();
		params.setDealNo(dealNo);
		ActionResult<QueryTransferDealServiceAttachment> result = action.execute(params);
		if (!result.isOk()) {
			logger.error("failed to query deal[{}] detail for ret[{}:{}]", dealNo, result.getRetCode(), result.getRetMsg());
			return;
		}

		MerchantAPIDockingConfig dockingConfig = apiDockingManager.getMerchantAPIDockingConfig(merchantId);
		String signType = dockingConfig.getSignType();

		Map<String, Object> outData = APIDockingGatewayDataUtil.parseAndTransform(result.getRetData());

		outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
		outData.put(APIDefinitionConstants.CFN_TIMESTAMP, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

		Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(outData);
		String sign = generateSign(toSignMap, signType, dockingConfig.getSignGenerationKey());
		outData.put(APIDefinitionConstants.CFN_SIGN, sign);

		final TransferDealStatusNotice notice = new TransferDealStatusNotice(notifyUrl, outData);
		providerJmsTemplate.send(transferDealStatusNoticeDestination, new MessageCreator() {
			@Override
            public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(notice);
			}
		});

	}

	private String generateSign(Map<String, Object> mapData, String signType, String signGenerationKey) {
		SignWorker generator = signWorkers.get(signType);
		try {
			return generator.generateSign(mapData, signGenerationKey);
		} catch (Exception e) {
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
		}
	}

	private String generateBatchFileSign(String merchantId, String batchNo, String timestamp, byte[] fileData,
			String signType, String signGenerationKey) {
		SignWorker signWorker = signWorkers.get(signType);
		if (signWorker == null) {
			throw new RuntimeException("signType[" + signType + "] is not supported");
		}
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			byteArray.write(fileData);
			byteArray.write(merchantId.getBytes());
			byteArray.write(batchNo.getBytes());
			byteArray.write(timestamp.getBytes());

			return signWorker.generateSign(byteArray.toByteArray(), signGenerationKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void recordDealStatus(String dealNo, TransferStatus status, String retCode, String retMsg) {
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(APIDockingRepositoryConstants.DEAL_NO, dealNo);
			params.put(APIDockingRepositoryConstants.STATUS, status.getCode());

			if (TransferStatus.TRANSFER_DONE.equals(status)) {
				params.put(APIDockingRepositoryConstants.ACCOUNT_DATE,
						new SimpleDateFormat("yyyyMMdd").format(new Date()));
			}

			params.put(APIDockingRepositoryConstants.RET_CODE, retCode);
			params.put(APIDockingRepositoryConstants.RET_MSG, retMsg);
			transferRecordDao.updateTransferRequest(params);
		} catch (Exception e) {
			logger.error("error occured in recording deal[" + dealNo + "] notify message", e);
		}
	}

	@Override
	public void registerBatch(String merchantId, boolean proxyMode, String batchNo, int acceptCount, String notifyUrl) {
		String key = generateBatchRuntimeKey(merchantId, batchNo);
		BatchTransferDataRuntime existedRuntime = batchRuntimeTable.putIfAbsent(key,
				new BatchTransferDataRuntime(acceptCount));
		if (existedRuntime != null && batchRuntimeTable.get(key).toUniformlyNotifyOnSetAcceptCount(acceptCount)) {
			try {
				updateBatchStatusAndUniformlyNotify(merchantId, proxyMode, batchNo, notifyUrl);
			} finally {
				batchRuntimeTable.remove(key);
			}
		}
	}

	private String generateBatchRuntimeKey(String merchantId, String batchNo) {
		return merchantId + "-" + batchNo;
	}

	private static class BatchTransferDataRuntime {

		private volatile int acceptCount;

		private AtomicInteger notifyResultCount = new AtomicInteger(0);

		public BatchTransferDataRuntime(int acceptCount) {
			this.acceptCount = acceptCount;
		}

		public BatchTransferDataRuntime() {
		}

		public boolean toUniformlyNotifyOnSetAcceptCount(int acceptCount) {
			this.acceptCount = acceptCount;
			return acceptCount == notifyResultCount.get();
		}

		public boolean toUniformlyNotifyOnOneMoreResultGot() {
			return acceptCount == notifyResultCount.incrementAndGet();
		}
	}
}
