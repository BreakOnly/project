package com.jrmf.taxsettlement.api.service.download;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.taxsettlement.util.code.VerifyCodeUtil;
import com.jrmf.taxsettlement.util.file.FileRepository;

@ActionConfig(name = "月报表下载URL获取")
public class GetMonthReportFileURLService
		implements Action<GetMonthReportFileURLServiceParams, GetMonthReportFileURLServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(GetMonthReportFileURLService.class);

	private static final String MONTH_REPORT_FILE_NAME_PREFIX = "MR_";

	private static final int DEFAULT_DOWNLOAD_FILE_URL_CACHE_LIFE = 1200;

	@Autowired
	private FileRepository fileRepository;
	
	@Autowired
	private UtilCacheManager utilCacheManager;

	private final int monthReportDownloadableDays = 180;

	@Override
	public String getActionType() {
		return APIDefinition.GET_MONTH_REPORT_FILE_URL.name();
	}

	@Override
	public ActionResult<GetMonthReportFileURLServiceAttachment> execute(
			GetMonthReportFileURLServiceParams actionParams) {

		String accountMonth = actionParams.getAccountMonth();
		try {
			if (isBeyondLimitMonth(accountMonth)) {
				throw new APIDockingException(APIDockingRetCodes.ACCOUNT_MONTH_BEYOND_LIMIT.getCode(), accountMonth);
			}
		} catch (ParseException e) {
			throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), e.getMessage());
		}

		String merchantId = actionParams.getMerchantId();
		String fileName = getFileName(merchantId, accountMonth);
		List<String> fileList = fileRepository.findFile(merchantId,
				new StringBuilder(fileName).append(FileRepository.ANY_PART_SIGNAL).toString());
		if (fileList.isEmpty()) {
			throw new APIDockingException(APIDockingRetCodes.DATA_FILE_NOT_EXISTED.getCode(), fileName);
		}

		String existFilePath = fileList.get(0);
		String checkTail = VerifyCodeUtil.generateVerifyCode(6);
		fileName = new StringBuilder(fileName).append("_").append(checkTail).toString();

		String storePath = new StringBuilder(merchantId).append(FileRepository.PATH_DIV).append(fileName)
				.append(".txt.gzip").toString();
		fileRepository.renameFile(existFilePath, storePath);

		GetMonthReportFileURLServiceAttachment attachment = new GetMonthReportFileURLServiceAttachment();
		attachment.setDownloadUrl(new StringBuilder(fileRepository.getPublishURLContext()).append(fileName).toString());

		utilCacheManager.put(fileName, storePath, DEFAULT_DOWNLOAD_FILE_URL_CACHE_LIFE);
		return new ActionResult<GetMonthReportFileURLServiceAttachment>(attachment);
	}

	private boolean isBeyondLimitMonth(String accountMonthStr) throws ParseException {
		Date today = new Date();
		DateFormat format = new SimpleDateFormat("yyyyMM");
		if (format.format(today).equals(accountMonthStr)) {
			return true;
		}

		Date accountDate = format.parse(accountMonthStr);
		int divDays = ((int) (today.getTime() / 1000) - (int) (accountDate.getTime() / 1000)) / 3600 / 24;
		return divDays > monthReportDownloadableDays;
	}

	private String getFileName(String merchantId, String accountMonth) {
		return new StringBuilder(MONTH_REPORT_FILE_NAME_PREFIX).append(merchantId).append("_").append(accountMonth)
				.toString();
	}
}
