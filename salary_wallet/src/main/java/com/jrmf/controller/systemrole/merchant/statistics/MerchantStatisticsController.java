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
	 * 批次用户列表
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
		String originalId = (String) request.getSession().getAttribute("customkey");// 商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String userName = (String) request.getParameter("userName");
		String userType = (String) request.getParameter("userType");
		String batcheId = (String) request.getParameter("batcheId");
//		String pageNo = (String) request.getParameter("pageNo");
		if (StringUtil.isEmpty(originalId) ||  StringUtil.isEmpty(batcheId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 删除单个用户信息
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
		String customkey = (String) request.getSession().getAttribute("customkey");// 商户标识
		String ids = (String) request.getParameter("ids");
		String batchId = (String) request.getParameter("batchId");
		logger.info("deleteUser方法  传参： ids=" + ids + "customkey=" + customkey);
		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(ids)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
	 * 删除批次信息
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
		// 商户标识
		String customkey = (String) request.getSession().getAttribute("customkey");
		String batchId = (String) request.getParameter("batchId");
		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		} else {
			try {
				commissionService.deleteByBatchId(batchId);
				channelHistoryService.deleteByOrderno(batchId);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口执行失败，请联系管理员！");
				logger.error(e.getMessage(), e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}

	/**
	 * 取消订单--删除单个佣金记录
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
		// 商户标识
		String originalId = (String) request.getSession().getAttribute("customkey");
		// 佣金id
		String id = (String) request.getParameter("id");
		// 批次Id
		String batchId = (String) request.getParameter("batchId");
		logger.info("deleteCommission方法  传参： id=" + id);
		if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(id) || StringUtil.isEmpty(batchId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数异常");
			return model;
		} else {
			try {
				commissionService.deleteById(Integer.parseInt(id));
				/**
				 * 更新批次金额信息
				 */
				commissionService.updateBatchMessage(batchId, originalId, model);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 佣金批次列表
	 */
	@RequestMapping(value = "/batch/listData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> ChannelHistory(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute("customkey");// 商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
		String name = (String) request.getParameter("name");
		String batchName = (String) request.getParameter("batchName");// 批次名称
		String batchDesc = (String) request.getParameter("batchDesc");// 批次说明
		String status = (String) request.getParameter("status");// 批次状态
		String payType = (String) request.getParameter("payType");// 支付通道
		String amount = (String) request.getParameter("amount");// 批次总金额
		String contentName = (String) request.getParameter("contentName");// 项目名称-->根据这个字段从custom_menu表中查询到id(menuId)
		String fileName = (String) request.getParameter("fileName");// 上传文件名称
		String recCustomkey = (String) request.getParameter("recCustomkey");// 服务公司id
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
			menuIds = menuIds.substring(0, menuIds.length() - 1);// 去掉最后的逗号
			parameterMap.put("menuId", menuIds);
		}
		parameterMap.put("fileName", fileName);
		parameterMap.put("recCustomkey", recCustomkey);
		logger.info("/channel/ChannelHistory方法  传参： customkey=" + originalId + "startTime=" + startTime + "endTime="
				+ endTime + "pageNo=" + pageNo + "status=" + status);
		if (StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 佣金批次汇总信息
	 */
	@RequestMapping(value = "/batch/summaryData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> summaryData(HttpServletRequest request, HttpServletResponse response) {
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute("customkey");// 商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String batchId = (String) request.getParameter("batchId");
		if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 佣金批次详情
	 */
	@RequestMapping(value = "/batch/detailData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String batchId = (String) request.getParameter("batchId");
		String userName = (String) request.getParameter("userName");// 收款人
		String status = (String) request.getParameter("status");// 订单状态
		String certId = (String) request.getParameter("cretId");// 身份证号
		String BankNo = (String) request.getParameter("BankNo");// 收款账号
		String amount = (String) request.getParameter("amount");// 订单总金额
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
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		return model;
	}

	/**
	 * 佣金批次详情--完成
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
				statusDesc = "验证通过";
			} else if (status1 == 1) {
				statusDesc = "发放成功";
			} else if (status1 == 2) {
				statusDesc = "发放失败-" + user.getRemark();
			} else if (status1 == 3) {
				statusDesc = "未签约";
			}
			strBuff.append(user.getUserName() == null ? "" : user.getUserName()).append(",")
			.append(user.getAccount() == null ? "" : user.getAccount()).append(",")
			.append(user.getCertId() == null ? "" : user.getCertId()).append(",")
			.append(user.getAmount() == null ? "" : user.getAmount()).append(",")
			.append(user.getOrderNo() == null ? "" : user.getOrderNo()).append(",").append(statusDesc);

			dataStr.add(strBuff.toString());
		}
		ArrayList<String> fieldName = new ArrayList<String>();
		fieldName.add("姓名");
		fieldName.add("银行卡号/支付宝账户");
		fieldName.add("身份证号码");
		fieldName.add("金额（元）");
		fieldName.add("订单号");
		fieldName.add("到账时间");
		fieldName.add("状态");
		String filename = today + "批量下发佣金批次详情";
		ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
	}

	/**
	 * 交易流水
	 */
	@RequestMapping(value = "/user/commissionData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionDetail(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String originalId = (String) request.getSession().getAttribute("customkey");// 商户标识
		String name = (String) request.getParameter("name");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
//		String pageNo = (String) request.getParameter("pageNo");
		logger.info("/user/commissionData 方法  传参： customkey=" + originalId);
		if (StringUtil.isEmpty(originalId)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
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
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 交易流水--汇总信息
	 */
	@RequestMapping(value = "/user/commissionSumData", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> commissionSumData(HttpServletRequest request,
			HttpServletResponse response) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();

		String customkey = (String) request.getSession().getAttribute("customkey");// 薪税服务公司标识
		String name = (String) request.getParameter("name");
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
		logger.info("/user/commissionSumData 方法  传参： customkey=" + customkey);
		if (StringUtil.isEmpty(customkey)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		} else {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("name", name);
				paramMap.put("startTime", startTime);
				paramMap.put("endTime", endTime);
				paramMap.put("status", 1);
				paramMap.put("originalId", customkey);
				// 发放成功总额（元）
				String successAmount = commissionService.getUserCommissionSum(paramMap);
				paramMap.put("status", 3);
				// 等待下发总额（元）
				String waitAmount = commissionService.getUserCommissionSum(paramMap);
				paramMap.put("status", 2);
				// 下发失败总额（元）
				String failureAmount = commissionService.getUserCommissionSum(paramMap);
				model.put("successAmount", successAmount == null ? "0.00" : successAmount);
				model.put("waitAmount", waitAmount == null ? "0.00" : waitAmount);
				model.put("failureAmount", failureAmount == null ? "0.00" : failureAmount);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
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
	 * 收款用户交易统计页面---用户交易统计
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
	 * 收款用户交易统计页面---用户交易统计明细
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
	 * 批次明细交易信息查询
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
		String userName = request.getParameter("userName");// 用户姓名
		String batchName = request.getParameter("batchName");// 批次名称
		String menuId = request.getParameter("menuId");// 项目编号
		String certId = request.getParameter("certId");// 身份证号
		String createTimeStart = request.getParameter("createTimeStart");// 创建时间起始
		String createTimeEnd = request.getParameter("createTimeEnd");// 创建时间结束
		String batchDesc = request.getParameter("batchDesc");// 批次说明
		String contentName = request.getParameter("contentName");// 项目名称
		String account = request.getParameter("account");// 收款账号
		String payType = request.getParameter("payType");// 支付通道
		if ("0".equals(payType)) {
			payType = null;
		}
		String companyId = request.getParameter("companyId");// 服务公司
		String status = request.getParameter("status");// 订单状态
		String amountStart = request.getParameter("amountStart");// 交易金额下限
		String amountEnd = request.getParameter("amountEnd");// 交易金额上限
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
	 * 批次明细交易信息查询
	 */
	@RequestMapping(value = "/group/commissionDetail", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> groupCommissionDetailResult(HttpServletRequest request,
			// 用户姓名
			@RequestParam(value = "userName", required = false) String userName,
			// 批次名称
			@RequestParam(value = "batchName", required = false) String batchName,
			// 项目编号
			@RequestParam(value = "menuId", required = false) String menuId,
			// 身份证号
			@RequestParam(value = "certId", required = false) String certId,
			// 创建时间起始
			@RequestParam(value = "createTimeStart", required = false) String createTimeStart,
			// 创建时间结束
			@RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
			// 批次说明
			@RequestParam(value = "batchDesc", required = false) String batchDesc,
			// 项目名称
			@RequestParam(value = "contentName", required = false) String contentName,
			// 收款账号
			@RequestParam(value = "account", required = false) String account,
			// 支付通道
			@RequestParam(value = "payType", required = false) String payType,
			// 服务公司
			@RequestParam(value = "companyId", required = false) String companyId,
			// 订单状态
			@RequestParam(value = "status", required = false) String status,
			// 交易金额下限
			@RequestParam(value = "amountStart", required = false) String amountStart,
			// 交易金额上限
			@RequestParam(value = "amountEnd", required = false) String amountEnd,
			// 当前页码
			@RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
			// 每页条数
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam("nodeId") int nodeId,
			@RequestParam("customType") int customType,
			@RequestParam(required = false) String customKey,
			@RequestParam(required = false) String customName) {

		if (customKey == null) {
			customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
		}

		ChannelCustom custom = customService.getCustomByCustomkey(customKey);
		//判断当前的节点是不是关联性代理商
		if (custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1) {
			logger.info("当前点击商户{}是关联性代理商", custom.getCompanyName());
			customType = CustomType.PROXYCHILDEN.getCode();
			//关联性代理的关联关系在custom_proxy_childen,防止传递过来的nodeId是其他关联关系表的
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
	 * 批次明细交易信息查询
	 */
	@RequestMapping(value = "/group/commissionDetail/excel")
	public void groupCommissionDetailResultExcel(HttpServletRequest request,
			HttpServletResponse response,
			// 用户姓名
			@RequestParam(value = "userName", required = false) String userName,
			// 批次名称
			@RequestParam(value = "batchName", required = false) String batchName,
			// 项目编号
			@RequestParam(value = "menuId", required = false) String menuId,
			// 身份证号
			@RequestParam(value = "certId", required = false) String certId,
			// 创建时间起始
			@RequestParam(value = "createTimeStart", required = false) String createTimeStart,
			// 创建时间结束
			@RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
			// 批次说明
			@RequestParam(value = "batchDesc", required = false) String batchDesc,
			// 项目名称
			@RequestParam(value = "contentName", required = false) String contentName,
			// 收款账号
			@RequestParam(value = "account", required = false) String account,
			// 支付通道
			@RequestParam(value = "payType", required = false) String payType,
			// 服务公司
			@RequestParam(value = "companyId", required = false) String companyId,
			// 订单状态
			@RequestParam(value = "status", required = false) String status,
			// 交易金额下限
			@RequestParam(value = "amountStart", required = false) String amountStart,
			// 交易金额上限
			@RequestParam(value = "amountEnd", required = false) String amountEnd,
			@RequestParam("nodeId") int nodeId,
			@RequestParam("customType") int customType,
			@RequestParam(required = false) String customKey,
			@RequestParam(required = false) String customName) throws Exception {

		if (customKey == null) {
			customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
		}

		ChannelCustom custom = customService.getCustomByCustomkey(customKey);
		//判断当前的节点是不是关联性代理商
		if (custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1) {
			logger.info("当前点击商户{}是关联性代理商", custom.getCompanyName());
			customType = CustomType.PROXYCHILDEN.getCode();
			//关联性代理的关联关系在custom_proxy_childen,防止传递过来的nodeId是其他关联关系表的
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
		String[] columnName = new String[]{"商户名称", "项目名称", "订单ID", "收款人姓名", "证件类型"
				, "证件号", "手机号", "收款账号","交易金额", "到账金额", "服务费率", "服务费(包含补差价)"
				, "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "订单备注"
				, "下发通道", "服务公司", "账号所属金融机构", "交易时间", "批次名称"
				, "批次说明", "交易结果描述", "操作账号", "复核账号", "最后更新时间"};
		String filename = "批次明细信息交易统计表";
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
	 * 批次明细交易信息查询(超级管理员权限)
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
			result.put(RespCode.RESP_MSG, "此功能为超级管理员所有，请核实身份。");
			return result;
		}
		// 用户姓名
		String userName = request.getParameter("userName");
		//服务公司id
		String companyId = request.getParameter("companyId");
		// 批次名称
		String batchName = request.getParameter("batchName");
		// 身份证号
		String certId = request.getParameter("certId");
		// 创建时间起始
		String createTimeStart = request.getParameter("createTimeStart");
		// 创建时间结束
		String createTimeEnd = request.getParameter("createTimeEnd");
		// 批次说明
		String batchDesc = request.getParameter("batchDesc");
		// 项目名称
		String contentName = request.getParameter("contentName");
		// 收款账号
		String account = request.getParameter("account");
		// 支付通道
		String payType = request.getParameter("payType");
		if ("0".equals(payType)) {
			payType = null;
		}
		// 订单状态
		String status = request.getParameter("status");
		// 交易金额下限
		String amountStart = request.getParameter("amountStart");
		// 交易金额上限
		String amountEnd = request.getParameter("amountEnd");
		// 商户名称
		String customName = request.getParameter("customName");
		// 当前页码
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
	 * 交易统计---项目维度
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
	 * 交易统计---项目维度--详情
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
		logger.info("调用银企直连交易历史查询接口--timeStart=" + timeStart + " timeEnd=" + timeEnd + " pageNo=" + pageNo);
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
	 * 商户交易统计
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

		//只有超管有权限
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
	 * 商户交易统计导出
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

		String[] colunmName = new String[]{"商户名称", "商户类型", "交易总金额", "成功交易总笔数", "代理商名称","最后下发时间", "充值确认总金额", "当前可用余额", "商户所属的业务平台方", "商户Key","销售经理","运营经理"};
		String filename = "商户交易统计表";
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
	 * 代理商交易统计
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
					return returnFail(RespCode.error101, "权限错误");
				}
			} else {
				return returnFail(RespCode.error101, "权限错误");
			}
		}


		//代理商查询自己的
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
	 * 代理商交易统计导出
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

		//代理商查询自己的
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

		String[] colunmName = new String[]{"商户名称", "商户类型", "服务公司", "交易总金额", "成功交易总笔数", "代理商名称", "充值确认总金额", "当前可用余额", "商户Key"};
		String filename = "代理商交易统计表";
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
	 * 联动步骤明细
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
		//获取商户用户分配权限模板列表.
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
	 * 重发
	 * @return
	 */
//	@RequestMapping("/againPay")
//	@ResponseBody
//	public Map<String, Object> againPay(LdOrderStep ldOrderStep){
//		logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"执行重发");
//		int respstat = RespCode.success;
//		HashMap<String, Object> result = new HashMap<>();
//		try{
//			if(!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())){
//				LdOrderStep ldOrderStepDetail = ldOrderStepService.getOrderStep(ldOrderStep.getStepOrderNo());
//				UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
//				if(StringUtil.isEmpty(userCommission.getBatchId())){
//					if(userCommission.getStatus()==3){
//						logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"为api模式");
//						//api联动重发
//						//上笔步骤编号
//						Integer stepOrder = ldOrderStepDetail.getStepOrder()-1;
//						Map<String, Object> params = new HashMap<String, Object>();
//						params.put("orderNo", ldOrderStepDetail.getOrderno());
//						params.put("stepOrder", stepOrder);
//						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);
//						if(ldPreOrderStep.getStatus()==1&&ldOrderStepDetail.getStatus()==2){
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态满足重发");
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"重发执行");
//							respstat = againPayDo(ldOrderStepDetail);
//						}else{
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝重发");
//							respstat = RespCode.PRE_STATUS_ERROR;
//						}
//					}else{
//						respstat = RespCode.NOT_SUPPORT_STATUS;
//						logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝重发");
//					}
//				}else{
//					ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(userCommission.getBatchId());
//					if(channelHistory.getStatus()==3&&userCommission.getStatus()==3){
//						logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"为web模式");
//						//web联动重发
//						//上笔步骤编号
//						Integer stepOrder = ldOrderStepDetail.getStepOrder()-1;
//						Map<String, Object> params = new HashMap<String, Object>();
//						params.put("orderNo", ldOrderStepDetail.getOrderno());
//						params.put("stepOrder", stepOrder);
//						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);
//						if(ldPreOrderStep.getStatus()==1&&ldOrderStepDetail.getStatus()==2){
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态满足重发");
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"重发执行");
//							respstat = againPayDo(ldOrderStepDetail);
//						}else{
//							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝重发");
//							respstat = RespCode.PRE_STATUS_ERROR;
//						}
//					}else{
//						logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝重发");
//						respstat = RespCode.NOT_SUPPORT_STATUS;
//					}
//				}
//			}else{
//				logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"不存在");
//				respstat = RespCode.error101;
//			}
//		}catch(Exception e){
//			respstat = RespCode.PAY_EXCEPTION;
//			logger.error("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"联动重发异常",e);
//		}
//		result.put(RespCode.RESP_STAT, respstat);
//		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
//		return result;
//
//	}

	/**
	 * 平台模式重发
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
//			//获取联动配置
//			CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
//			//收款账户
//			PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),ldOrderStepDetail.getPathno());
//			logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"获取的配置信息:"+paymentConfig.toString());
//			if(paymentConfig != null){
//				Company company2 = companyService.getCompanyByUserId(Integer.parseInt(conLdConfig.getCompanyidTwo()));
//				//调用支付通道工厂模式
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
//						logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+",交易走当板");
//						//挡板有效
//						paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS,"交易成功",ldOrderStepDetail.getStepOrderNo());
//					}else{
//						paymentReturn = proxy.paymentTransfer(userCommission2);
//
//					}
//				}else{
//					logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+",付款失败：账号所属银行信息获取失败");
//					paymentReturn = new PaymentReturn<>(PayRespCode.RESP_FAILURE,"付款失败：账号所属银行信息获取失败",ldOrderStepDetail.getStepOrderNo());
//				}
//				//付款受理成功
//				if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())|| PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
//					logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+",付款受理成功");
//					if(ldOrderStepDetail.getBusinessType()==2||ldOrderStepDetail.getBusinessType()==4){
//						customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStepDetail.getIssuedCompanyid(),
//								userCommission.getOriginalId(),
//								userCommission.getCertId(),
//								ldOrderStepDetail.getAmount(),
//								true);
//						logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"更新累计金额：本次累计{}元", ldOrderStepDetail.getAmount());
//					}
//					ldOrderStepDetail.setStatus(3);
//					ldOrderStepDetail.setStatusDesc("处理中");
//					ldOrderStepService.updateById(ldOrderStepDetail);
//				}else{
//					ldOrderStepDetail.setStatus(2);
//					failMessage = paymentReturn.getFailMessage();
//					logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+",付款受理失败，失败原因："+failMessage);
//					if (failMessage.length() > 200) {
//						failMessage = failMessage.substring(0, 200);
//					} else if (failMessage.length() == 0) {
//						failMessage = RespCode.CONNECTION_ERROR;
//					}
//					ldOrderStepDetail.setStatusDesc(failMessage);
//					ldOrderStepService.updateById(ldOrderStepDetail);
//				}
//			}else{
//				logger.error("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"下发上送过程异常：未配置商户下发通道路由");
//				logger.error("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + userCommission.getOriginalId() + "--服务公司ID："+ ldOrderStepDetail.getIssuedCompanyid());
//				ldOrderStepDetail.setStatus(2);
//				ldOrderStepDetail.setStatusDesc("失败-未配置商户下发通道路由");
//				ldOrderStepService.updateById(ldOrderStepDetail);
//			}
//			respstat = RespCode.success;
//		}catch(Exception e){
//			logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+",付款受理异常",e);
//			respstat = RespCode.PAY_EXCEPTION;
//			ldOrderStepDetail.setStatus(3);
//			ldOrderStepDetail.setStatusDesc("处理中");
//			ldOrderStepService.updateById(ldOrderStepDetail);
//			//更新下发累计金额
//			if(ldOrderStepDetail.getBusinessType()==2||ldOrderStepDetail.getBusinessType()==4){
//				customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStepDetail.getIssuedCompanyid(),
//						userCommission.getOriginalId(),
//						String.valueOf(userCommission.getCertId()),
//						ldOrderStepDetail.getAmount(),
//						true);
//				logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"更新累计金额：本次累计{}元", ldOrderStepDetail.getAmount());
//			}
//		}
//		return respstat;
//	}

//	/**
//	 * 同步状态
//	 * @return
//	 */
//	@RequestMapping("/queryStatus")
//	@ResponseBody
//	public Map<String, Object> queryStatus(LdOrderStep ldOrderStep){
//		logger.info("交易明细步骤订单号："+ldOrderStep.getStepOrderNo()+",同步状态");
//		int respstat = RespCode.success;
//		HashMap<String, Object> result = new HashMap<>();
//		PaymentReturn<TransStatus> paymentReturn = null;
//		try{
//			if(!StringUtil.isEmpty(ldOrderStep.getStepOrderNo())){
//				LdOrderStep ldOrderStepDetail = ldOrderStepService.getOrderStep(ldOrderStep.getStepOrderNo());
//				if(ldOrderStepDetail.getStatus()==3){
//					UserCommission userCommission = commissionService.getUserCommission(ldOrderStepDetail.getOrderno());
//					//收款账户
//					PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),ldOrderStepDetail.getPathno());
//					logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"获取的配置信息:"+paymentConfig.toString());
//					if(paymentConfig != null){
//						if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(userCommission.getOriginalId())) {//挡板有效
//							logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+",查询走当板");
//							TransStatus transStatus1 = new TransStatus( ldOrderStepDetail.getStepOrderNo(), PayRespCode.RESP_TRANSFER_SUCCESS,"付款成功");
//							paymentReturn = new PaymentReturn<TransStatus>(PayRespCode.RESP_SUCCESS,"查询成功",transStatus1);
//						}else{
//							//调用支付通道工厂模式
//							Payment payment = PaymentFactory.paymentEntity(paymentConfig);
//							PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
//							Payment proxy = paymentProxy.getProxy();
//							paymentReturn = proxy.queryTransferResult(ldOrderStepDetail.getStepOrderNo());
//						}
//						logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"查询下发操作执行结果----------：" + paymentReturn.toString());
//						if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
//							TransStatus transStatus = paymentReturn.getAttachment();
//							logger.error("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"查询结果--------resultCode:" + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
//							if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
//								logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"付款成功");
//								ldOrderStepDetail.setStatus(1);
//								ldOrderStepDetail.setStatusDesc("成功");
//								ldOrderStepService.update(ldOrderStepDetail);
//							} else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
//								logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"付款失败，失败原因："+transStatus.getResultMsg());
//								ldOrderStepDetail.setStatus(2);
//								String errorMsg = transStatus.getResultMsg();
//								if(errorMsg !=null){
//									if (errorMsg.contains("余额")) {
//										errorMsg = "网络异常，请联系管理员";
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
//									logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"更新累计金额：减去累计{}元", ldOrderStepDetail.getAmount());
//								}
//
//							} else {
//								logger.error("未知错误---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
//								ldOrderStepDetail.setStatus(3);
//								ldOrderStepDetail.setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());
//								ldOrderStepService.update(ldOrderStepDetail);
//							}
//							int totalCount = ldOrderStepService.getCountByOrderNo(ldOrderStepDetail.getOrderno());
//							int successCount = ldOrderStepService.getCountSuccessByOrderNo(ldOrderStepDetail.getOrderno());
//							int failCount = ldOrderStepService.getCountFailByOrderNo(ldOrderStepDetail.getOrderno());
//							if(successCount==totalCount){
//								logger.info("交易明细订单号："+userCommission.getOrderNo()+"联动明细步骤全部为成功，更新交易明细订单为成功");
//								userCommission.setStatus(1);
//								userCommission.setStatusDesc("成功");
//								commissionService.updateUserCommissionById(userCommission);
//								if(StringUtil.isEmpty(userCommission.getBatchId())){
//									transferDealStatusNotifier.notify(userCommission.getOrderNo(), TransferStatus.TRANSFER_DONE,CommonRetCodes.ACTION_DONE.getCode(),CommonRetCodes.ACTION_DONE.getDesc());
//									logger.info("交易明细订单号："+userCommission.getOrderNo()+",交易成功！系统内部回调成功！");
//								}
//							}else if(failCount==totalCount){
//								logger.info("交易明细订单号："+userCommission.getOrderNo()+"联动明细步骤全部为失败，更新交易明细订单为失败");
//								userCommission.setStatus(2);
//								userCommission.setStatusDesc("交易失败");
//								commissionService.updateUserCommissionById(userCommission);
//								//退款
//								if(StringUtil.isEmpty(userCommission.getBatchId())){
//									updateBalance(userCommission,userCommission.getCompanyId(),CommonString.REFUND);
//									logger.info("退款成功！订单号{}",userCommission.getOrderNo());
//									transferDealStatusNotifier.notify(userCommission.getOrderNo(), TransferStatus.TRANSFER_FAILED,CommonRetCodes.UNCATCH_ERROR.getCode(),userCommission.getStatusDesc());
//									logger.info("交易明细订单号："+userCommission.getOrderNo()+",交易失败！系统内部回调成功！");
//								}
//							}
//							logger.info("明细落地修改操作完成 orderNo[{}]", ldOrderStepDetail.getStepOrderNo());
//						} else {
//							respstat = RespCode.QUERY_FAIL;
//							logger.error("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"查询失败---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
//						}
//					}else{
//						logger.info("交易明细订单号："+userCommission.getOrderNo()+",步骤订单号："+ldOrderStepDetail.getStepOrderNo()+"查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + userCommission.getOriginalId() + "--服务公司ID："+ ldOrderStep.getIssuedCompanyid());
//					}
//				}else{
//					respstat = RespCode.NOT_SUPPORT_STATUS;
//					logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足");
//				}
//			}else{
//				respstat = RespCode.error101;
//				logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"参数异常");
//			}
//		}catch(Exception e){
//			logger.error("联动同步状态异常",e);
//			respstat = RespCode.SURE_STATUS;
//		}
//		result.put(RespCode.RESP_STAT, respstat);
//		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
//		return result;
//	}

	// 扣款或退款
	private void updateBalance(UserCommission transferParam,String companyId, int operating) {
		Map<String, Object> params = new HashMap<>(5);
		BigDecimal Magnification = new BigDecimal(operating * 100);
		params.put(CommonString.CUSTOMKEY, transferParam.getOriginalId());
		params.put(CommonString.COMPANYID, companyId);
		params.put(CommonString.PAYTYPE, transferParam.getPayType());
		params.put(CommonString.BALANCE,(new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(Magnification));
		customBalanceDao.updateBalance(params);
		logger.info("订单号{}"+transferParam.getOrderNo()+"操作金额："+(new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(new BigDecimal(operating))+"元");
	}

	/**
	 * 联动冲正明细
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
		//获取商户用户分配权限模板列表.
		List<Map<String, Object>> relationList = ldOrderCorrectService.queryLdCorrectOrderDetailList(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 冲正
	 * @return
	 */
	@RequestMapping("/correct")
	@ResponseBody
	public Map<String, Object> correct(LdOrderStep ldOrderStep){
		logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"执行冲正");
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
						//下笔步骤编号
						Integer stepOrder = ldOrderStepDetail.getStepOrder()+1;
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orderNo", ldOrderStepDetail.getOrderno());
						params.put("stepOrder", stepOrder);
						LdOrderStep ldNextOrderStep = ldOrderStepService.getPreStepOrder(params);
						if(ldNextOrderStep.getStatus()==2){
							if(ldOrderStepDetail.getCorrectStatus()!=null&&ldOrderStepDetail.getCorrectStatus()!=0){
								respstat = RespCode.NOT_SUPPORT_STATUS;
								errMsg="冲正状态不为待处理无法再次操作";
							}else{
								Map<String, Object> configParams = new HashMap<String, Object>();
								configParams.put("customkey", userCommission.getOriginalId());
								configParams.put("companyId", userCommission.getCompanyId());
								//获取联动配置
								CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(configParams);
								if(ldOrderStepDetail.getIssuedCompanyid().equals(conLdConfig.getCompanyidOne())){
									payCompanyId=conLdConfig.getCompanyidTwo();
								}else{
									payCompanyId=conLdConfig.getCompanyidOne();
								}
								companyPay = companyService.getCompanyByUserId(Integer.parseInt(payCompanyId));
								receiveCompanyPay = companyService.getCompanyByUserId(Integer.parseInt(ldOrderStepDetail.getIssuedCompanyid()));
								//付款通道配置信息
								PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(userCommission.getPayType()), userCommission.getOriginalId(), payCompanyId,conLdConfig.getPathno());
								//添加冲正信息
								LdOrderCorrect correct = addCorrectOrder(payCompanyId, companyPay,receiveCompanyPay, ldOrderStepDetail);
								ldOrderStepDetail.setIsCorrect(1);
								ldOrderStepDetail.setCorrectStatus(0);
								ldOrderStepService.updateById(ldOrderStepDetail);
								if(paymentConfig != null){
									//调用支付通道工厂模式
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
											logger.info("步骤订单号："+correct.getStepOrderNo()+",冲正订单号："+correct.getCorrectOrderNo()+",交易走当板");
											//挡板有效
											paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS,"交易成功",correct.getCorrectOrderNo());
										}else{
											paymentReturn = proxy.paymentTransfer(userCommission2);

										}
									}else{
										logger.info("步骤订单号："+correct.getStepOrderNo()+",冲正订单号："+correct.getCorrectOrderNo()+",付款失败：账号所属银行信息获取失败");
										paymentReturn = new PaymentReturn<>(PayRespCode.RESP_FAILURE,"付款失败：账号所属银行信息获取失败",correct.getCorrectOrderNo());
									}
									//付款受理成功
									if(PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())|| PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
										correct.setStatus(3);
										correct.setStatusDesc("处理中");
										ldOrderCorrectService.updateByPrimaryKeySelective(correct);
										ldOrderStepDetail.setCorrectStatus(3);
										ldOrderStepService.updateById(ldOrderStepDetail);
									}else{
										correct.setStatus(2);
										failMessage = paymentReturn.getFailMessage();
										logger.info("步骤订单号："+correct.getStepOrderNo()+",冲正订单号："+correct.getCorrectOrderNo()+",付款受理失败，失败原因："+failMessage);
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
										errMsg="冲正失败，通道受理失败";
									}
								}else{
									logger.error("步骤订单号："+correct.getStepOrderNo()+",冲正订单号："+correct.getCorrectOrderNo()+"查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + userCommission.getOriginalId() + "--服务公司ID："+ ldOrderStepDetail.getIssuedCompanyid());
									correct.setStatus(2);
									correct.setStatusDesc("失败-未配置商户下发通道路由");
									ldOrderCorrectService.updateByPrimaryKeySelective(correct);
									ldOrderStepDetail.setCorrectStatus(2);
									ldOrderStepService.updateById(ldOrderStepDetail);
									respstat = RespCode.PAY_EXCEPTION;
									errMsg="冲正失败，未配置商户下发通道路由";
								}
							}
						}else{
							respstat = RespCode.NOT_SUPPORT_STATUS;
							errMsg="转账下发不为失败,无法冲正";
							logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝冲正");
						}
					}else{
						respstat = RespCode.NOT_SUPPORT_STATUS;
						errMsg="转账状态不为成功,无法冲正";
						logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"状态不满足，拒绝冲正");
					}
				}else{
					//业务类型不满足
					respstat = RespCode.BUSINESS_TYPE_NOT;
					errMsg="业务类型不为转账,无法冲正";
					logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"业务类型不满足，拒绝冲正");
				}
			}else{
				logger.info("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"不存在");
				respstat = RespCode.error101;
			}
		}catch(Exception e){
			respstat = RespCode.PAY_EXCEPTION;
			logger.error("交易明细步骤订单订单号："+ldOrderStep.getStepOrderNo()+"联动冲正异常",e);
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
		correct.setStatusDesc("待处理");
		correct.setStepOrderNo(ldOrderStepDetail.getStepOrderNo());
		ldOrderCorrectService.insert(correct);
		return correct;
	}


	/**
	 * api拆单步骤明细
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
		//获取商户用户分配权限模板列表.
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
	 * api拆单步骤重发
	 *
	 * @return
	 */
	@RequestMapping("/splitOrderAgainPay")
	@ResponseBody
	public Map<String, Object> splitOrderAgainPay(LdOrderStep ldOrderStep) {
		int respstat = 0;
		logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "执行重发");
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
						logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "为api拆单模式");

						Integer stepOrder = ldOrderStepDetail.getStepOrder() == 1 ? 2 : 1;
						Map<String, Object> params = new HashMap<>();
						params.put("orderNo", ldOrderStepDetail.getOrderno());
						params.put("stepOrder", stepOrder);
						LdOrderStep ldPreOrderStep = ldOrderStepService.getPreStepOrder(params);

						if (LdOrderStatusEnum.FAILURE.getCode() == ldOrderStepDetail.getStatus()
								&& LdOrderStatusEnum.SUCCESS.getCode() == ldPreOrderStep.getStatus()) {
							logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "状态满足重发");
							logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "重发执行");

							if (!utilCacheManager.lockWithTimeout(key, 1000, 600000)) {
								return returnFail(RespCode.error101, "请勿频繁重发，十分钟后再试!");
							}
							respstat = againPayDoBySplit(ldOrderStepDetail);
						} else {
							logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "状态不满足，拒绝重发");
							respstat = RespCode.PRE_STATUS_ERROR;
						}
					} else {
						respstat = RespCode.NOT_SUPPORT_STATUS;
						logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "状态不满足，拒绝重发");
					}
				}
			} else {
				logger.info("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "不存在");
				respstat = RespCode.error101;
			}
		} catch (Exception e) {
			respstat = RespCode.PAY_EXCEPTION;
			logger.error("交易明细步骤订单订单号：" + ldOrderStep.getStepOrderNo() + "联动重发异常", e);
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;

	}

	/**
	 * api拆单重发
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

			//收款账户
			PaymentConfig paymentConfig = companyService
					.getPaymentConfigInfoPlus(String.valueOf(userCommission.getPayType()),
							userCommission.getOriginalId(), ldOrderStepDetail.getIssuedCompanyid(),
							ldOrderStepDetail.getIssuedRealCompanyId(),
							ldOrderStepDetail.getPathno());
			logger.info(
					"交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStepDetail.getStepOrderNo()
							+ "获取的配置信息:" + paymentConfig.toString());
			//调用支付通道工厂模式
			Payment payment = PaymentFactory.paymentEntity(paymentConfig);
			PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME,
					utilCacheManager);
			Payment proxy = paymentProxy.getProxy();

			UserCommission ldOrderStepCommission = ldOrderStepDetail.toUserCommission(userCommission);

			if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K"
					.equals(userCommission.getOriginalId())) {
				logger.info("交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStepDetail
						.getStepOrderNo() + ",交易走当板");
				//挡板有效
				paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS, "交易成功",
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
					"交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStepDetail.getStepOrderNo()
							+ ",付款受理异常", e);
			respstat = RespCode.PAY_EXCEPTION;
			ldOrderStepDetail.setStatus(CommissionStatus.SUBMITTED.getCode());
			ldOrderStepDetail.setStatusDesc(CommissionStatus.SUBMITTED.getDesc());
			ldOrderStepService.updateById(ldOrderStepDetail);

		}

		return respstat;
	}

}
