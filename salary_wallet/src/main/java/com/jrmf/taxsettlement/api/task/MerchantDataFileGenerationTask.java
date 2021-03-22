package com.jrmf.taxsettlement.api.task;

import com.alibaba.fastjson.JSON;
import com.jrmf.taxsettlement.api.*;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import org.springframework.stereotype.Component;

@Component("MerchantDataFileGenerationTask")
public class MerchantDataFileGenerationTask {

	private Logger logger = LoggerFactory.getLogger(MerchantDataFileGenerationTask.class);

	private static final String PROCESS = "process";

	private static final String DFT_MERCHANT_DAY_SERIAL = "MERCHANT_DAY_SERIAL";

	private static final String DFT_MERCHANT_COMPANY_DAY_SERIAL = "MERCHANT_COMPANY_DAY_SERIAL";

	private static final String DFT_MERCHANT_MONTH_REPORT = "MERCHANT_MONTH_REPORT";

	@Autowired
	private APIDockingManager apiDockingManager;

	@Autowired
	private MerchantDataFileGenerators merchantDataFileGenerators;

	@XxlJob("invokeToGenerateMerchantDaySerialFileOfLastDay")
	public ReturnT<String> invokeToGenerateMerchantDaySerialFileOfLastDay(String date) {

		String lastDate = getLastDate();

		if (!StringUtil.isEmpty(date)) {
			lastDate = date;
		}

		logger.info("Task-GenerateMerchantDaySerialFileOfLastDay[{}] start", lastDate);

		MerchantDataFileGenerator generator = merchantDataFileGenerators.get(DFT_MERCHANT_DAY_SERIAL);
		List<MerchantAPIDockingProfile> profileList = getCurrentAPIOpenMerchantProfiles();
		int openApiMerchantCount = profileList.size();

		List<Future<?>> futures = new ArrayList<>(openApiMerchantCount);
		
		for (MerchantAPIDockingProfile profile : profileList) {
			String finalLastDate = lastDate;
			futures.add(ThreadUtil.pdfThreadPool.submit(new Runnable() {

				@Override
				public void run() {
					MDC.put(PROCESS, UUID.randomUUID().toString().replaceAll("-", ""));
					logger.info("start to generate day serial data file of date[{}] for merchant[{}]",
							finalLastDate,
							profile.getMerchantId());

					Map<String, Object> params = new HashMap<String, Object>();
					params.put(APIDefinitionConstants.CFN_MERCHANT_ID, profile.getMerchantId());
					params.put(APIDefinitionConstants.CFN_ACCOUNT_DATE, finalLastDate);
					try {
						generator.generateDataFile(params);

						logger.info("complish to generate day serial data file of date[{}] for merchant[{}]",
								finalLastDate,
								profile.getMerchantId());
					} catch (Exception e) {
						logger.error("error occured in generating day serial data file of date[" + finalLastDate
								+ "] for merchant[" + profile.getMerchantId() + "]", e);
					}
				}
			}));
		}
		
		for(Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				logger.error("error occured in waiting task finish", e);
			} 
		}
		
