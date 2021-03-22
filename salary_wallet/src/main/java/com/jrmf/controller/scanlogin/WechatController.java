package com.jrmf.controller.scanlogin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.WechatInfo;
import com.jrmf.controller.websocket.WebSocketController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.WechatConfig;
import com.jrmf.persistence.ChannelDao;
import com.jrmf.persistence.UserRelatedDao;
import com.jrmf.persistence.WechatConfigDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/wechat")
public class WechatController extends BaseController {

	private static Logger logger = LoggerFactory
			.getLogger(WechatController.class);
	private static final String snsAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
	private static final String cgiAccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
	private static final String cgiQrcodeCreateUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
	private static final String cgiShowQrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode";
//	private static final String authorizeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static ConcurrentMap<Long, String>  sencenIdMap = new ConcurrentHashMap<Long, String>();


	@Autowired
	ChannelDao channelDao;
	@Autowired
	UserRelatedDao userRelatedDao;
	@Autowired
	private WechatConfigDao wechatConfigDao;
	@Autowired
	private WechatInfo wechatInfo;
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private WebSocketController webSocketController;
	@Autowired
	private UtilCacheManager utilCacheManager;

	/**
	 * 验证服务器token
	 * 
	 * @param notifyData
	 * @throws ServletException
	 * @throws IOException
	 */
	@GetMapping(value = "/wx")
	public void wxCheckToken(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		logger.info("-------------验证token---------------");
		String signature = request.getParameter("signature");// / 微信加密签名
		String timestamp = request.getParameter("timestamp");// / 时间戳
		String nonce = request.getParameter("nonce"); // / 随机数
		String echostr = request.getParameter("echostr"); // 随机字符串
		PrintWriter out = response.getWriter();

		if (WeixinUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}

		out.close();
		out = null;
	}

	/**
	 * 获取消息推送
	 * 
	 * @param notifyData
	 * @throws ServletException
	 * @throws IOException
	 */
	@PostMapping(value = "/wx")
	public void getMessagePushEvent(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.info("-------------微信事件通知-----------开始-----");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
		Map<String, String> map = XMLUtils.xmlToMap(request);
		logger.info("-------------微信事件通知-----------结束-----" + map.toString());
	
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 60);
		
