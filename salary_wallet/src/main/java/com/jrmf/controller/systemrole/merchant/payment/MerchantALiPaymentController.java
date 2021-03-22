package com.jrmf.controller.systemrole.merchant.payment;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.threadpool.ThreadUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

@Controller
@RequestMapping("/merchant/aliPay")
public class MerchantALiPaymentController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(MerchantALiPaymentController.class);
	@Autowired
	protected UserSerivce userSerivce;
	@Autowired
	protected CustomInfoService customInfoService;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private ChannelHistoryService channelHistoryService;
	@Autowired
	private UserRelatedService userRelatedService;
	@Autowired
	private OrderNoUtil orderNoUtil;
	@Autowired
	private ChannelRelatedService channelRelatedService;
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private AlipayService  alipayServiceImpl;
	@Autowired
	private BaseInfo baseInfo;

	/**
	 * 说明: 导入支付宝资金下发批次信息
	 *
	 * @param request
	 * @param response
	 * @param file
	 * @return:
	 */
	@RequestMapping("/alipay/inputBatchInfo")
	@ResponseBody
	public Map<String, Object> alipayInputBatchInfo(HttpServletRequest request, HttpServletResponse response,
													MultipartFile file) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>();
		String customkey = (String) request.getSession().getAttribute("customkey");// 商户标识
		String companyId = request.getParameter("companyId");
		ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey,companyId);
		ChannelCustom loginUser = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");
		String operatorName = loginUser.getUsername();//操作人
		int importCount = 0;// 导入数据
		long begin = System.currentTimeMillis();// 开始时间
		if (StringUtil.isEmpty(customkey)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}
		if(channelRelated == null){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "未配置服务公司");
			return model;
		}
		try {
			if (file != null) {
				InputStream is = file.getInputStream();
				Workbook workbook = null;
				try {
					workbook = new XSSFWorkbook(is);
				} catch (Exception ex) {
					workbook = new HSSFWorkbook(is);
				}
				String amountSum = "0.00";// 批次总金额
				String serviceFee = "0.00";// 批次服务费
				String mfkjServiceFee = "0.00";// 批魔方次服务费
				String handleAmount = "0.00";// 批次应付款金额
				Sheet sheet = workbook.getSheetAt(0);
				int number = 0;
				/**
				 * 验证数据amount,idCard, bankCard, mobileNo, name
				 */
				int num = 0;
				Set<String> result = new HashSet<String>();
				for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
					XSSFRow row = (XSSFRow) sheet.getRow(j);
					if(row==null){
						continue;
					}
					if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
						continue;
					}
					++num;
					if(num>2000 || num<0){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG, "导入数据不得大于2000条或小于0条，请重新导入！");
						return model;
					}
					String msg = commissionService.isValidateData(
							StringUtil.getXSSFCell(row.getCell(1)),
							StringUtil.getXSSFCell(row.getCell(3)),
							null,
							null,
							StringUtil.getXSSFCell(row.getCell(0)));
					String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
					String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));//身份证号码
					if(StringUtil.isEmpty(userName+cardNo)){
						continue;
					}
					if(!result.add(userName+cardNo)){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG,userName +":数据重复，请修改后再上传！");
						return model;
					}
					if(!StringUtil.isEmpty(msg)){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG,userName +":"+ msg);
						return model;
					}
				}
				String batchId = orderNoUtil.getChannelSerialno();// 临时批次号
				model.put("batchId", batchId);

				int userId = 0;
				//存放批次详情，进行批处理
				List<UserCommission> commissionBatch = new ArrayList<UserCommission>();
				for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
					XSSFRow row = (XSSFRow) sheet.getRow(j);
					if (row == null) {
						continue;
					}
					if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
						continue;
					}
					number++;
					String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
					String amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 金额(必要)
					String alipayAccount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));
					String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 身份证号
					String remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));
					// 信息校验
					if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(amount) || StringUtils.isEmpty(cardNo)
							|| StringUtils.isEmpty(alipayAccount)) {
						createErrorCommion(amount, customkey, "", batchId, channelRelated.getMerchantId(), "",
								"", "", channelRelated.getCompanyId(), "信息不完整",remark, alipayAccount,userName,cardNo);
						continue;
					}
					//用户查重
					Map<String,Object> paramMap= new HashMap<String,Object>();
					paramMap.put("userName",userName);
					paramMap.put("certId",cardNo);
					User user = userSerivce.getUsersCountByCard(paramMap);
					if (user == null) {
						User user_info = new User();
						user_info.setAccount(alipayAccount);
						user_info.setUserName(userName);
						user_info.setCertId(cardNo);
						user_info.setCreateTime(sdf.format(new Date()));
						user_info.setUserType(11);
						user_info.setCompanyUserNo(channelRelated.getCompanyId());
						user_info.setMerchantId(channelRelated.getMerchantId());
						userSerivce.addUser(user_info);
						userId = user_info.getId();

						/**
						 * 插入用户中间表关联
						 */
						UserRelated userRelated = new UserRelated();
						//初始状态：未开户
						userRelated.setStatus(0);
						userRelated.setCreateTime(DateUtils.getNowDate());
						userRelated.setOriginalId(customkey);
						userRelated.setUserId(userId);
						userRelated.setCompanyId(channelRelated.getCompanyId());
						userRelatedService.createUserRelated(userRelated);
					}else{
						userId = user.getId();
						UserRelated related = userRelatedService.getRelatedByUserId(userId+"", customkey);
						if(related==null){
							UserRelated userRelated = new UserRelated();
							//初始状态：未开户
							userRelated.setStatus(-1);
							userRelated.setCreateTime(DateUtils.getNowDate());
							userRelated.setOriginalId(customkey);
							userRelated.setUserId(userId);
							userRelated.setCompanyId(channelRelated.getCompanyId());
							userRelatedService.createUserRelated(userRelated);
						}
					}

					if (Double.parseDouble(amount) > 200000.00) {
						createErrorCommion(amount, customkey, userId + "", batchId, channelRelated.getMerchantId(), "",
								"", "", channelRelated.getCompanyId(), "单笔转账金额超限（大于20万）。",remark, alipayAccount, userName,
								cardNo);
						respstat = RespCode.error101;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG, "单笔转账金额超过20万,请重新导入.");
						return model;
					}
					String commissionMfkjFree = "0";
					UserCommission commission = new UserCommission();
					// 利润
					if (ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit()) < 0) {
						commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltLower(), 2);
						commission.setProfilt(channelRelated.getProfiltLower());
					} else {
						commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltUpper(), 2);
						commission.setProfilt(channelRelated.getProfiltUpper());
					}
					// 服务费
					String commissionAygFree = ArithmeticUtil.mulStr(amount, channelRelated.getServiceRates(), 2);
					amountSum = ArithmeticUtil.addStr(amountSum, amount);
					serviceFee = ArithmeticUtil.addStr(serviceFee, commissionAygFree);
					mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, commissionMfkjFree);

					commission.setAmount(amount);
					commission.setCreatetime(DateUtils.getNowDate());
					commission.setUserId(userId + "");
					commission.setStatus(0);
					commission.setBatchId(batchId);
					commission.setOriginalId(customkey);
					commission.setMerchantId(channelRelated.getMerchantId());
					commission.setCompanyId(channelRelated.getCompanyId());
					commission.setOrderNo(orderNoUtil.getChannelSerialno());
					commission.setOperatorName(operatorName);//设置操作人
					commission.setStatusDesc("待提交");
					commission.setSumFee(commissionAygFree);
					commission.setProfiltFree(commissionMfkjFree);
					commission.setCalculationRates(channelRelated.getServiceRates());
					commission.setAccount(alipayAccount);
					commission.setPayType(2);
					commission.setInvoiceStatus(2);//未开票
					commissionBatch.add(commission);
					importCount++;
				}
				if(commissionBatch.size() != 0){
					commissionService.addUserCommissionBatch(commissionBatch);
				}
				handleAmount = ArithmeticUtil.addStr(amountSum, serviceFee);//总金额
				/**
				 * 保存临时批次信息
				 */
				ChannelHistory history = new ChannelHistory();
				history.setAmount(amountSum);
				history.setCustomkey(customkey);
				history.setRecCustomkey(channelRelated.getCompanyId());
				history.setOrdername("佣金发放");
				history.setOrderno(batchId);
				history.setStatus(0);
				history.setPayType(2);
				history.setOperatorName(operatorName);//设置操作人
				history.setTransfertype(2);
				history.setServiceFee(serviceFee);//服务费
				history.setPassNum(importCount);
				history.setFailedNum(number-importCount);
				history.setBatchNum(number);
				history.setHandleAmount(handleAmount);
				channelHistoryService.addChannelHistory(history);

				long end = System.currentTimeMillis();
				double second = (end - begin) / 1000;// 处理所用时间秒。
				logger.info("支付宝下发导入用时："+second+"秒");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.error(e.getMessage());
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "导入失败");
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "导入成功");
		logger.info("返回结果：" + model);
		return model;
	}

	
	
	
	/**
	 * 说明:调用支付宝接口进行资金下发 生成批次订单
	 *
	 * @param response
	 * @return:
	 */
	@RequestMapping(value = "/alipay/batchTransferAccounts",method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> batchTransferAccounts(HttpServletRequest request, HttpServletResponse response) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// 商户标识originalId
		String remark = (String) request.getParameter("remark");// 备注信息
		String batchId = (String) request.getParameter("batchId");// 临时批次号
		String tranPassword = (String) request.getParameter("tranPassword");//交易密码
		ChannelCustom loginUser = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");
		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId) || StringUtil.isEmpty(tranPassword)) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数异常");
			return model;
		}
		logger.info("/channel/commonOption 方法  传参： customkey=" + customkey + ",batchId=" + batchId );

		/**
		 * 验证交易密码
		 */
		if(loginUser.getTranPassword() != null && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword,customkey))){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "交易密码错误！");
			return model;
		}
		// 核对总金额服务费等
		String amountSum = "0.00";// 批次总金额
		String mfkjServiceFee = "0.00";// 批魔方次服务费
		String batchServiceFee = "0.00";// 总服务费

		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchId);
		//获取代下发详情信息
		List<UserCommission> list = commissionService.getUserCommissionByParam(map);
		ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey,list.get(0).getCompanyId());
		ChannelCustom custom = customService.getCustomByCustomkey(customkey);//商户信息
		for (UserCommission user : list) {
			String user_amount = user.getAmount();
			String commissionMfkjFree = "0";
			// 利润
			if (ArithmeticUtil.compareTod(user_amount, baseInfo.getCalculationLimit()) < 0) {
				commissionMfkjFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getProfiltLower(), 2);
			} else {
				commissionMfkjFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getProfiltUpper(), 2);
			}
			// 服务费
			String commissionFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getServiceRates(), 2);

			amountSum = ArithmeticUtil.addStr(amountSum, user_amount);// 总金额
			batchServiceFee = ArithmeticUtil.addStr(batchServiceFee, commissionFree);// 总服务费
			mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, commissionMfkjFree);// 魔方总服务费
		}
		String handleAmount = ArithmeticUtil.addStr(amountSum, batchServiceFee);
		// 校验余额
		String balance = channelHistoryService.getBalance(customkey, channelRelated.getCompanyId(),"2"); //支付方式2 代表支付宝
		if (ArithmeticUtil.compareTod(balance, ArithmeticUtil.addStr(amountSum, batchServiceFee))>0) {
			String orderNo = orderNoUtil.getChannelSerialno();// 生成交易订单号（申请单号）
			/**
			 * 保存批次信息
			 */
			ChannelHistory history = new ChannelHistory();
			history.setAmount(amountSum);
			history.setHandleAmount(handleAmount);
			history.setAccountName(custom.getCompanyName());
			history.setCustomkey(customkey);
			history.setRecCustomkey(channelRelated.getCompanyId());// 薪税服务公司userId
			history.setOrdername("佣金下发");
			history.setOperatorName(loginUser.getUsername());
			history.setOrderno(orderNo);
			history.setStatus(3);
			history.setRemark(remark);
			history.setPayType(2);// 支付宝
			history.setTransfertype(2);// 交易类型 发放佣金
			history.setServiceFee(batchServiceFee);// 服务费
			history.setMfkjServiceFee(mfkjServiceFee);//魔方服务费，包含再内。
			channelHistoryService.addChannelHistory(history);
			/**
			 * 删除临时批次此信息
			 */
			channelHistoryService.deleteByOrderno(batchId);
			/**
			 * 更新佣金表订单号
			 */
			String newBatchId = history.getId()+"";
//			commissionService.updateUserCommissionByBacthId(newBatchId,batchId);
//			model.put("batchNo", newBatchId); //批次信息
			//将临时批次号替换成真实批次号
			for (UserCommission userCommission : list) {
				userCommission.setStatus(3);//修改状态为已提交
				userCommission.setBatchId(newBatchId);//新的batchId
				commissionService.updateUserCommissionBatchIdAndStatus(userCommission);
			}
			// 调用支付宝接口进行下发
			List<Map<String, Object>> param = new ArrayList<>();
			for (UserCommission userCommission : list) {
				if(userCommission.getStatus()!=3){
					continue;
				}

				Map<String, Object> param_map = new HashMap<>();
				User user2 = userSerivce.getUserByUserId(Integer.parseInt(userCommission.getUserId()));

				param_map.put("out_biz_no", userCommission.getOrderNo());
				param_map.put("payee_type", AlipayConfigUtil.getLoginID());
				param_map.put("payee_account", userCommission.getAccount());
				param_map.put("amount", userCommission.getAmount());
				param_map.put("payer_show_name", AlipayConfigUtil.getPayerShowName());
				param_map.put("payee_real_name", user2.getUserName());
				param_map.put("remark","转账");
				param.add(param_map);
			}
			if(param.size()==0){
				ChannelHistory history1 = channelHistoryService.getChannelHistoryById(newBatchId);
				if(history1.getStatus()==0){
					history.setStatus(2);
					channelHistoryService.updateChannelHistory(history);
				}
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "提交成功，请稍后查看记录。");
				return model;
			}
			List<List<Map<String, Object>>> averageAssign = averageAssign(param, 5);
			CyclicBarrier cb = new CyclicBarrier(averageAssign.size(), new CountAliPayData(newBatchId,commissionService, channelHistoryService));
			for (int i = 0; i < averageAssign.size(); i++) {
				ThreadUtil.cashThreadPool.execute(new ExecuteBatch(cb,alipayServiceImpl,averageAssign.get(i),commissionService));
			}
		} else {
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "账户余额不足！");
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "提交成功，请稍后查看记录。");
		return model;
	}

	private void createErrorCommion(String amount,String customkey,String userId,String batchId,String merchantId,
									String commissionMfkjFree,String serviceRates, String commissionAygFree,
									String companyId,String statusDesc, String remark,String alipayAccount,String userName,
									String certId){
		if(StringUtil.isEmpty(userId)){
			User user = new User();
			user.setCertId(certId);
			user.setUserName(userName);
			user.setMerchantId(merchantId);
			user.setUserType(0);//错误信息
			user.setCompanyUserNo(companyId);
			userSerivce.addUser(user);
			userId = user.getId()+"";
		}

		UserCommission commission = new UserCommission();
		commission.setAmount(amount);
		commission.setCreatetime(DateUtils.getNowDate());
		commission.setUserId(userId);
		commission.setStatus(2);
		commission.setStatusDesc(statusDesc);
		commission.setRemark(remark);
		commission.setBatchId(batchId);
		commission.setCompanyId(companyId);
		commission.setOrderNo(orderNoUtil.getChannelSerialno());
		commission.setProfiltFree(commissionMfkjFree);
		commission.setCalculationRates(serviceRates);
		commission.setSumFee(commissionAygFree);
		commission.setOriginalId(customkey);
		commission.setMerchantId(merchantId);
		commission.setPayType(1);
		commission.setAccount(alipayAccount);
		commissionService.addUserCommission(commission);
	}
}
