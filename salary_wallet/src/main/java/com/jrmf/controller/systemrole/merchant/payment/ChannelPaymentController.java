package com.jrmf.controller.systemrole.merchant.payment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.WechatInfo;
import com.jrmf.domain.*;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.request.weixin.WeixinOpenIdBindCheckParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.openapi.model.response.weixin.WeixinOpenIdBindCheckResult;
import com.jrmf.payment.util.ClientMapUtil;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.persistence.*;
import com.jrmf.service.*;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.alipay.ALiPayConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/channel/payment")
public class ChannelPaymentController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(ChannelPaymentController.class);
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private ChannelHistoryService historyService;
	@Autowired
	private ChannelInterimBatchService2 channelInterimBatchService2;

	@Autowired
	private CustomPermissionService customPermissionService;
	@Autowired
	private ChannelRelatedService channelRelatedService;
//	@Autowired
//	private ConfirmGrantService confirmGrantService;
	@Autowired
	ChannelDao channelDao;
	@Autowired
	UserRelatedDao userRelatedDao;
	@Autowired
	private UserCommission2Dao userCommissionDao2;
	@Autowired
	private WechatInfo wechatInfo;
	@Autowired
	private WxBindInfoDao wxBindInfoDao;
	@Autowired
	private OrderNoUtil orderNoUtil;
	@Autowired
	private CompanyService companyService;

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/batch/taskattachment/import", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> inputData(HttpServletRequest request,
			HttpServletResponse response, @RequestParam(value="file", required=false) MultipartFile file) throws Exception {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);

		String name = request.getParameter("name");
		String operate = request.getParameter("operate");
		// ??????????????????
//		String name = "wsheng";
//		String operate = "GOM";
		String taskattachmentName = UUID.randomUUID().toString().replace("-", "");
		String taskattachmentAbsoutePath = "/taskattachment/";
		String uploadFileName;
		logger.info("???????????????????????????" + taskattachmentAbsoutePath);
		String fileName = file.getOriginalFilename();
		String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
        	if("GOM".equals(operate)){
        		String batchId = request.getParameter("batchId");
//        		String batchId = "14627";
        		ChannelHistory history = historyService.getChannelHistoryById(batchId);
        		history.setTaskAttachmentFile(fileName.substring(0, fileName.indexOf(".")) + taskattachmentName + "." +  fileType);
        		historyService.updateChannelHistory(history);
        	}

			byte[] bytes = file.getBytes();
			InputStream backFileInputStream = new ByteArrayInputStream(bytes);
					 uploadFileName =
							fileName.substring(0, fileName.indexOf(".")) + taskattachmentName + "." + fileType;
					boolean backFile = FtpTool
							.uploadFile(taskattachmentAbsoutePath, uploadFileName, backFileInputStream);

			if (!backFile) {
				model.put(RespCode.RESP_STAT, RespCode.error101);
				model.put(RespCode.RESP_MSG, "????????????");
				return model;
			}

        } catch (Exception e) {
			logger.error(e.getMessage(),e);
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		model.put("taskAttachmentFile", taskattachmentAbsoutePath + uploadFileName);
	    logger.info("???????????????" + model);
        return model;
	}

    /**
     * ????????????????????????
     * @throws IOException
     **/
    @RequestMapping(value = "/batch/taskattachment/export")
	public ResponseEntity<byte[]> downloadWhiteListTmp(HttpServletRequest request) throws IOException {

		String batchId = request.getParameter("batchId");
		ChannelHistory channelHistory = historyService.getChannelHistoryById(batchId);
		String filePath = "/taskattachment/" + channelHistory.getTaskAttachmentFile();
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

		byte[] bytes = FtpTool.downloadFtpFile(filePath.substring(0, filePath.lastIndexOf("/")), fileName);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);
		return new ResponseEntity<byte[]>(bytes,
				headers, HttpStatus.OK);