		String msgType = map.get("MsgType");
		if (msgType.equals(MessageType.REQ_MESSAGE_TYPE_EVENT)) {// 事件推送
			
			boolean loginFlag = false;
			ChannelCustom custom = null;
			String eventType = map.get("Event");// 事件类型
			String toUserName = map.get("ToUserName");
			String fromUserName = map.get("FromUserName");
			String eventKey = map.get("EventKey");
			JSONObject messageJson = new JSONObject();
			Map<String,Object> messageMap = new HashMap<String,Object>();
			
			if (eventType.equals(MessageType.EVENT_TYPE_SUBSCRIBE)) {// 关注
				
				custom = customService.getCustomByOfficialAccOpenId(fromUserName);
				if(custom != null){//已关联账户
					custom.setSubscribeStatus("1");
				    customService.updateCustomById(custom);
				    
				    setLoginSession(custom, session);  //构造session
				    loginFlag = true;
				}
				
				Long eventKeyLong = Long.valueOf(eventKey.substring(eventKey.indexOf("_") + 1));
				String sencenIdStr = sencenIdMap.get(eventKeyLong);
				messageJson.put("loginFlag", loginFlag);
				messageJson.put("equipmentType", sencenIdStr);
				
				webSocketController.onMessage(messageJson.toJSONString(), null);
		        
				if(loginFlag){
					messageMap.put("FromUserName", toUserName);
					messageMap.put("ToUserName", fromUserName);
					messageMap.put("MsgType", MessageType.REQ_MESSAGE_TYPE_TEXT);
					messageMap.put("CreateTime", new Date().getTime());
					messageMap.put("Content", "扫码关注并登陆成功，如非本人操作，请即时联系客服人员 ********");
					String textMessage = XMLUtils.map2xml(messageMap);
					out.write(textMessage);
				}

			}
			// 取消关注
			else if (eventType.equals(MessageType.EVENT_TYPE_UNSUBSCRIBE)) {
				
				custom = customService.getCustomByOfficialAccOpenId(fromUserName);
				if(custom != null){//已关联账户
					custom.setSubscribeStatus("0");
				    customService.updateCustomById(custom);
				}

			}
			// 扫描带参数二维码
			else if (eventType.equals(MessageType.EVENT_TYPE_SCAN)) {

				custom = customService.getCustomByOfficialAccOpenId(fromUserName);
				if(custom != null){//已做账户关联
					setLoginSession(custom, session);
					loginFlag = true;
				}

				String sencenIdStr = sencenIdMap.get(Long.valueOf(eventKey));
				messageJson.put("loginFlag", loginFlag);
				messageJson.put("equipmentType", sencenIdStr);
				
				webSocketController.onMessage(messageJson.toJSONString(), null);

				if(loginFlag){
					messageMap.put("FromUserName", toUserName);
					messageMap.put("ToUserName", fromUserName);
					messageMap.put("MsgType", MessageType.REQ_MESSAGE_TYPE_TEXT);
					messageMap.put("CreateTime", new Date().getTime());
					messageMap.put("Content", "扫码登陆成功，如非本人操作，请即时联系客服人员 ********");
					String textMessage = XMLUtils.map2xml(messageMap);
					out.write(textMessage);
				}
			}
		}
	}

    /**
     * 通过
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/loginbyopenId")
    public @ResponseBody Map<String, Object> validMobileCode(HttpServletRequest request, HttpServletResponse resp)
            throws Exception {
     
    	int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 60);

        String officialAccOpenId = request.getParameter("openId");
        ChannelCustom custom = customService.getCustomByOfficialAccOpenId(officialAccOpenId);
        String customkey;
        if (custom == null
                || (StringUtil.isEmpty(custom.getCustomkey()) && StringUtil.isEmpty(custom.getMasterCustom()))) {
            respstat = RespCode.error302;
        } else {
            if (1 != custom.getEnabled()) {
                return retModel(RespCode.error303, result);
            }
            if (!"1".equals(custom.getSubscribeStatus())) {
                return retModel(RespCode.UNSUBSCRIBE, result);
            }
            
            if (StringUtil.isEmpty(custom.getCustomkey())) {
                customkey = custom.getMasterCustom();
            } else {
                customkey = custom.getCustomkey();
            }

            session.setAttribute("customLogin", custom);
            result.put("mobilePhone", custom.getPhoneNo());
            session.setAttribute("customkey", customkey);
        }

        Map<String, Object> response = new HashMap<String, Object>();

        response.put("isSetTransPassword", StringUtil.isEmpty(custom.getTranPassword()) ? false : true);
        if (custom.getCustomType() == CustomType.ROOT.getCode()) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
            response.put("companyName", masterCustom.getCompanyName() + "-" + custom.getCompanyName());
        } else {
        	response.put("companyName", custom.getCompanyName());
        }
        response.put("isRoot", StringUtil.isEmpty(custom.getMasterCustom()) ? true : false);
        response.put("isReview", custom.getDataReview() == 1 ? true : false);
        response.put("isSuperRoot", "mfkj".equals(custom.getCustomkey()));
        response.put("loginRole",custom.getLoginRole());
        logger.info(response.toString());
        return retModel(respstat, response);
    }
	
	public void setLoginSession(ChannelCustom custom, HttpSession session){
        
        session.setMaxInactiveInterval(5 * 60);
		
        String customkey = custom.getCustomkey();
        if (StringUtil.isEmpty(customkey)) {
            customkey = custom.getMasterCustom();
        } else {
            customkey = custom.getCustomkey();
        }
        session.setAttribute("customLogin", custom);
        session.setAttribute("customkey", customkey);
	}

	@RequestMapping("getqrcode")
	private @ResponseBody Map<String, Object> getWXPublicQRCode(HttpServletRequest req,
			HttpServletResponse resp) {

		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

		String codeType = req.getParameter("codeType");
		if (StringUtil.isEmpty(codeType)) {
			codeType = "0";
		}

		String sceneIdStr = req.getParameter("sceneId");
		Long sceneIdLang = utilCacheManager.increase("sceneId");
		sencenIdMap.put(sceneIdLang, sceneIdStr);

		String domainName = req.getServerName();
		WechatConfig wechatConfig = wechatConfigDao.getWechatConfigByDomainName(domainName);

		String access_token = getToken(wechatConfig.getOfficialAccAppId(), wechatConfig.getOfficialAccAppSeckey());

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> sceneMap = new HashMap<>();
		Map<String, Object> sceneIdMap = new HashMap<>();
		if ("0".equals(codeType)) {// 临时二维码
			map.put("expire_seconds", 3600);
			map.put("action_name", "QR_SCENE");
		} else if ("1".equals(codeType)) {// 永久二维码
			map.put("action_name", "QR_LIMIT_SCENE");

		}
		sceneIdMap.put("scene_id", sceneIdLang);
		sceneMap.put("scene", sceneIdMap);
		map.put("action_info", sceneMap);
		logger.info("------map:" + map);
		String json = JSONObject.toJSONString(map);
		String ticket = getTicket(access_token, json.toString());

		result.put("qrCodeUrl", cgiShowQrcodeUrl + "?ticket=" + URLEncoder.encode(ticket));
		logger.info("------ticket:" + ticket);
		
		return result;
	}

	//公众号-账号-绑定
	@RequestMapping(value = "/scanbind")
	public void bindredirect(HttpServletRequest request, HttpServletResponse response){
		
		String userName = request.getParameter("userName");
		
		String weBindUrl = "";
        try {
    		String backUrl = wechatInfo.getBaseUrl() + "/wechat/bindNotify.do?userName=" + userName;
    		String domainName = request.getServerName();
			WechatConfig wechatConfig = wechatConfigDao.getWechatConfigByDomainName(domainName);
    		
    		weBindUrl = wechatInfo.getAuthorizeUrl() + "?appid=" + wechatConfig.getOfficialAccAppId()
    				+ "&redirect_uri=" + URLEncoder.encode(backUrl,"UTF-8")
    				+ "&response_type=code"
    				+ "&scope=snsapi_base" //snsapi_userinfo   snsapi_base
    				+ "&state=STATE#wechat_redirect";
			
            response.sendRedirect(weBindUrl);
	
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        logger.info("-------------------bindredirect:" + weBindUrl);

	}

	@RequestMapping("bindNotify")
	public void bind(HttpServletRequest request, HttpServletResponse response) {
		try {
			String code = request.getParameter("code");
			String userName = request.getParameter("userName");
			logger.info("微信登陆---code：" + code);
			String domainName = request.getServerName();
			WechatConfig wechatConfig = wechatConfigDao.getWechatConfigByDomainName(domainName);
			Map<String, Object> result = obtainOpenId(code, wechatConfig.getOfficialAccAppId(), wechatConfig.getOfficialAccAppSeckey());
			if (result != null) {
				String openid = String.valueOf(result.get("openid"));
				logger.info("---微信登陆---获取openid=" + openid + "--userName:" + userName);
				//根据账户名和openId查更用户信息
				ChannelCustom custom = customService.customUsername(userName);
				custom.setOfficialAccOpenId(openid);
			    customService.updateCustomById(custom);

			    response.sendRedirect("/wechat/wechatSuccess.do");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	@RequestMapping("wechatIndex")
	public String wechatIndex(HttpServletRequest req, HttpServletResponse resp) {
		return "wechatIndex";
	}

	@RequestMapping("wechatlogin")
	public String wechatLogin(HttpServletRequest req, HttpServletResponse resp) {
		return "wechatLogin";
	}

	@RequestMapping("wechatSuccess")
	public String wechatSuccess(HttpServletRequest req, HttpServletResponse resp) {
		return "wechatSuccess";
	}

	@RequestMapping(value = "/testwebscoket")
	public void testwebscoket(HttpServletRequest request, HttpServletResponse response){
		
		JSONObject messageJson = new JSONObject();
		messageJson.put("loginFlag", "true");
		messageJson.put("equipmentType", "1001");
		webSocketController.onMessage(messageJson.toJSONString(), null);
	}
	
	private Map<String, Object> obtainOpenId(String code, String appId,
			String appSeckey) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String url = snsAccessTokenUrl + "?appid=" + appId + "&secret="
					+ appSeckey + "&code=" + code
					+ "&grant_type=authorization_code";
			JSONObject jsonObject = doGetJson(url);
			logger.info("------obtainOpenId:" + jsonObject);

			String openid = jsonObject.getString("openid");
			String token = jsonObject.getString("access_token");

			result.put("openid", openid);
			result.put("access_token", token);

			return result;
		} catch (Exception e) {
			logger.error("获取微信授权异常");
			logger.error(e.getMessage());
		}
		return result;
	}

	// 接口获取token
	public static String getToken(String appId, String appSeckey) {
		String tokenUrl = cgiAccessTokenUrl
				+ "?grant_type=client_credential&appid=" + appId + "&secret="
				+ appSeckey;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = doGetJson(tokenUrl);
			logger.info("-----getToken:" + jsonObject.toJSONString());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		String token =(String)jsonObject.get("access_token");
		return token;
	}

	public String getTicket(String access_token, String data) {
		String ticketUrl = cgiQrcodeCreateUrl + "?access_token=" + access_token;
		String ticketStr = WeixinUtil.httpRequestString(ticketUrl, "POST", data);
		logger.info("-----ticketStr:" + ticketStr);
		JSONObject ticketData = JSONObject.parseObject(ticketStr);
		logger.info("-----getTicket:" + ticketData.toString());
		String ticket = (String) ticketData.get("ticket");
		return ticket;
	}

	// public static String getUserInfo(String opendID, String token){
	// String GET_USERINFO_URL =
	// "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
	//
	// String url = GET_USERINFO_URL.replace("ACCESS_TOKEN" , token);
	// url = url.replace("OPENID" ,opendID);
	// JSONObject jsonObject = new JSONObject();
	// try {
	// jsonObject = doGetJson(url);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return jsonObject.toString();
	// }

	private static JSONObject doGetJson(String url) throws IOException {
		JSONObject jsonObject = null;
		@SuppressWarnings({ "resource" })
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

//	private static JSONObject doPostJson(String url) throws IOException {
//		JSONObject jsonObject = null;
//		@SuppressWarnings({ "resource" })
//		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
//		HttpGet httpGet = new HttpGet(url);
//		HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
//		HttpEntity httpEntity = httpResponse.getEntity();
//		if (httpEntity != null) {
//			String result = EntityUtils.toString(httpEntity, "UTF-8");
//			jsonObject = JSON.parseObject(result);
//		}
//		httpGet.releaseConnection();
//		return jsonObject;
//	}

	public static void main(String[] args) throws UnsupportedEncodingException {

//		String password = CipherUtil.generatePassword("123456",
//				"212677l7302T41B533Pk");
//		System.out.println(password);

		// logger.info(getToken("wx813aa3a01aa34ffe",
		// "bde01b344f934f42d33ec6b9277a7668"));
		logger.info(URLEncoder
				.encode("http://ms-wallet.jrmf360.com/wechat/wechatloginNotify.do?configId=1",
						"UTF-8"));
		// logger.info(URLDecoder.decode("\\xe6", ""));
//		WechatController ss = new WechatController();
	}
	
	@RequestMapping("wechatInfo")
	public @ResponseBody Map<String, Object> wechatInfo(HttpServletRequest req,
			HttpServletResponse resp) {

		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

		String domainName = req.getServerName();
		WechatConfig wechatConfig = wechatConfigDao
				.getWechatConfigByDomainName(domainName);

		String appId = wechatConfig.getAppId();
		String notifyUrl = req.getScheme() + "://" + req.getServerName()
				+ "/wechat/wechatloginNotify.do?configId="
				+ wechatConfig.getId();

		result.put("wechatAppid", appId);
		result.put("wechatNotifyUrl", notifyUrl);

		return result;
	}
	
	@RequestMapping("wechatloginNotify")
	public void wechatloginNotify(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String code = request.getParameter("code");
			String configId = request.getParameter("configId");
			logger.info("------微信登陆-----wechatloginNotify----code:" + code
					+ "---configId:" + configId);
			int id = Integer.valueOf(configId);
			WechatConfig wechatConfig = wechatConfigDao.getWechatConfigById(id);
			Map<String, Object> result = obtainOpenId(code,
					wechatConfig.getAppId(), wechatConfig.getAppSeckey());
			if (result != null) {
				String openid = String.valueOf(result.get("openid"));
				logger.info("------微信登陆-wechatloginNotify--获取openid=" + openid);

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("openId", openid);
				List<ChannelCustom> channelCustomList = customService
						.getCustomList(paramMap);

				if (channelCustomList != null && channelCustomList.size() > 0) {// 已关注公众号
					HttpSession session = request.getSession();
					ChannelCustom custom = customService
							.customUsername(channelCustomList.get(0)
									.getUsername());
					session.setAttribute("customLogin", custom);
				}
				response.sendRedirect("/wechat/wechatIndex.do");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
