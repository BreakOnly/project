package com.jrmf.controller.wechat;

import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CustomInfo;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.User;
import com.jrmf.domain.UserRelated;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CustomInfoService;
import com.jrmf.service.OemConfigService;
import com.jrmf.service.ParameterService;
import com.jrmf.service.UserRelatedService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ParamSignTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SMSSendUtils;
import com.jrmf.utils.StringUtil;

/**
 * 微信登陆----商户公众号登陆
 * @author zhangzehui
 *
 */
@SuppressWarnings("deprecation")
@Controller
@RequestMapping("wechat/")
public class WeChatActionController extends BaseController{
	
	private static Logger logger = LoggerFactory.getLogger(WeChatActionController.class);

	/**
	 * 静态配置，后期更换位置
	 */
	private static final String APP_ID = "wx0ae1acfbd8771830";

	private static final String APP_SECRET = "00a297f16e06def1229318a4b250c406";
	
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
	
	final String flag = SMSSendUtils.VALI_MOBILE;
	
	@Autowired
	private UserSerivce userSerivce;
	@Autowired
	private CustomInfoService customInfoService;
	@Autowired
	private UserRelatedService relatedService;
	@Autowired
	private ChannelRelatedService channelRelatedService;
	@Autowired
	private ParameterService parameterService;
    @Autowired
    private OemConfigService oemConfigService;
	
