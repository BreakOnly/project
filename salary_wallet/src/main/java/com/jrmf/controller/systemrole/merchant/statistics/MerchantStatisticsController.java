package com.jrmf.controller.systemrole.merchant.statistics;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.bankapi.TransHistoryRecord;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.*;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Controller
@RequestMapping("/merchant/statistics")
public class MerchantStatisticsController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(MerchantStatisticsController.class);
	@Autowired
	protected UserSerivce userSerivce;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private ChannelHistoryService channelHistoryService;
	@Autowired
	private TransferBankService transferBankService;
	@Autowired
	private UserRelatedService userRelatedService;
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private PingAnBankService pingAnBankServiceImpl;
	@Autowired
	private OrganizationTreeService organizationTreeService;
	@Autowired
	private CustomProxyDao customProxyDao;
	@Autowired
	private LdOrderStepService ldOrderStepService;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private UtilCacheManager utilCacheManager;
	@Autowired
	private BaseInfo baseInfo;
	@Autowired
	private CustomLdConfigService customLdConfigService;
	@Autowired
	private CustomBalanceDao customBalanceDao;
	@Autowired
	private CustomLimitConfService customLimitConfService;
	@Autowired
	private TransferDealStatusNotifier transferDealStatusNotifier;
	@Autowired
	private LdOrderCorrectService ldOrderCorrectService;

	/**
	 * ??????????????????
	 *
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/openAccount/userList", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> companyUserList(HttpServletResponse response, HttpServletRequest request,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute("customkey");// ????????????
		Map<String, Object> model = new HashMap<String, Object>();
		String userName = (String) request.getParameter("userName");
		String userType = (String) request.getParameter("userType");
		String batcheId = (String) request.getParameter("batcheId");
//		String pageNo = (String) request.getParameter("pageNo");
		if (StringUtil.isEmpty(originalId) ||  StringUtil.isEmpty(batcheId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("userType", userType);
				paramMap.put("batcheId", batcheId);
				paramMap.put("userName", userName);
//				int total = userSerivce.getUserRelatedCountByParam(paramMap);
//				int pageSize = 10;
//				if (StringUtils.isEmpty(pageNo)) {
//					pageNo = "1";
//				}
//				paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//				paramMap.put("limit", pageSize);
				PageHelper.startPage(pageNo, pageSize);
				List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
				PageInfo<User> pageInfo = new PageInfo<>(userList);

				model.put("userList", pageInfo.getList());
				model.put("total", pageInfo.getTotal());

			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ????????????????????????
	 *
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/openAccount/deleteUser", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> deleteUser(HttpServletResponse response, HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
		String ids = (String) request.getParameter("ids");
		String batchId = (String) request.getParameter("batchId");
		logger.info("deleteUser??????  ????????? ids=" + ids + "customkey=" + customkey);
		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(ids)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				User user = userSerivce.getUserByUserId(Integer.parseInt(ids));
				userSerivce.deleteByIds(ids);
				transferBankService.deleteByUserIds(ids);
				userRelatedService.deleteByOriginalId(ids, customkey);
				UserBatch userBatch = userSerivce.getUserBatchByBatchId(batchId);
				int passnum = userBatch.getPassNum();
				int errornum = userBatch.getErrorNum();
				int batchnum = userBatch.getBatchNum();
				if (user.getUserType() == 11) {
					passnum = passnum - 1;
				} else if (user.getUserType() == 0) {
					errornum = errornum - 1;
				} else if (user.getUserType() == 1) {
					passnum = passnum - 1;
				}
				batchnum = batchnum - 1;
				userSerivce.updateUserBatch(batchId, passnum, batchnum, errornum);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ??????????????????
	 *
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/commission/deleteBatch", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionDeleteBatch(HttpServletResponse response,
			HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);
		// ????????????
		String customkey = (String) request.getSession().getAttribute("customkey");
		String batchId = (String) request.getParameter("batchId");
		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				commissionService.deleteByBatchId(batchId);
				channelHistoryService.deleteByOrderno(batchId);
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
	 * ????????????--????????????????????????
	 *
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/company/deleteCommission", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> deleteCommission(HttpServletResponse response,
			HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);
		// ????????????
		String originalId = (String) request.getSession().getAttribute("customkey");
		// ??????id
		String id = (String) request.getParameter("id");
		// ??????Id
		String batchId = (String) request.getParameter("batchId");
		logger.info("deleteCommission??????  ????????? id=" + id);
		if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(id) || StringUtil.isEmpty(batchId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				commissionService.deleteById(Integer.parseInt(id));
				/**
				 * ????????????????????????
				 */
				commissionService.updateBatchMessage(batchId, originalId, model);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/batch/listData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> ChannelHistory(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute("customkey");// ????????????
		Map<String, Object> model = new HashMap<String, Object>();
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
		String name = (String) request.getParameter("name");
		String batchName = (String) request.getParameter("batchName");// ????????????
		String batchDesc = (String) request.getParameter("batchDesc");// ????????????
		String status = (String) request.getParameter("status");// ????????????
		String payType = (String) request.getParameter("payType");// ????????????
		String amount = (String) request.getParameter("amount");// ???????????????
		String contentName = (String) request.getParameter("contentName");// ????????????-->?????????????????????custom_menu???????????????id(menuId)
		String fileName = (String) request.getParameter("fileName");// ??????????????????
		String recCustomkey = (String) request.getParameter("recCustomkey");// ????????????id
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("batchName", batchName);
		parameterMap.put("batchDesc", batchDesc);
		parameterMap.put("status", status);
		parameterMap.put("payType", payType);
		parameterMap.put("amount", amount);
		if (!StringUtils.isEmpty(contentName)) {
			List<CustomMenu> customMenuByName = customService.getCustomMenuByName(originalId, contentName);
			String menuIds = "";
			for (CustomMenu customMenu : customMenuByName) {
				menuIds += customMenu.getId() + ",";
			}
			menuIds = menuIds.substring(0, menuIds.length() - 1);// ?????????????????????
			parameterMap.put("menuId", menuIds);
		}
		parameterMap.put("fileName", fileName);
		parameterMap.put("recCustomkey", recCustomkey);
		logger.info("/channel/ChannelHistory??????  ????????? customkey=" + originalId + "startTime=" + startTime + "endTime="
				+ endTime + "pageNo=" + pageNo + "status=" + status);
		if (StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("customkey", originalId);
				paramMap.put("transfertype", 2);
				paramMap.put("name", name);
				paramMap.put("startTime", startTime);
				paramMap.put("endTime", endTime);

				for (Entry<String, Object> entry : parameterMap.entrySet()) {
					if (!StringUtils.isEmpty((String) entry.getValue())) {
						paramMap.put(entry.getKey(), entry.getValue());
					}
				}
//				int total = channelHistoryService.getChannelHistoryByParam(paramMap).size();
//				int pageSize = 10;
//				if (StringUtils.isEmpty(pageNo)) {
//					pageNo = "1";
//				}
//				paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//				paramMap.put("limit", pageSize);
				PageHelper.startPage(pageNo, pageSize);
				List<ChannelHistory> list = channelHistoryService.getChannelHistoryByParam(paramMap);
				PageInfo<ChannelHistory> pageInfo = new PageInfo<>(list);

				model.put("list", pageInfo.getList());
				model.put("total", pageInfo.getTotal());
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ????????????????????????
	 */
	@RequestMapping(value = "/batch/summaryData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> summaryData(HttpServletRequest request, HttpServletResponse response) {
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute("customkey");// ????????????
		Map<String, Object> model = new HashMap<String, Object>();
		String batchId = (String) request.getParameter("batchId");
		if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
				if (history == null) {
					history = channelHistoryService.getChannelHistoryByOrderno(batchId);
					CustomMenu customMenu = customService.getCustomMenuById(history.getMenuId());
					if (customMenu != null) {
						model.put("customMenu", customMenu);
					}
				}
				model.put("history", history);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/batch/detailData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String batchId = (String) request.getParameter("batchId");
		String userName = (String) request.getParameter("userName");// ?????????
		String status = (String) request.getParameter("status");// ????????????
		String certId = (String) request.getParameter("cretId");// ????????????
		String BankNo = (String) request.getParameter("BankNo");// ????????????
		String amount = (String) request.getParameter("amount");// ???????????????
//		String pageNo = (String) request.getParameter("pageNo");
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("batchId", batchId);
		parameterMap.put("userName", userName);
		parameterMap.put("status", status);
		parameterMap.put("certId", certId);
		parameterMap.put("alipayAccount", BankNo);
		parameterMap.put("amount", amount);
		if (StringUtil.isEmpty(batchId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("batchId", batchId);
				for (Entry<String, Object> entry : parameterMap.entrySet()) {
					if (!StringUtils.isEmpty(entry.getKey())) {
						paramMap.put(entry.getKey(), entry.getValue());
					}
				}
//				int pageSize = 10;
//				int total = commissionService.getUserCommissionByParam(paramMap).size();
//				if (StringUtils.isEmpty(pageNo)) {
//					pageNo = "1";
//				}
//				paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//				paramMap.put("limit", pageSize);

				PageHelper.startPage(pageNo, pageSize);
				List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
				PageInfo<UserCommission> pageInfo = new PageInfo<>(list);
				model.put("list", pageInfo.getList());
				model.put("total", pageInfo.getTotal());
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		return model;
	}

	/**
	 * ??????????????????--??????
	 *
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/batch/exportDetailData")
	public void exportDetailData(ModelMap model, String batchId, String certId, String status, String userName,
			String companyId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("batchId", batchId);
		paramMap.put("status", status);
		paramMap.put("certId", certId);
		paramMap.put("userName", userName);
		List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);

		String today = DateUtils.getNowDay();
		ArrayList<String> dataStr = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			UserCommission user = list.get(i);
			StringBuffer strBuff = new StringBuffer();
			String statusDesc = "";

			int status1 = user.getStatus();
			if (status1 == 0) {
				statusDesc = "????????????";
			} else if (status1 == 1) {
				statusDesc = "????????????";
			} else if (status1 == 2) {
				statusDesc = "????????????-" + user.getRemark();
			} else if (status1 == 3) {
				statusDesc = "?????????";
			}
			strBuff.append(user.getUserName() == null ? "" : user.getUserName()).append(",")
			.append(user.getAccount() == null ? "" : user.getAccount()).append(",")
			.append(user.getCertId() == null ? "" : user.getCertId()).append(",")
			.append(user.getAmount() == null ? "" : user.getAmount()).append(",")
			.append(user.getOrderNo() == null ? "" : user.getOrderNo()).append(",").append(statusDesc);

			dataStr.add(strBuff.toString());
		}
		ArrayList<String> fieldName = new ArrayList<String>();
		fieldName.add("??????");
		fieldName.add("????????????/???????????????");
		fieldName.add("???????????????");
		fieldName.add("???????????????");
		fieldName.add("?????????");
		fieldName.add("????????????");
		fieldName.add("??????");
		String filename = today + "??????????????????????????????";
		ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
	}

	/**
	 * ????????????
	 */
	@RequestMapping(value = "/user/commissionData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionDetail(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String originalId = (String) request.getSession().getAttribute("customkey");// ????????????
		String name = (String) request.getParameter("name");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
		logger.info("/user/commissionData ??????  ????????? customkey=" + originalId);
		if (StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("originalId", originalId);
				paramMap.put("name", name);
				paramMap.put("startTime", startTime);
				paramMap.put("endTime", endTime);
//				int total = commissionService.getUserCommissionedByParam(paramMap).size();
//				int pageSize = 10;
//				if (StringUtils.isEmpty(pageNo)) {
//					pageNo = "1";
//				}
//				paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//				paramMap.put("limit", pageSize);
				PageHelper.startPage(pageNo, pageSize);
				List<UserCommission> list = commissionService.getUserCommissionedByParam(paramMap);
				PageInfo<UserCommission> pageInfo = new PageInfo<>(list);
				model.put("list", pageInfo.getList());
				model.put("total", pageInfo.getTotal());
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ????????????--????????????
	 */
	@RequestMapping(value = "/user/commissionSumData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionSumData(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();

		String customkey = (String) request.getSession().getAttribute("customkey");// ????????????????????????
		String name = (String) request.getParameter("name");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
		logger.info("/user/commissionSumData ??????  ????????? customkey=" + customkey);
		if (StringUtil.isEmpty(customkey)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "??????????????????");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("name", name);
				paramMap.put("startTime", startTime);
				paramMap.put("endTime", endTime);
				paramMap.put("status", 1);
				paramMap.put("originalId", customkey);
				// ???????????????????????????
				String successAmount = commissionService.getUserCommissionSum(paramMap);
				paramMap.put("status", 3);
				// ???????????????????????????
				String waitAmount = commissionService.getUserCommissionSum(paramMap);
				paramMap.put("status", 2);
				// ???????????????????????????
				String failureAmount = commissionService.getUserCommissionSum(paramMap);
				model.put("successAmount", successAmount == null ? "0.00" : successAmount);
				model.put("waitAmount", waitAmount == null ? "0.00" : waitAmount);
				model.put("failureAmount", failureAmount == null ? "0.00" : failureAmount);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "??????");
		logger.info("???????????????" + model);
		return model;
	}

	/**
	 * ??????????????????????????????---??????????????????
	 */
	@RequestMapping(value = "/user/dealRecord")
	public @ResponseBody
	Map<String, Object> dealRecord(HttpSession session,
			@RequestParam(required = false) String certId,
			@RequestParam(required = false) String batchName,
			@RequestParam(required = false) String menuId,
			@RequestParam(required = false) String userName,
			@RequestParam(required = false) String tradeTimeStart,
			@RequestParam(required = false) String tradeTimeEnd,
			@RequestParam(required = false) String batchDesc,
			@RequestParam(required = false, defaultValue = "0") Integer payType,
			@RequestParam(required = false, defaultValue = "0") Integer companyId,
			@RequestParam(required = false, defaultValue = "1") Integer pageNo,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>(5);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String customkey = (String) session.getAttribute(CommonString.CUSTOMKEY);
		try {
			String menuIds = "";
			menuIds = getMenuIds(menuId, menuIds);
			Map<String, Object> param = new HashMap<>(20);
			param.put("customkey", customkey);
			param.put("certId", certId);
			param.put("menuIds", menuIds);
			param.put("batchName", batchName);
			param.put("userName", userName);
			param.put("tradeTimeStart", tradeTimeStart);
			param.put("tradeTimeEnd", tradeTimeEnd);
			param.put("batchDesc", batchDesc);
			param.put("payType", payType);
			param.put("companyId", companyId);


			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> userDealRecord = commissionService.getUserDealRecord(param);
			PageInfo page = new PageInfo(userDealRecord);
			result.put("total", page.getTotal());
			result.put("userDealDetail", page.getList());
//			int total = commissionService.getUserDealRecord(param).size();
//			result.put("total", total);
//			param.put("start", (pageNo - 1) * pageSize);
//			param.put("limit", pageSize);
//			result.put("userDealDetail", userDealRecord);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	/**
	 * ??????????????????????????????---????????????????????????
	 */
	@RequestMapping(value = "/user/dealDetail")
	public @ResponseBody
	Map<String, Object> getDealDetail(HttpServletRequest request, String userId, String menuId,
			@RequestParam(required = false, defaultValue = "0") Integer companyId,
			@RequestParam(required = false, defaultValue = "1") Integer pageNo,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(required = false) String certId,
			@RequestParam(required = false) String batchName,
			@RequestParam(required = false) String userName,
			@RequestParam(required = false) String tradeTimeStart,
			@RequestParam(required = false) String tradeTimeEnd,
			@RequestParam(required = false) String batchDesc,
			@RequestParam(required = false, defaultValue = "0") Integer payType) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>(5);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String originalId = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
		try {
			String menuIds = "";
			menuIds = getMenuIds(menuId, menuIds);
			Map<String, Object> param = new HashMap<>(20);
			param.put("originalId", originalId);
			param.put("userId", userId);
			param.put("menuIds", menuIds);
			param.put("batchName", batchName);
			param.put("userName", userName);
			param.put("tradeTimeStart", tradeTimeStart);
			param.put("tradeTimeEnd", tradeTimeEnd);
			param.put("batchDesc", batchDesc);
			param.put("payType", payType);
			param.put("certId", certId);
			param.put("companyId", companyId);

			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> userDealDetail = commissionService.getUserDealDetail(param);
			PageInfo page = new PageInfo(userDealDetail);
			result.put("total", page.getTotal());
			result.put("userDealDetail", page.getList());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	/**
	 * ??????????????????????????????
	 */
	@RequestMapping(value = "/user/commissionDetailResult", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionDetailResult(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String originalId = (String) request.getSession().getAttribute("customkey");
		String userName = request.getParameter("userName");// ????????????
		String batchName = request.getParameter("batchName");// ????????????
		String menuId = request.getParameter("menuId");// ????????????
		String certId = request.getParameter("certId");// ????????????
		String createTimeStart = request.getParameter("createTimeStart");// ??????????????????
		String createTimeEnd = request.getParameter("createTimeEnd");// ??????????????????
		String batchDesc = request.getParameter("batchDesc");// ????????????
		String contentName = request.getParameter("contentName");// ????????????
		String account = request.getParameter("account");// ????????????
		String payType = request.getParameter("payType");// ????????????
		if ("0".equals(payType)) {
			payType = null;
		}
		String companyId = request.getParameter("companyId");// ????????????
		String status = request.getParameter("status");// ????????????
		String amountStart = request.getParameter("amountStart");// ??????????????????
		String amountEnd = request.getParameter("amountEnd");// ??????????????????
		try {
			String menuIds = "";
			menuIds = getMenuIds(menuId, menuIds);
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("originalId", originalId);
			param.put("userName", userName);
			param.put("batchName", batchName);
			param.put("menuIds", menuIds);
			param.put("certId", certId);
			param.put("createTimeStart", createTimeStart);
			param.put("createTimeEnd", createTimeEnd);
			param.put("batchDesc", batchDesc);
			param.put("contentName", contentName);
			param.put("account", account);
			param.put("payType", payType);
			param.put("companyId", companyId);
			param.put("status", status);
			param.put("amountStart", StringUtil.isEmpty(amountStart) ? "" : Double.parseDouble(amountStart));
			param.put("amountEnd", StringUtil.isEmpty(amountEnd) ? "" : Double.parseDouble(amountEnd));

			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> commissionDetailResult = commissionService.commissionDetailResult(param);
			PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);

			result.put("total", pageInfo.getTotal());
			result.put("commissionDetailResult", pageInfo.getList());
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	/**
	 * ??????????????????????????????
	 */
	@RequestMapping(value = "/group/commissionDetail", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> groupCommissionDetailResult(HttpServletRequest request,
			// ????????????
			@RequestParam(value = "userName", required = false) String userName,
			// ????????????
			@RequestParam(value = "batchName", required = false) String batchName,
			// ????????????
			@RequestParam(value = "menuId", required = false) String menuId,
			// ????????????
			@RequestParam(value = "certId", required = false) String certId,
			// ??????????????????
			@RequestParam(value = "createTimeStart", required = false) String createTimeStart,
			// ??????????????????
			@RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
			// ????????????
			@RequestParam(value = "batchDesc", required = false) String batchDesc,
			// ????????????
			@RequestParam(value = "contentName", required = false) String contentName,
			// ????????????
			@RequestParam(value = "account", required = false) String account,
			// ????????????
			@RequestParam(value = "payType", required = false) String payType,
			// ????????????
			@RequestParam(value = "companyId", required = false) String companyId,
			// ????????????
			@RequestParam(value = "status", required = false) String status,
			// ??????????????????
			@RequestParam(value = "amountStart", required = false) String amountStart,
			// ??????????????????
			@RequestParam(value = "amountEnd", required = false) String amountEnd,
			// ????????????
			@RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
			// ????????????
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam("nodeId") int nodeId,
			@RequestParam("customType") int customType,
			@RequestParam(required = false) String customKey,
			@RequestParam(required = false) String customName) {

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
		String queryMode = QueryType.QUERY_CURRENT_AND_CHILDREN;
		List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, queryMode, nodeId);

		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>(6);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
		int loginRole = channelCustom.getLoginRole();
		String menuIds = "";
		menuIds = getMenuIds(menuId, menuIds);
		Map<String, Object> param = new HashMap<>(25);
		if (LoginRole.OPERATE_ACCOUNT.getCode() == loginRole) {
			param.put("operatorName", channelCustom.getUsername());
		}
		param.put("originalIds", customKeyList);
		param.put("userName", userName);
		param.put("batchName", batchName);
		param.put("menuIds", menuIds);
		param.put("certId", certId);
		param.put("createTimeStart", createTimeStart);
		param.put("createTimeEnd", createTimeEnd);
		param.put("batchDesc", batchDesc);
		param.put("contentName", contentName);
		param.put("account", account);
		param.put("payType", payType);
		param.put("companyId", companyId);
		param.put("status", status);
		param.put("customName", customName);
		param.put("amountStart", StringUtil.isEmpty(amountStart) ? "" : Double.parseDouble(amountStart));
		param.put("amountEnd", StringUtil.isEmpty(amountEnd) ? "" : Double.parseDouble(amountEnd));
		PageHelper.startPage(pageNo,pageSize);
		List<UserCommission> commissionDetailResult = commissionService.listCommissionByCustomKeys(param);
		PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);

		result.put("total", pageInfo.getTotal());
		result.put("commissionDetailResult", pageInfo.getList());
		return result;
	}

	/**
	 * ??????????????????????????????
	 */
	@RequestMapping(value = "/group/commissionDetail/excel")
	public void groupCommissionDetailResultExcel(HttpServletRequest request,
			HttpServletResponse response,
			// ????????????
			@RequestParam(value = "userName", required = false) String userName,
			// ????????????
			@RequestParam(value = "batchName", required = false) String batchName,
			// ????????????
			@RequestParam(value = "menuId", required = false) String menuId,
			// ????????????
			@RequestParam(value = "certId", required = false) String certId,
			// ??????????????????
			@RequestParam(value = "createTimeStart", required = false) String createTimeStart,
			// ??????????????????
			@RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
			// ????????????
			@RequestParam(value = "batchDesc", required = false) String batchDesc,
			// ????????????
			@RequestParam(value = "contentName", required = false) String contentName,
			// ????????????
			@RequestParam(value = "account", required = false) String account,
			// ????????????
			@RequestParam(value = "payType", required = false) String payType,
			// ????????????
			@RequestParam(value = "companyId", required = false) String companyId,
			// ????????????
			@RequestParam(value = "status", required = false) String status,
			// ??????????????????
			@RequestParam(value = "amountStart", required = false) String amountStart,
			// ??????????????????
			@RequestParam(value = "amountEnd", required = false) String amountEnd,
			@RequestParam("nodeId") int nodeId,
			@RequestParam("customType") int customType,
			@RequestParam(required = false) String customKey,
			@RequestParam(required = false) String customName) throws Exception {

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

		String queryMode = QueryType.QUERY_CURRENT_AND_CHILDREN;
		List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, queryMode, nodeId);

		ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
		int loginRole = channelCustom.getLoginRole();
		String menuIds = "";
		menuIds = getMenuIds(menuId, menuIds);
		Map<String, Object> param = new HashMap<>(25);
		if (LoginRole.OPERATE_ACCOUNT.getCode() == loginRole) {
			param.put("operatorName", channelCustom.getUsername());
		}
		param.put("originalIds", customKeyList);
		param.put("userName", userName);
		param.put("batchName", batchName);
		param.put("menuIds", menuIds);
		param.put("certId", certId);
		param.put("createTimeStart", createTimeStart);
		param.put("createTimeEnd", createTimeEnd);
		param.put("batchDesc", batchDesc);
		param.put("contentName", contentName);
		param.put("account", account);
		param.put("payType", payType);
		param.put("companyId", companyId);
		param.put("status", status);
		param.put("customName", customName);
		param.put("amountStart", StringUtil.isEmpty(amountStart) ? "" : Double.parseDouble(amountStart));
		param.put("amountEnd", StringUtil.isEmpty(amountEnd) ? "" : Double.parseDouble(amountEnd));
		List<UserCommission> commissionDetailResult = commissionService.listCommissionByCustomKeys(param);
		List<Map<String, Object>> data = new ArrayList<>();
		String[] columnName = new String[]{"????????????", "????????????", "??????ID", "???????????????", "????????????"
				, "?????????", "?????????", "????????????","????????????", "????????????", "????????????", "?????????(???????????????)"
				, "?????????????????????", "??????????????????", "?????????????????????", "????????????", "????????????"
				, "????????????", "????????????", "????????????????????????", "????????????", "????????????"
				, "????????????", "??????????????????", "????????????", "????????????", "??????????????????"};
		String filename = "?????????????????????????????????";
		for (UserCommission userCommission : commissionDetailResult) {
			Map<String, Object> dataMap = new HashMap<>(36);
			dataMap.put("1", userCommission.getCustomName());
			dataMap.put("2", userCommission.getContentName());
			dataMap.put("3", userCommission.getOrderNo());
			dataMap.put("4", userCommission.getUserName());
			dataMap.put("5", CertType.codeOf(userCommission.getDocumentType()).getDesc());
			dataMap.put("6", userCommission.getCertId());
			dataMap.put("7", userCommission.getPhoneNo());
			dataMap.put("8", userCommission.getAccount());
			dataMap.put("9", userCommission.getSourceAmount());
			dataMap.put("10", userCommission.getAmount());
			dataMap.put("11", userCommission.getCalculationRates());
			dataMap.put("12", userCommission.getSumFee());
			dataMap.put("13", userCommission.getSupplementAmount());
			dataMap.put("14", userCommission.getSupplementFee());
			dataMap.put("15", userCommission.getFeeRuleType());
			dataMap.put("16", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
			dataMap.put("17", userCommission.getSourceRemark());
			dataMap.put("18", PayType.codeOf(userCommission.getPayType()).getDesc());
			dataMap.put("19", userCommission.getCompanyName());
			dataMap.put("20", userCommission.getBankName());
			dataMap.put("21", userCommission.getCreatetime());
			dataMap.put("22", userCommission.getBatchName());
			dataMap.put("23", userCommission.getBatchDesc());
			dataMap.put("24", userCommission.getStatusDesc());
			dataMap.put("25", userCommission.getOperatorName());
			dataMap.put("26", userCommission.getReviewName());
			dataMap.put("27", userCommission.getUpdatetime());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, columnName, filename, data);
	}

	/**
	 * ??????????????????????????????(?????????????????????)
	 */
	@RequestMapping(value = "/user/root/commissionDetailResult", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionDetailResult(HttpServletRequest request,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>(5);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
		if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "??????????????????????????????????????????????????????");
			return result;
		}
		// ????????????
		String userName = request.getParameter("userName");
		//????????????id
		String companyId = request.getParameter("companyId");
		// ????????????
		String batchName = request.getParameter("batchName");
		// ????????????
		String certId = request.getParameter("certId");
		// ??????????????????
		String createTimeStart = request.getParameter("createTimeStart");
		// ??????????????????
		String createTimeEnd = request.getParameter("createTimeEnd");
		// ????????????
		String batchDesc = request.getParameter("batchDesc");
		// ????????????
		String contentName = request.getParameter("contentName");
		// ????????????
		String account = request.getParameter("account");
		// ????????????
		String payType = request.getParameter("payType");
		if ("0".equals(payType)) {
			payType = null;
		}
		// ????????????
		String status = request.getParameter("status");
		// ??????????????????
		String amountStart = request.getParameter("amountStart");
		// ??????????????????
		String amountEnd = request.getParameter("amountEnd");
		// ????????????
		String customName = request.getParameter("customName");
		// ????????????
//		String pageNo = request.getParameter("pageNo");
		try {
			Map<String, Object> param = new HashMap<>(20);
			param.put("userName", userName);
			param.put("batchName", batchName);
			param.put("certId", certId);
			param.put("createTimeStart", createTimeStart);
			param.put("createTimeEnd", createTimeEnd);
			param.put("batchDesc", batchDesc);
			param.put("contentName", contentName);
			param.put("account", account);
			param.put("payType", payType);
			param.put("companyId", companyId);
			param.put("status", status);
			param.put("amountStart", StringUtil.isEmpty(amountStart) ? "" : Double.parseDouble(amountStart));
			param.put("amountEnd", StringUtil.isEmpty(amountEnd) ? "" : Double.parseDouble(amountEnd));
			param.put("customName", customName);

			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> commissionDetailResult = commissionService.commissionDetailResult(param);
			PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);
			result.put("total", pageInfo.getTotal());
			result.put("commissionDetailResult", pageInfo.getList());
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	/**
	 * ????????????---????????????
	 */
	@RequestMapping(value = "/commissionByContentResult", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionByContentResult(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String originalId = (String) request.getSession().getAttribute("customkey");
		String batchName = request.getParameter("batchName");
		String menuId = request.getParameter("menuId");
		String createTimeStart = request.getParameter("createTimeStart");
		String createTimeEnd = request.getParameter("createTimeEnd");
		String batchDesc = request.getParameter("batchDesc");
		String contentName = request.getParameter("contentName");
		String payType = request.getParameter("payType");
		String companyId = request.getParameter("companyId");
//		String pageNo = request.getParameter("pageNo");
		try {
			String menuIds = "";
			menuIds = getMenuIds(menuId, menuIds);
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("originalId", originalId);
			param.put("batchName", batchName);
			param.put("menuIds", menuIds);
			param.put("tradeTimeStart", createTimeStart);
			param.put("tradeTimeEnd", createTimeEnd);
			param.put("batchDesc", batchDesc);
			param.put("contentName", contentName);
			param.put("payType", StringUtil.isEmpty(payType) ? null : Integer.parseInt(payType));
			param.put("companyId", companyId);
//			int total = commissionService.commissionByMemuResult(param).size();
//			int pageSize = 10;
//			if (StringUtils.isEmpty(pageNo)) {
//				pageNo = "1";
//			}
//			param.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//			param.put("limit", pageSize);

			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> commissionDetailResult = commissionService.commissionByMemuResult(param);
			PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);
			result.put("total", pageInfo.getTotal());
			result.put("commissionDetailResult", pageInfo.getList());
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	/**
	 * ????????????---????????????--??????
	 */
	@RequestMapping(value = "/commissionByContentDetail", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionByContentDetail(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String originalId = (String) request.getSession().getAttribute("customkey");
			String userName = request.getParameter("userName");
			String menuId = request.getParameter("menuId");
			String certId = request.getParameter("certId");
			String account = request.getParameter("account");
			String payType = request.getParameter("payType");
			String batchDesc = request.getParameter("batchDesc");
			String amount = request.getParameter("amount");
			amount = ArithmeticUtil.formatDecimals(amount);
			String batchName = request.getParameter("batchName");
			String createTimeStart = request.getParameter("createTimeStart");
			String createTimeEnd = request.getParameter("createTimeEnd");
			String contentName = request.getParameter("contentName");
			String companyId = request.getParameter("companyId");
//			String pageNo = request.getParameter("pageNo");
			if (StringUtils.isEmpty(menuId)) {
				respstat = RespCode.error101;
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				return result;
			}
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("originalId", originalId);
			param.put("menuId", menuId);
			param.put("userName", userName);
			param.put("certId", certId);
			param.put("account", account);
			param.put("payType", payType);
			param.put("batchDesc", batchDesc);
			param.put("amount", amount);
			param.put("batchName", batchName);
			param.put("tradeTimeStart", createTimeStart);
			param.put("tradeTimeEnd", createTimeEnd);
			param.put("contentName", contentName);
			param.put("companyId", companyId);
			PageHelper.startPage(pageNo, pageSize);
			List<UserCommission> commissionDetailResult = commissionService.commissionByMemuDetail(param);
			PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);
			result.put("commissionDetailResult", pageInfo.getList());
			result.put("total", pageInfo.getTotal());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	@RequestMapping(value = "/getTransHistoryPage", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> getTransHistoryPage(String timeStart, String timeEnd,
			@RequestParam(required = false, defaultValue = "1") int pageNo) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		logger.info("??????????????????????????????????????????--timeStart=" + timeStart + " timeEnd=" + timeEnd + " pageNo=" + pageNo);
		if (StringUtil.isEmpty(timeStart) || StringUtil.isEmpty(timeEnd)) {
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		try {
			List<TransHistoryRecord> queryTransHistoryPage = pingAnBankServiceImpl.queryTransHistoryPage(timeStart,
					timeEnd, pageNo);
			result.put("queryTransHistoryPage", queryTransHistoryPage);
			result.put("total", queryTransHistoryPage.size());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respstat = RespCode.error107;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		return result;
	}

	private String getMenuIds(String menuId, String menuIds) {
		if (!StringUtil.isEmpty(menuId)) {
			Map<String, Object> param = new HashMap<String, Object>();
			CustomMenu customMenuById = customService.getCustomMenuById(Integer.parseInt(menuId));
			if (customMenuById == null) {
				menuIds = menuId;
			} else {
				param.put("levelCode", customMenuById.getLevelCode());
				List<CustomMenu> nodeTree = customService.getNodeTree(param);
				for (CustomMenu customMenu : nodeTree) {
					menuIds += customMenu.getId() + ",";
				}
			}
		}
		return menuIds;
	}


	/**
	 * ??????????????????
	 *
	 * @author linsong
	 * @date 2019/4/25
	 */
	@RequestMapping(value = "/transactionList")
	@ResponseBody
	public Map<String, Object> transactionList(HttpServletRequest request, MerchantTransaction transaction,
			@RequestParam(required = false, defaultValue = "1") Integer pageNo, @RequestParam(required = false, defaultValue = "10") Integer pageSize,
			String startTime, String endTime, String startAmount, String endAmount, int resultState,String operationsManager,String businessManager) {

	//	ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

		//?????????????????????
//		if (!(CommonString.ROOT.equals(loginUser.getCustomkey()) || (CustomType.ROOT.getCode() == loginUser.getCustomType() && CommonString.ROOT.equals(loginUser.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == loginUser.getLoginRole()))) {
//			return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
//		}

		Map<String, Object> model = new HashMap<>(20);
		model.put("companyName", transaction.getCompanyName());
		model.put("agentName", transaction.getAgentName());
		model.put("businessPlatform", transaction.getBusinessPlatform());
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("startAmount", startAmount);
		model.put("endAmount", endAmount);
		model.put("resultState", resultState);
		model.put("operationsManager", operationsManager);
		model.put("businessManager", businessManager);

		PageHelper.startPage(pageNo, pageSize);
		List<MerchantTransaction> transactionList = channelHistoryService.selectTransactionList(model);

		PageInfo pageInfo = new PageInfo(transactionList);
		model.put("total", pageInfo.getTotal());
		model.put("list", pageInfo.getList());

		return returnSuccess(model);
	}

	/**
	 * ????????????????????????
	 *
	 * @author linsong
	 * @date 2019/4/25
	 */
	@RequestMapping(value = "/transactionList/export")
	public void transactionListExport(HttpServletResponse response, MerchantTransaction transaction,
			String startTime, String endTime, String startAmount, String endAmount , int resultState,String operationsManager,String businessManager) {


		Map<String, Object> model = new HashMap<>(20);
		model.put("companyName", transaction.getCompanyName());
		model.put("agentName", transaction.getAgentName());
		model.put("businessPlatform", transaction.getBusinessPlatform());
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("startAmount", startAmount);
		model.put("endAmount", endAmount);
    model.put("resultState", resultState);
		model.put("operationsManager", operationsManager);
		model.put("businessManager", businessManager);

		List<MerchantTransaction> transactionList = channelHistoryService.selectTransactionList(model);

		String[] colunmName = new String[]{"????????????", "????????????", "???????????????", "?????????????????????", "???????????????","??????????????????", "?????????????????????", "??????????????????", "??????????????????????????????", "??????Key","????????????","????????????"};
		String filename = "?????????????????????";
		List<Map<String, Object>> data = new ArrayList<>();
		for (MerchantTransaction merchantTransaction : transactionList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", merchantTransaction.getCompanyName());
			dataMap.put("2", CustomType.codeOf(merchantTransaction.getCustomType()).getDesc());
			dataMap.put("3", merchantTransaction.getBusinessAmount());
			dataMap.put("4", merchantTransaction.getBusinessCount());
			dataMap.put("5", merchantTransaction.getAgentName());
			dataMap.put("6", merchantTransaction.getLastPaymentTime());
			dataMap.put("7", merchantTransaction.getHistoryAmount());
			dataMap.put("8", merchantTransaction.getBalance());
			dataMap.put("9", merchantTransaction.getBusinessPlatform());
			dataMap.put("10", merchantTransaction.getCustomkey());
			dataMap.put("11", merchantTransaction.getBusinessManager());
			dataMap.put("12", merchantTransaction.getOperationsManager());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}

	/**
	 * ?????????????????????
	 *
	 * @author linsong
	 * @date 2019/4/28
	 */
	@RequestMapping(value = "/transactionProxyList")
	@ResponseBody
	public Map<String, Object> transactionProxyList(HttpServletRequest request, MerchantTransaction transaction,
			@RequestParam(required = false, defaultValue = "1") Integer pageNo, @RequestParam(required = false, defaultValue = "10") Integer pageSize,
			String startTime, String endTime, String startAmount, String endAmount) {

		ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

		Map<String, Object> model = new HashMap<>(20);

		if (!isRootAdmin(loginUser) && (CustomType.PROXY.getCode() != loginUser.getCustomType())) {
			if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
				ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
				if (CustomType.PROXY.getCode() == masterCustom.getCustomType()) {
					loginUser = masterCustom;
				} else {
					return returnFail(RespCode.error101, "????????????");
				}
			} else {
				return returnFail(RespCode.error101, "????????????");
			}
		}


		//????????????????????????
		if (CustomType.PROXY.getCode() == loginUser.getCustomType()) {
			String customkeys = loginUser.getCustomkey();
			if (loginUser.getProxyType() == 1) {
				List<String> customKeyList = organizationTreeService.queryAllCurrentAndChildrenCustomkeys(customkeys);
				customkeys = StringUtils.join(customKeyList, ",");
				model.put("customKeys", customkeys);
			} else {
				model.put("agentId", customkeys);

			}
		}

		model.put("companyName", transaction.getCompanyName());
		model.put("companyId", transaction.getCompanyId());
		model.put("agentName", transaction.getAgentName());
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("startAmount", startAmount);
		model.put("endAmount", endAmount);


		PageHelper.startPage(pageNo, pageSize);
		List<MerchantTransaction> transactionList = channelHistoryService.selectTransactionListByProxy(model);

		PageInfo pageInfo = new PageInfo(transactionList);
		model.put("total", pageInfo.getTotal());
		model.put("list", pageInfo.getList());

		return returnSuccess(model);
	}

	/**
	 * ???????????????????????????
	 *
	 * @author linsong
	 * @date 2019/4/28
	 */
	@RequestMapping(value = "/transactionProxyList/export")
	public void transactionProxyListExport(HttpServletRequest request, HttpServletResponse response, MerchantTransaction transaction,
			String startTime, String endTime, String startAmount, String endAmount) {

		ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

		Map<String, Object> model = new HashMap<>(20);

		if (!isRootAdmin(loginUser) && (CustomType.PROXY.getCode() != loginUser.getCustomType())) {
			if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
				ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
				if (CustomType.PROXY.getCode() == masterCustom.getCustomType()) {
					loginUser = masterCustom;
				}
			}
		}

		//????????????????????????
		if (CustomType.PROXY.getCode() == loginUser.getCustomType()) {
			String customkeys = loginUser.getCustomkey();
			if (loginUser.getProxyType() == 1) {
				List<String> customKeyList = organizationTreeService.queryAllCurrentAndChildrenCustomkeys(customkeys);
				customkeys = StringUtils.join(customKeyList, ",");
				model.put("customKeys", customkeys);
			} else {
				model.put("agentId", customkeys);

			}
		}

		model.put("companyName", transaction.getCompanyName());
		model.put("agentName", transaction.getAgentName());
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("startAmount", startAmount);
		model.put("endAmount", endAmount);
		model.put("companyId", transaction.getCompanyId());

		List<MerchantTransaction> transactionList = channelHistoryService.selectTransactionListByProxy(model);

		String[] colunmName = new String[]{"????????????", "????????????", "????????????", "???????????????", "?????????????????????", "???????????????", "?????????????????????", "??????????????????", "??????Key"};
		String filename = "????????????????????????";
		List<Map<String, Object>> data = new ArrayList<>();
		for (MerchantTransaction merchantTransaction : transactionList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", merchantTransaction.getCustomName());
			dataMap.put("2", CustomType.codeOf(merchantTransaction.getCustomType()).getDesc());
			dataMap.put("3", merchantTransaction.getCompanyName());
			dataMap.put("4", merchantTransaction.getBusinessAmount());
			dataMap.put("5", merchantTransaction.getBusinessCount());
			dataMap.put("6", merchantTransaction.getAgentName());
			dataMap.put("7", merchantTransaction.getHistoryAmount());
			dataMap.put("8", merchantTransaction.getBalance());
			dataMap.put("9", merchantTransaction.getCustomkey());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}

	/**
	 * ??????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryLdStepOrderDetail")
	@ResponseBody
	public Map<String, Object> queryLdStepOrderDetail(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (!(CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
			page.getParams().put("loginCustomer", customLogin.getCustomkey());
		}
		page.getParams().put("notInBusinessType",
				String.valueOf(LdBusinessTypeEnum.SPLITORDER.getCode()));
//		int total = ldOrderStepService.queryLdStepOrderDetailListCount(page);
		//??????????????????????????????????????????.
		PageHelper.startPage(page.getPageNo(),page.getPageSize());
		List<Map<String, Object>> relationList = ldOrderStepService.queryLdStepOrderDetailList(page);
		PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(relationList);
		result.put("total", pageInfo.getTotal());
		result.put("relationList", pageInfo.getList());
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????
	 * @return
	 */
//	@RequestMapping("/againPay")
//	@ResponseBody
//	public Map<String, Object> againPay(LdOrderStep ldOrderStep){
//		logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????");
//		int respstat = RespCode.success;
//		HashMap<String, Object> result = new HashMap<>();
//		try{
//			if(!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())){
//				LdOrderStep ldOrderStepDetail = ldOrderStepService.getOrderStep(ldOrderStep.getStepOrderNo());
//				UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
//				if(StringUtil.isEmpty(userCommission.getBatchId())){
//					if(userCommission.getStatus()==3){
//						logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"???api??????");
//						//api????????????
//						//??????????????????
//						Integer stepOrder = ldOrderStepDetail.getStepOrder()-1;
//						Map<String, Object> params = new HashMap<String, Object>();
//						params.put("orderNo", ldOrderStepDetail.getOrderno());
//						params.put("stepOrder", stepOrder);
//						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);
//						if(ldPreOrderStep.getStatus()==1&&ldOrderStepDetail.getStatus()==2){
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????");
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????");
//							respstat = againPayDo(ldOrderStepDetail);
//						}else{
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
//							respstat = RespCode.PRE_STATUS_ERROR;
//						}
//					}else{
//						respstat = RespCode.NOT_SUPPORT_STATUS;
//						logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
//					}
//				}else{
//					ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(userCommission.getBatchId());
//					if(channelHistory.getStatus()==3&&userCommission.getStatus()==3){
//						logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"???web??????");
//						//web????????????
//						//??????????????????
//						Integer stepOrder = ldOrderStepDetail.getStepOrder()-1;
//						Map<String, Object> params = new HashMap<String, Object>();
//						params.put("orderNo", ldOrderStepDetail.getOrderno());
//						params.put("stepOrder", stepOrder);
//						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);
//						if(ldPreOrderStep.getStatus()==1&&ldOrderStepDetail.getStatus()==2){
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????");
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????");
//							respstat = againPayDo(ldOrderStepDetail);
//						}else{
//							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
//							respstat = RespCode.PRE_STATUS_ERROR;
//						}
//					}else{
//						logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
//						respstat = RespCode.NOT_SUPPORT_STATUS;
//					}
//				}
//			}else{
//				logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"?????????");
//				respstat = RespCode.error101;
//			}
//		}catch(Exception e){
//			respstat = RespCode.PAY_EXCEPTION;
//			logger.error("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????",e);
//		}
//		result.put(RespCode.RESP_STAT, respstat);
//		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
//		return result;
//
//	}

	/**
	 * ??????????????????
	 * @param ldOrderStepDetail
	 * @return
	 */
//	private int againPayDo(LdOrderStep ldOrderStepDetail) {
//		int respstat;
//		PaymentReturn<String> paymentReturn;
//		String failMessage;
//		ldOrderStepDetail.setPreStepOrder(ldOrderStepDetail.getStepOrderNo());
//		ldOrderStepDetail.setStepOrderNo(OrderNoUtil.getOrderNo());
//		UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
//		try{
//			Map<String, Object> params = new HashMap<String, Object>();
//			params.put("customkey", userCommission.getOriginalId());
//			params.put("companyId", userCommission.getCompanyId());
//			//??????????????????
//			CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
//			//????????????
//			PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),ldOrderStepDetail.getPathno());
//			logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"?????????????????????:"+paymentConfig.toString());
//			if(paymentConfig != null){
//				Company company2 = companyService.getCompanyByUserId(Integer.parseInt(conLdConfig.getCompanyidTwo()));
//				//??????????????????????????????
//				Payment payment = PaymentFactory.paymentEntity(paymentConfig);
//				PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
//				Payment proxy = paymentProxy.getProxy();
//				UserCommission userCommission2 = new UserCommission();
//				userCommission2.setAccount(ldOrderStepDetail.getReceiveAccount());
//				userCommission2.setAmount(ldOrderStepDetail.getAmount());
//				if(ldOrderStepDetail.getBusinessType()==1){
//					userCommission2.setUserName(company2.getCompanyName());
//					if(company2.getBankName()!=null&&company2.getBankNo()!=null){
//						userCommission2.setBankName(company2.getBankName());
//						userCommission2.setBankNo(company2.getBankNo());
//					}
//				}else{
//					userCommission2.setUserName(userCommission.getUserName());
//					userCommission2.setBankName(userCommission.getBankName());
//					userCommission2.setBankNo(userCommission.getBankNo());
//				}
//				userCommission2.setCompanyId(ldOrderStepDetail.getIssuedCompanyid());
//				userCommission2.setOriginalId(userCommission.getOriginalId());
//				userCommission2.setOrderNo(ldOrderStepDetail.getStepOrderNo());
//				if(userCommission2.getBankName()!=null&&userCommission2.getBankNo()!=null){
//					if("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(userCommission.getOriginalId())){
//						logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+",???????????????");
//						//????????????
//						paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS,"????????????",ldOrderStepDetail.getStepOrderNo());
//					}else{
//						paymentReturn = proxy.paymentTransfer(userCommission2);
//
//					}
//				}else{
//					logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+",???????????????????????????????????????????????????");
//					paymentReturn = new PaymentReturn<>(PayRespCode.RESP_FAILURE,"???????????????????????????????????????????????????",ldOrderStepDetail.getStepOrderNo());
//				}
//				//??????????????????
//				if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())|| PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
//					logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+",??????????????????");
//					if(ldOrderStepDetail.getBusinessType()==2||ldOrderStepDetail.getBusinessType()==4){
//						customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStepDetail.getIssuedCompanyid(),
//								userCommission.getOriginalId(),
//								userCommission.getCertId(),
//								ldOrderStepDetail.getAmount(),
//								true);
//						logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"?????????????????????????????????{}???", ldOrderStepDetail.getAmount());
//					}
//					ldOrderStepDetail.setStatus(3);
//					ldOrderStepDetail.setStatusDesc("?????????");
//					ldOrderStepService.updateById(ldOrderStepDetail);
//				}else{
//					ldOrderStepDetail.setStatus(2);
//					failMessage = paymentReturn.getFailMessage();
//					logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+",????????????????????????????????????"+failMessage);
//					if (failMessage.length() > 200) {
//						failMessage = failMessage.substring(0, 200);
//					} else if (failMessage.length() == 0) {
//						failMessage = RespCode.CONNECTION_ERROR;
//					}
//					ldOrderStepDetail.setStatusDesc(failMessage);
//					ldOrderStepService.updateById(ldOrderStepDetail);
//				}
//			}else{
//				logger.error("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????????????????????????????????????????????????????");
//				logger.error("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????????????????????????????----?????????????????????????????????-----customKey:" + userCommission.getOriginalId() + "--????????????ID???"+ ldOrderStepDetail.getIssuedCompanyid());
//				ldOrderStepDetail.setStatus(2);
//				ldOrderStepDetail.setStatusDesc("??????-?????????????????????????????????");
//				ldOrderStepService.updateById(ldOrderStepDetail);
//			}
//			respstat = RespCode.success;
//		}catch(Exception e){
//			logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+",??????????????????",e);
//			respstat = RespCode.PAY_EXCEPTION;
//			ldOrderStepDetail.setStatus(3);
//			ldOrderStepDetail.setStatusDesc("?????????");
//			ldOrderStepService.updateById(ldOrderStepDetail);
//			//????????????????????????
//			if(ldOrderStepDetail.getBusinessType()==2||ldOrderStepDetail.getBusinessType()==4){
//				customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStepDetail.getIssuedCompanyid(),
//						userCommission.getOriginalId(),
//						String.valueOf(userCommission.getCertId()),
//						ldOrderStepDetail.getAmount(),
//						true);
//				logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"?????????????????????????????????{}???", ldOrderStepDetail.getAmount());
//			}
//		}
//		return respstat;
//	}

//	/**
//	 * ????????????
//	 * @return
//	 */
//	@RequestMapping("/queryStatus")
//	@ResponseBody
//	public Map<String, Object> queryStatus(LdOrderStep ldOrderStep){
//		logger.info("??????????????????????????????"+ldOrderStep.getStepOrderNo()+",????????????");
//		int respstat = RespCode.success;
//		HashMap<String, Object> result = new HashMap<>();
//		PaymentReturn<TransStatus> paymentReturn = null;
//		try{
//			if(!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())){
//				LdOrderStep ldOrderStepDetail = ldOrderStepService.getOrderStep(ldOrderStep.getStepOrderNo());
//				if(ldOrderStepDetail.getStatus()==3){
//					UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
//					//????????????
//					PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),ldOrderStepDetail.getPathno());
//					logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"?????????????????????:"+paymentConfig.toString());
//					if(paymentConfig != null){
//						if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(userCommission.getOriginalId())) {//????????????
//							logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStep.getStepOrderNo()+",???????????????");
//							TransStatus transStatus1 = new TransStatus( ldOrderStepDetail.getStepOrderNo(), PayRespCode.RESP_TRANSFER_SUCCESS,"????????????");
//							paymentReturn = new PaymentReturn<TransStatus>(PayRespCode.RESP_SUCCESS,"????????????",transStatus1);
//						}else{
//							//??????????????????????????????
//							Payment payment = PaymentFactory.paymentEntity(paymentConfig);
//							PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
//							Payment proxy = paymentProxy.getProxy();
//							paymentReturn = proxy.queryTransferResult(ldOrderStepDetail.getStepOrderNo());
//						}
//						logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"??????????????????????????????----------???" + paymentReturn.toString());
//						if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
//							TransStatus transStatus = paymentReturn.getAttachment();
//							logger.error("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????--------resultCode:" + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
//							if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
//								logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????");
//								ldOrderStepDetail.setStatus(1);
//								ldOrderStepDetail.setStatusDesc("??????");
//								ldOrderStepService.update(ldOrderStepDetail);
//							} else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
//								logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"??????????????????????????????"+transStatus.getResultMsg());
//								ldOrderStepDetail.setStatus(2);
//								String errorMsg = transStatus.getResultMsg();
//								if(errorMsg !=null){
//									if (errorMsg.contains("??????")) {
//										errorMsg = "?????????????????????????????????";
//									} else {
//										String statusDesc = transStatus.getResultMsg();
//										if (statusDesc.length() > 200) {
//											statusDesc = statusDesc.substring(0, 200);
//										}
//										String s = statusDesc.replaceAll(",", "-");
//										errorMsg = s;
//									}
//								}
//								ldOrderStepDetail.setStatusDesc(errorMsg);
//								ldOrderStepService.update(ldOrderStepDetail);
//								if(ldOrderStepDetail.getBusinessType()==2||ldOrderStepDetail.getBusinessType()==4){
//									customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStepDetail.getIssuedCompanyid(),
//											userCommission.getOriginalId(),
//											userCommission.getCertId(),
//											ldOrderStepDetail.getAmount(),
//											false);
//									logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"?????????????????????????????????{}???", ldOrderStepDetail.getAmount());
//								}
//
//							} else {
//								logger.error("????????????---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
//								ldOrderStepDetail.setStatus(3);
//								ldOrderStepDetail.setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());
//								ldOrderStepService.update(ldOrderStepDetail);
//							}
//							int totalCount = ldOrderStepService.getCountByOrderNo(ldOrderStepDetail.getOrderno());
//							int successCount = ldOrderStepService.getCountSuccessByOrderNo(ldOrderStepDetail.getOrderno());
//							int failCount = ldOrderStepService.getCountFailByOrderNo(ldOrderStepDetail.getOrderno());
//							if(successCount==totalCount){
//								logger.info("????????????????????????"+userCommission.getOrderNo()+"?????????????????????????????????????????????????????????????????????");
//								userCommission.setStatus(1);
//								userCommission.setStatusDesc("??????");
//								commissionService.updateUserCommissionById(userCommission);
//								if(StringUtil.isEmpty(userCommission.getBatchId())){
//									transferDealStatusNotifier.notify(userCommission.getOrderNo(), TransferStatus.TRANSFER_DONE,CommonRetCodes.ACTION_DONE.getCode(),CommonRetCodes.ACTION_DONE.getDesc());
//									logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????????????????????????????");
//								}
//							}else if(failCount==totalCount){
//								logger.info("????????????????????????"+userCommission.getOrderNo()+"?????????????????????????????????????????????????????????????????????");
//								userCommission.setStatus(2);
//								userCommission.setStatusDesc("????????????");
//								commissionService.updateUserCommissionById(userCommission);
//								//??????
//								if(StringUtil.isEmpty(userCommission.getBatchId())){
//									updateBalance(userCommission,userCommission.getCompanyId(),CommonString.REFUND);
//									logger.info("????????????????????????{}",userCommission.getOrderNo());
//									transferDealStatusNotifier.notify(userCommission.getOrderNo(), TransferStatus.TRANSFER_FAILED,CommonRetCodes.UNCATCH_ERROR.getCode(),userCommission.getStatusDesc());
//									logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????????????????????????????");
//								}
//							}
//							logger.info("?????????????????????????????? orderNo[{}]", ldOrderStepDetail.getStepOrderNo());
//						} else {
//							respstat = RespCode.QUERY_FAIL;
//							logger.error("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
//						}
//					}else{
//						logger.info("????????????????????????"+userCommission.getOrderNo()+",??????????????????"+ldOrderStepDetail.getStepOrderNo()+"????????????????????????????????????----?????????????????????????????????-----customKey:" + userCommission.getOriginalId() + "--????????????ID???"+ ldOrderStep.getIssuedCompanyid());
//					}
//				}else{
//					respstat = RespCode.NOT_SUPPORT_STATUS;
//					logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"???????????????");
//				}
//			}else{
//				respstat = RespCode.error101;
//				logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????");
//			}
//		}catch(Exception e){
//			logger.error("????????????????????????",e);
//			respstat = RespCode.SURE_STATUS;
//		}
//		result.put(RespCode.RESP_STAT, respstat);
//		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
//		return result;
//	}

	// ???????????????
	private void updateBalance(UserCommission transferParam,String companyId, int operating) {
		Map<String, Object> params = new HashMap<>(5);
		BigDecimal Magnification = new BigDecimal(operating * 100);
		params.put(CommonString.CUSTOMKEY, transferParam.getOriginalId());
		params.put(CommonString.COMPANYID, companyId);
		params.put(CommonString.PAYTYPE, transferParam.getPayType());
		params.put(CommonString.BALANCE,(new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(Magnification));
		customBalanceDao.updateBalance(params);
		logger.info("?????????{}"+transferParam.getOrderNo()+"???????????????"+(new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(new BigDecimal(operating))+"???");
	}

	/**
	 * ??????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryLdCorrectOrderDetail")
	@ResponseBody
	public Map<String, Object> queryLdCorrectOrderDetail(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (!(CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
			page.getParams().put("loginCustomer", customLogin.getCustomkey());
		}
		int total = ldOrderCorrectService.queryLdCorrectOrderDetailListCount(page);
		//??????????????????????????????????????????.
		List<Map<String, Object>> relationList = ldOrderCorrectService.queryLdCorrectOrderDetailList(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * ??????
	 * @return
	 */
	@RequestMapping("/correct")
	@ResponseBody
	public Map<String, Object> correct(LdOrderStep ldOrderStep){
		logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????");
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		PaymentReturn<String> paymentReturn=null;
		String failMessage=null;
		String payCompanyId=null;
		Company companyPay = null;
		Company receiveCompanyPay=null;
		String errMsg = "";
		try{
			if(!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())){
				LdOrderStep ldOrderStepDetail = ldOrderStepService.getOrderStep(ldOrderStep.getStepOrderNo());
				UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
				if(ldOrderStepDetail.getBusinessType()==1){
					if(ldOrderStepDetail.getStatus()==1){
						//??????????????????
						Integer stepOrder = ldOrderStepDetail.getStepOrder()+1;
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orderNo", ldOrderStepDetail.getOrderno());
						params.put("stepOrder", stepOrder);
						LdOrderStep ldNextOrderStep = ldOrderStepService.getPreStepOrder(params);
						if(ldNextOrderStep.getStatus()==2){
							if(ldOrderStepDetail.getCorrectStatus()!=null&&ldOrderStepDetail.getCorrectStatus()!=0){
								respstat = RespCode.NOT_SUPPORT_STATUS;
								errMsg="?????????????????????????????????????????????";
							}else{
								Map<String, Object> configParams = new HashMap<String, Object>();
								configParams.put("customkey", userCommission.getOriginalId());
								configParams.put("companyId", userCommission.getCompanyId());
								//??????????????????
								CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(configParams);
								if(ldOrderStepDetail.getIssuedCompanyid().equals(conLdConfig.getCompanyidOne())){
									payCompanyId=conLdConfig.getCompanyidTwo();
								}else{
									payCompanyId=conLdConfig.getCompanyidOne();
								}
								companyPay = companyService.getCompanyByUserId(Integer.parseInt(payCompanyId));
								receiveCompanyPay = companyService.getCompanyByUserId(Integer.parseInt(ldOrderStepDetail.getIssuedCompanyid()));
								//????????????????????????
								PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), payCompanyId,conLdConfig.getPathno());
								//??????????????????
								LdOrderCorrect correct = addCorrectOrder(payCompanyId, companyPay,receiveCompanyPay, ldOrderStepDetail);
								ldOrderStepDetail.setIsCorrect(1);
								ldOrderStepDetail.setCorrectStatus(0);
								ldOrderStepService.updateById(ldOrderStepDetail);
								if(paymentConfig != null){
									//??????????????????????????????
									Payment payment = PaymentFactory.paymentEntity(paymentConfig);
									PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
									Payment proxy = paymentProxy.getProxy();
									UserCommission userCommission2 = new UserCommission();
									userCommission2.setAccount(correct.getReceiveAccount());
									userCommission2.setAmount(correct.getAmount());
									userCommission2.setUserName(correct.getReceiveUser());
									if(receiveCompanyPay.getBankName()!=null&&receiveCompanyPay.getBankNo()!=null){
										userCommission2.setBankName(receiveCompanyPay.getBankName());
										userCommission2.setBankNo(receiveCompanyPay.getBankNo());
									}
									userCommission2.setCompanyId(payCompanyId);
									userCommission2.setOriginalId(userCommission.getOriginalId());
									userCommission2.setOrderNo(correct.getCorrectOrderNo());
									if(userCommission2.getBankName()!=null&&userCommission2.getBankNo()!=null){
										if("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(userCommission.getOriginalId())){
											logger.info("??????????????????"+correct.getStepOrderNo()+",??????????????????"+correct.getCorrectOrderNo()+",???????????????");
											//????????????
											paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS,"????????????",correct.getCorrectOrderNo());
										}else{
											paymentReturn = proxy.paymentTransfer(userCommission2);

										}
									}else{
										logger.info("??????????????????"+correct.getStepOrderNo()+",??????????????????"+correct.getCorrectOrderNo()+",???????????????????????????????????????????????????");
										paymentReturn = new PaymentReturn<>(PayRespCode.RESP_FAILURE,"???????????????????????????????????????????????????",correct.getCorrectOrderNo());
									}
									//??????????????????
									if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())|| PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
										correct.setStatus(3);
										correct.setStatusDesc("?????????");
										ldOrderCorrectService.updateByPrimaryKeySelective(correct);
										ldOrderStepDetail.setCorrectStatus(3);
										ldOrderStepService.updateById(ldOrderStepDetail);
									}else{
										correct.setStatus(2);
										failMessage = paymentReturn.getFailMessage();
										logger.info("??????????????????"+correct.getStepOrderNo()+",??????????????????"+correct.getCorrectOrderNo()+",????????????????????????????????????"+failMessage);
										if (failMessage.length() > 200) {
											failMessage = failMessage.substring(0, 200);
										} else if (failMessage.length() == 0) {
											failMessage = RespCode.CONNECTION_ERROR;
										}
										correct.setStatusDesc(failMessage);
										ldOrderCorrectService.updateByPrimaryKeySelective(correct);
										ldOrderStepDetail.setCorrectStatus(2);
										ldOrderStepService.updateById(ldOrderStepDetail);
										respstat = RespCode.PAY_EXCEPTION;
										errMsg="?????????????????????????????????";
									}
								}else{
									logger.error("??????????????????"+correct.getStepOrderNo()+",??????????????????"+correct.getCorrectOrderNo()+"????????????????????????????????????----?????????????????????????????????-----customKey:" + userCommission.getOriginalId() + "--????????????ID???"+ ldOrderStepDetail.getIssuedCompanyid());
									correct.setStatus(2);
									correct.setStatusDesc("??????-?????????????????????????????????");
									ldOrderCorrectService.updateByPrimaryKeySelective(correct);
									ldOrderStepDetail.setCorrectStatus(2);
									ldOrderStepService.updateById(ldOrderStepDetail);
									respstat = RespCode.PAY_EXCEPTION;
									errMsg="????????????????????????????????????????????????";
								}
							}
						}else{
							respstat = RespCode.NOT_SUPPORT_STATUS;
							errMsg="????????????????????????,????????????";
							logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
						}
					}else{
						respstat = RespCode.NOT_SUPPORT_STATUS;
						errMsg="????????????????????????,????????????";
						logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????????????????");
					}
				}else{
					//?????????????????????
					respstat = RespCode.BUSINESS_TYPE_NOT;
					errMsg="????????????????????????,????????????";
					logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"????????????????????????????????????");
				}
			}else{
				logger.info("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"?????????");
				respstat = RespCode.error101;
			}
		}catch(Exception e){
			respstat = RespCode.PAY_EXCEPTION;
			logger.error("????????????????????????????????????"+ldOrderStep.getStepOrderNo()+"??????????????????",e);
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, errMsg);
		return result;

	}

	private LdOrderCorrect addCorrectOrder(String payCompanyId, Company companyPay,
			Company receiveCompanyPay, LdOrderStep ldOrderStepDetail) {
		LdOrderCorrect correct = new LdOrderCorrect();
		String correctOrderNo = OrderNoUtil.getOrderNo();
		correct.setAmount(ldOrderStepDetail.getAmount());
		correct.setBusinessType(1);
		correct.setCorrectOrderNo(correctOrderNo);
		correct.setCreateTime(DateUtils.getNowDate());
		correct.setIssuedCompanyid(payCompanyId);
		correct.setOrderNo(ldOrderStepDetail.getOrderno());
		correct.setPathno(ldOrderStepDetail.getPathno());
		correct.setPaymentAccount(companyPay.getCardNo());
		correct.setPaymentUser(companyPay.getCompanyName());
		correct.setReceiveAccount(receiveCompanyPay.getCardNo());
		correct.setReceiveUser(receiveCompanyPay.getCompanyName());
		correct.setStatus(0);
		correct.setStatusDesc("?????????");
		correct.setStepOrderNo(ldOrderStepDetail.getStepOrderNo());
		ldOrderCorrectService.insert(correct);
		return correct;
	}


	/**
	 * api??????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/querySplitStepOrderDetail")
	@ResponseBody
	public Map<String, Object> querySplitStepOrderDetail(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (!(CommonString.ROOT.equals(customLogin.getCustomkey()) || (
				CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT
						.equals(customLogin.getMasterCustom())
						&& LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
			page.getParams().put("loginCustomer", customLogin.getCustomkey());
		}
//		int total = ldOrderStepService.queryLdStepOrderDetailListCount(page);
		//??????????????????????????????????????????.
		PageHelper.startPage(page.getPageNo(), page.getPageSize());
		List<Map<String, Object>> relationList = ldOrderStepService.queryLdStepOrderDetailList(page);
		PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(relationList);
		result.put("total", pageInfo.getTotal());
		result.put("relationList", pageInfo.getList());
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * api??????????????????
	 *
	 * @return
	 */
	@RequestMapping("/splitOrderAgainPay")
	@ResponseBody
	public Map<String, Object> splitOrderAgainPay(LdOrderStep ldOrderStep) {
		int respstat = 0;
		logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "????????????");
		HashMap<String, Object> result = new HashMap<>();

		String key = "againPayDoBySplit," + ldOrderStep.getOrderno() + "," + ldOrderStep.getStepOrderNo();

		try {
			if (!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())) {
				LdOrderStep ldOrderStepDetail = ldOrderStepService
						.getOrderStep(ldOrderStep.getStepOrderNo());
				UserCommission userCommission = commissionService
						.getUserCommission(ldOrderStepDetail.getOrderno());
				if (StringUtil.isEmpty(userCommission.getBatchId())) {
					if (CommissionStatus.SUBMITTED.getCode() == userCommission.getStatus()) {
						logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "???api????????????");

						Integer stepOrder = ldOrderStepDetail.getStepOrder() == 1 ? 2 : 1;
						Map<String, Object> params = new HashMap<>();
						params.put("orderNo", ldOrderStepDetail.getOrderno());
						params.put("stepOrder", stepOrder);
						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);

						if (LdOrderStatusEnum.FAILURE.getCode() == ldOrderStepDetail.getStatus()
								&& LdOrderStatusEnum.SUCCESS.getCode() == ldPreOrderStep.getStatus()) {
							logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "??????????????????");
							logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "????????????");

							if (!utilCacheManager.lockWithTimeout(key, 1000, 600000)) {
								return returnFail(RespCode.error101, "???????????????????????????????????????!");
							}
							respstat = againPayDoBySplit(ldOrderStepDetail);
						} else {
							logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "??????????????????????????????");
							respstat = RespCode.PRE_STATUS_ERROR;
						}
					} else {
						respstat = RespCode.NOT_SUPPORT_STATUS;
						logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "??????????????????????????????");
					}
				}
			} else {
				logger.info("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "?????????");
				respstat = RespCode.error101;
			}
		} catch (Exception e) {
			respstat = RespCode.PAY_EXCEPTION;
			logger.error("????????????????????????????????????" + ldOrderStep.getStepOrderNo() + "??????????????????", e);
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;

	}

	/**
	 * api????????????
	 *
	 * @return
	 */
	private int againPayDoBySplit(LdOrderStep ldOrderStepDetail) {
		int respstat;
		PaymentReturn<String> paymentReturn;
		ldOrderStepDetail.setPreStepOrder(ldOrderStepDetail.getStepOrderNo());
		ldOrderStepDetail.setStepOrderNo(OrderNoUtil.getOrderNo());
		UserCommission userCommission = commissionService
				.getUserCommission(ldOrderStepDetail.getOrderno());
		try {

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("customkey", userCommission.getOriginalId());
			params.put("companyId", userCommission.getCompanyId());

			//????????????
			PaymentConfig paymentConfig = companyService
					.getPaymentConfigInfoPlus(String.valueOf(userCommission.getPayType()),
							userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),
							ldOrderStepDetail.getIssuedRealCompanyId(),
							ldOrderStepDetail.getPathno());
			logger.info(
					"????????????????????????" + userCommission.getOrderNo() + ",??????????????????" + ldOrderStepDetail.getStepOrderNo()
							+ "?????????????????????:" + paymentConfig.toString());
			//??????????????????????????????
			Payment payment = PaymentFactory.paymentEntity(paymentConfig);
			PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME,
					utilCacheManager);
			Payment proxy = paymentProxy.getProxy();

			UserCommission ldOrderStepCommission = ldOrderStepDetail.toUserCommission(userCommission);

			if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K"
					.equals(userCommission.getOriginalId())) {
				logger.info("????????????????????????" + userCommission.getOrderNo() + ",??????????????????" + ldOrderStepDetail
						.getStepOrderNo() + ",???????????????");
				//????????????
				paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS, "????????????",
						ldOrderStepDetail.getStepOrderNo());
			} else {
				paymentReturn = proxy.paymentTransfer(ldOrderStepCommission);
			}

			if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
				ldOrderStepDetail.setStatus(CommissionStatus.SUBMITTED.getCode());
				ldOrderStepDetail.setStatusDesc(CommissionStatus.SUBMITTED.getDesc());
			} else if (PayRespCode.RESP_CHECK_FAIL.equals(paymentReturn.getRetCode())
					|| PayRespCode.RESP_CHECK_COUNT_FAIL.equals(paymentReturn.getRetCode())) {
				ldOrderStepDetail.setStatus(CommissionStatus.FAILURE.getCode());
				ldOrderStepDetail.setStatusDesc(paymentReturn.getFailMessage());
			} else {
				ldOrderStepDetail.setStatus(CommissionStatus.SUBMITTED.getCode());
				ldOrderStepDetail.setStatusDesc(paymentReturn.getFailMessage());
			}
			ldOrderStepService.updateById(ldOrderStepDetail);

			respstat = RespCode.success;
		} catch (Exception e) {
			logger.info(
					"????????????????????????" + userCommission.getOrderNo() + ",??????????????????" + ldOrderStepDetail.getStepOrderNo()
							+ ",??????????????????", e);
			respstat = RespCode.PAY_EXCEPTION;
			ldOrderStepDetail.setStatus(CommissionStatus.SUBMITTED.getCode());
			ldOrderStepDetail.setStatusDesc(CommissionStatus.SUBMITTED.getDesc());
			ldOrderStepService.updateById(ldOrderStepDetail);

		}

		return respstat;
	}

}
