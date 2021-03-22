package com.jrmf.taxsettlement.api.service.download;

import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.Objects;
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
import org.springframework.beans.factory.annotation.Value;

@ActionConfig(name = "日流水文件URL获取")
public class GetDaySerialFileURLService
		implements Action<GetDaySerialFileURLServiceParams, GetDaySerialFileURLServiceAttachment> {

	private static final Logger logger = LoggerFactory.getLogger(GetDaySerialFileURLService.class);

	private static final String DAY_SERIAL_FILE_NAME_PREFIX = "DS_";

	private static final int DEFAULT_DOWNLOAD_FILE_URL_CACHE_LIFE = 1200;
	
	@Autowired
	private FileRepository fileRepository;
	
	@Autowired
	private UtilCacheManager utilCacheManager;

	@Value("${fileRepositoryRootPath}")
	private String fileRepositoryRootPath;
	@Value("${filePublishUrlContext}")
	private String filePublishUrlContext;

	private final int daySerialReportDownloadableDays = 90;

	@Override
	public String getActionType() {
		return APIDefinition.GET_DAY_SERIAL_FILE_URL.name();
	}

	@Override
	public ActionResult<GetDaySerialFileURLServiceAttachment> execute(GetDaySerialFileURLServiceParams actionParams) {

		String accountDate = actionParams.getAccountDate();
		try {
			if (isBeyondLimitDate(accountDate)) {
				throw new APIDockingException(APIDockingRetCodes.ACCOUNT_DATE_BEYOND_LIMIT.getCode(), accountDate);
			}
		} catch (ParseException e) {
			throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), e.getMessage());
		}

		String merchantId = actionParams.getMerchantId();
		String companyId = actionParams.getTransferCorpId();
		String fileName = getFileName(merchantId, companyId, accountDate);

		String existFilePath =
				fileRepositoryRootPath + FileRepository.PATH_DIV + merchantId + FileRepository.PATH_DIV;

		if (!FtpTool.checkFile(existFilePath + fileName + ".gzip")) {
			throw new APIDockingException(APIDockingRetCodes.DATA_FILE_NOT_EXISTED.getCode(), fileName);
		}

		String checkTail = VerifyCodeUtil.generateVerifyCode(6);
		String storeFileName = fileName + "_" + checkTail;

		FtpTool.uploadFile(existFilePath, storeFileName + ".txt.gzip",
				new ByteArrayInputStream(
						Objects.requireNonNull(FtpTool.downloadFtpFile(existFilePath, fileName + ".gzip"))));

//		fileRepository.renameFile(existFilePath,
//				storePath);
		
		GetDaySerialFileURLServiceAttachment attachment = new GetDaySerialFileURLServiceAttachment();
		attachment.setDownloadUrl(new StringBuilder(filePublishUrlContext).append(storeFileName).toString());

		utilCacheManager.put(storeFileName, existFilePath + storeFileName + ".txt.gzip",
				DEFAULT_DOWNLOAD_FILE_URL_CACHE_LIFE);
		return new ActionResult<GetDaySerialFileURLServiceAttachment>(attachment);
	}

	private boolean isBeyondLimitDate(String accountDateStr) throws ParseException {
		Date today = new Date();
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		if (format.format(today).equals(accountDateStr)) {
			return true;
		}

		Date accountDate = format.parse(accountDateStr);
		int divDays = ((int) (today.getTime() / 1000) - (int) (accountDate.getTime() / 1000)) / 3600 / 24;
		return divDays > daySerialReportDownloadableDays;
	}

	private String getFileName(String merchantId, String companyId, String accountDate) {
		StringBuilder fileName = new StringBuilder(DAY_SERIAL_FILE_NAME_PREFIX).append(merchantId);
		if (!StringUtil.isEmpty(companyId)) {
			fileName.append("_").append(companyId);
		}
		fileName.append("_").append(accountDate);
		return fileName.toString();
	}

}
