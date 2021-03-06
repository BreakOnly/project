package com.jrmf.controller.systemrole.merchant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.domain.*;
import com.jrmf.persistence.CustomMenuDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.bestSign.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/wallet/config")
public class WalletConfigController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(WalletConfigController.class);
	@Autowired
	private ChannelCustomService customService;

	@Autowired
	private ChannelRelatedService channelRelatedService;

	@Autowired
	private UserSerivce userSerivce;

	@Autowired
	CustomPermissionService customPermissionService;

	@Autowired
	private BestSignConfig bestSignConfig;

	@Autowired
	private OrganizationTreeService organizationTreeService;

	@Autowired
	private CustomMenuDao customMenuDao;

	@Autowired
	private CustomPermissionTemplateService customPermissionTemplateService;

	@Autowired
	private CustomProxyDao customProxyDao;


	/**
	 * Author Nicholas-Ning
	 * Description //TODO ????????????--??????
	 * Date 12:06 2018/12/18
	 * Param [request]
	 * return java.util.Map<java.lang.String,java.lang.Object>
	 **/
	@RequestMapping(value = "/customs")
	public @ResponseBody
	Map<String, Object> listData(@RequestParam(value = "companyName", required = false) String companyName,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "hideAdmin", required = false) String hideAdmin,
			@RequestParam(value = "customType", required = false) String customType,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "1") Integer pageNo,
			HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>(6);
		Map<String, Object> paramMap = new HashMap<>(10);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(!CommonString.ROOT.equals(customLogin.getCustomkey())
				&& !CommonString.ROOT.equals(customLogin.getMasterCustom())){
			paramMap.put("platform", "go");
		}


		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("name", companyName);
		paramMap.put("enabled", 1);
		paramMap.put("customType", customType);
		paramMap.put("hideAdmin", hideAdmin);
