package com.jrmf.controller.wechat;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jrmf.domain.User;
import com.jrmf.domain.UserRelated;
import com.jrmf.service.UserRelatedService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SMSSendUtils;

/**
 * 微信公众号用户签约
 * 
 * @author guoto
 *
 */
@SuppressWarnings("deprecation")
@Controller
@RequestMapping("wechat/public/")
public class WeChatPublicController {
	private static Logger logger = LoggerFactory.getLogger(WeChatPublicController.class);

	/**
	 * 静态配置，后期更换位置
	 */
	private static final String APP_ID = "wx8d8e557a9f9102bb";

	private static final String APP_SECRET = "ff86c67e0ecdba8935d1bb2bacbf78ca";

	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

	final String flag = SMSSendUtils.VALI_MOBILE;

	@Autowired
	private UserSerivce userSerivce;
	@Autowired
	private UserRelatedService relatedService;

	@RequestMapping("login")
	public void login(HttpServletRequest request, HttpServletResponse response) {
		try {
			String code = request.getParameter("code");
			logger.info("微信登陆---wechat/public/login.do方法：code=" + code);
			// 拿到用户的微信id
			Map<String, Object> result = obtainOpenId(code);
			if (result != null) {
				Object openid = result.get("openid");
				/**
				 * db中按照微信id拿到用户信息
				 */
				User user = null;
				if (openid != null) {
					// 将openid存入Session
					request.getSession().setAttribute("openid", openid);
					logger.info(openid.toString());
					// 通过wechatId获取用户信息
					user = userSerivce.selectUserByWechartId(openid.toString());
				}
				if (user != null) {
					Map<String, Object> param = new HashMap<>(5);
					param.put("userId", user.getId());
					List<UserRelated> list = relatedService.getRelatedByParam(param);
					if (list.size() == 0) {
						// 用户不存在，跳转错误页面
						response.sendRedirect("signFail.do");
						return;
					} else {
						for (UserRelated userRelated : list) {
							if (userRelated.getStatus() == 1) {
								// 跳转已签约页面
								response.sendRedirect("signed.do");
								return;
							}
						}
					}
				}
				response.sendRedirect("/wechat/public/gotocheckIdentity.do");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * 微信公众号---实名认证
	 * 
	 * @return
	 */
	@RequestMapping("checkIdentity")
	@ResponseBody
	public Map<String, Object> checkIdentity(HttpServletRequest request, HttpServletResponse response) {
		String userName = (String) request.getParameter("username");
		String certId = (String) request.getParameter("certId");
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("respstat", respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info("公众号实名认证	方法参数：username=" + userName + " certId=" + certId);
		// 非空验证
		try {
			if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(certId)) {
				respstat = RespCode.error101;
				result.put("respstat", respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				return result;
			}
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("userName", userName);
			param.put("certId", certId);
			List<User> list = new ArrayList<User>();
			list = userSerivce.getUserByParam(param);
			if (list.size() != 0) {
				for (User user : list) {
					param.clear();
					param.put("userId", user.getId());
					List<UserRelated> userRelated = relatedService.getRelatedByParam(param);
					if (list.size() == 0) {
						// 用户不存在，跳转错误页面
						respstat = RespCode.error101;
						result.put("respstat", respstat);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
						return result;
					} else {
						for (UserRelated userRelated1 : userRelated) {
							if (userRelated1.getStatus() == 1) {
								// 跳转已签约页面
								respstat = RespCode.error133;
								result.put("respstat", respstat);
								result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
								return result;
							}
						}
					}
				}
				for (User user : list) {
					if (user.getUserType() == 11 || user.getUserType() == 1) {
						// 将userId放入Session
						request.getSession().setAttribute("userId", user.getId() + "");
						// 跳转签约页面
						return result;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			respstat = RespCode.error107;
			result.put("respstat", respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		respstat = RespCode.error105;
		result.put("respstat", respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 微信公众号---签约
	 * 
	 * @return
	 */
	@RequestMapping("agreement")
	@ResponseBody
	public Map<String, Object> agreement(HttpServletRequest request, HttpServletResponse response) {
		String userId = (String) request.getSession().getAttribute("userId");
		String openid = (String) request.getSession().getAttribute("openid");
		logger.info("用户签约接口参数：userId=" + userId + " openid=" + openid);
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("respstat", respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		try {
			User user = null;
			if (!StringUtils.isEmpty(userId)) {
				user = userSerivce.getUserByUserId(Integer.parseInt(userId));
			}
			if (user != null) {
				// 签约完成，修改用户签约关系表
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("userName", user.getUserName());
				param.put("certId", user.getCertId());
				List<User> list = new ArrayList<>();
				list = userSerivce.getUserByParam(param);
				if (list.size() != 0) {
					for (User user2 : list) {
						userSerivce.addWechartId(user2.getId() + "", openid);
						Map<String, Object> param1 = new HashMap<String, Object>();
						param1.put("userId", user2.getId());
						List<UserRelated> list1 = relatedService.getRelatedByParam(param1);
						if (list1.size() == 0) {
							// 用户不存在，跳转错误页面
							respstat = RespCode.error105;
							result.put("respstat", respstat);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
							return result;
						} else {
							for (UserRelated userRelated : list1) {
								if (userRelated.getStatus() != 1) {
									userRelated.setStatus(1);
									relatedService.updateUserRelated(userRelated);
								}
							}
						}
					}
				}
			} else {
				// 用户信息不存在，跳转错误页面.
				respstat = RespCode.error105;
				result.put("respstat", respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				return result;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 微信公众号---去往实名制认证页面
	 * 
	 * @return
	 */
	@RequestMapping("signSuccess")
	public String signSuccess(HttpServletRequest req, HttpServletResponse resp) {
		return "signSuccess";
	}

	/**
	 * 实名认证错误页面 说明:
	 * 
	 * @param req
	 * @param resp
	 * @return:
	 */
	@RequestMapping("signFail")
	public String signFail(HttpServletRequest req, HttpServletResponse resp) {
		return "signFail";
	}

	/**
	 * 微信公众号---去往实名制认证页面
	 * 
	 * @return
	 */
	@RequestMapping("gotocheckIdentity")
	public String gotocheckIdentity(HttpServletRequest req, HttpServletResponse resp) {
		return "checkIdentity";
	}

	/**
	 * 微信公众号---去往签约页面
	 * 
	 * @return
	 */
	@RequestMapping("gotoagreement")
	public String gotoagreement(HttpServletRequest req, HttpServletResponse resp) {
		return "agreement";
	}

	@RequestMapping("signed")
	public String signed(HttpServletRequest req, HttpServletResponse resp) {
		return "signed";
	}

	/**
	 * 获取微信信息
	 * 
	 * @param code
	 * @return
	 */
	private static Map<String, Object> obtainOpenId(String code) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			/**
			 * 拼接获取微信token链接
			 */
			String url = ACCESS_TOKEN_URL + "?appid=" + APP_ID + "&secret=" + APP_SECRET + "&code=" + code
					+ "&grant_type=authorization_code";
			JSONObject jsonObject = doGetJson(url);
			logger.info("获取微信token返回结果：access_token:" + jsonObject);

			String openid = jsonObject.getString("openid");
			String token = jsonObject.getString("access_token");

			/**
			 * 拼接获取微信用户信息链接
			 */
			/*
			 * String infoUrl = USERINFO_URL+"?access_token=" + token +
			 * "&openid=" + openid + "&lang=zh_CN"; JSONObject userInfo =
			 * doGetJson(infoUrl); logger.info("获取微信用户信息返回结果：userInfo:" +
			 * userInfo);
			 */

			result.put("openid", openid);
			result.put("token", token);
			/* result.put("userInfo", userInfo); */

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
		String backUrl = "http://wallet-s.jrmf360.com/wechat/public/login.do";
		/**
		 * 这儿一定要注意！！首尾不能有多的空格（因为直接复制往往会多出空格），其次就是参数的顺序不能变动
		 **/
		try {
			System.out.println("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + APP_ID + "&redirect_uri="
					+ URLEncoder.encode(backUrl, "UTF-8") + "&response_type=code" + "&scope=snsapi_base"
					+ "&state=STATE#wechat_redirect");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}
}
