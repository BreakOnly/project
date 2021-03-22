package com.jrmf.taxsettlement.api.gateway.batch;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRepositoryConstants;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.MerchantAPITransferBatchDao;
import com.jrmf.taxsettlement.api.gateway.batch.form.BatchFormDistillResult;
import com.jrmf.taxsettlement.api.gateway.batch.form.BatchFormDistiller;
import com.jrmf.taxsettlement.api.gateway.paramseditor.ServiceParamsEditor;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.AbstractTransferServiceParams;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.commons.lang.xwork.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DefaultBatchTransferRequestDealer implements BatchTransferRequestDealer {

	private final static String PROCESS = "process";

	private static final Logger logger = LoggerFactory.getLogger(DefaultBatchTransferRequestDealer.class);

	private BatchFormDistiller batchFormDistiller;

	private List<ServiceParamsEditor> paramsEditors = new ArrayList<ServiceParamsEditor>();

	public DefaultBatchTransferRequestDealer(int concurrentThreadCount, BatchFormDistiller batchFormDistiller) {
		this.batchFormDistiller = batchFormDistiller;
	}
	
	public DefaultBatchTransferRequestDealer(int concurrentThreadCount, BatchFormDistiller batchFormDistiller, List<ServiceParamsEditor> paramsEditors) {
		this.batchFormDistiller = batchFormDistiller;
		this.paramsEditors.addAll(paramsEditors);
	}

	@Override
	public BatchDealResult batchSubmit(String merchantId, String partnerId, String notifyUrl, String batchNo,
			String transferCorpId, byte[] fileBytes, Action<ActionParams, ActionAttachment> dockingService,
			MerchantAPITransferBatchDao apiTransferBatchDao, TransferDealStatusNotifier statusNotifier)
			throws Exception {

		logger.debug("start batch[{}] of merchant[{}] deal with partner[{}], notifyUrl[{}], transferCorpId[{}]",
				new Object[] { batchNo, merchantId, partnerId, notifyUrl, transferCorpId });

		Type genericType = null;
		Type[] genericInterfaces = dockingService.getClass().getGenericInterfaces();
		if (genericInterfaces == null || genericInterfaces.length == 0) {
			genericType = dockingService.getClass().getGenericSuperclass();
		} else {
			genericType = genericInterfaces[0];
		}

		Class<? extends ActionParams> exactalParamClass = (Class<? extends ActionParams>) ((ParameterizedType) genericType)
				.getActualTypeArguments()[0];
		BatchFormDistillResult distillResult = batchFormDistiller.distill(fileBytes, exactalParamClass);

		int distilledCount = distillResult.getDistilledActionParamsList().size();
		int undistillCount = distillResult.getUndistillDataBriefMsgList().size();

		logger.debug("finish distilling data of batch[{}] with result:distilled[{}], undistill[{}]",
				new Object[] { batchNo, distilledCount, undistillCount });

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		params.put(APIDockingRepositoryConstants.BATCH_NO, batchNo);
		params.put(APIDockingRepositoryConstants.PARTNER_ID, partnerId);
		params.put(APIDockingRepositoryConstants.TRANSFER_CORP_ID, transferCorpId);
		params.put(APIDockingRepositoryConstants.NOTIFY_URL, notifyUrl);
		params.put(APIDockingRepositoryConstants.TOTAL_REQUEST_COUNT, distilledCount + undistillCount);
		params.put(APIDockingRepositoryConstants.DISTILLED_REQUEST_COUNT, distilledCount);
		params.put(APIDockingRepositoryConstants.UNDISTILL_REQUEST_COUNT, undistillCount);
		params.put(APIDockingRepositoryConstants.TRANSFER_DONE_REQUEST_COUNT, 0);
		params.put(APIDockingRepositoryConstants.TRANSFER_FAIL_REQUEST_COUNT, 0);
		params.put(APIDockingRepositoryConstants.STATUS, TransferBatchStatus.BATCH_ACCEPTED.getCode());

		try {
			apiTransferBatchDao.addNewTransferBatch(params);
		} catch (Exception e) {
			logger.error("error occured in adding batch record and abort", e);
			throw new APIDockingException(APIDockingRetCodes.BATCH_NO_EXISTED.getCode(),
					new StringBuilder(merchantId).append("-").append(batchNo).toString());
		}

		BatchDealResult batchDealResult = new BatchDealResult();
		for (String undistillStr : distillResult.getUndistillDataBriefMsgList()) {
			batchDealResult.addSingleResult(
					new ActionResult<ActionAttachment>(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), undistillStr));
		}

		TransformAndCheckResult transformResult = batchTransformAndCheck(merchantId, partnerId, batchNo, notifyUrl,
				transferCorpId, distillResult.getDistilledActionParamsList());
		List<ActionResult<ActionAttachment>> unacceptResults = transformResult.getUnacceptActionResults();
		for (ActionResult<ActionAttachment> unacceptResult : unacceptResults) {
			batchDealResult.addSingleResult(unacceptResult);
		}
		logger.debug("unaccept request count of batch[{}] is[{}]", new Object[] { batchNo, unacceptResults.size() });

		List<AbstractTransferServiceParams> transferServiceParamsList = transformResult.getTransferServiceParamsList();
		int toSubmitServiceCount = transferServiceParamsList.size();
		List<Future<ActionResult<ActionAttachment>>> futureList = new ArrayList<Future<ActionResult<ActionAttachment>>>(
				toSubmitServiceCount);

		logger.debug("start to submit request count[{}] of batch[{}]", new Object[] { toSubmitServiceCount, batchNo });
		boolean proxyMode = false;
		for (AbstractTransferServiceParams actionParams : transferServiceParamsList) {
			
			if(!merchantId.equals(actionParams.getMerchantId())) {
                proxyMode = true;
            }
			
			futureList.add(ThreadUtil.pdfThreadPool.submit(new Callable<ActionResult<ActionAttachment>>() {

				@Override
				public ActionResult<ActionAttachment> call() throws Exception {
					MDC.put(PROCESS, java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
					return dockingService.execute(actionParams);
				}
			}));
		}

		for (Future<ActionResult<ActionAttachment>> future : futureList) {
			try {
				ActionResult<ActionAttachment> result = future.get();
				batchDealResult.addSingleResult(result);
			} catch (ExecutionException e) {
				logger.error("error occured in batch deal", e);
				if (e.getCause() instanceof APIDockingException) {
					APIDockingException apiDockingException = (APIDockingException) e.getCause();
					batchDealResult.addSingleResult(new ActionResult<ActionAttachment>(
							apiDockingException.getErrorCode(), apiDockingException.getErrorMsg()));
				} else {
					batchDealResult.addSingleResult(new ActionResult<ActionAttachment>(
							CommonRetCodes.SYSTEM_BUSY_NOW.getCode(), e.getMessage()));
				}
			} catch (InterruptedException e) {
				batchDealResult.addSingleResult(
						new ActionResult<ActionAttachment>(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage()));
			}
		}

		Map<String, Object> updateParams = new HashMap<>(8);
		updateParams.put(APIDockingRepositoryConstants.MERCHANT_ID, merchantId);
		updateParams.put(APIDockingRepositoryConstants.BATCH_NO, batchNo);
		updateParams.put(APIDockingRepositoryConstants.ACCEPT_REQUEST_COUNT, batchDealResult.getAcceptCount());
		updateParams.put(APIDockingRepositoryConstants.UNACCEPT_REQUEST_COUNT, batchDealResult.getUnacceptCount());
		updateParams.put(APIDockingRepositoryConstants.STATUS, TransferBatchStatus.BATCH_TRANSFERING.getCode());

		try {
			apiTransferBatchDao.updateTransferBatch(updateParams);
		} catch (Exception e) {
			logger.error("error occured in updating batch record", e);
		}

		int acceptCount = batchDealResult.getAcceptCount();
		if (acceptCount > 0 && !StringUtils.isEmpty(notifyUrl)) {
			statusNotifier.registerBatch(merchantId, proxyMode, batchNo, acceptCount, notifyUrl);
		}

		logger.debug("finish batch[{}] of merchant[{}] distill[{}|{}|{}] and submit request and synchronized return",
				new Object[] { batchNo, merchantId, batchDealResult.getTotalCount(), batchDealResult.getAcceptCount(),
						batchDealResult.getUnacceptCount() });

		return batchDealResult;
	}

	protected TransformAndCheckResult batchTransformAndCheck(String merchantId, String partnerId, String batchNo,
			String notifyUrl, String transferCorpId, List<? extends ActionParams> distilledActionParamsList) {

		TransformAndCheckResult result = new TransformAndCheckResult();
		List<AbstractTransferServiceParams> transferServiceParamsList = new ArrayList<AbstractTransferServiceParams>(
				distilledActionParamsList.size());
		for (ActionParams actionParams : distilledActionParamsList) {
			actionParams.setMerchantId(merchantId);
			actionParams.setPartnerId(partnerId);

			AbstractTransferServiceParams transferServiceParams = (AbstractTransferServiceParams) actionParams;
			transferServiceParams.setBatchNo(batchNo);
			transferServiceParams.setNotifyUrl(notifyUrl);

			if (StringUtils.isEmpty(transferServiceParams.getTransferCorpId())) {
				transferServiceParams.setTransferCorpId(transferCorpId);
			}
			
			boolean toAdd = true;
			for(ServiceParamsEditor editor : paramsEditors) {
				try {
					editor.edit(transferServiceParams); 
				} catch(APIDockingException e) {
					result.addUnacceptActionResults(
							new ActionResult<ActionAttachment>(e.getErrorCode(), e.getErrorMsg()));
					toAdd = false;
					break;
				}
			}
			
			if(toAdd) {
                transferServiceParamsList.add(transferServiceParams);
            }
		}
		result.setTransferServiceParamsList(transferServiceParamsList);

		return result;
	}

}
