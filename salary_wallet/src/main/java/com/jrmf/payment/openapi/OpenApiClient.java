package com.jrmf.payment.openapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.payment.entity.AygPayment;
import com.jrmf.payment.openapi.exception.AygOpenApiException;
import com.jrmf.payment.openapi.log.Log;
import com.jrmf.payment.openapi.log.LogFactory;
import com.jrmf.payment.openapi.model.request.FormDataItem;
import com.jrmf.payment.openapi.model.request.IBase64Param;
import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.request.IFormDataParam;
import com.jrmf.payment.openapi.model.request.OpenApiBaseParam;
import com.jrmf.payment.openapi.model.request.econtract.ContractTemplateDownloadParam;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.utils.ParameterUtils;
import com.jrmf.payment.openapi.utils.RsaUtils;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月4日
 */
public class OpenApiClient {


//	private static final Log logger = LogFactory.getLog("com.jrmf.payment.openapi");
	
	private Logger logger = LoggerFactory.getLogger(OpenApiClient.class);
	
	private static final String BLANK_JSON_STRING = "{}";
	
	private static OkHttpClient httpClient;

	private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

	private String appId;
	private String baseUrl;
	private PrivateKey privateKey;

	private OpenApiClient(String appId, PrivateKey privateKey, String baseUrl,int readTimeout) {
		this.appId = appId;
		this.privateKey = privateKey;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

		httpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS)
				.build();
	}

	@SuppressWarnings("unchecked")
	public <RES> OpenApiBaseResponse<RES> execute(IBaseParam<RES> param) {
		String url = baseUrl.concat(param.requestURI());
		
		if(logger.isDebugEnabled()){
			logger.debug("AYG_SDK_BEGIN -> requestURI:" + param.requestURI());
		}
		Response response = null;
		try {
			if (param instanceof IFormDataParam) {
				response = postFormData(url, (IFormDataParam<?>) param);
			} else {
				response = postBaseData(url, param);
			}

			logger.info("-----爱员工--execute------response:" + com.alibaba.fastjson.JSONObject.toJSONString(response));
			
			String responseString = null;
			if(response.body() != null){
				responseString = response.body().string();
			}
			
			if(logger.isDebugEnabled()){
				StringBuilder logMsg = new StringBuilder("AYG_SDK_RESPONSE -> ");
				if(response != null){
					logMsg.append("code:").append(response.code());
					logMsg.append(",body:").append(responseString);
					if(StringUtils.isNotBlank(response.message()))logMsg.append("message:").append(response.message());
				}
				logger.debug(logMsg.toString());
			}
			
			logger.info("-----爱员工--execute------responseString:" + responseString);

			if (response.isSuccessful()) {
				JSONObject jsonObject = JSON.parseObject(responseString);
				OpenApiBaseResponse<RES> result = new OpenApiBaseResponse<>();
				result.setCode(jsonObject.getString("code"));
				result.setMsg(jsonObject.getString("msg"));
				
				String dataJson = jsonObject.getString("data");
				if(StringUtils.isNotBlank(dataJson)){
					RES data;
					//特殊处理
					if(StringUtils.isNotBlank(param.methodName()) && param.methodName().equals("ayg.salary.queryByDay")){
						data = (RES) JSON.parseArray(dataJson, param.respDataClass());
					}else{
						data = (RES) JSON.parseObject(dataJson, param.respDataClass());
					}
					result.setData(data);
				}

				return result;
			}
			throw new AygOpenApiException(String.format("AYG_SDK错误码:%s,错误信息:%s", response.code(),response.message()));
		} catch (Exception e) {
			if(e instanceof AygOpenApiException)throw (AygOpenApiException)e;
			logger.error("AYG_SDK_ERROR",e);
			throw new AygOpenApiException("调用API发生内部错误:"+e.getMessage(), e);
		}finally {
			if(response != null)response.close();
		}
		
	}

	@SuppressWarnings("rawtypes")
	private Response postBaseData(String url, IBaseParam<?> param) throws IOException {
		OpenApiBaseParam apiBaseParam = new OpenApiBaseParam();
		apiBaseParam.setAppId(appId);
		apiBaseParam.setTimestamp(String.valueOf(System.currentTimeMillis()));
		
		//特殊处理，部分接口requestId是有业务含义
		try {
			Field requestIdField = param.getClass().getDeclaredField("requestId");
			requestIdField.setAccessible(true);
			Object object = requestIdField.get(param);
			if(object != null) {
				apiBaseParam.setNonce(object.toString());
			}
		} catch (Exception e) {
			
		}
		
		if(StringUtils.isBlank(apiBaseParam.getNonce())){					
			apiBaseParam.setNonce(UUID.randomUUID().toString().replaceAll("-", ""));
		}
		
		//发放API特殊处理
		if(StringUtils.isNotBlank(param.methodName())){
			apiBaseParam.setMethod(param.methodName());
			apiBaseParam.setVersion(param.version());
			apiBaseParam.setTimestamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		}
		
		if(!BLANK_JSON_STRING.equals(JSON.toJSONString(param))){					
			apiBaseParam.setData(param);
		}
		
		// 生成签名
		String content = "";
		if (param instanceof IBase64Param) {
			content = ((IBase64Param) param).format();
		} else {
			content = ParameterUtils.objectToSignContent(apiBaseParam);
		}
		String signature = RsaUtils.signature(privateKey, content);
		apiBaseParam.setSign(signature);
//		System.out.print("-----爱员工--postBaseData------apiBaseParam:" + com.alibaba.fastjson.JSONObject.toJSONString(apiBaseParam));
		logger.info("-----爱员工--postBaseData------apiBaseParam:" + com.alibaba.fastjson.JSONObject.toJSONString(apiBaseParam));

		Response response = postJson(url, apiBaseParam);
		return response;
	}
	
	private Response postJson(String url, OpenApiBaseParam params) throws IOException {
		if(logger.isDebugEnabled()){
			logger.debug("AYG_SDK_URL -> url:" + url);
		}
		String jsonString = JSON.toJSONString(params);
		if(logger.isDebugEnabled()){
			logger.debug("AYG_SDK_REQUEST -> postData:" + jsonString);
		}
		RequestBody body = FormBody.create(JSON_MEDIA_TYPE, jsonString);
		Request request = new Request.Builder().url(url).post(body).build();
		Response response = httpClient.newCall(request).execute();
        return response;
	}

	private Response postFormData(String url, IFormDataParam<?> params) throws IOException {	
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

		Map<String, Object> signFieldMap = new HashMap<>();
		
		Map<String, String> queryParams = params.queryParams();
//		System.out.print("-----爱员工--postFormData------queryParams:" + queryParams.toString());
		logger.info("-----爱员工--postFormData------queryParams:" + queryParams.toString());

		if (queryParams != null) {
			queryParams.put("appId", appId);
			queryParams.put("nonce", UUID.randomUUID().toString().replaceAll("-", ""));
			signFieldMap.putAll(queryParams);
			for (String key : queryParams.keySet()) {
				urlBuilder.addQueryParameter(key, queryParams.get(key));
			}
		}

		okhttp3.Request.Builder builder = new Request.Builder();
		List<FormDataItem> formData = params.formData();
		if (formData != null && !formData.isEmpty()) {
			MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
			multipartBodyBuilder.setType(MultipartBody.FORM);
			for (FormDataItem item : formData) {
				if (item.getFile() != null) {
					multipartBodyBuilder.addFormDataPart(item.getName(), item.getFile().getName(),
							RequestBody.create(MediaType.parse(item.getMimeType()), item.getFile()));
					//
					signFieldMap.put(item.getName(), DigestUtils.md5Hex(FileUtils.readFileToByteArray(item.getFile())));
				} else {
					//TODO 暂无这类请求类型
					//signFieldMap.put(item.getName(), item.getValue());
				}
			}
			
			String signContent = ParameterUtils.mapToSignContent(signFieldMap);
			String signature = RsaUtils.signature(privateKey,signContent);
			urlBuilder.addQueryParameter("sign", signature);
			
			HttpUrl httpUrl = urlBuilder.build();
			builder.url(httpUrl);
			
			if(logger.isDebugEnabled()){
				logger.debug("AYG_SDK_REQUEST -> url:" + httpUrl.toString());
			}
			
			RequestBody requestBody = multipartBodyBuilder.build();
			builder.post(requestBody);
		} else {
			builder.url(urlBuilder.build());
		}
		Response response = httpClient.newCall(builder.build()).execute();
		return response;
	}
	
	public byte [] downloadContract(ContractTemplateDownloadParam param) throws IOException {
		
		Request request = new Request.Builder().url(baseUrl + param.requestURI()).build();
		Response response = httpClient.newCall(request).execute();
		byte [] results = response.body().bytes();
		if (StringUtils.isNotBlank(param.getLocalPath())) {
			File file = new File(param.getLocalPath() + "service_contract.pdf");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(results);
			fos.flush();
			fos.close();
		}
		return results;
	}

	public static class Builder {
		private String appId;
		private String baseUrl = "https://openapi.aiyuangong.com";
		private PrivateKey privateKey;
		private int readTimeout = 30;

		public Builder appId(String appId) {
			this.appId = appId;
			return this;
		}

		public Builder privateKey(String privateKey) {
			this.privateKey = RsaUtils.loadPrivateKey(privateKey);
			return this;
		}

		public Builder useSandbox() {
			this.baseUrl = "https://openapitest.aiyuangong.com";
			return this;
		}
		
		public Builder readTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public Builder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		public OpenApiClient build() {
			return new OpenApiClient(appId, privateKey, baseUrl,readTimeout);
		}
	}
}
