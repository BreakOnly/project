package com.jrmf.controller.systemrole.management;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.*;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CustomPermissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/management/config")
public class ManagementConfigController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(ManagementConfigController.class);
	@Autowired
	private ChannelCustomService customService;

	@Autowired
	private ChannelRelatedService channelRelatedService;
	
	@Autowired
	private UserSerivce userSerivce;
	
	@Autowired
	CustomPermissionService customPermissionService;
	
	/**
	 * Author Nicholas-Ning
	 * Description //TODO 商户管理--数量
	 * Date 16:37 2018/12/19
	 * Param [request]
	 * return java.util.Map<java.lang.String,java.lang.Object>
	 **/
	@RequestMapping(value = "/custom/countData")
	public @ResponseBody Map<String, Object> countData(HttpServletRequest request) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		
		String companyName = (String)request.getParameter("companyName");
		String customType = (String)request.getParameter("customType");
		String startTime = (String)request.getParameter("startTime");
		String endTime = (String)request.getParameter("endTime");
		try {
			Map<String,Object> paramMap= new HashMap<String,Object>();
			paramMap.put("startTime",startTime);
			paramMap.put("endTime",endTime);
			paramMap.put("name",companyName);
			paramMap.put("enable",1);
			paramMap.put("customType",customType);
			int total = customService.getCustomCount(paramMap);
			result.put("total", total);
		} catch (Exception e) {
			logger.error("", e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + result);
		return result;
	}

	/**
	 * Author Nicholas-Ning
	 * Description //TODO 商户管理--列表
	 * Date 14:28 2019/1/4
	 * Param [request]
	 * return java.util.Map<java.lang.String,java.lang.Object>
	 **/
	@RequestMapping(value = "/custom/listData")
	public @ResponseBody Map<String, Object> listData(HttpServletRequest request) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		
		String companyName = (String)request.getParameter("companyName");
		String customType = (String)request.getParameter("customType");
		String startTime = (String)request.getParameter("startTime");
		String endTime = (String)request.getParameter("endTime");
		String pageIndex = (String)request.getParameter("pageIndex");
		if( StringUtils.isEmpty(pageIndex)){
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		try {
			int pageSize = 10;
			Map<String,Object> paramMap= new HashMap<String,Object>();
			paramMap.put("start", (Integer.parseInt(pageIndex) - 1) * pageSize);
			paramMap.put("limit", pageSize);
			paramMap.put("startTime",startTime);
			paramMap.put("endTime",endTime);
			paramMap.put("name",companyName);
			paramMap.put("enabled",1);
			paramMap.put("customType",customType);
			List<ChannelCustom> customList = customService.getCustomListExRoot(paramMap);
			result.put("customList", customList);
		} catch (Exception e) {
			logger.error("", e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		return result;
	}
	

	/**
	 * 商户管理---导出
	 * @param model
	 * @param companyName
	 * @param startTime
	 * @param endTime
	 * @param customName
	 * @param sort
	 * @param pageIndex
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/custom/exportCustomData")
	public void exportCustomManage(ModelMap model,String companyName, String startTime, String endTime,String customName,String sort,String pageIndex,
			String customType,HttpServletRequest request,HttpServletResponse response) throws Exception {
		Map<String,Object> paramMap= new HashMap<String,Object>();
		paramMap.put("startTime",startTime);
		paramMap.put("endTime",endTime);
		paramMap.put("name",companyName);
		paramMap.put("enabled",1);
		paramMap.put("customType",customType);
		List<ChannelCustom> customList =customService.getCustomListExRoot(paramMap);
		
		String today = DateUtils.getNowDay();
		ArrayList<String> dataStr = new ArrayList<String>();
		for (int i = 0; i < customList.size(); i++) {
			ChannelCustom custom = customList.get(i);
			StringBuffer strBuff = new StringBuffer();
			String role = "";
			int type   = custom.getCustomType();
			if(type==1){
				role = "商户";
			}else if(type==2){
				role = "服务公司";
			}else if(type==3){
				role = "代理商";
			}
			strBuff.append(custom.getId() == 0 ? ""
							: custom.getId())
					.append(",")
					.append(custom.getId()== 0 ? ""
							:custom.getId())
					.append(",")
					.append(custom.getCompanyName()== null ? ""
							:custom.getCompanyName())
					.append(",")
					.append(custom.getCustomkey()== null ? ""
							:custom.getCustomkey())
					.append(",")
					.append(custom.getUsername() == null ? ""
							: custom.getUsername())
					.append(",")
					.append(custom.getPhoneNo() == null ? ""
							:custom.getPhoneNo())
					.append(",")
					.append(custom.getAgentId() == null ? ""
							:custom.getAgentId())
					.append(",")
					.append(role)
					.append(",")
					.append(custom.getCreateTime() == null ? ""
							:custom.getCreateTime());
					
			dataStr.add(strBuff.toString());
		}
		ArrayList<String> fieldName = new ArrayList<String>();
		fieldName.add("渠道id");
		fieldName.add("公司名称");
		fieldName.add("渠道Key");
		fieldName.add("账户");
		fieldName.add("手机号");
		fieldName.add("代理商ID");
		fieldName.add("角色");
		fieldName.add("服务截止时间");
		String filename = today + "商户数据";
		ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
	}
	
	/**
	 * 商户详情
	 * @param request
	 * @param ChannelId
	 * @return
	 */
	@RequestMapping(value = "/custom/details.do")
	public @ResponseBody Map<String, Object> details(HttpServletRequest request,String ChannelId) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ChannelCustom channelCustom =customService.getCustomById(Integer.parseInt(ChannelId));
			result.put("channelCustom", channelCustom);
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		return result;
	}
	
	/**
	 * 商户审核--数量
	 * @param request
	 * @param name
	 * @param username
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/review/countData")
	public @ResponseBody Map<String, Object> getAllChannelCount(HttpServletRequest request,
			String name,String username,ModelMap model) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String customKey = (String) request.getSession()
					.getAttribute("customkey");
			Map<String,Object> paramMap= new HashMap<String,Object>();
			paramMap.put("name",name);
			/***
			 * 管理员 、代理商可统计
			 */
			if(!"mfkj".equals(customKey)){
				paramMap.put("AgentId",customKey);
			}
			paramMap.put("enabled",0);//未开启的渠道
			paramMap.put("customType",1);//未开启的渠道
			int total = customService.getCustomListExRoot(paramMap).size();
			result.put("total", total);
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		return result;

	}
	
	/**
	 * 商户审核--列表
	 * @param request
	 * @param pageIndex
	 * @param name
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/review/listData")
	public @ResponseBody Map<String, Object> getAllChannelByPage(HttpServletRequest request,
			String pageIndex,String name,ModelMap model) {
		int respstat = 1;
		String customKey = (String) request.getSession()
				.getAttribute("customkey");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			int pageSize = 10;
			Map<String,Object> paramMap= new HashMap<String,Object>();
			paramMap.put("start",(Integer.parseInt(pageIndex)-1)*pageSize);
			paramMap.put("limit",pageSize);
			paramMap.put("name",name);
			/***
			 * 管理员 、代理商可统计
			 */
			if(!"mfkj".equals(customKey)){
				paramMap.put("AgentId",customKey);
			}
			paramMap.put("enabled",0);//未开启的渠道
			paramMap.put("customType",1);//未开启的渠道
			List<ChannelCustom> channels = customService.getCustomListExRoot(paramMap);
			result.put("list", channels);
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		return result;
	}
	
	/**
	 * 审核
	 * @param request
	 * @param ChannelId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/review/enabled")
	public @ResponseBody Map<String, Object> customReview(HttpServletRequest request,String ChannelId,
			String enabled,ModelMap model) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if("1".equals(enabled)){
				ChannelCustom loginCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
				customService.checkCustom(Integer.parseInt(ChannelId),enabled,loginCustom.getUsername());
			}else{
				customService.deleteById(ChannelId);
			}
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "成功");
		return result;
	}
	
	/**
	 * 是否设置交易密码
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/tranPassword/whether", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteBatch(HttpServletResponse response,HttpServletRequest request){
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");//商户标识
		ChannelCustom loginUser = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");
		if(StringUtil.isEmpty(customkey) || loginUser==null){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				ChannelCustom custom = customService.getCustomById(loginUser.getId());
				if(StringUtil.isEmpty(custom.getTranPassword())){
					model.put("whether", 0);
				}else{
					model.put("whether", 1);
				}
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口执行失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 薪税钱包配置——商户管理——列表
	 * 说明:
	 * @param request
	 * @param model
	 * @param companyName
	 * @param customType
	 * @param startTime
	 * @param endTime
	 * @return:
	 */
	@RequestMapping(value = "/customLevel/getCustomManageByPage")
	public @ResponseBody Map<String, Object> getCustomManageByPage(HttpServletRequest request,
			ModelMap model,String companyName,String customType, String startTime, String endTime,
			String pageNo) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// 渠道名称
		int pageSize = 10;
		Map<String,Object> paramMap= new HashMap<String,Object>();
		paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
		paramMap.put("limit", pageSize);
		paramMap.put("startTime",startTime);
		paramMap.put("endTime",endTime);
		paramMap.put("name",companyName);
		paramMap.put("enabled",1);
		paramMap.put("customType",customType);
		if(!"mfkj".equals(customkey)){
			paramMap.put("customkey",customkey);
		}
		List<ChannelCustom> customList =customService.getCustomList(paramMap);
		result.put("customList", customList);
		return retModel(respstat, result);
	}
	
	/**
	 * 薪税钱包配置——配置商户服务公司——商户服务公司列表
	 */
	@RequestMapping(value = "/companyListData")
	public @ResponseBody Map<String, Object> companyListData(HttpServletRequest request, ModelMap model,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		String customkey = (String) request.getSession().getAttribute("customkey");
		String customName = (String) request.getParameter("customName");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
//		String pageSize = request.getParameter("pageSize") == null ? "10" : (String) request.getParameter("pageSize");
		logger.info("/companyListData方法  传参： customkey =" + customkey);

		Map<String, Object> result = new HashMap<String, Object>();

		if (StringUtil.isEmpty(customkey)) {
			return retModel(RespCode.error101, result);
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("customType", 1);
				paramMap.put("startTime", startTime);
				paramMap.put("endTime", endTime);
				paramMap.put("name", customName);
				if (!"mfkj".equals(customkey)) {
					paramMap.put("masterCustom", customkey);
				}
//				int total = customService.getCustomList(paramMap).size();
//				if (!StringUtil.isEmpty(pageNo)) {
//					paramMap.put("start", (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//					paramMap.put("limit", Integer.parseInt(pageSize));
//				}
				PageHelper.startPage(pageNo, pageSize);
				List<ChannelCustom> list = customService.getCustomList(paramMap);
				PageInfo<ChannelCustom> pageInfo = new PageInfo<>(list);
				result.put("list", pageInfo.getList());
				result.put("total", pageInfo.getTotal());
				result.put("pageNo", pageNo);
				result.put("pageSize", pageSize);
			} catch (Exception e) {
				logger.error("", e);
				return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", result);
			}
		}
		return retModel(respstat, result);
	}
	
	/**
	 * 商户关联的薪税服务公司列表
	 */
	@RequestMapping(value = "/originalForList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> originalForList(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String originalId = (String) request.getParameter("originalId");
		logger.info("/originalForList 方法  传参： originalId="+originalId);
		try {
			if(StringUtil.isEmpty(originalId)){
				return retModelMsg(RespCode.error107, "启用失败，配置不存在！", model);
			}
			Map<String,Object> param= new HashMap<String,Object>();
			param.put("originalId",originalId);
			List<ChannelRelated> list = channelRelatedService.getRelatedByParam(param);
			List<ChannelRelated> relateds = new ArrayList<ChannelRelated>();
			for (ChannelRelated channelRelated : list) {
				ChannelRelated related = muStr(channelRelated);
				relateds.add(related);
			}
			model.put("list", relateds);
		} catch (Exception e) {
			logger.error("", e);
			return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("返回结果：" + model);
		return result;
	}
	
	public ChannelRelated muStr(ChannelRelated related){
		String ServiceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "100", 3);
		String ProfiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "100", 3);
		String ProfiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "100", 3);
		related.setServiceRates(ServiceRate);
		related.setProfiltLower(ProfiltLower);
		related.setProfiltUpper(ProfiltUpper);
		return related;
	}
	
	/**
	 * 配置商户的薪资服务公司--薪税平台列表
	 */
	@RequestMapping(value = "/merchantList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> merchantList(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			List<CustomInfo> list = customInfoService.getAllActiveCustom();
			model.put("list", list);
		} catch (Exception e) {
			logger.error("", e);
			return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("返回结果：" + result);
		return result;
	}
	
	/**
	 * 配置商户的薪资服务公司--薪资服务公司列表
	 */
	@RequestMapping(value = "/companyList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> companyListBychannelId(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String merchantId = (String) request.getParameter("merchantId");
		logger.info("/companyList 方法  传参： customkey="+merchantId);
		if(StringUtil.isEmpty(merchantId)){
			return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
		}
		try {
			List<User> list = userSerivce.getCompanyByMerchantId(merchantId);
			model.put("list", list);
		} catch (Exception e) {
			logger.error("", e);
			return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("返回结果：" + result);
		return result;  
	}
	
	/**
	 * 配置商户的薪资服务公司--修改费率
	 */
	@RequestMapping(value = "/updateCompany", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> updateCompany(HttpServletRequest request,
			HttpServletResponse response,ChannelRelated related) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customKey = (String)request.getSession().getAttribute("customkey");
		if(!"mfkj".equals(customKey)|| StringUtil.isEmpty(related.getServiceRates()) || StringUtil.isEmpty(related.getProfiltLower())
				|| StringUtil.isEmpty(related.getProfiltUpper())){
			return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
		}
		try {
			String ServiceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "0.01", 3);
			String ProfiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "0.01", 3);
			String ProfiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "0.01", 3);
			related.setServiceRates(ServiceRate);
			related.setProfiltLower(ProfiltLower);
			related.setProfiltUpper(ProfiltUpper);
			channelRelatedService.updateChannelRelated(related);
			
		} catch (Exception e) {
			logger.error("", e);
			return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("返回结果：" + result);
		return result;
	}
	
	/**
	 * 配置商户的薪资服务公司--保存薪资服务公司关系
	 */
	@RequestMapping(value = "/selectCompany", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> selectCompany(HttpServletRequest request,
			HttpServletResponse response,ChannelRelated related) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customKey = (String)request.getSession().getAttribute("customkey");
		if(StringUtil.isEmpty(related.getMerchantId()) 
				|| StringUtil.isEmpty(related.getCompanyId()) || !"mfkj".equals(customKey)
				|| StringUtil.isEmpty(related.getServiceRates()) || StringUtil.isEmpty(related.getProfiltLower())
				|| StringUtil.isEmpty(related.getProfiltUpper()) || StringUtil.isEmpty(related.getOriginalId())){
			return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
		}
		
		Map<String,Object> param= new HashMap<String,Object>();
		param.put("originalId",related.getOriginalId());
		param.put("companyId",related.getCompanyId());
		
		int countByappId = channelRelatedService.getRelatedCountByParam(param); 
		if(countByappId>0){
			return retModelMsg(RespCode.error107, "关联关系已存在，请勿重复提交！", model);
		}
		try {
			String ServiceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "0.01", 3);
			String ProfiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "0.01", 3);
			String ProfiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "0.01", 3);
			related.setServiceRates(ServiceRate);
			related.setProfiltLower(ProfiltLower);
			related.setProfiltUpper(ProfiltUpper);
			//状态为 启用
			related.setStatus(1);
			/**
			 * 保存启用状态的关系
			 */
			channelRelatedService.createChannelRelated(related);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("返回结果：" + result);
		return result;
	}
	
	/**
	 * 启用配置
	 */
	@RequestMapping(value = "/originalEnable", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> originalEnable(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String id = (String) request.getParameter("id");
		logger.info("/originalEnable 方法  传参： id = "+ id);
		try {
			ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
			if(channelRelated==null || StringUtil.isEmpty(id)){
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "启用失败，配置不存在！");
				return model;
			}
			/**
			 * 变更其他状态为历史状态
			 */
			Map<String,Object> param= new HashMap<String,Object>();
			param.put("originalId",channelRelated.getOriginalId());
			param.put("status","1");
			channelRelatedService.updateRelatedStatus(param);
			channelRelated.setStatus(1);//启用该配置
			channelRelatedService.updateChannelRelated(channelRelated);
		} catch (Exception e) {
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 关联关系详情
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> detail(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String id = (String) request.getParameter("id");
		logger.info("/detail 方法  传参： id = "+ id);
		try {
			ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
			if(channelRelated==null || StringUtil.isEmpty(id)){
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "启用失败，配置不存在！");
				return model;
			}
			channelRelated = muStr(channelRelated);
			model.put("channelRelated", channelRelated);
		} catch (Exception e) {
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	/**
	 * 获取全部权限
	 * 说明:
	 * @param model
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/getAllPermission")
	public @ResponseBody HashMap<String, Object> getAllPermission(
			Model model, HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();
		
		ChannelCustom customLogin = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");// 渠道
		
		String customKey = (String) request.getSession()
				.getAttribute("customkey");// 渠道名称
		
		try {
			if("mfkj".equals(customKey)){
				allPermission = customPermissionService
						.getAllPermission();
			}else{
				allPermission = customPermissionService
						.getCustomPermissionReal(customLogin.getId());
			}
			for (int i = 0; i < allPermission.size(); i++) {
				ChannelPermission e = allPermission.get(i);
				//除魔方科技以外的其它公司不能给别人分配添加管理员相关的功能，目前把不能分配的功能写死
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("name", e.getContentName());
				mapList.add(map);
			}
		} catch (Exception e) {
			logger.error("", e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "权限列表获取异常！请练习管理员。");
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
	/**
	 * 获取当前权限
	 * 说明:
	 * @param model
	 * @param request
	 * @param id
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/getChannelUserPermission")
	public @ResponseBody HashMap<String, Object> getCustomPermission(
			Model model, HttpServletRequest request, String id) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();

		ChannelCustom customLogin = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");// 渠道名称
		
		String customKey = (String) request.getSession()
				.getAttribute("customkey");// 渠道
		//这个map用于储存所有权限每个节点有多少个子节点
		Map<Integer,Integer> perIds = new HashMap<Integer,Integer>();
		//这个map用于储存用户权限每个节点有多少个子节点
		Map<Integer,Integer> userIds = new HashMap<Integer,Integer>();
		
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();
		if("mfkj".equals(customKey)){
			allPermission = customPermissionService
					.getAllPermission();
		}else{
			allPermission = customPermissionService
					.getCustomPermissionReal(customLogin.getId());
		}
		List<ChannelPermission> customPermission = customPermissionService
				.getCustomPermissionReal(Integer.parseInt(id));
		for (int i = 0; i < allPermission.size(); i++) {
			ChannelPermission e = allPermission.get(i);
			//将所有节点初始化到map种，每个节点的子节点数量为0
			perIds.put(e.getId(), 0);
			//除魔方科技以外的其它公司不能给别人分配添加管理员相关的功能，目前把不能分配的功能写死
			if (!"mfkj".equals(customKey)) {
				if(!"/custom/catalog/channelUser/getChannelUser.do".equals(e.getLink()) ||
						!"/custom/catalog/channelUser/addChannelUser.do".equals(e.getLink())){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", e.getId());
					map.put("pId", e.getParentId());
					map.put("name", e.getContentName());
					for (ChannelPermission permission : customPermission) {
						if (permission.getId() == e.getId()) {
							map.put("checked", true);
						}
					}
					mapList.add(map);
				}
			}else{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("name", e.getContentName());
				for (ChannelPermission permission : customPermission) {
					if (permission.getId() == e.getId()) {
						map.put("checked", true);
					}
					//将用户权限每个节点的子节点数初始化到map中。
					userIds.put(permission.getId(), 0);
				}
				mapList.add(map);
			}
		}
		//遍历mapList,若哪个节点没有checked = true ，则递归删除其父节点
		List<Integer> removeKeys = new ArrayList<>();
		for(Map<String, Object> map : mapList){
			if(!map.containsKey("checked")){
				removeKeys.add((Integer)map.get("id"));
			}
		}
		//递归删除
		for (Integer integer : removeKeys) {
			removeNode(integer,mapList);
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info("mapList",mapList);
		return result;
	}
	
	private void removeNode(Integer remove,List<Map<String, Object>> mapList) {
		for (Map<String, Object> map : mapList) {
			Integer deleteKey = (Integer) map.get("id");
			Integer pId = (Integer) map.get("pId");
			if (Integer.compare(deleteKey, remove) == 0) {
				if(map.containsKey("checked")){
					map.remove("checked");
				}
				removeNode(pId, mapList);
			}
		}
	}

	/**
	 * 整体保存权限
	 * 问题记录：财务中心  permissionid=146 这个id没有传到后端。导致用户商户无法分配财务中心的权限
	 * 说明:
	 * @param model
	 * @param request
	 * @param customId
	 * @param ids
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/saveChannelUserPermission")
	public @ResponseBody HashMap<String, Object> saveCustomPermission(
			Model model, HttpServletRequest request, String customId, String ids) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		if(StringUtils.isEmpty(ids)){
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		if(ids.charAt(0)==','){
			ids = ids.substring(1,ids.length());
		}
		String[] array = ids.split(",");
		
		try {
			customPermissionService.deleteCustomPermission(customId);
			customPermissionService.saveCustomPermission(customId, array);
		} catch (Exception e) {
			logger.error("", e);
			logger.error(e.getMessage(),e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "操作失败！");
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
	/**
	 * 获取权限详情
	 * 说明:
	 * @param model
	 * @param request
	 * @param id
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/getPermissionDetail")
	public @ResponseBody HashMap<String, Object> getPermissionDetail(
			Model model, HttpServletRequest request,String id) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		ChannelPermission permission = customPermissionService.getPermissionDetailById(id);
		result.put("permission", permission);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
	/**
	 * 修改权限详情
	 * 说明:
	 * @param model
	 * @param request
	 * @param permiisson
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/savePermission")
	public @ResponseBody HashMap<String, Object> savePermission(
			Model model, HttpServletRequest request,ChannelPermission permiisson) {
		int respstat = RespCode.success;
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			if(permiisson.getId() != 0){
				customPermissionService.updatePermission(permiisson);
			}else{
				customPermissionService.savePermission(permiisson);
			}
		} catch (Exception e) {
			logger.error("", e);
			logger.error(e.getMessage(),e);
			respstat = RespCode.error000;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "修改失败！");
			return result;
		}
		result.put("permiisson", permiisson);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
}