	/**
	 * 薪税钱包------微信登陆（携带商户标识）
	 * @return
	 */
	@RequestMapping("login")
	public void login(HttpServletRequest req, HttpServletResponse resp) {

		try {

			String originalId = req.getParameter("originalId");
			String companyId = req.getParameter("companyId");
		    String code = req.getParameter("code");
			if(StringUtil.isEmpty(code) || StringUtil.isEmpty(originalId)){
				logger.error("参数异常");
				req.setAttribute("state", "101");
		    	req.setAttribute("respmsg", "参数异常");
				resp.sendRedirect("error.do");
				return;
			}
		    logger.info("微信登陆---wechat/login.do方法：originalId="+originalId+",code="+code);
		    ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);
		    if(channelRelated == null){
		    	logger.error("非法请求");
		    	req.setAttribute("state", "102");
		    	req.setAttribute("respmsg", "非法请求");
		    	resp.sendRedirect("error.do");
		    	return;
		    }
		    
		    Map<String, Object> result = obtainOpenId(code);
		    if(result!=null){
		    	/**
		    	 * 将信息存入session
		    	 */
		    	String openid = result.get("openid").toString();
		    	req.getSession().setAttribute("openid", openid);
		    	req.getSession().setAttribute("originalId", originalId);
		    	
		    	/**
		    	 * 判断该用户是否关联微信,1.未关联进入关联页面 2 关联进入薪税钱包页面
		    	 */
		    	User user = userSerivce.selectUserByWechartId(openid);
		    	if(user!=null){
		    		String merchantId = user.getMerchantId();//平台ID
		    		CustomInfo info = customInfoService.searchCustomInfoByKey(merchantId);
		    		req.getSession().setAttribute("merchantId", merchantId);
		    		UserRelated related = relatedService.getRelatedByUserId(user.getId()+"",originalId);
		    		if(related!=null){
		    			String userNo = related.getUserNo();
		    			String signKey = info.getSalt();
		    			String timeStamp =  DateUtils.getNowDate();
		    			
		    			logger.info("跳转薪税钱包 方法： userNo="+userNo+", timeStamp="+timeStamp+
								", merchantId="+merchantId+", originalId="+originalId);
		    			
		    			HashMap<String, String> sparams = new HashMap<String,String>();
		    			sparams.put("userNo", userNo);
		    			sparams.put("merchantId", merchantId);
		    			sparams.put("originalId", originalId);
		    			sparams.put("timeStamp", timeStamp);
		    			String sign = ParamSignTool.sign(signKey, sparams);
		    			/**
		    			 * 重定向至 薪税钱包首页
		    			 */
		    			String url = "/wallet/H5/v1/custom/transfers/showHistory.shtml?userNo="+userNo
		    					+"&merchantId="+merchantId+"&timeStamp="+timeStamp+"&originalId="+originalId+"&sign="+sign;
		    			resp.sendRedirect(url); 
		    			return;
		    		}else{
		    			logger.error("用户不存在");
		    			req.setAttribute("state", "103");
				    	req.setAttribute("respmsg", "用户不存在");
		    			resp.sendRedirect("error.do");
		    			return;
		    		}
		    	}/*else if(!StringUtil.isEmpty(mobilePhone)){
					*//**
					 * 首次接入  手机号验证成功，查询手机号所属用户是否开户
					 *//*
					User mobileUser = userSerivce.getUserByMobilePhone(mobilePhone,merchantId);
					if(mobileUser != null){
						if(mobileUser.getUserType() == 1){
							
							UserRelated related = relatedService.getRelatedByUserId(mobileUser.getId()+"",originalId);
							CustomInfo info = customInfoService.searchCustomInfoByKey(merchantId);
							if(related!=null){
								userSerivce.addWechartId(mobileUser.getId()+"",openid);
								String userNo = related.getUserNo();
								String signKey = info.getSalt();
								String timeStamp =  DateUtils.getNowDate();
								
								HashMap<String, String> sparams = new HashMap<String,String>();
								sparams.put("userNo", userNo);
								sparams.put("merchantId", merchantId);
								sparams.put("originalId", originalId);
								sparams.put("timeStamp", timeStamp);
								String sign = ParamSignTool.sign(signKey, sparams);
								*//**
								 * 重定向至 薪税钱包首页
								 *//*
								String url = "/wallet/H5/v1/custom/transfers/showHistory.shtml?userNo="+userNo
										+"&merchantId="+merchantId+"&timeStamp="+timeStamp+"&originalId="+originalId+"&sign="+sign;
								resp.sendRedirect(url); 
								return;
							}else{
								resp.sendRedirect("error.do");
								return;
							}
						}else{
							resp.sendRedirect("error.do");
							return;
						}
					}else{
						resp.sendRedirect("error.do");
						return;
					}
		    	}*/else{
		    		/**
		    		 * 转发到手机关联页面
		    		 */
		    		resp.sendRedirect("registered.do");
		    		return;
		    	}
		    	
		    }else{
		    	logger.error("获取微信信息异常");
		    	req.setAttribute("state", "104");
		    	req.setAttribute("respmsg", "获取微信信息异常");
		    	resp.sendRedirect("error.do");
		    	return;
		    }
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				req.setAttribute("state", "105");
		    	req.setAttribute("respmsg", "方法异常");
				resp.sendRedirect("error.do");
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 薪税钱包------微信关联手机号页面
	 * @return
	 */
	@RequestMapping("registered")
	public String registered(HttpServletRequest req, HttpServletResponse resp) {
		return "wechart/checkTel.jsp";
		
	}
	
	/**
	 * 薪税钱包------微信openId绑定成功页
	 * @return
	 */
	@RequestMapping("success")
	public String success(HttpServletRequest req, HttpServletResponse resp,ModelMap model) {
		String url = req.getSession().getAttribute("url")+"";
		model.addAttribute("url", url);
		return "wechart/success.jsp";
		
	}
	
	/**
	 * 薪税钱包------选择签约企业页面
	 * @return
	 */
	@RequestMapping("selectCompany")
	public String selectCompany(HttpServletRequest req, HttpServletResponse resp,ModelMap model) {
		try {
			Integer userId = (Integer) req.getSession().getAttribute("userId");
			List<Map<String, Object>> companys = new ArrayList<Map<String, Object>>();
			if(userId==null){
				logger.error("用户userId丢失");
				return "wechart/error.jsp";
			}else{
				Map<String, Object> param = new HashMap<String, Object>();
	    		param.put("userId", userId);
	    		List<UserRelated> list = relatedService.getRelatedByParam(param);
	    		for (UserRelated userRelated : list) {
	    			param.put("companyId", userRelated.getCompanyId());
	    			param.put("originalId", userRelated.getOriginalId());
					List<ChannelRelated> channelList = channelRelatedService.getRelatedByParam(param);
					if(channelList.size()==0){
						continue;
					}
					ChannelRelated channelRelated = channelList.get(0);
					if(channelRelated!=null){
						Map<String, Object> company = new HashMap<String, Object>();
						company.put("sign", userRelated.getStatus());
						company.put("companyName", channelRelated.getCompanyName());
						company.put("id", userRelated.getId());
						companys.add(company);
					}
				}
	    		model.put("companys", companys);
	    		return "wechart/selectCompany.jsp";
			}
		} catch (Exception e) {
			e.fillInStackTrace();
			logger.error(e.getMessage(),e.fillInStackTrace());
			req.setAttribute("state", "105");
	    	req.setAttribute("respmsg", "方法异常");
			return "wechart/error.jsp";
		}
	}
	
	/**
	 * 薪税钱包------错误页面
	 * @return
	 */
	@RequestMapping("error")
	public String error(HttpServletRequest req, HttpServletResponse resp,ModelMap model) {
		String state = (String) req.getAttribute("state");
		String respmsg = (String) req.getAttribute("respmsg");
		model.put("state", state);
		model.put("respmsg", respmsg);
		return "wechart/error.jsp";
		
	}
	
	/**
	 * 发送手机验证码
	 * @return
	 */
	@RequestMapping(value ="sendCode", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> accountDetail(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		final String mobilePhone = (String) request.getParameter("bindtel");
		logger.info("sendCode方法  传参： mobilePhone="+mobilePhone);
		if(StringUtil.isEmpty(mobilePhone)){
			respstat = RespCode.error101;
		}else{
			try {
				if(!StringUtil.isMobileNO(mobilePhone)){
					respstat = RespCode.error101;
				}
				/**
				 * 发送验证码
				 */
				if(respstat == RespCode.success){
	                Map<String, Object> map = new HashMap<>(4);
	                map.put("portalDomain", request.getServerName());
	                OemConfig oemConfig = oemConfigService.getOemByParam(map);
	                if (oemConfig.getSmsStatus() != 1) {
	                    retModel(respstat, model);
	                    logger.info("返回结果：" + model);
	                    return model;
	                }
	                String smsSignature = oemConfig.getSmsSignature();
					String code = StringUtil.GetRandomNumberStr6();
					String[] mobiletelno = {mobilePhone};
					final String content = "【" + smsSignature + "】"+code+"，为了您的帐号安全，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务。";
					final String templateParam = "{\"code\":\"" + code + "\"}";
					sendCode(request, flag, code, mobiletelno, content, smsSignature,
							SmsTemplateCodeEnum.LOGIN.getCode(),
							templateParam);
				}
				
			} catch (Exception e) {
				respstat = RespCode.error000;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "验证码发送失败！");
				logger.error(e.getMessage(),e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 验证手机动态码
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/validMobileCode")
	public @ResponseBody
	Map<String, Object> validMobileCode(HttpServletRequest request,HttpServletResponse resp) throws Exception {
		
		
		String code = request.getParameter("code");
		String mobilePhone = request.getParameter("bindtel");
		logger.info("validMobileCode 方法： code="+code+", mobilePhone="+mobilePhone);
		

		Map<String, Object> response = new HashMap<String, Object>();

		int respstat = RespCode.success;
		if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
			respstat = RespCode.error101;
		}
		if (!StringUtil.isMobileNO(mobilePhone)) {
			respstat = RespCode.error101;
		}

		if (RespCode.success==respstat) {
			try {
				
				Parameter param = parameterService.valiMobiletelno(mobilePhone,
						code, flag);
				if (param != null) {
					
					/**
					 * 手机号验证成功，查询手机号所属用户是否开户
					 */
					User user = userSerivce.getUserByMobilePhone(mobilePhone);
					if(user != null){
						/**
						 * 开户状态是否为成功
						 */
						if(user.getUserType() == 1){
							Map<String, Object> param1 = new HashMap<String, Object>();
							param1.put("userId", user.getId());
							List<UserRelated> list = relatedService.getRelatedByParam(param1);
							
							/**
							 * 所属公司关联关系是否存在
							 */
							if(list.size()>0){
								UserRelated related = list.get(0);
								String originalId = related.getOriginalId();
								CustomInfo info = customInfoService.searchCustomInfoByKey(user.getMerchantId());
								/**
								 * 绑定微信openId(首次开通薪税钱包绑定)
								 */
								String openid = request.getSession().getAttribute("openid")+"";
								userSerivce.addWechartId(user.getId()+"",openid);
								
								String userNo = related.getUserNo();
								String signKey = info.getSalt();
								String timeStamp =  DateUtils.getNowDate();
								
								HashMap<String, String> sparams = new HashMap<String,String>();
								sparams.put("userNo", userNo);
								sparams.put("merchantId", user.getMerchantId());
								sparams.put("originalId", originalId);
								sparams.put("timeStamp", timeStamp);
								String sign = ParamSignTool.sign(signKey, sparams);
								/**
								 * 重定向至 薪税钱包首页
								 */
								String url = "/wallet/H5/v1/custom/transfers/showHistory.shtml?userNo="+userNo
										+"&merchantId="+user.getMerchantId()+"&timeStamp="+timeStamp+"&originalId="+originalId+"&sign="+sign;
								request.getSession().setAttribute("url", url);
								respstat = RespCode.success;
							}else{
								respstat = RespCode.error106;
							}
						}else{
							respstat = RespCode.error106;
						}
					}else{
						respstat = RespCode.error105;
					}
					
				} else {
					respstat = RespCode.error141;
				}
			} catch (Exception e) {
				respstat = RespCode.error000;
				logger.error(e.getMessage(),e);
			}
		}
		response.put(RespCode.RESP_STAT, respstat);
		response.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return response;
	}

	/**
	 * 获取微信信息
	 * @param code
	 * @return
	 */
	private static Map<String, Object> obtainOpenId(String code){
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			/**
			 * 拼接获取微信token链接
			 */
			String url = ACCESS_TOKEN_URL+"?appid=" + APP_ID + "&secret=" + APP_SECRET
					+ "&code=" + code + "&grant_type=authorization_code";
			JSONObject jsonObject = doGetJson(url);
			logger.info("获取微信token返回结果：access_token:" + jsonObject);
			
			String openid = jsonObject.getString("openid");
			String token = jsonObject.getString("access_token");
			
			/**
			 * 拼接获取微信用户信息链接
			 */
			/*String infoUrl = USERINFO_URL+"?access_token=" + token + "&openid=" + openid
					+ "&lang=zh_CN";
			JSONObject userInfo = doGetJson(infoUrl);
			logger.info("获取微信用户信息返回结果：userInfo:" + userInfo);*/
			
			result.put("openid", openid);
			result.put("token", token);
			/*result.put("userInfo", userInfo);*/
			
			return result;
		} catch (Exception e) {
			logger.error("获取微信用户信息异常");
			logger.error(e.getMessage());
		}
		return result;
	}
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
	
	public static void main(String[] args) {
		String backUrl="http://micro.jrmf360.com/wechat/login.do?originalId=hongrilikang";
        /**
        *这儿一定要注意！！首尾不能有多的空格（因为直接复制往往会多出空格），其次就是参数的顺序不能变动
        **/
        try {
			System.out.println("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + APP_ID+
			        "&redirect_uri=" + URLEncoder.encode(backUrl,"UTF-8")+
			        "&response_type=code" +
			        "&scope=snsapi_base" +
			        "&state=STATE#wechat_redirect");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
        String startTime = "2017-11-11";
		System.out.println(startTime.replaceAll("-", ""));
	}
}