		logger.info("Task-GenerateMerchantDaySerialFileOfLastDay[{}] finished", lastDate);
		return ReturnT.SUCCESS;
	}

	private String getLastDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		return new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
	}


	@XxlJob("invokeToGenerateMerchantCompanyDaySerialFileOfLastDay")
	public ReturnT<String> invokeToGenerateMerchantCompanyDaySerialFileOfLastDay(String date) {

		String lastDate = getLastDate();

		if (!StringUtil.isEmpty(date)) {
			lastDate = date;
		}

		logger.info("Task-GenerateMerchantCompanyDaySerialFileOfLastDay[{}] start", lastDate);

		MerchantDataFileGenerator generator = merchantDataFileGenerators.get(DFT_MERCHANT_COMPANY_DAY_SERIAL);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(APIDockingRepositoryConstants.API_DOCKING_MODES,
				new Integer[] { APIDockingMode.PRODUCTION.getModeCode(), APIDockingMode.TEST.getModeCode() });
		List<MerchantAPIDockingProfile> profileList = apiDockingManager.listMerchantCompanyIdAPIDockingProfile(params);

		int openApiMerchantCount = profileList.size();

		List<Future<?>> futures = new ArrayList<>(openApiMerchantCount);

		for (MerchantAPIDockingProfile profile : profileList) {
			String finalLastDate = lastDate;
			futures.add(ThreadUtil.pdfThreadPool.submit(() -> {
				MDC.put(PROCESS, UUID.randomUUID().toString().replaceAll("-", ""));
				logger.info("start to generate day serial data file of date[{}] for merchant[{}] and companyId[{}]",
						finalLastDate, profile.getMerchantId(), profile.getCompanyId());

				Map<String, Object> params1 = new HashMap<>();
				params1.put(APIDefinitionConstants.CFN_MERCHANT_ID, profile.getMerchantId());
				params1.put(APIDefinitionConstants.CFN_TRANSFER_CORP_ID, profile.getCompanyId());
				params1.put(APIDefinitionConstants.CFN_ACCOUNT_DATE, finalLastDate);

				try {
					generator.generateDataFile(params1);

					logger.info(
							"complish to generate day serial data file of date[{}] for merchant[{}] and companyId[{}]",
							finalLastDate, profile.getMerchantId(), profile.getCompanyId());
				} catch (Exception e) {
					logger.error("error occured in generating day serial data file of date[" + finalLastDate
							+ "] for merchant[" + profile.getMerchantId() + "] and companyId[" + profile.getCompanyId() + "]", e);
				}
			}));
		}

		for(Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				logger.error("error occured in waiting task finish", e);
			}
		}

		logger.info("Task-GenerateMerchantDaySerialFileOfLastDay[{}] finished", lastDate);
		return ReturnT.SUCCESS;
	}

	@XxlJob("invokeToGenerateMerchantMonthReportFileOfLastMonth")
	public ReturnT<String> invokeToGenerateMerchantMonthReportFileOfLastMonth(String args) {
		String lastMonth = getLastMonth();
		logger.info("Task-GenerateMerchantMonthReportFileOfLastDay[{}] start", lastMonth);

		MerchantDataFileGenerator generator = merchantDataFileGenerators.get(DFT_MERCHANT_MONTH_REPORT);
		List<MerchantAPIDockingProfile> profileList = getCurrentAPIOpenMerchantProfiles();
		int openApiMerchantCount = profileList.size();

		List<Future<?>> futures = new ArrayList<Future<?>>(openApiMerchantCount);
		
		for (MerchantAPIDockingProfile profile : profileList) {
			futures.add(ThreadUtil.pdfThreadPool.submit(new Runnable() {

				@Override
				public void run() {
					MDC.put(PROCESS, UUID.randomUUID().toString().replaceAll("-", ""));
					logger.info("start to generate month report data file of date[{}] for merchant[{}]", lastMonth,
							profile.getMerchantId());

					Map<String, Object> params = new HashMap<String, Object>();
					params.put(APIDefinitionConstants.CFN_MERCHANT_ID, profile.getMerchantId());
					params.put(APIDefinitionConstants.CFN_ACCOUNT_MONTH, lastMonth);
					try {
						generator.generateDataFile(params);

						logger.info("complish to generate day month report file of date[{}] for merchant[{}]", lastMonth,
								profile.getMerchantId());
					} catch (Exception e) {
						logger.error("error occured in generating month report data file of date[" + lastMonth
								+ "] for merchant[" + profile.getMerchantId() + "]", e);
					}
				}
			}));
		}
		
		for(Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				logger.error("error occured in waiting task finish", e);
			} 
		}
		
		logger.info("Task-GenerateMerchantMonthReportFileOfLastDay[{}] finished", lastMonth);
		return ReturnT.SUCCESS;
	}

	private String getLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -1);
		return new SimpleDateFormat("yyyyMM").format(calendar.getTime());
	}
	
	private List<MerchantAPIDockingProfile> getCurrentAPIOpenMerchantProfiles() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(APIDockingRepositoryConstants.API_DOCKING_MODES,
				new Integer[] { APIDockingMode.PRODUCTION.getModeCode(), APIDockingMode.TEST.getModeCode() });

		PageData<MerchantAPIDockingProfile> pageData = apiDockingManager.listMerchantAPIDockingProfile(params);
		List<MerchantAPIDockingProfile> profileList = pageData.getPageRecords();

		logger.debug("current api open merchant list is:{}", JSON.toJSONString(profileList));
		return profileList;
	}
}