//		HttpHeaders headers = new HttpHeaders();
//
//		String batchId = request.getParameter("batchId");
//		ChannelHistory channelHistory = historyService.getChannelHistoryById(batchId);
//
//		String filePath = channelHistory.getTaskAttachmentFile();
//		ServletContext servletContext = request.getServletContext();
//		String realPath = servletContext.getRealPath(filePath);
//		File file = new File(realPath);
//		InputStream in;
//		byte[] body = null;
//		ResponseEntity<byte[]> response = null;
//		try {
//			in = new FileInputStream(file);
//			body = new byte[in.available()];// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//			in.read(body);// ????????????????????????
//			String fileName = filePath.substring(filePath.lastIndexOf("_"));
//			fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
//			headers.add("Content-Disposition", "attachment;filename=" + fileName);
//			HttpStatus statusCode = HttpStatus.OK;// ???????????????
//			response = new ResponseEntity<byte[]>(body, headers, statusCode);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		return response;
	}

	//?????????????????????
	@RequestMapping(value = "/aygrecepitrequest")
	public void aygRecepitBatchJob(HttpServletRequest request) throws Exception {

		String companyId = request.getParameter("companyId");
		String originalId = request.getParameter("originalId");
		String tradeStartTime = request.getParameter("tradeStartTime");
		String tradeEndTime = request.getParameter("tradeEndTime");

        Map<String, Object> params = new HashMap<>(20);
        params.put("companyId", companyId);
        params.put("originalId", originalId);
        params.put("tradeStartTime", tradeStartTime);
        params.put("tradeEndTime", tradeEndTime);
        List<UserCommission> comissionList = userCommissionDao2.getSuccessUserCommission(params);

		String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";
        ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId, companyId);
		String appId = related.getAppIdAyg();
		OpenApiClient client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).build();

		String orderNo = "";
        for(UserCommission comsis : comissionList){
        	orderNo = comsis.getOrderNo();
        	logger.info("?????????????????????allCommission???????????? orderNo:" + orderNo);
        	ReceiptRequestParam param = new ReceiptRequestParam();
    		String reqNo = orderNoUtil.getChannelSerialno();
    		param.setReqNo(reqNo);
    		param.setNotifyUrl("http://ms-wallet.jrmf360.com/channel/payment/receiptAsyncNotify.do");
    		param.setOutOrderNo(orderNo);
    		param.setAttach("??????");

    		OpenApiBaseResponse<BaseResponseResult<String>> response = client.execute(param);

    		String aygCode = response.getCode();
    		String msg = response.getMsg();
    		if(response != null && OpenApiBaseResponse.SUCCESS_CODE.equals(aygCode)){
    			logger.info("?????????????????????allCommission_success????????????orderNo:" + orderNo);
    		}else if("2002".equals(aygCode) || "2101".equals(aygCode)){
    			logger.info("?????????????????????allCommission_fail????????????orderNo???"+orderNo);
    		}else{
    			logger.info("?????????????????????allCommission_exception????????????orderNo???"+orderNo + ",aygCode:" + aygCode + ",msg:" + msg);
    		}

        }
     }


    public static void getFile(byte[] bfile, String filePath,String fileName) throws Exception {

		InputStream backFileInputStream = new ByteArrayInputStream(bfile);
		boolean backFile = FtpTool.uploadFile(filePath, fileName, backFileInputStream);

		if (!backFile) {
			logger.error("- - - - - -- - - - - -????????????getFile???????????????- - - - - -- - - - - -");
		}


//		BufferedOutputStream bos = null;
//        FileOutputStream fos = null;
//        File file = null;
//        try {
//            File dir = new File(filePath);
//            if(!dir.exists()&&dir.isDirectory()){//??????????????????????????????
//                dir.mkdirs();
//            }
//            file = new File(filePath + File.separator + fileName);
//            fos = new FileOutputStream(file);
//            bos = new BufferedOutputStream(fos);
//            bos.write(bfile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
    }


	public static void WriteAdd(String file, String conent) throws Exception {

//		BufferedWriter out = null;
//
//		try {
//
//			File files = new File(file);
//			boolean existsFlag = files.exists();
//			if(!existsFlag){
//			        files.createNewFile();
//			}
//			out = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(file, true)));
//			out.write(conent + "\r\n");
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if(out != null){
//					out.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 *
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/paymentList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> paymentList(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
//		String originalId = (String) request.getSession().getAttribute("customkey");
		String originalId =  request.getParameter("customkey");
		String companyId =  request.getParameter("companyId");

//		originalId = "423LCWtLLa348l0sy18U";

		ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("companyId", channelRelated.getCompanyId());
		param.put("originalId", originalId);
		param.put("merchantId", originalId);

		List<CompanyPayment> paymentList = customService.getPaymentList(param, false);
		if(paymentList.size() == 0){
			paymentList = customService.getPaymentList(param, true);
		}
		result.put("paymentList", paymentList);

		return result;
	}


	/**
	 * ????????????????????????????????????
	 *
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/commonOption", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> commonOption(HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

		String originalId =  request.getParameter("customkey");
		String companyId =  request.getParameter("companyId");
		String paymentType = request.getParameter("paymentType");

		if(StringUtil.isEmpty(companyId)){
			return returnFail(RespCode.error101,RespCode.codeMaps.get(RespCode.error101));
		}
		ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);
		if(channelRelated == null){
			return returnFail(RespCode.error101,"?????????????????????????????????????????????");
		}

		PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(paymentType, originalId,companyId);
		paymentConfig.setAppIdAyg(channelRelated.getAppIdAyg());
		result.put("paymentConfig", paymentConfig.toString());

//		String orderNo = request.getParameter("orderNo");
//		Payment payment = PaymentFactory.paymentEntity(paymentConfig);
//		UserCommission userCommission = new UserCommission();
//		userCommission.setPayType(Integer.valueOf(paymentType));
//		userCommission.setAccount("6230582000048845591");
//		userCommission.setAmount("0.01");;
//		userCommission.setBankName("????????????");
//		userCommission.setUserName("??????");
//		userCommission.setOrderNo(orderNo);
//		userCommission.setPhone("17701393451");
//		PaymentReturn paymentReturn = payment.paymentTransfer(userCommission);

//		result = confirmGrantService.grantTransfer(originalId,companyId ,batchId, remark, "test");

		return result;
	}


	/**
	 * https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=APPID&scope=SCOPE&redirect_uri=ENCODED_URL
	 * ??????????????????
	 *
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/alipayAuth")
	public @ResponseBody Map<String, Object> alipayAuth(HttpServletRequest request) {

		Map<String, Object> result = new HashMap<>();

		AlipayClient alipayClient = new DefaultAlipayClient(ALiPayConfig.URL,
				ALiPayConfig.APP_ID, ALiPayConfig.APP_PRIVATE_KEY, ALiPayConfig.TYPE,
				ALiPayConfig.CHARSET, ALiPayConfig.ALIPAY_PUBLIC_KEY, ALiPayConfig.SECREAT);
		String auth_code = request.getParameter("auth_code");

		AlipaySystemOauthTokenRequest request1 = new AlipaySystemOauthTokenRequest();
		request1.setCode(auth_code);
		request1.setGrantType("authorization_code");
		try {
			String accessToken = "";

		    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request1);
//		    System.out.println(oauthTokenResponse.getAccessToken());
		    accessToken = oauthTokenResponse.getAccessToken();

			AlipayUserInfoShareRequest request2 = new AlipayUserInfoShareRequest();
			AlipayUserInfoShareResponse response = alipayClient.execute(request2, accessToken);
			if(response.isSuccess()){
//				System.out.println("????????????");
//				System.out.println("--------------userId:" + response.getUserId());
//				System.out.println("------------userName:" + response.getUserName());
				result.put("userId", response.getUserId());
				result.put("userName", response.getUserName());
				result.put("city", response.getCity());
				result.put("nmobile", response.getMobile());
				result.put("certNo", response.getCertNo());
			} else {
//				System.out.println("????????????");
			}
		} catch (AlipayApiException e) {
			logger.error(e.getMessage(),e);
		}

		return result;
	}

	@RequestMapping(value = "/wxbindurl")
	public @ResponseBody Map<String, Object> getWxBindUrl(HttpServletRequest request){

		Map<String, Object> result = new HashMap<>();

		String plat_form = request.getParameter("plat_form");
		String clientUrl = request.getParameter("clientUrl");
		String partner_id = request.getParameter("partner_id");
		String merchant_id = request.getParameter("merchant_id");
		String clientOpenId = request.getParameter("clientOpenId");
		String transfer_corp_id = request.getParameter("transfer_corp_id");

		String bindStatus = "0";
		String wechatBindUrl = "";
        try {

            Map<String, Object> params = new HashMap<>();
            params.put("clientOpenId", clientOpenId);
            params.put("originId", merchant_id);
            params.put("companyId", transfer_corp_id);
            params.put("platForm", plat_form);
            WxBindInfo wxBindInfo = wxBindInfoDao.geWxBindInfo(params);
        	if(wxBindInfo != null){
        		String openId = wxBindInfo.getOpenId();
        		String aygStatus = wxBindInfo.getAygStatus();
        		if(!"1".equals(aygStatus)){
    				ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(merchant_id, transfer_corp_id);
    				String aygAppId = related.getAppIdAyg();

    				OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);
    				if(client == null){
    					synchronized(ClientMapUtil.httpClient){
    						client = ClientMapUtil.httpClient.get(aygAppId);
    						if(client == null){
    							String privateKey = wechatInfo.getAygPrivateKey();
    							client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
    							ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
    						}
    					}
    				}

    				WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
    				param.setOpenId(openId);
    				param.setPlatform(plat_form);
    				param.setRedirctUri(clientUrl);

    				OpenApiBaseResponse<WeixinOpenIdBindCheckResult> bindResponse = client.execute(param);
    				logger.info("---????????????----??????--------??????????????????????????????" + bindResponse);
    				if(PayRespCode.RESP_SUCCESS.equals(bindResponse.getCode())){
    					String aygBindStatus = bindResponse.getData().getBindStatus();
    					if("1".equals(aygBindStatus)){
    						wxBindInfo.setAygStatus("1");
    						wxBindInfoDao.updateWxBindInfo(wxBindInfo);

    		        		bindStatus = "1";
    		        		wechatBindUrl = clientUrl;
    					}else{
    						bindStatus = "0";
    		        		wechatBindUrl = wechatInfo.getBaseUrl() + wechatInfo.getBasePath() + "/bindredirect.do?clientOpenId="+clientOpenId
    		        				+"&merchant_id=" + merchant_id
    		        				+"&transfer_corp_id=" + transfer_corp_id
    		        				+"&partner_id=" + partner_id
    		        				+"&plat_form=" + plat_form
    		        				+"&clientUrl=" + URLEncoder.encode(clientUrl,"UTF-8");
    					}
    				}

        		}else{
	        		bindStatus = "1";
	        		wechatBindUrl = clientUrl;
        		}
        	}else{//???--???????????????url
        		wechatBindUrl = wechatInfo.getBaseUrl() + wechatInfo.getBasePath() + "/bindredirect.do?clientOpenId="+clientOpenId
        				+"&merchant_id=" + merchant_id
        				+"&transfer_corp_id=" + transfer_corp_id
        				+"&partner_id=" + partner_id
        				+"&plat_form=" + plat_form
        				+"&clientUrl=" + URLEncoder.encode(clientUrl,"UTF-8");
        	}

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

        logger.info("-------------------wechatBindUrl:" + wechatBindUrl);
        result.put("bindStatus", bindStatus);
        result.put("wechatBindUrl", wechatBindUrl);
        return result;
	}


	@RequestMapping(value = "/bindredirect")
	public void bindredirect(HttpServletRequest request, HttpServletResponse response){

		String plat_form = request.getParameter("plat_form");
		String clientUrl = request.getParameter("clientUrl");
		String partner_id = request.getParameter("partner_id");
		String merchant_id = request.getParameter("merchant_id");
		String clientOpenId = request.getParameter("clientOpenId");
		String transfer_corp_id = request.getParameter("transfer_corp_id");

		String weBindUrl = "";
        try {
    		String backUrl = wechatInfo.getBaseUrl() + wechatInfo.getBasePath() + "/bind.do?clientOpenId="+clientOpenId
    				+"&merchant_id=" + merchant_id
    				+"&transfer_corp_id=" + transfer_corp_id
    				+"&partner_id=" + partner_id
    				+"&plat_form=" + plat_form
    				+"&clientUrl=" + URLEncoder.encode(clientUrl,"UTF-8");

    		weBindUrl = wechatInfo.getAuthorizeUrl() + "?appid=" + wechatInfo.getZhishuitongAppid()
    				+ "&redirect_uri=" + URLEncoder.encode(backUrl,"UTF-8")
    				+ "&response_type=code"
    				+ "&scope=snsapi_userinfo" //snsapi_userinfo   snsapi_base
    				+ "&state=STATE#wechat_redirect";

            response.sendRedirect(weBindUrl);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

        logger.info("-------------------bindredirect:" + weBindUrl);

	}

	@RequestMapping("bind")
	public void bind(HttpServletRequest request, HttpServletResponse response) {
		try {
			String code = request.getParameter("code");
			logger.info("????????????---code???" + code);
			Map<String, Object> result = obtainOpenId(code);
			if (result != null) {
				String openid = String.valueOf(result.get("openid"));
				logger.info("---????????????---??????openid=" +openid);

				String clientUrl = request.getParameter("clientUrl");
				String plat_form = request.getParameter("plat_form");
				String partner_id = request.getParameter("partner_id");
				String merchant_id = request.getParameter("merchant_id");
				String clientOpenId = request.getParameter("clientOpenId");
				String transfer_corp_id = request.getParameter("transfer_corp_id");


	            Map<String, Object> params = new HashMap<>();
	            params.put("clientOpenId", clientOpenId);
	            params.put("originId", merchant_id);
	            params.put("companyId", transfer_corp_id);
	            params.put("platForm", plat_form);
	            WxBindInfo wxBindInfo = wxBindInfoDao.geWxBindInfo(params);
	        	if(wxBindInfo == null){//?????????????????????
					WxBindInfo wxBindInfoNew = new  WxBindInfo(openid,
							clientOpenId,
							"0",
							plat_form,
							merchant_id,
							transfer_corp_id,
							partner_id);
					wxBindInfoDao.saveWxBindInfo(wxBindInfoNew);
	        	}

				ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(merchant_id, transfer_corp_id);
				String aygAppId = related.getAppIdAyg();

				OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);
				if(client == null){
					synchronized(ClientMapUtil.httpClient){
						client = ClientMapUtil.httpClient.get(aygAppId);
						if(client == null){
							String privateKey = wechatInfo.getAygPrivateKey();
							client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
							ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
						}
					}
				}

				WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
				param.setOpenId(openid);
				param.setPlatform(plat_form);
				param.setRedirctUri(clientUrl);//"http://wallet-pre.jrmf360.com/static/img/bind-success.png"

				OpenApiBaseResponse<WeixinOpenIdBindCheckResult> bindResponse = client.execute(param);
				logger.info("---????????????------------??????????????????????????????" + bindResponse);
				String redirectUrl = "";
				if(PayRespCode.RESP_SUCCESS.equals(bindResponse.getCode())){
					redirectUrl = bindResponse.getData().getRedirectUrl();
				}else{
					redirectUrl = clientUrl;
				}

				response.sendRedirect(redirectUrl);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	@RequestMapping("wxbind")
	public void wxbind(HttpServletRequest request, HttpServletResponse response) {
		try {
			String code = request.getParameter("code");
			logger.info("????????????---code???" + code);
			Map<String, Object> result = obtainOpenId(code);
			if (result != null) {
				String openid = String.valueOf(result.get("openid"));
				logger.info("---????????????---??????openid=" +openid);

				String clientUrl = request.getParameter("clientUrl");
				String plat_form = request.getParameter("plat_form");
				String partner_id = request.getParameter("partner_id");
				String merchant_id = request.getParameter("merchant_id");
				String clientOpenId = request.getParameter("clientOpenId");
				String transfer_corp_id = request.getParameter("transfer_corp_id");


	            Map<String, Object> params = new HashMap<>();
	            params.put("clientOpenId", clientOpenId);
	            params.put("originId", merchant_id);
	            params.put("companyId", transfer_corp_id);
	            params.put("platForm", plat_form);
	            WxBindInfo wxBindInfo = wxBindInfoDao.geWxBindInfo(params);
	        	if(wxBindInfo == null){//?????????????????????
					WxBindInfo wxBindInfoNew = new  WxBindInfo(openid,
							clientOpenId,
							"0",
							plat_form,
							merchant_id,
							transfer_corp_id,
							partner_id);
					wxBindInfoDao.saveWxBindInfo(wxBindInfoNew);
	        	}

				ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(merchant_id, transfer_corp_id);
				String aygAppId = related.getAppIdAyg();

				OpenApiClient client = ClientMapUtil.httpClient.get(aygAppId);
				if(client == null){
					synchronized(ClientMapUtil.httpClient){
						client = ClientMapUtil.httpClient.get(aygAppId);
						if(client == null){
							String privateKey = wechatInfo.getAygPrivateKey();
							client = new OpenApiClient.Builder().appId(aygAppId).privateKey(privateKey).build();
							ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
						}
					}
				}

				WeixinOpenIdBindCheckParam param = new WeixinOpenIdBindCheckParam();
				param.setOpenId(openid);
				param.setPlatform(plat_form);
				param.setRedirctUri(clientUrl);//"http://wallet-pre.jrmf360.com/static/img/bind-success.png"

				OpenApiBaseResponse<WeixinOpenIdBindCheckResult> bindResponse = client.execute(param);
				logger.info("---????????????------------??????????????????????????????" + bindResponse);
				String redirectUrl = "";
				if(PayRespCode.RESP_SUCCESS.equals(bindResponse.getCode())){
					redirectUrl = bindResponse.getData().getRedirectUrl();
				}else{
					redirectUrl = clientUrl;
				}

				response.sendRedirect(redirectUrl);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	private  Map<String, Object> obtainOpenId(String code){
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String url = wechatInfo.getSnsAccessTokenUrl()
					+ "?appid=" + wechatInfo.getZhishuitongAppid()
					+ "&secret=" + wechatInfo.getZhishuitongAppSeckey()
					+ "&code=" + code
					+ "&grant_type=authorization_code";
			JSONObject jsonObject = doGetJson(url);
			logger.info("????????????token???????????????access_token:" + jsonObject);

			String openid = jsonObject.getString("openid");
			String token = jsonObject.getString("access_token");

			result.put("openid", openid);
			result.put("access_token", token);

			return result;
		} catch (Exception e) {
			logger.error("????????????????????????");
			logger.error(e.getMessage());
		}
		return result;
	}

	//????????????token
//    public static String getToken(){
//		String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+APP_ID+"&secret="+APP_SECRET;
//        JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject = doGetJson(tokenUrl);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String token = jsonObject.getString("access_token");
//        return token;
//    }

//    public static String getUserInfo(String opendID, String token){
//        String GET_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
//
//        String url = GET_USERINFO_URL.replace("ACCESS_TOKEN" , token);
//        url = url.replace("OPENID" ,opendID);
//        JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject = doGetJson(url);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//        return jsonObject.toString();
//    }

	private static JSONObject doGetJson(String url) throws IOException {
		JSONObject jsonObject = null;
		@SuppressWarnings({ "resource", "deprecation" })
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		if (httpEntity != null) {
			String result = EntityUtils.toString(httpEntity, "UTF-8");
            jsonObject = JSON.parseObject(result);
		}
		httpGet.releaseConnection();
		return jsonObject;
	}


	public static void main(String[] args) throws UnsupportedEncodingException {
		logger.info(URLEncoder.encode("http://ms-wallet.jrmf360.com/channel/payment/wechatloginNotify.do", "UTF-8"));
//		logger.info(URLDecoder.decode("\\xe6", ""));
	}
}
