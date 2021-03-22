package com.jrmf.controller.wechat;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.threadpool.ThreadUtil;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
/**
 * 魔方公众号--斜杠公社微信登陆
 * @author zhangzehui
 *
 */
@SuppressWarnings("deprecation")
@Controller
@RequestMapping("wechat/mf/")
public class WeChatMfActionController extends BaseController{
	
	private static Logger logger = LoggerFactory.getLogger(WeChatMfActionController.class);

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
	 * 薪税钱包------微信登陆（不携带商户标识，魔方公众号登陆）
	 * @return
	 */
	@RequestMapping("loginForMfkj")
	public void loginForMfkj(HttpServletRequest req, HttpServletResponse resp) {

		try {

		    String code = req.getParameter("code");
		    Map<String, Object> result = obtainOpenId(code);
		    if(result!=null){
		    	/**
		    	 * 将信息存入session
		    	 */
		    	String openid = result.get("openid").toString();
		    	req.getSession().setAttribute("openid", openid);
		    	
		    	/**
		    	 * 判断该用户是否关联微信,1.未关联进入关联页面 2 关联进入薪税钱包页面
		    	 */
		    	User user = userSerivce.selectUserByWechartId(openid);
		    	if(user!=null){
		    		Map<String, Object> param = new HashMap<String, Object>();
		    		param.put("userId", user.getId());
		    		param.put("userNo", user.getUserNo());
		    		List<UserRelated> list = relatedService.getRelatedByParam(param);
		    		String merchantId = user.getMerchantId();//平台ID
		    		req.getSession().setAttribute("merchantId", merchantId);
		    		if(list.size()>0){
		    			CustomInfo info = customInfoService.searchCustomInfoByKey(merchantId);
		    			UserRelated related = list.get(0);
		    			String userNo = related.getUserNo();
		    			String signKey = info.getSalt();
		    			String timeStamp =  DateUtils.getNowDate();
		    			String originalId = related.getOriginalId();
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
		    			resp.sendRedirect("mfError.do");
		    			return;
		    		}
		    	}else{
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
		    	resp.sendRedirect("mfError.do");
		    	return;
		    }
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				req.setAttribute("state", "105");
		    	req.setAttribute("respmsg", "方法异常");
				resp.sendRedirect("mfError.do");
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
		return "wechart/checkTelForMf.jsp";
		
	}
	
	/**
	 * 薪税钱包------合作协议模板
	 * @return
	 */
	@RequestMapping("template")
	public String template(HttpServletRequest req, HttpServletResponse resp) {
		return "wechart/agreement.html";
		
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
				return "wechart/errorMf.jsp";
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
			return "wechart/errorMf.jsp";
		}
	}
	
	/**
	 * 薪税钱包------选择签约企业，进入首页
	 * @param originalId  商户标识
	 * @param merchantId  平台标识
	 * @return
	 */
	/*@RequestMapping(value = "/index")
	public @ResponseBody Map<String, Object> index(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, Object> model = new HashMap<String, Object>();
		int respstat = RespCode.success;
		try {
			String id = req.getParameter("id");
			if(StringUtil.isEmpty(id)){
				logger.error("参数异常");
				respstat = RespCode.error000;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "请先选择一个企业");
				return model;
			}
			UserRelated related = relatedService.getRelatedById(id);
			CustomInfo info = customInfoService.searchCustomInfoByKey(aiyuangong);
			String userNo = related.getUserNo();
			String signKey = info.getSalt();
			String timeStamp =  DateUtils.getNowDate();
			String originalId = related.getOriginalId();
			HashMap<String, String> sparams = new HashMap<String,String>();
			sparams.put("userNo", userNo);
			sparams.put("merchantId", aiyuangong);
			sparams.put("originalId", originalId);
			sparams.put("timeStamp", timeStamp);
			String sign = ParamSignTool.sign(signKey, sparams);
			*//**
			 * 重定向至 薪税钱包首页
			 *//*
			String url = "/wallet/H5/v1/custom/transfers/showHistory.shtml?userNo="+userNo
					+"&merchantId="+aiyuangong+"&timeStamp="+timeStamp+"&originalId="+originalId+"&sign="+sign;
			model.put("url", url);
		} catch (Exception e) {
			e.fillInStackTrace();
			logger.error(e.getMessage(),e.fillInStackTrace());
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info("返回结果：" + model);
		return model;
	}*/
	
	/**
	 * 薪税钱包------错误页面
	 * @return
	 */
	@RequestMapping("mfError")
	public String error(HttpServletRequest req, HttpServletResponse resp,ModelMap model) {
		String state = (String) req.getAttribute("state");
		String respmsg = (String) req.getAttribute("respmsg");
		model.put("state", state);
		model.put("respmsg", respmsg);
		return "wechart/errorMf.jsp";
		
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
							param1.put("userNo", user.getUserNo());
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
								logger.info("跳转薪税钱包 方法： userNo="+userNo+", timeStamp="+timeStamp+
										", merchantId="+user.getMerchantId()+", originalId="+originalId);
								if(StringUtil.isEmpty(userNo) || StringUtil.isEmpty(user.getMerchantId()) || StringUtil.isEmpty(originalId)
										|| StringUtil.isEmpty(timeStamp)){
									respstat = RespCode.error105;
								}else{
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
									logger.info("请求URL="+url);
									respstat = RespCode.success;
								}
								
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
            jsonObject = JSONObject.parseObject(result);
		}
		httpGet.releaseConnection();
		return jsonObject;
	}
	
	public static void main(String[] args) {
		String backUrl="http://wallet-s.jrmf360.com/wechat/mf/loginForMfkj.do";
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        String startTime = "2017-11-11";
		System.out.println(startTime.replaceAll("-", ""));
	}
}
