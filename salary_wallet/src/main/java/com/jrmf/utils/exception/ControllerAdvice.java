package com.jrmf.utils.exception;

import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.TaxSettlementInertnessDataCache;
import com.jrmf.taxsettlement.api.gateway.control.APIDockingManagementException;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.utils.RespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ControllerAdvice
 */
@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {
	
	private static Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

	@Autowired
	private TaxSettlementInertnessDataCache dataCache;

	@Autowired
	private APIDockingManager apiDockingManager;

	@Autowired
	private SignWorkers signWorkers;

	@ExceptionHandler(APIDockingException.class)
	@ResponseBody
	public Map<String, Object> exceptionHandle(APIDockingException e, HttpServletRequest request) {

		logger.error("", e);
		
		String errorCode = e.getErrorCode();
		String  serialNumber = e.getSerialNumber();
		String merchantId = (String) request.getAttribute(APIDefinitionConstants.CFN_MERCHANT_ID);

		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(APIDefinitionConstants.CFN_RET_CODE, errorCode);
	  String errMsg =	dataCache.getErrorMsg(errorCode);
	  if(StringUtils.isEmpty(errMsg)){
			retMap.put(APIDefinitionConstants.CFN_RET_MSG, e.getErrorMsg());
		}else {
			retMap.put(APIDefinitionConstants.CFN_RET_MSG, errMsg);
		}
		retMap.put(APIDefinitionConstants.CFN_TIMESTAMP, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

	  if(!StringUtils.isEmpty(serialNumber)){
			retMap.put(APIDefinitionConstants.SERIAL_NUMBER,serialNumber);
		}

		if (merchantId != null) {
			MerchantAPIDockingConfig dockingConfig = apiDockingManager.getMerchantAPIDockingConfig(merchantId);
			SignWorker worker = signWorkers.get(dockingConfig.getSignType());
			try {
				String signGet = worker.generateSign(retMap, dockingConfig.getSignGenerationKey());
				retMap.put(APIDefinitionConstants.CFN_SIGN, signGet);
			} catch (Exception anotherExp) {
				logger.error("error occured in sign generating", anotherExp);
			}
		}
		return retMap;
	}
	
	@ExceptionHandler(APIDockingManagementException.class)
	@ResponseBody
	public Map<String, Object> exceptionHandle(APIDockingManagementException e, HttpServletRequest request) {

		String errorCode = e.getErrorCode();

		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(RespCode.RESP_STAT, errorCode);
		retMap.put(RespCode.RESP_MSG, dataCache.getErrorMsg(errorCode));
		return retMap;
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String, Object> exceptionHandle(Exception e) {

		if (e instanceof LoginException) {
			LoginException exception = (LoginException) e;
			int state = exception.getState();
			String respmsg = exception.getRespmsg();
			return responseMap(state, respmsg);
		} else if (e instanceof SessionDestroyedException) {
			SessionDestroyedException exception = (SessionDestroyedException) e;
			int state = exception.getState();
			String respmsg = exception.getRespmsg();
			return responseMap(state, respmsg);
		} else if (e instanceof IOException) {
			logger.error("\nIOException--->", e);
			int state = RespCode.FILE_IOEXCEPTION;
			String respmsg = RespCode.codeMaps.get(state);
			return responseMap(state, respmsg);
		} else if (e instanceof ImportException) {
            logger.error("\nImportException--->", e);
            int state = ((ImportException) e).getState();
            String respmsg = ((ImportException) e).getRespmsg();
            return responseMap(state, respmsg);
        } else {
			logger.error("\nException--->", e);
			return responseMap(0, e.getClass().getName());
		}
	}

	private Map<String, Object> responseMap(int code, String msg) {
		Map<String, Object> map = new HashMap<>(4);
		map.put(RespCode.RESP_STAT, code);
		map.put(RespCode.RESP_MSG, msg);
		logger.error("Exception--->code:{}--msg:{}", code, msg);
		return map;
	}
}