//		int total = customService.getListCustomDetail(paramMap).size();
//		paramMap.put("start", getFirst(pageNo, pageSize));
//		paramMap.put("limit", Integer.parseInt(pageSize));
		PageHelper.startPage(pageNo,pageSize);
		List<ChannelCustom> customList = customService.getListCustomDetail(paramMap);
		PageInfo<ChannelCustom> pageInfo = new PageInfo<>(customList);

		result.put("total", pageInfo.getTotal());
		result.put("customList", pageInfo.getList());
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "??????");
		return result;
	}


	/**
	 * ????????????---??????
	 *
	 * @throws Exception ioException
	 */
	@RequestMapping(value = "/custom/exportCustomData")
	public void exportCustomManage(HttpServletResponse response,
			@RequestParam(value = "companyName", required = false) String companyName,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "customType", required = false) String customType) throws Exception {
		Map<String, Object> paramMap = new HashMap<>(7);
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("name", companyName);
		paramMap.put("enabled", 1);
		paramMap.put("customType", customType);
		List<ChannelCustom> customList = customService.getListCustomDetail(paramMap);
		String today = DateUtils.getNowDay();
		ArrayList<String> dataStr = new ArrayList<>();
		for (ChannelCustom custom : customList) {
			StringBuilder strBuff = new StringBuilder();
			CustomType type = CustomType.codeOf(custom.getCustomType());
			String role = type == null ? "" : type.getDesc();
			LoginRole loginRole = LoginRole.codeOf(custom.getLoginRole());
			String loginRoleDesc = loginRole == null ? "" : loginRole.getDesc();
			strBuff.append(StringUtil.isEmpty(custom.getEmail()) ? "" : custom.getEmail()).append(",")
			.append(StringUtil.isEmpty(custom.getUsername()) ? "" : custom.getUsername()).append(",")
			.append(StringUtil.isEmpty(custom.getCompanyName()) ? "" : custom.getCompanyName()).append(",")
			.append(StringUtil.isEmpty(custom.getPhoneNo()) ? "" : custom.getPhoneNo()).append(",")
			.append(role).append(",")
			.append(loginRoleDesc).append(",")
			.append(StringUtil.isEmpty(custom.getCreateTime()) ? "" : custom.getCreateTime());

			dataStr.add(strBuff.toString());
		}
		ArrayList<String> fieldName = new ArrayList<>();
		fieldName.add("??????");
		fieldName.add("????????????");
		fieldName.add("????????????");
		fieldName.add("?????????");
		fieldName.add("??????????????????");
		fieldName.add("??????????????????");
		fieldName.add("??????????????????");
		String filename = today + "????????????";
		ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
	}

	/**
	 * ??????????????????,??????customkey ??????????????????????????????
	 */
	@RequestMapping(value = "/group/customs")
	public @ResponseBody
	Map<String, Object> getGroupCustomList(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "0") Integer nodeId,
			@RequestParam("pageNo") int pageNo,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "companyName", required = false) String companyName,
			@RequestParam(required = false) int customType,
			@RequestParam(required = false) String customKey) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>(5);
		if (customKey == null) {
			customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
		}

		ChannelCustom custom = customService.getCustomByCustomkey(customKey);
		//????????????????????????????????????????????????
		if (custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1) {
			logger.info("??????????????????{}?????????????????????", custom.getCompanyName());
			customType = CustomType.PROXYCHILDEN.getCode();
			//?????????????????????????????????custom_proxy_childen,?????????????????????nodeId???????????????????????????
			OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customKey,null);
			nodeId = node.getId();
		}

		//        int type = custom.getCustomType();

		//????????????????????????customKey??????key????????????????????????
		StringBuilder customKeyStr = new StringBuilder();
		List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
		for (String key : customKeyList) {
			customKeyStr.append(",").append(key);
		}
		customKeyStr = new StringBuilder(customKeyStr.substring(0));

		Map<String, Object> paramMap = new HashMap<>(6);
		paramMap.put("companyName", companyName);
		paramMap.put("customkey", customKeyStr.toString());
		int total = customService.listCustomByCustomKeys(paramMap).size();
		int start = (pageNo - 1) * pageSize;
		paramMap.put("start", start);
		paramMap.put("limit", pageSize);
		List<ChannelCustom> channelCustomList = customService.listCustomByCustomKeys(paramMap);
		result.put("list", channelCustomList);
		result.put("total", total);
		return retModel(respstat, result);
	}

	/**
	 * ??????????????????,??????customkey ??????????????????????????????
	 */
	@RequestMapping(value = "/group/customs/excel")
	public void getGroupCustomList(HttpServletResponse response,
			HttpServletRequest request,
			@RequestParam("nodeId") int nodeId,
			@RequestParam(value = "companyName", required = false) String companyName,
			@RequestParam(required = false) int customType,
			@RequestParam(required = false) String customKey) throws Exception {
		//????????????????????????customKey??????key????????????????????????
		StringBuilder customKeyStr = new StringBuilder();
		if (customKey == null) {
			customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
		}

		ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);
		//????????????????????????????????????????????????
		if (channelCustom.getCustomType() == CustomType.PROXY.getCode() && channelCustom.getProxyType() == 1) {
			logger.info("??????????????????{}?????????????????????", channelCustom.getCompanyName());
			customType = CustomType.PROXYCHILDEN.getCode();
			OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customKey,null);
			nodeId = node.getId();
		}
		//        ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);
		//        int customType = channelCustom.getCustomType();
		List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
		for (String key : customKeyList) {
			customKeyStr.append(",").append(key);
		}
		customKeyStr = new StringBuilder(customKeyStr.substring(0));
		Map<String, Object> paramMap = new HashMap<>(6);
		paramMap.put("companyName", companyName);
		paramMap.put("customkey", customKeyStr.toString());
		List<ChannelCustom> channelCustomList = customService.listCustomByCustomKeys(paramMap);
		String today = DateUtils.getNowDay();
		ArrayList<String> dataStr = new ArrayList<>();
		for (ChannelCustom custom : channelCustomList) {
			StringBuilder strBuff = new StringBuilder();
			CustomType type = CustomType.codeOf(custom.getCustomType());
			String role = type == null ? "" : type.getDesc();
			strBuff.append(StringUtil.isEmpty(custom.getCompanyName()) ? "" : custom.getCompanyName()).append(",")
			.append(StringUtil.isEmpty(custom.getEmail()) ? "" : custom.getEmail()).append(",")
			.append(role).append(",")
			.append(StringUtil.isEmpty(custom.getCreateTime()) ? "" : custom.getCreateTime());
			dataStr.add(strBuff.toString());
		}
		ArrayList<String> fieldName = new ArrayList<>();
		fieldName.add("????????????");
		fieldName.add("????????????");
		fieldName.add("????????????");
		fieldName.add("????????????");
		String filename = today + "????????????";
		ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
	}

	/**
	 * ????????????
	 *
	 * @param request
	 * @param ChannelId
	 * @return
	 */
	@RequestMapping(value = "/custom/details.do")
	public @ResponseBody
	Map<String, Object> details(HttpServletRequest request, String ChannelId) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ChannelCustom channelCustom = customService.getCustomById(Integer.parseInt(ChannelId));
			result.put("channelCustom", channelCustom);
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "??????");
		return result;
	}

	/**
	 * ????????????--??????
	 *
	 * @param request
	 * @param name
	 * @param username
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/review/countData")
	public @ResponseBody
	Map<String, Object> getAllChannelCount(HttpServletRequest request, String name,
			String username, ModelMap model) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String customKey = (String) request.getSession().getAttribute("customkey");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("name", name);
			/***
			 * ????????? ?????????????????????
			 */
			if (!"mfkj".equals(customKey)) {
				paramMap.put("AgentId", customKey);
			}
			paramMap.put("enabled", 0);// ??????????????????
			paramMap.put("customType", 1);// ??????????????????
			int total = customService.getCustomListExRoot(paramMap).size();
			result.put("total", total);
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "??????");
		return result;

	}

	/**
	 * ????????????--??????
	 *
	 * @param pageIndex
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/review/listData")
	public @ResponseBody
	Map<String, Object> getAllChannelByPage(HttpServletRequest request,String name,
			@RequestParam(required = false, defaultValue = "1") Integer pageIndex,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();

		try {

			ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
			if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
				return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
			}

			Map<String, Object> paramMap = new HashMap<>(10);
			paramMap.put("name", name);
			// ??????????????????
			paramMap.put("enabled", 0);
			// ??????????????????
			paramMap.put("customType", 1);

//			int total = customService.getCustomListExRoot(paramMap).size();
//			paramMap.put("start", (Integer.parseInt(pageIndex) - 1) * pageSize);
//			paramMap.put("limit", pageSize);
			PageHelper.startPage(pageIndex, pageSize);
			List<ChannelCustom> channels = customService.getCustomListExRoot(paramMap);
			PageInfo<ChannelCustom> pageInfo = new PageInfo<>(channels);

			result.put("list", pageInfo.getList());
			result.put("total", pageInfo.getTotal());
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "??????");
		return result;
	}

	/**
	 * ??????
	 *
	 * @param ChannelId
	 * @param ChannelId
	 * @param enabled
	 * @return
	 */
	@RequestMapping(value = "/review/enabled")
	public @ResponseBody
	Map<String, Object> customReview(HttpServletRequest request,String ChannelId, String enabled) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if ("1".equals(enabled)) {
				ChannelCustom loginCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
				customService.checkCustom(Integer.parseInt(ChannelId), enabled,loginCustom.getUsername());
				ChannelCustom custom = customService.getCustomById(Integer.parseInt(ChannelId));
				logger.info("????????????????????????={}", customService.enableOrganizationRelation(custom.getCustomkey()));
				CustomMenu defaultMenu = new CustomMenu();
				defaultMenu.setIsParentNode(1);
				defaultMenu.setEnabled(1);
				defaultMenu.setOriginalId(custom.getCustomkey());
				defaultMenu.setContentLevel("1");
				defaultMenu.setContentName("????????????");
				defaultMenu.setIsShow(1);
				defaultMenu.setShowLevel(1);
				defaultMenu.setParentId(0);
				if (customMenuDao.savePermission(defaultMenu) == 1) {
					defaultMenu.setLevelCode(defaultMenu.getId() + "-");
					if (customMenuDao.updatePermission(defaultMenu) == 1) {
						logger.info("customKey={},??????????????????????????????????????????", custom.getCustomkey());
					}
				}
			} else {
				customService.deleteById(ChannelId);
			}
		} catch (Exception e) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, "??????");
		return result;
	}

	/**
	 * ????????????????????????
	 *
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/tranPassword/whether", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> deleteBatch(HttpServletResponse response, HttpServletRequest request) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
		ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (StringUtil.isEmpty(customkey) || loginUser == null) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				ChannelCustom custom = customService.getCustomById(loginUser.getId());
				if (StringUtil.isEmpty(custom.getTranPassword())) {
					model.put("whether", 0);
				} else {
					model.put("whether", 1);
				}
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error(e.getMessage(), e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * Author Nicholas-Ning
	 * Description //TODO ???????????????????????????????????????????????? ??????:
	 * Date 18:41 2018/11/23
	 * Param [request, model, companyName, customType, startTime, endTime, pageNo]
	 * return java.util.Map<java.lang.String,java.lang.Object>
	 **/
	@RequestMapping(value = "/customLevel/getCustomManageByPage")
	public @ResponseBody
	Map<String, Object> getCustomManageByPage(HttpServletRequest request, ModelMap model,
			String companyName, String customType, String startTime, String endTime, String pageNo) {
		int respstat = 1;
		Map<String, Object> result = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
		int pageSize = 10;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
		paramMap.put("limit", pageSize);
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("name", companyName);
		paramMap.put("enabled", 1);
		paramMap.put("customType", customType);
		if (!"mfkj".equals(customkey)) {
			paramMap.put("customkey", customkey);
		}
		List<ChannelCustom> customList = customService.getCustomList(paramMap);
		result.put("customList", customList);
		return retModel(respstat, result);
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????
	 */
	@RequestMapping(value = "/companyListData")
	public @ResponseBody
	Map<String, Object> companyListData(HttpServletRequest request, ModelMap model,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		String customkey = (String) request.getSession().getAttribute("customkey");
		String customName = (String) request.getParameter("customName");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
//		String pageSize = request.getParameter("pageSize") == null ? "10" : (String) request.getParameter("pageSize");
		logger.info("/companyListData??????  ????????? customkey =" + customkey);

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
				logger.error(e.getMessage(), e);
				return retModelMsg(RespCode.error107, "??????????????????????????????????????????", result);
			}
		}
		return retModel(respstat, result);
	}

	/**
	 * ???????????????????????????????????????
	 */
	@RequestMapping(value = "/originalForList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> originalForList(HttpServletRequest request, HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String originalId = (String) request.getParameter("originalId");
		logger.info("/originalForList ??????  ????????? originalId=" + originalId);
		try {
			if (StringUtil.isEmpty(originalId)) {
				return retModelMsg(RespCode.error107, "?????????????????????????????????", model);
			}
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("originalId", originalId);
			List<ChannelRelated> list = channelRelatedService.getRelatedByParam(param);
			List<ChannelRelated> relateds = new ArrayList<ChannelRelated>();
			for (ChannelRelated channelRelated : list) {
				ChannelRelated related = muStr(channelRelated);
				relateds.add(related);
			}
			model.put("list", relateds);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "??????????????????????????????????????????", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("???????????????" + model);
		return result;
	}

	public ChannelRelated muStr(ChannelRelated related) {
		String ServiceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "100", 3);
		String ProfiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "100", 3);
		String ProfiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "100", 3);
		related.setServiceRates(ServiceRate);
		related.setProfiltLower(ProfiltLower);
		related.setProfiltUpper(ProfiltUpper);
		return related;
	}

	/**
	 * ?????????????????????????????????--??????????????????
	 */
	@RequestMapping(value = "/merchantList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> merchantList(HttpServletRequest request, HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			List<CustomInfo> list = customInfoService.getAllActiveCustom();
			model.put("list", list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "??????????????????????????????????????????", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("???????????????" + result);
		return result;
	}

	/**
	 * ?????????????????????????????????--????????????????????????
	 */
	@RequestMapping(value = "/companyList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> companyListBychannelId(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String merchantId = (String) request.getParameter("merchantId");
		logger.info("/companyList ??????  ????????? customkey=" + merchantId);
		if (StringUtil.isEmpty(merchantId)) {
			return retModelMsg(RespCode.error107, "????????????????????????????????????", model);
		}
		try {
			List<User> list = userSerivce.getCompanyByMerchantId(merchantId);
			model.put("list", list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "??????????????????????????????????????????", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("???????????????" + result);
		return result;
	}

	/**
	 * ?????????????????????????????????--????????????
	 */
	@RequestMapping(value = "/updateCompany", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> updateCompany(HttpServletRequest request, HttpServletResponse response,
			ChannelRelated related) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customKey = (String) request.getSession().getAttribute("customkey");
		if (!"mfkj".equals(customKey) || StringUtil.isEmpty(related.getServiceRates())
				|| StringUtil.isEmpty(related.getProfiltLower()) || StringUtil.isEmpty(related.getProfiltUpper())) {
			return retModelMsg(RespCode.error107, "????????????????????????????????????", model);
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
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "??????????????????????????????????????????", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("???????????????" + result);
		return result;
	}

	/**
	 * ?????????????????????????????????--??????????????????????????????
	 */
	@RequestMapping(value = "/selectCompany", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> selectCompany(HttpServletRequest request, HttpServletResponse response,
			ChannelRelated related) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String customKey = (String) request.getSession().getAttribute("customkey");
		if (StringUtil.isEmpty(related.getMerchantId()) || StringUtil.isEmpty(related.getCompanyId())
				|| !"mfkj".equals(customKey) || StringUtil.isEmpty(related.getServiceRates())
				|| StringUtil.isEmpty(related.getProfiltLower()) || StringUtil.isEmpty(related.getProfiltUpper())
				|| StringUtil.isEmpty(related.getOriginalId())) {
			return retModelMsg(RespCode.error107, "????????????????????????????????????", model);
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("originalId", related.getOriginalId());
		param.put("companyId", related.getCompanyId());

		int countByappId = channelRelatedService.getRelatedCountByParam(param);
		if (countByappId > 0) {
			return retModelMsg(RespCode.error107, "?????????????????????????????????????????????", model);
		}
		try {
			String ServiceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "0.01", 3);
			String ProfiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "0.01", 3);
			String ProfiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "0.01", 3);
			related.setServiceRates(ServiceRate);
			related.setProfiltLower(ProfiltLower);
			related.setProfiltUpper(ProfiltUpper);
			related.setStatus(1);// ????????? ??????
			/**
			 * ?????????????????????????????????
			 */
			Map<String, Object> param2 = new HashMap<String, Object>();
			param2.put("originalId", related.getOriginalId());
			param2.put("status", "1");
			channelRelatedService.updateRelatedStatus(param2);
			/**
			 * ???????????????????????????
			 */
			channelRelatedService.createChannelRelated(related);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return retModelMsg(RespCode.error107, "??????????????????????????????????????????", model);
		}
		Map<String, Object> result = retModel(respstat, model);
		logger.info("???????????????" + result);
		return result;
	}

	/**
	 * ????????????
	 */
	@RequestMapping(value = "/originalEnable", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> originalEnable(HttpServletRequest request, HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String id = (String) request.getParameter("id");
		logger.info("/originalEnable ??????  ????????? id = " + id);
		try {
			ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
			if (channelRelated == null || StringUtil.isEmpty(id)) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "?????????????????????????????????");
				return model;
			}
			/**
			 * ?????????????????????????????????
			 */
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("originalId", channelRelated.getOriginalId());
			param.put("status", "1");
			channelRelatedService.updateRelatedStatus(param);
			channelRelated.setStatus(1);// ???????????????
			channelRelatedService.updateChannelRelated(channelRelated);
		} catch (Exception e) {
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> detail(HttpServletRequest request, HttpServletResponse response) {
		int respstat = 1;
		Map<String, Object> model = new HashMap<String, Object>();
		String id = (String) request.getParameter("id");
		logger.info("/detail ??????  ????????? id = " + id);
		try {
			ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
			if (channelRelated == null || StringUtil.isEmpty(id)) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "?????????????????????????????????");
				return model;
			}
			channelRelated = muStr(channelRelated);
			model.put("channelRelated", channelRelated);
		} catch (Exception e) {
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
			logger.error(e.getMessage(), e);
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ?????????????????? ??????:
	 *
	 * @param model
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/getAllPermission")
	public @ResponseBody
	HashMap<String, Object> getAllPermission(Model model, HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();

		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");// ??????

		String customKey = (String) request.getSession().getAttribute("customkey");// ????????????

		try {
			if ("mfkj".equals(customKey)) {
				allPermission = customPermissionService.getAllPermission();
			} else {
				allPermission = customPermissionService.getCustomPermissionReal(customLogin.getId());
			}
			for (int i = 0; i < allPermission.size(); i++) {
				ChannelPermission e = allPermission.get(i);
				// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("name", e.getContentName());
				//???????????????????????????????????????
				int childCount = customPermissionService.checkIsHaveChild(e.getId());
				if (childCount > 0) {
					map.put("hasChilden", 0);
				} else {
					map.put("hasChilden", 1);
				}
				mapList.add(map);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "????????????????????????????????????????????????");
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ?????????????????? ??????:
	 *
	 * @param model
	 * @param request
	 * @param id
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/getChannelUserPermission")
	public @ResponseBody
	HashMap<String, Object> getCustomPermission(Model model, HttpServletRequest request,
			String id) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();

		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");// ????????????

		String customKey = (String) request.getSession().getAttribute("customkey");// ??????
		// ??????map?????????????????????????????????????????????????????????
		Map<Integer, Integer> perIds = new HashMap<Integer, Integer>();
		// ??????map?????????????????????????????????????????????????????????
		Map<Integer, Integer> userIds = new HashMap<Integer, Integer>();

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();
		if ("mfkj".equals(customKey)) {
			allPermission = customPermissionService.getAllPermission();
		} else {
			allPermission = customPermissionService.getCustomPermissionReal(customLogin.getId());
		}
		List<ChannelPermission> customPermission = customPermissionService
				.getCustomPermissionReal(Integer.parseInt(id));
		for (int i = 0; i < allPermission.size(); i++) {
			ChannelPermission e = allPermission.get(i);
			// ???????????????????????????map???????????????????????????????????????0
			perIds.put(e.getId(), 0);
			// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			if (!"mfkj".equals(customKey)) {
				if (!"/custom/catalog/channelUser/getChannelUser.do".equals(e.getLink())
						|| !"/custom/catalog/channelUser/addChannelUser.do".equals(e.getLink())) {
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
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("name", e.getContentName());
				for (ChannelPermission permission : customPermission) {
					if (permission.getId() == e.getId()) {
						map.put("checked", true);
					}
					// ??????????????????????????????????????????????????????map??????
					userIds.put(permission.getId(), 0);
				}
				mapList.add(map);
			}
		}
		// ??????mapList,?????????????????????checked = true ??????????????????????????????
		List<Integer> removeKeys = new ArrayList<>();
		for (Map<String, Object> map : mapList) {
			if (!map.containsKey("checked")) {
				removeKeys.add((Integer) map.get("id"));
			}
		}
		// ????????????
		for (Integer integer : removeKeys) {
			removeNode(integer, mapList);
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info(mapList.toString());
		return result;
	}

	private void removeNode(Integer remove, List<Map<String, Object>> mapList) {
		for (Map<String, Object> map : mapList) {
			Integer deleteKey = (Integer) map.get("id");
			Integer pId = (Integer) map.get("pId");
			if (Integer.compare(deleteKey, remove) == 0) {
				if (map.containsKey("checked")) {
					map.remove("checked");
				}
				removeNode(pId, mapList);
			}
		}
	}

	/**
	 * ?????????????????? ??????????????????????????? permissionid=146 ??????id???????????????????????????????????????????????????????????????????????? ??????:
	 *
	 * @param model
	 * @param request
	 * @param customId
	 * @param ids
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/saveChannelUserPermission")
	public @ResponseBody
	HashMap<String, Object> saveCustomPermission(Model model, HttpServletRequest request,
			String customId, String ids) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		if (StringUtils.isEmpty(ids)) {
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		if (ids.charAt(0) == ',') {
			ids = ids.substring(1, ids.length());
		}
		String[] array = ids.split(",");

		try {
			customPermissionService.deleteCustomPermission(customId);
			customPermissionService.saveCustomPermission(customId, array);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "???????????????");
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ?????????????????? ??????:
	 *
	 * @param model
	 * @param request
	 * @param id
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/getPermissionDetail")
	public @ResponseBody
	HashMap<String, Object> getPermissionDetail(Model model, HttpServletRequest request,
			String id) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>(5);
		ChannelPermission permission = customPermissionService.getPermissionDetailById(id);
		result.put("permission", permission);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????????????????????????? ??????:
	 *
	 * @param model
	 * @param request,
	 * @param permiisson
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/savePermission")
	public @ResponseBody
	Map<String, Object> savePermission(Model model, HttpServletRequest request,
			ChannelPermission permiisson) {

		try {
			if (permiisson.getId() != 0) {
				customPermissionService.updatePermission(permiisson);
			} else {
				customPermissionService.savePermission(permiisson);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return returnFail(RespCode.error101, RespCode.UPDATE_FAILED);
		}
		return returnSuccess(permiisson);
	}

	/**
	 * ??????????????????
	 *
	 * @param menuSource
	 * @param request
	 * @return
	 */
	@RequestMapping("/channelUser/menuPermissionTemp")
	@ResponseBody
	public Map<String, Object> menuPermissionTemp(HttpServletRequest request, Integer menuSource) {
		Page page = new Page(request);
		if (menuSource != null && menuSource == 1) {
			//????????????
			Integer customId = Integer.parseInt(page.getParams().get("customId"));
			//???????????????key
			String customKey = customPermissionTemplateService.getCustomMaster(customId);
			page.getParams().put("customKey", customKey);
			page.getParams().put("type", "2");
		}
		//????????????
		Map<String, Object> result = new HashMap<String, Object>();
		int total = customPermissionTemplateService.getMenuTempCount(page);
		//??????????????????????????????????????????
		List<Map<String, Object>> tempList = customPermissionTemplateService.getList(page);
		result.put("total", total);
		result.put("list", tempList);
		return retModel(RespCode.success, result);
	}


	/**
	 * ?????????????????????
	 * @param customId
	 * @return
	 */
	@RequestMapping("/channelUser/getMasterMerChantName")
	@ResponseBody
	public Map<String, Object> getMasterMerChantName(Integer customId) {
		int respstat = RespCode.success;
		//????????????
		Map<String, Object> result = new HashMap<String, Object>();
		//???????????????key
		String customKey = customPermissionTemplateService.getCustomMaster(customId);
		//?????????????????????
		ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customKey);
		//??????????????????
		result.put("merchantName", masterChannelCustom.getCompanyName());
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????/??????????????????
	 *
	 * @param customPermissionTemplate
	 * @return
	 */
	@RequestMapping("/channelUser/savePermissionTemp")
	@ResponseBody
	public Map<String, Object> savePermissionTemp(CustomPermissionTemplate customPermissionTemplate, String ids, HttpServletRequest request, Integer menuSource,Integer customId) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		try {
			if (customPermissionTemplate.getId() == null) {
				if (menuSource != null && menuSource == 1) {
					//????????????
					customPermissionTemplate.setType(2);
				} else {
					customPermissionTemplate.setType(1);
				}
				String customKey = "";
				//???????????????key
				if (customId == null && CommonString.ROOT.equals(customLogin.getCustomkey())) {
					customKey = customLogin.getCustomkey();
				} else if (customId == null && CustomType.ROOT.getCode() == customLogin.getCustomType()
						&& CommonString.ROOT.equals(customLogin.getMasterCustom())
						&& LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()) {
					customKey = customLogin.getMasterCustom();
				} else {
					customKey = customPermissionTemplateService.getCustomMaster(customId);
				}
				customPermissionTemplate.setAddUser(customLogin.getUsername());
				customPermissionTemplate.setCustomKey(customKey);
				customPermissionTemplate.setMenuIds(ids);
				customPermissionTemplate.setStatus(1);
				customPermissionTemplate.setCreateTime(DateUtils.getNowDate());
				customPermissionTemplateService.insertPermissionTemplate(customPermissionTemplate);
			} else {
				if (customPermissionTemplate.getStatus() != null && customPermissionTemplate.getStatus() == 2) {
					//????????????
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("tempId", customPermissionTemplate.getId());
					int count = customPermissionTemplateService.checkCustomUseTemp(paramsMap);
					if (count > 0) {
						respstat = RespCode.CUSTOM_TEMP_USE;
					} else {
						//??????????????????
						customPermissionTemplate.setUpdateTime(DateUtils.getNowDate());
						customPermissionTemplateService.updateCustomPermissionTemp(customPermissionTemplate);
					}
				} else {
					//??????????????????
					customPermissionTemplate.setMenuIds(ids);
					customPermissionTemplate.setUpdateTime(DateUtils.getNowDate());
					customPermissionTemplateService.updateCustomPermissionTemp(customPermissionTemplate);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put(RespCode.RESP_STAT, RespCode.error107);
			result.put(RespCode.RESP_MSG, "????????????");
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ????????????????????????
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping("/channelUser/getPermissionTempDetail")
	@ResponseBody
	public Map<String, Object> getPermissionTempDetail(Integer id, HttpServletRequest request,Integer customId) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
			if (id != null) {
				//????????????id??????????????????
				CustomPermissionTemplate customPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(id);
				int type = customPermissionTemplate.getType();
				if (type == 2) {
					//???????????????
					String customKey = customPermissionTemplate.getCustomKey();
					ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);
					if ((CommonString.ROOT.equals(channelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == channelCustom.getCustomType() && CommonString.ROOT.equals(channelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
						allPermission = customPermissionService.getAllPermission();
					} else {
						allPermission = getCustomMenuTemp(channelCustom);
					}
				} else {
					//???????????????
					allPermission = customPermissionService.getAllPermission();
				}
			} else {
				//??????
				//???????????????
				allPermission = customPermissionService.getAllPermission();
			}
		} else {
			if(customId!=null){
				//???????????????key
				String customKey = customPermissionTemplateService.getCustomMaster(customId);
				//?????????????????????
				ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customKey);
				allPermission = getCustomMenuTemp(masterChannelCustom);
			}else{			
				allPermission = getCustomMenuTemp(customLogin);
			}
		}
		if (id != null) {
			//????????????id??????????????????
			CustomPermissionTemplate customPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(id);
			//?????????????????????????????????????????????
			for (ChannelPermission channelPermission : allPermission) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", channelPermission.getId());
				map.put("pId", channelPermission.getParentId());
				map.put("name", channelPermission.getContentName());
				map.put("description", channelPermission.getDescription());
				//???????????????????????????????????????
				int childCount = customPermissionService.checkIsHaveChild(channelPermission.getId());
				if (childCount > 0) {
					map.put("hasChilden", 0);
				} else {
					map.put("hasChilden", 1);
				}
				if (customPermissionTemplate != null && customPermissionTemplate.getMenuIds() != null) {
					for (String menu_id : customPermissionTemplate.getMenuIds().split(",")) {
						if (menu_id.equals(String.valueOf(channelPermission.getId()))) {
							map.put("checked", true);
						}
					}
				}
				mapList.add(map);
			}
			// ??????mapList,?????????????????????checked = true ??????????????????????????????
			List<Integer> removeKeys = new ArrayList<>();
			for (Map<String, Object> map : mapList) {
				if (!map.containsKey("checked")) {
					removeKeys.add((Integer) map.get("id"));
				}
			}
			// ????????????
			for (Integer integer : removeKeys) {
				removeNode(integer, mapList);
			}
		} else {
			//?????????????????????????????????????????????
			for (ChannelPermission channelPermission : allPermission) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", channelPermission.getId());
				map.put("pId", channelPermission.getParentId());
				map.put("name", channelPermission.getContentName());
				map.put("description", channelPermission.getDescription());
				//???????????????????????????????????????
				int childCount = customPermissionService.checkIsHaveChild(channelPermission.getId());
				if (childCount > 0) {
					map.put("hasChilden", 0);
				} else {
					map.put("hasChilden", 1);
				}
				mapList.add(map);
			}
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	@RequestMapping("/channelUser/getPermissionTempList")
	@ResponseBody
	public Map<String, Object> getPermissionTempList(Integer id, HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		if (id == null){
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
			return result;
		}
		List<Map<String, Object>> mapList = new ArrayList<>();
		List<ChannelPermission> allPermission = new ArrayList<>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
		if (isMFKJAccount(customLogin)) {
			//????????????id??????????????????
			CustomPermissionTemplate customPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(id);
			int type = customPermissionTemplate.getType();
			if (type == 2) {
				//???????????????
				String customKey = customPermissionTemplate.getCustomKey();
				ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);
				if ((CommonString.ROOT.equals(channelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == channelCustom.getCustomType() && CommonString.ROOT.equals(channelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
					allPermission = customPermissionService.getAllPermission();
				} else {
					allPermission = getCustomMenuTemp(channelCustom);
				}
			} else {
				//???????????????
				allPermission = customPermissionService.getAllPermission();
			}
		} else if (isPlatformAccount(customLogin)){
			//??????????????????
			CustomPermissionTemplate loginPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(id);
			allPermission = customPermissionService.getCustomPermissionByIds(loginPermissionTemplate.getMenuIds());
		}else{
			allPermission = getCustomMenuTemp(customLogin);
		}
		//????????????id??????????????????
		CustomPermissionTemplate customPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(id);
		//?????????????????????????????????????????????
		for (ChannelPermission channelPermission : allPermission) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", channelPermission.getId());
			map.put("pId", channelPermission.getParentId());
			map.put("name", channelPermission.getContentName());
			map.put("description", channelPermission.getDescription());
			//???????????????????????????????????????
			int childCount = customPermissionService.checkIsHaveChild(channelPermission.getId());
			if (childCount > 0) {
				map.put("hasChilden", 0);
			} else {
				map.put("hasChilden", 1);
			}
			if (customPermissionTemplate != null && customPermissionTemplate.getMenuIds() != null) {
				for (String menu_id : customPermissionTemplate.getMenuIds().split(",")) {
					if (menu_id.equals(String.valueOf(channelPermission.getId()))) {
						map.put("checked", true);
					}
				}
			}
			mapList.add(map);
		}
		// ??????mapList,?????????????????????checked = true ??????????????????????????????
		List<Integer> removeKeys = new ArrayList<>();
		for (Map<String, Object> map : mapList) {
			if (!map.containsKey("checked")) {
				removeKeys.add((Integer) map.get("id"));
			}
		}
		// ????????????
		for (Integer integer : removeKeys) {
			removeNode(integer, mapList);
		}
		result.put("list", mapList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}


	/**
	 * ??????????????????????????????
	 *
	 * @param customLogin
	 * @return
	 */
	private List<ChannelPermission> getCustomMenuTemp(ChannelCustom customLogin) {
		List<ChannelPermission> allPermission;
		//???????????????
		Map<String, Object> typeMap = customPermissionService.getTempTypeByCustomId(customLogin.getId());
		Integer type = typeMap == null ? 2 : Integer.parseInt(String.valueOf(typeMap.get("type")));
		if (type == 1) {
			//??????
			allPermission = customPermissionService.getCustomPermissionReal(customLogin.getId());
		} else {
			//????????????
			//??????????????????(??????????????????????????????????????????)
			List<String> tempIdList = customPermissionService.getCustomPermissionTemp(customLogin.getId());
			StringBuffer sb = new StringBuffer();
			Set<String> menuIdSet = new HashSet<String>();
			for (String tempId : tempIdList) {
				//??????????????????
				CustomPermissionTemplate loginPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(Integer.parseInt(tempId));
				String menuIds = loginPermissionTemplate.getMenuIds();
				String[] menuIdArray = menuIds == null ? new String[]{} : menuIds.split(",");
				for (String menuId : menuIdArray) {
					menuIdSet.add(menuId);
				}
			}
			Iterator<String> itr = menuIdSet.iterator();
			while (itr.hasNext()) {
				sb.append(itr.next()).append(",");
			}
			String menuIds = sb.length() < 1 ? "" : sb.substring(0, sb.length() - 1);
			allPermission = customPermissionService.getCustomPermissionByIds(menuIds);
		}
		return allPermission;
	}

	/**
	 * ????????????????????????????????????
	 *
	 * @return
	 */
	@RequestMapping("/channelUser/getPermissionTempType")
	@ResponseBody
	public Map<String, Object> getCustomerTempType(Integer customId, HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		//??????????????????
		ChannelCustom channelCustom = customService.getCustomById(customId);
		Map<String, Object> typeMap = null;
		if (channelCustom.getCustomType() == 4) {
			//?????????
			//???????????????key
			String customKey = channelCustom.getMasterCustom();
			//?????????????????????
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customKey);
			//??????????????????????????????????????????(1.??????????????????????????????,2.??????????????????????????????)
			typeMap = customPermissionService.getTempTypeByCustomId(masterChannelCustom.getId());
		} else {
			//??????????????????????????????????????????(1.??????????????????????????????,2.??????????????????????????????)
			typeMap = customPermissionService.getTempTypeByCustomId(channelCustom.getId());
		}
		Integer type = typeMap == null ? 2 : Integer.parseInt(String.valueOf(typeMap.get("type")));
		//?????????????????????????????????1.?????????2.?????????
		result.put("type", type);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????????????????(?????????)
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/channelUser/getCustomPermissionTempOptions")
	@ResponseBody
	public HashMap<String, Object> getCustomPermissionTempOptions(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		page.getParams().put("type", "2");
		int total = customPermissionService.getPermissionTempMappingCount(page);
		//??????????????????????????????????????????.
		List<Map<String, Object>> relationList = customPermissionService.getPermissionTempMapping(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put("customId", page.getParams().get("customId"));
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/channelUser/getCustomPermissionTemplateSelect")
	@ResponseBody
	public HashMap<String, Object> getCustomPermissionTemplateSelect(HttpServletRequest request, Integer customId, Integer customType, Integer menuSource) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		CustomPermissionTemplate customPermissionTemplate = new CustomPermissionTemplate();
		ChannelCustom channelCustom = new ChannelCustom();
		String customKey = "";
		List<CustomPermissionTemplate> tempList = new ArrayList<CustomPermissionTemplate>();
		if (menuSource == 1) {
			//????????????
			customPermissionTemplate.setType(2);
			//???????????????key
			customKey = customPermissionTemplateService.getCustomMaster(customId);
			//?????????????????????
			channelCustom = customService.getCustomByCustomkey(customKey);
			//??????????????????
			result.put("merchantName", channelCustom.getCompanyName());
		} else if (menuSource == 2) {
			//????????????
			if (customType != null && customType == 4) {
				//???????????????
				customPermissionTemplate.setType(2);
				//???????????????key
				customKey = customPermissionTemplateService.getCustomMaster(customId);
				//?????????????????????
				channelCustom = customService.getCustomByCustomkey(customKey);
				result.put("merchantName", channelCustom.getCompanyName());
			} else {
				if(!ObjectUtils.isEmpty(customId)) {
					//????????????
					customPermissionTemplate.setType(1);
					customKey = (String) request.getSession().getAttribute("customkey");
					//??????????????????
					channelCustom = customService.getCustomById(customId);
					result.put("merchantName", channelCustom.getCompanyName());
				}
			}
		} else if (menuSource == 3) {
			//????????????
			customPermissionTemplate.setType(1);
			customKey = (String) request.getSession().getAttribute("customkey");
		}
		customPermissionTemplate.setCustomKey(customKey);

		if(menuSource != 1){
			/**
			 * @Description ???????????????????????????????????????
			 **/
			ChannelCustom checkChannel = (ChannelCustom) request.getSession().getAttribute("customLogin");
			Integer platformId = checkCustom(checkChannel);
			if(!ObjectUtils.isEmpty(platformId)) {
				if(menuSource == 2 && customType != null && customType == 4){

					customPermissionTemplate = new CustomPermissionTemplate();
					customPermissionTemplate.setType(2);
					customPermissionTemplate.setCustomKey(customKey);
				}else {
					customPermissionTemplate = new CustomPermissionTemplate();
					customPermissionTemplate.setType(1);
					/**
					 * @Description custom_id
					 **/
					customPermissionTemplate.setId(platformId);
				}

			}
		}

		//?????????????????????????????????
		tempList = customPermissionTemplateService.getListByPojo(customPermissionTemplate);
		result.put("type", customPermissionTemplate.getType());
		result.put("tempList", tempList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ????????????????????????
	 *
	 * @return
	 */
	@RequestMapping("/channelUser/saveCustomPermissionTempMapping")
	@ResponseBody
	public Map<String, Object> saveCustomPermissionTempMapping(HttpServletRequest request, String customId, String tempId, Integer switchType) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("customId", customId);
		paramsMap.put("tempId", tempId);
		int oldCount = customPermissionTemplateService.checkCustomOldPermission(paramsMap);
		if ((switchType != null && switchType == 1) || oldCount > 0) {
			//???????????????????????????????????????????????????????????????
			customPermissionTemplateService.deleteCustomPermissionRelationOld(paramsMap);
		}
		try {
			paramsMap.put("type", 2);
			int count = customPermissionTemplateService.getCustomPermissionRelationTempCount(paramsMap);
			if (count > 0) {
				respstat = RespCode.CUSTOM_TEMP_EXISTS;
			} else {
				paramsMap.put("create_time", DateUtils.getNowDate());
				paramsMap.put("add_user", customLogin.getUsername());
				customPermissionTemplateService.insertCustomPermissionRelation(paramsMap);
			}
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
			respstat = RespCode.error101;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param customId
	 * @param tempId
	 * @return
	 */
	@RequestMapping("/channelUser/deleteCustomPermissionTempMapping")
	@ResponseBody
	public Map<String, Object> deleteCustomPermissionTempMapping(String customId, String tempId) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("customId", customId);
		paramsMap.put("tempId", tempId);
		try {
			customPermissionTemplateService.deleteCustomPermissionRelation(paramsMap);
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
			respstat = RespCode.error101;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param customId
	 * @param menuSource
	 * @return
	 */
	@RequestMapping("/channelUser/switchMenuType")
	@ResponseBody
	public Map<String, Object> switchMenuType(HttpServletRequest request, Integer customId, Integer menuSource) {
		Map<String, Object> result = new HashMap<>();
		if (menuSource != null && menuSource == 1) {
			//????????????
			//????????????????????????
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			if (!(CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
				//?????????????????????
				ChannelCustom childChannelCustom = customService.getCustomById(customId);
				String masterCustomKey = childChannelCustom.getMasterCustom();
				//?????????????????????
				ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(masterCustomKey);
				//???????????????
				Map<String, Object> typeMap = customPermissionService.getTempTypeByCustomId(masterChannelCustom.getId());
				if (typeMap != null && Integer.parseInt(String.valueOf(typeMap.get("type"))) == 2) {
					result.put(RespCode.RESP_STAT, RespCode.success);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
				} else {
					result.put(RespCode.RESP_STAT, "327");
					result.put(RespCode.RESP_MSG, "????????????????????????????????????????????????");
				}
			} else {
				result.put(RespCode.RESP_STAT, RespCode.success);
				result.put(RespCode.RESP_MSG, "??????????????????????????????????????????????????????");
			}
		} else {
			//????????????
			ChannelCustom channelCustom = customService.getCustomById(customId);
			Integer customType = channelCustom.getCustomType();
			if (customType != null && customType == 4) {
				//?????????
				String masterCustomKey = channelCustom.getMasterCustom();
				//?????????????????????
				ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(masterCustomKey);
				if (!(CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
					Map<String, Object> typeMap = customPermissionService.getTempTypeByCustomId(masterChannelCustom.getId());
					if (typeMap != null && Integer.parseInt(String.valueOf(typeMap.get("type"))) != 2) {
						result.put(RespCode.RESP_STAT, "327");
						result.put(RespCode.RESP_MSG, "????????????????????????????????????????????????");
					} else {
						result.put(RespCode.RESP_STAT, RespCode.success);
						result.put(RespCode.RESP_MSG, "??????????????????????????????????????????????????????");
					}
				} else {
					result.put(RespCode.RESP_STAT, RespCode.success);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
				}
			} else {
				//?????????
				result.put(RespCode.RESP_STAT, RespCode.success);
				result.put(RespCode.RESP_MSG, "??????????????????????????????????????????????????????????????????");
			}

		}
		return result;
	}


}
