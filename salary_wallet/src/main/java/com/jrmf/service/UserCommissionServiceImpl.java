package com.jrmf.service;

import com.google.code.yanf4j.util.ConcurrentHashSet;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.constant.TempStatus;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.*;
import com.jrmf.oldsalarywallet.dao.ChannelInterimBatchDao;
import com.jrmf.oldsalarywallet.dao.CommissionTemporaryDao;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.persistence.*;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.CheckUserNameCertIdCountException;
import com.jrmf.utils.threadpool.ThreadUtil;
import com.jrmf.utils.transaction.TransactionRunner;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhangzehui
 * @version 创建时间：2017年12月16日
 */
@Service("userCommissionService")
public class UserCommissionServiceImpl implements UserCommissionService {

	private static Logger logger = LoggerFactory.getLogger(UserCommissionServiceImpl.class);

	@Autowired
	private UserCommission2Dao commissionDao2;
	@Autowired
	private OrderNoUtil orderNoUtil;
	@Autowired
	private UserCommissionDao commissionDao;
	@Autowired
	private ChannelRelatedDao channelRelatedDao;
	@Autowired
	private ChannelHistoryService channelHistoryService;
	@Autowired
	private TransferBankDao transferBankDao;
	@Autowired
	private UserRelatedDao userRelatedDao;
	@Autowired
	private UserSerivce userSerivce;
	@Autowired
	private DataService dataService;
	@Autowired
	private CommissionTemporaryDao temporaryDao;
	@Autowired
	private ChannelInterimBatchDao interimBatchDao;
	@Autowired
	private ChannelCustomDao channelCustomDao;
	@Autowired
	private CustomMenuDao customMenuDao;
	@Autowired
	private BankCardBinService cardBinService;
	@Autowired
	private TransactionRunner transactionRunner;
	@Autowired
	private AgreementTemplateService agreementTemplateService;
    @Autowired
	private CalculationFeeService calculationFeeService;
	@Autowired
	private BaseInfo baseInfo;
	@Autowired
	private CustomLimitConfService customLimitConfService;
	@Autowired
	private UsersAgreementService usersAgreementService;
	@Autowired
	private CustomCompanyRateConfService customCompanyRateConfService;
	@Autowired
	private ChannelInterimBatchService channelInterimBatchService;
	@Autowired
	private UtilCacheManager utilCacheManager;

	@Override
	public void addUserCommission(UserCommission commission) {
		commissionDao.addUserCommission(commission);
	}

	@Override
	public List<UserCommission> getUserCommissionByParam(Map<String, Object> param) {
		return commissionDao.getUserCommissionByParam(param);
	}

	@Override
	public int updateUserCommission(UserCommission history) {
		return commissionDao.updateUserCommission(history);
	}

	@Override
	public void deleteById(int id) {
		commissionDao.deleteById(id);
	}

	@Override
	public String getStockByBatchId(String batchId, String companyId) {
		return commissionDao.getStockByBatchId(batchId, companyId);
	}

	@Override
	public void updateUserCommissionByCompanyId(Map<String, Object> param) {
		commissionDao.updateUserCommissionByCompanyId(param);
	}

	@Override
	public List<UserCommission> getUserCommissionByIds(String ids) {
		return commissionDao.getUserCommissionByIds(ids);
	}

	@Override
	public void deleteByBatchId(String batchId) {
		commissionDao.deleteByBatchId(batchId);
	}

	@Override
	public List<CommissionDetail> getCommissionDeatailList(Map<String, Object> param) {
		return commissionDao.getCommissionDeatailList(param);
	}

	@Override
	public String getAmountByBatchId(String batchId) {
		return commissionDao.getAmountByBatchId(batchId);
	}

	@Override
	public int getBatchNum(String batchId, String status) {
		return commissionDao.getBatchNum(batchId, status);
	}

	@Override
	public void deleteTemporary() {
		commissionDao.deleteTemporary();
	}

	@Override
	public List<UserCommission> getCommissionsByBatchId(String batchId) {
		return commissionDao.getCommissionsByBatchId(batchId);
	}

	@Override
	public UserCommission getCommissionsById(@Param("id") String id) {
		return commissionDao.getCommissionsById(id);
	}

	@Override
	public Map<String, Object> updateBatchMessage(String batchId, String originalId, Map<String, Object> model) {
		/**
		 * 更新批次金额信息
		 */
		List<UserCommission> list = commissionDao.getCommissionsByBatchId(batchId);
		String serviceFee = "0.00";
		String mfkjServiceFee = "0.00";
		String amountSum = "0.00";
		String failedAmountSum = "0.00";
		String batchAmount = "0.00";
		String handleAmount = "0.00";
		int passNum = 0;
		int failedNum = 0;
		int batchNum = list.size();
		for (UserCommission com : list) {
			batchAmount = ArithmeticUtil.addStr(batchAmount, com.getAmount());
			if (com.getStatus() == 0) {
				++passNum;
				/**
				 * 服务费
				 */
				serviceFee = ArithmeticUtil.addStr(serviceFee, com.getSumFee());
				mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, com.getProfiltFree());
				amountSum = ArithmeticUtil.addStr(amountSum, com.getAmount());
			} else if (com.getStatus() == 2) {
				failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, com.getAmount());
				++failedNum;
			}
		}
		handleAmount = ArithmeticUtil.addStr(amountSum, mfkjServiceFee);
		ChannelHistory history = channelHistoryService.getChannelHistoryByOrderno(batchId);
		history.setAmount(amountSum);
		history.setServiceFee(serviceFee);
		history.setMfkjServiceFee(mfkjServiceFee);
		history.setHandleAmount(handleAmount);
		history.setBatchAmount(batchAmount);
		history.setBatchNum(batchNum);
		history.setPassNum(passNum);
		history.setFailedNum(failedNum);
		history.setFailedAmount(failedAmountSum);
		channelHistoryService.updateChannelHistorySummary(history);

		model.put("amountSum", amountSum);
		model.put("serviceFee", serviceFee);
		return model;
	}

	@Override
	public void updateBatchData(String batchId, String originalId) {
		/**
		 * 更新批次金额信息
		 */
		List<UserCommission> list = commissionDao.getCommissionsByBatchId(batchId);
		String serviceFee = "0.00";
		String mfkjServiceFee = "0.00";
		String amountSum = "0.00";
		String handleAmount = "0.00";
		String failedAmount = "0.00";
		List<String> failedCommissionOrderNo = new ArrayList<>();
		int passNum = 0;
		int faileNum = 0;
		for (UserCommission com : list) {
			if (com.getStatus() == 1) {
				++passNum;
				serviceFee = ArithmeticUtil.addStr(serviceFee, com.getSumFee());
				mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, com.getProfiltFree());
				amountSum = ArithmeticUtil.addStr(amountSum, com.getAmount());
			} else if (com.getStatus() == 2) {
				failedAmount = ArithmeticUtil.addStr(failedAmount, com.getAmount());
				++faileNum;
				failedCommissionOrderNo.add(com.getOrderNo());
			}
		}
		//本批次下发成功的钱
		handleAmount = ArithmeticUtil.addStr(amountSum, serviceFee);
		ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
		history.setAmount(amountSum);
		history.setServiceFee(serviceFee);
		history.setMfkjServiceFee(mfkjServiceFee);
		history.setHandleAmount(handleAmount);
		history.setPassNum(passNum);
		history.setFailedNum(faileNum);
		history.setFailedAmount(failedAmount);
		channelHistoryService.updateChannelHistorySummary(history);
	}

	@Override
	public UserCommission getUserCommission(String orderNo) {
		return commissionDao.getCommByOrderNo(orderNo);
	}

	@Override
	public void addUserCommissionBatch(List<UserCommission> commissionBatch) {
		commissionDao.addUserCommissionBatch(commissionBatch);
	}

	@Override
	public void updateUserCommissionByBacthId(String newBatchId, String batchId) {
		commissionDao.updateUserCommissionByBacthId(newBatchId, batchId);
	}

	@Override
	public Map<String, Object> transfer(String originalId, String batchId, String remark, String operatorName) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);
		try {
			/**
			 * 计算该批次薪税服务公司在商户对应的备付金是否足够
			 */
			ChannelInterimBatch interimBatch = interimBatchDao.getChannelInterimBatchByOrderno(batchId, originalId);
			ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(originalId, interimBatch.getRecCustomkey());
			String handleAmount = interimBatch.getHandleAmount();

			String compBalance = channelHistoryService.getBalance(originalId, related.getCompanyId(), "4");
			ChannelCustom custom = channelCustomDao.getCustomByCustomkey(originalId,null);
			ChannelCustom company = channelCustomDao.getCustomByCustomkey(interimBatch.getRecCustomkey(),null);
			logger.info("余额：" + compBalance + "下发总金额：" + handleAmount);
			if (ArithmeticUtil.compareTod(compBalance, handleAmount) < 0) {
				respstat = RespCode.error115;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, custom.getCompanyName() + "银行卡下发预存金额不足");
				return model;
			}
			// 生成交易订单号（申请单号）
			String orderNo = orderNoUtil.getChannelSerialno();
			/**
			 * 临时数据持久化到正式表中
			 */
			ChannelHistory history = new ChannelHistory();
			history.setAmount("0");
			history.setPassNum(0);
			history.setAccountName(custom.getCompanyName());
			history.setCustomkey(originalId);
			history.setRecCustomkey(interimBatch.getRecCustomkey());
			history.setOrdername("佣金发放");
			history.setOrderno(orderNo);
			history.setOriginalBeachNo(interimBatch.getOrderno());
			// 初始化状态为处理中
			history.setStatus(3);
			history.setRemark(remark);
			// 支付方式： 银企直联
			history.setPayType(4);
			// 交易类型 发放佣金
			history.setTransfertype(2);
			history.setServiceFee(interimBatch.getServiceFee());
			history.setMfkjServiceFee(interimBatch.getMfkjServiceFee());
			history.setOperatorName(operatorName);
			history.setBatchAmount(interimBatch.getBatchAmount());
			history.setBatchNum(interimBatch.getBatchNum());
			history.setHandleAmount("0");
			history.setMenuId(interimBatch.getMenuId());
			history.setBatchName(interimBatch.getBatchName());
			history.setBatchDesc(interimBatch.getBatchDesc());
			history.setFileName(interimBatch.getFileName());
			history.setFailedAmount(interimBatch.getFailedAmount());
			channelHistoryService.addChannelHistory(history);

			/**
			 * 更新临时批次此信息
			 */
			interimBatchDao.updateInterimBatchStatus(batchId);

			/**
			 * 更新佣金表批次号
			 */
			String newBatchId = history.getId() + "";
			CustomMenu customMenu = customMenuDao.getCustomMenuById(interimBatch.getMenuId());
			Map<String, Object> batchData = new HashMap<>(20);
			batchData.put("batchId", newBatchId);
			batchData.put("operatorName", operatorName);
			batchData.put("originalId", originalId);
			batchData.put("fileName", interimBatch.getFileName());
			batchData.put("batchDesc", interimBatch.getBatchDesc());
			batchData.put("batchName", interimBatch.getBatchName());
			batchData.put("menuId", interimBatch.getMenuId());
			batchData.put("menuName", customMenu.getContentName());
			batchData.put("customName", custom.getCompanyName());
			batchData.put("companyName", company.getCompanyName());
			batchData.put("companyId", company.getId());
			logger.info("银企直联打款开始---");
			/**
			 * 待发放佣金列表
			 */
			List<CommissionTemporary> users = temporaryDao.getCommissionsByBatchId(batchId, originalId);
			ChannelRelated channelRelated = channelRelatedDao.getRelatedByCompAndOrig(originalId, interimBatch.getRecCustomkey());

			List<List<CommissionTemporary>> averageAssign = StringUtil.averageAssign(users, 5);
			CyclicBarrier cb = new CyclicBarrier(averageAssign.size(),
					new CountPABankOptionData(newBatchId, commissionDao, channelHistoryService, temporaryDao));
			for (int i = 0; i < averageAssign.size(); i++) {
				logger.info("线程" + i + ",执行数据量--" + averageAssign.get(i).size());
				ThreadUtil.cashThreadPool.execute(new ExecuteBatchToPAOption(cb, orderNoUtil, averageAssign.get(i), commissionDao, userSerivce,
						channelRelated, transferBankDao, userRelatedDao, transactionRunner, batchData, baseInfo));
			}
			logger.info("银企直联打款结束---");
			// 正式表的批次号
			model.put("batchNo", newBatchId);
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "打款成功");
		} catch (Exception e) {
			logger.error("", e);
			logger.info("银企直联打款异常---");
			respstat = RespCode.error115;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "打款异常");
		}
		return model;
	}

	@Override
	public UserCommission getCommissionsByAygId(String orderNo) {
		return commissionDao.getCommissionsByAygId(orderNo);
	}

	@Override
	public List<UserCommission> getListByTypeAndStatus(int status, String payType) {
		return commissionDao.getListByTypeAndStatus(status, payType);
	}

	@Override
	public List<UserCommission> getListByTypeAndStatusOnJob(int status, String payType) {
		return commissionDao.getListByTypeAndStatusOnJob(status, payType);
	}

	@Override
	public String getUserCommissionSum(Map<String, Object> param) {
		return commissionDao.getUserCommissionSum(param);
	}

	@Override
	public String isValidateData(String amount, String idCard, String bankCard, String mobileNo, String name) {
		logger.info("验证格式" + name);
		String msg = "";
		try {
			if (!StringUtil.isEmpty(mobileNo) && !StringUtil.isMobileNO(mobileNo)) {
				logger.info(mobileNo + "手机号格式错误");
				msg = "手机号格式错误";
				return msg;
			}
			if (!StringUtil.isEmpty(idCard) && !StringUtil.iDCardValidate(idCard)) {
				logger.info(idCard + "身份证号格式错误");
				msg = "身份证号格式错误";
				return msg;
			}
			if (!StringUtil.isEmpty(bankCard) && !StringUtil.checkBankCard(bankCard)) {
				logger.info(bankCard + "银行卡号格式错误");
				msg = "银行卡号格式错误";
				return msg;
			}
			if (!StringUtil.isEmpty(amount) && !StringUtil.isMoney(amount)) {
				logger.info(amount + "金额格式错误");
				msg = "金额格式错误";
				return msg;
			}
			if (!StringUtil.isEmpty(name) && !StringUtil.isChinese(name)) {
				logger.info(name + "姓名格式错误");
				msg = "姓名格式错误";
				return msg;
			}
		} catch (Exception e) {
			logger.error("", e);
			logger.info("格式校验错误");
			msg = "格式校验错误";
			return msg;
		}

		return msg;
	}

	@Override
	public void updateUserCommissionBatchIdAndStatus(UserCommission userCommission) {
		commissionDao.updateUserCommissionBatchIdAndStatus(userCommission);
	}

	@Override
	public void updateUserCommissionByInvoice(String invoiceBatchNo, String ids) {
		commissionDao.updateUserCommissionByInvoice(invoiceBatchNo, ids);
	}

	@Override
	public List<UserCommission> getUserCommissionToInvoice(Map<String, Object> paramMap) {
		return commissionDao.getUserCommissionToInvoice(paramMap);
	}

	@Override
	public List<UserCommission> getUserCommissionedByParam(Map<String, Object> paramMap) {
		return commissionDao.getUserCommissionedByParam(paramMap);
	}

	@Override
	public void updateBatchDataByExecuting(String batchId) {

		/**
		 * 更新批次金额信息
		 */
		List<UserCommission> list = commissionDao.getCommissionsByBatchId(batchId);
		String serviceFee = "0.00";
		String mfkjServiceFee = "0.00";
		String amountSum = "0.00";
		String failedAmountSum = "0.00";
		String batchAmount = "0.00";
		String handleAmount = "0.00";
		int passNum = 0;
		int failedNum = 0;
		int batchNum = list.size();
		for (UserCommission com : list) {
			batchAmount = ArithmeticUtil.addStr(batchAmount, com.getAmount());
			if (com.getStatus() == 3) {
				++passNum;
				/**
				 * 服务费
				 */
				serviceFee = ArithmeticUtil.addStr(serviceFee, com.getSumFee());
				mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, com.getProfiltFree());
				amountSum = ArithmeticUtil.addStr(amountSum, com.getAmount());
			} else if (com.getStatus() == 2) {
				failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, com.getAmount());
				++failedNum;
			}
		}
		handleAmount = ArithmeticUtil.addStr(amountSum, serviceFee);
		ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
		history.setAmount(amountSum);
		history.setServiceFee(serviceFee);
		history.setMfkjServiceFee(mfkjServiceFee);
		history.setHandleAmount(handleAmount);
		history.setBatchAmount(batchAmount);
		history.setBatchNum(batchNum);
		history.setPassNum(passNum);
		history.setFailedNum(failedNum);
		history.setFailedAmount(failedAmountSum);
		channelHistoryService.updateChannelHistorySummary(history);
	}

	@Override
	public Map<String, Object> inputHsBankCommissionDate(Workbook workbook, String customkey, String operatorName) {
		//		Map<String, Object> model = new HashMap<String, Object>();
		//		int respstat = RespCode.success;
		//		try {
		//			Sheet sheet = workbook.getSheetAt(0);
		//			String batchId = StringUtil.getChannelSerialno();// 临时批次号
		//			model.put("batchId", batchId);
		////			ChannelRelated channelRelated = channelRelatedDao.getRelatedByEnable(customkey);
		//			if (channelRelated == null) {
		//				respstat = RespCode.error107;
		//				model.put(RespCode.RESP_STAT, respstat);
		//				model.put(RespCode.RESP_MSG, "请联系管理员配置薪资服务公司配置信息！");
		//				logger.info("返回结果：" + model);
		//				return model;
		//			}
		//			/**
		//			 * 验证数据amount,idCard, bankCard, mobileNo, name
		//			 */
		//			logger.info(" 验证数据");
		//			int num = 0;
		//			Set<String> result = new HashSet<String>();
		//			for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
		//				XSSFRow row = (XSSFRow) sheet.getRow(j);
		//				if (row == null) {
		//					continue;
		//				}
		//				if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
		//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
		//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
		//					continue;
		//				}
		//				++num;
		//				if (num > 2000 || num < 0) {
		//					respstat = RespCode.error107;
		//					model.put(RespCode.RESP_STAT, respstat);
		//					model.put(RespCode.RESP_MSG, "导入数据不得大于2000条或小于0条，请重新导入！");
		//					return model;
		//				}
		//				//String amount, String idCard, String bankCard, String mobileNo, String name
		//				String msg = this.isValidateData(StringUtil.getXSSFCell(row.getCell(4)),
		//						StringUtil.getXSSFCell(row.getCell(3)), StringUtil.getXSSFCell(row.getCell(1)), null,
		//						StringUtil.getXSSFCell(row.getCell(0)));
		//				String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
		//				String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 身份证号码
		//				if (StringUtil.isEmpty(userName + cardNo)) {
		//					continue;
		//				}
		//				if (!result.add(userName + cardNo)) {
		//					respstat = RespCode.error107;
		//					model.put(RespCode.RESP_STAT, respstat);
		//					model.put(RespCode.RESP_MSG, userName + ":数据重复，请修改后再上传！");
		//					return model;
		//				}
		//				if (!StringUtil.isEmpty(msg)) {
		//					respstat = RespCode.error107;
		//					model.put(RespCode.RESP_STAT, respstat);
		//					model.put(RespCode.RESP_MSG, userName + ":" + msg);
		//					return model;
		//				}
		//			}
		//			logger.info("验证数据结束");
		//
		//			List<Map<String, String>> commissionDatas = new ArrayList<Map<String, String>>();
		//			for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
		//				XSSFRow row = (XSSFRow) sheet.getRow(j);
		//				if (row == null) {
		//					break;
		//				}
		//				if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
		//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
		//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
		//					continue;
		//				}
		//				String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
		//				String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 身份证号码
		//				String amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));// 佣金
		//				Map<String, String> data = new HashMap<String, String>();
		//				data.put("userName", userName);
		//				data.put("cardNo", cardNo);
		//				data.put("amount", amount);
		//				commissionDatas.add(data);
		//			}
		//			List<List<Map<String, String>>> averageAssign = StringUtil.averageAssign(commissionDatas, 5);
		//			CountDownLatch cb = new CountDownLatch(averageAssign.size());
		//			for (int i = 0; i < averageAssign.size(); i++) {
		//				logger.info("线程" + i + ",执行数据量--" + averageAssign.get(i).size());
		//				new Thread(new ExecuteBatchToHsInput(cb, averageAssign.get(i), operatorName, userSerivce, commissionDao,
		//						batchId, customkey, channelRelated)).start();
		//			}
		//			/**
		//			 * 阻塞当前线程直到latch中数值为零才执行
		//			 */
		//			cb.await();
		//
		//			logger.info("-------------------佣金明细处理完成，开始生成批次信息---------------------------");
		//			List<UserCommission> commissionsByBatchId = commissionDao.getCommissionsByBatchId(batchId);
		//			int passNum = 0;// 成功总数
		//			String amount = "0.00";// 实际下发金额
		//			String serviceFee = "0.00";// 实际服务费
		//			String serviceMFFee = "0.00";// 实际魔方服务费
		//			int failedNum = 0;// 失败总数
		//			int batchNum = commissionsByBatchId.size();// 批次总数
		//			String handleAmount = "0.00";// 订单应付总额
		//			String batchAmount = "0.00";// 批次总额
		//			for (UserCommission userCommission : commissionsByBatchId) {
		//				if (userCommission.getStatus() == 0) {
		//					++passNum;
		//					// 批次总金额
		//					amount = ArithmeticUtil.addStr(amount, userCommission.getAmount());
		//					// 批次总服务费
		//					serviceFee = ArithmeticUtil.addStr(serviceFee, userCommission.getSumFee());
		//					// 批次总利润
		//					serviceMFFee = ArithmeticUtil.addStr(serviceMFFee, userCommission.getProfiltFree());
		//				} else {
		//					++failedNum;
		//				}
		//				batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
		//			}
		//			handleAmount = ArithmeticUtil.addStr(amount, serviceFee);
		//			ChannelHistory history = new ChannelHistory();
		//			history.setAmount(handleAmount);
		//			history.setBatchAmount(batchAmount);
		//			history.setCustomkey(customkey);
		//			history.setRecCustomkey(channelRelated.getCompanyId());
		//			history.setOrdername("佣金发放");
		//			history.setOrderno(batchId);
		//			history.setStatus(0);
		//			history.setPayType(1);
		//			history.setTransfertype(2);
		//			history.setServiceFee(serviceFee);// 服务费
		//			history.setPassNum(passNum);
		//			history.setFailedNum(failedNum);
		//			history.setBatchNum(batchNum);
		//			history.setHandleAmount(handleAmount);
		//			channelHistoryDao.addChannelHistory(history);
		//			logger.info("-------------------批次信息生成结束---------------------------");
		//		} catch (Exception e) {
		//			logger.error("", e);
		//			respstat = RespCode.error107;
		//			model.put(RespCode.RESP_STAT, respstat);
		//			model.put(RespCode.RESP_MSG, "系统异常，导入失败！");
		//			return model;
		//		}
		return null;
	}

	@Override
	public Map<String, Object> inputPABankCommissionDate(Workbook workbook, Map<String, String> batchData) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);
		try {
			String customkey = batchData.get("customkey");
			String menuId = batchData.get("menuId");
			String companyId = batchData.get("companyId");
			ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(customkey, companyId);
			if (related == null) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "请联系管理员配置薪资服务公司配置信息！");
				logger.info("返回结果：" + model);
				return model;
			}
			Sheet sheet = workbook.getSheetAt(0);
			/**
			 * 验证数据amount,idCard, bankCard, mobileNo, name
			 */
			int num = 0;
			// 获取每行
			for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
				XSSFRow row = (XSSFRow) sheet.getRow(j);
				if (row == null) {
					continue;
				}
				if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
					sheet.removeRow(row);
					continue;
				}
				++num;
			}
			logger.info("excel导入数据条数------------------" + num + "---------");
			if (num > 2000 || num < 0) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "导入数据不得大于2000条或小于0条，请重新导入！");
				return model;
			}
			/**
			 * 初始化缓存数据
			 */
			// 临时批次号
			String batchId = orderNoUtil.getChannelSerialno();
			model.put("batchId", batchId);
			batchData.put("batchId", batchId);
			//			dataService.initDataMap(batchId, num);

			/**
			 * 存放批次详情，进行批处理
			 */
			logger.info("-------------------封装导入的数据---------------------------");
			List<Map<String, String>> commissionDatas = new ArrayList<>();
			// 获取每行
			for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
				XSSFRow row = (XSSFRow) sheet.getRow(j);
				if (row == null) {
					continue;
				}
				if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
					continue;
				}
				String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
				String bankCard = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 收款人银行卡号
				String certId = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 身份证号
				String amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));// 金额(必要)
				String bankName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5)));// 银行姓名
				String documentType = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(6)));// 证件类型
				String remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(7)));// 备注
				Map<String, String> data = new HashMap<String, String>();
				data.put("userName", userName);
				data.put("bankCard", bankCard);
				data.put("certId", certId);
				data.put("amount", ArithmeticUtil.formatDecimals(amount));
				data.put("bankName", bankName);
				data.put("documentType", documentType);
				data.put("remark", remark);
				data.put("menuId", menuId);
				commissionDatas.add(data);
				logger.info("-------------------userName：" + userName
						+ " bankCard=" + bankCard
						+ " certId=" + certId
						+ " amount=" + amount
						+ " bankName=" + bankName
						+ " documentType=" + documentType
						+ "---------------------------");
			}

			/**
			 * 多线程处理佣金明细数据
			 */
			logger.info("-------------------批次信息生成开始---------------------------");
			Set<String> validateSet = new ConcurrentHashSet<String>();
			List<List<Map<String, String>>> averageAssign = StringUtil.averageAssign(commissionDatas, 5);
			CountDownLatch barrier = new CountDownLatch(averageAssign.size());
			//
			/*CyclicBarrier barrier = new CyclicBarrier(averageAssign.size(),
					new CountPABankInputData(batchaData, temporaryDao, interimBatchDao, related, dataService));*/
			for (int i = 0; i < averageAssign.size(); i++) {
				System.out.println("线程" + i + ",执行数据量--" + averageAssign.get(i).size());
				ThreadUtil.cashThreadPool.execute(new ExecuteBatchToPAInput(barrier, averageAssign.get(i), orderNoUtil, temporaryDao, related,
						transferBankDao, dataService, batchData, validateSet, cardBinService));
			}
			logger.info("-------------------佣金明细处理完成，开始生成临时批次信息---------------------------");
			/**
			 * 阻塞当前线程直到latch中数值为零才执行
			 */
			barrier.await();
			logger.info("-------------------临时批次信息生成开始---------------------------");
			String fileName = batchData.get("fileName");
			String operatorName = batchData.get("operatorName");
			String batchDesc = batchData.get("batchDesc");
			String batchName = batchData.get("batchName");
			String originalId = related.getOriginalId();
			List<CommissionTemporary> commissionsByBatchId = temporaryDao.getCommissionsByBatchId(batchId, originalId);
			int passNum = 0;//验证成功总数
			int failedNum = 0;//验证失败总数
			String amountSum = "0.00";//实际下发金额
			String failedAmountSum = "0.00";//失败总金额
			String serviceFeeSum = "0.00";//实际服务费
			String serviceMFFeeSum = "0.00";//实际魔方服务费
			String handleAmount = "0.00";//订单应付总额
			String batchAmount = "0.00";
			int batchNum = commissionsByBatchId.size();//批次总数
			for (CommissionTemporary temporary : commissionsByBatchId) {
				String amount = temporary.getAmount();
				if (temporary.getStatus() == 1) {
					passNum++;
					String commissionMfkjFree = "0";
					if (ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit()) < 0) {
						commissionMfkjFree = ArithmeticUtil.mulStr(amount, related.getProfiltLower(), 2);
					} else {
						commissionMfkjFree = ArithmeticUtil.mulStr(amount, related.getProfiltUpper(), 2);
					}
					//服务费
					String commissionAygFree = ArithmeticUtil.mulStr(amount, related.getServiceRates(), 2);

					//批次总金额
					amountSum = ArithmeticUtil.addStr(amountSum, amount);
					//批次总服务费
					serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum, commissionAygFree);
					//批次总利润
					serviceMFFeeSum = ArithmeticUtil.addStr(serviceMFFeeSum, commissionMfkjFree);
				} else if (temporary.getStatus() == 2) {
					failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
					;
					failedNum++;
				}
				batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
			}
			handleAmount = ArithmeticUtil.addStr(amountSum, serviceFeeSum);
			ChannelInterimBatch batch = new ChannelInterimBatch();
			batch.setAmount(amountSum);
			batch.setBatchAmount(batchAmount);
			batch.setCustomkey(related.getOriginalId());
			batch.setRecCustomkey(related.getCompanyId());
			batch.setOrdername("佣金发放");
			batch.setOrderno(batchId);
			batch.setPayType(4);
			batch.setServiceFee(serviceFeeSum);//服务费
			batch.setMfkjServiceFee(serviceMFFeeSum);
			batch.setPassNum(passNum);
			batch.setFailedNum(failedNum);
			batch.setBatchNum(batchNum);
			batch.setHandleAmount(handleAmount);
			batch.setFileName(fileName);
			batch.setOperatorName(operatorName);
			batch.setBatchDesc(batchDesc);
			batch.setBatchName(batchName);
			batch.setFailedAmount(failedAmountSum);
			batch.setMenuId(Integer.parseInt(menuId));
			if (failedNum > 0) {
				if (passNum == 0) {
					batch.setStatus(2);//全部失败
				} else {
					batch.setStatus(3);//部分失败
				}
			} else if (failedNum == 0) {
				if (passNum == 0) {
					batch.setStatus(2);//全部失败
				} else {
					batch.setStatus(1);//全部成功
				}
			}
			interimBatchDao.addChannelInterimBatch(batch);
			/**
			 * 批次缓存数据结束
			 */
			//			dataService.complete(batchId);
			logger.info("-----------------临时批次信息生成结束-------------------------");
			logger.info("-------------------批次信息生成结束---------------------------");

		} catch (Exception e) {
			logger.error("", e);
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "系统异常，导入失败！");
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "导入成功");
		return model;
	}

	@Override
	public String getServiceRatesFreeByBatchId(String batchId) {
		return commissionDao.getServiceRatesFreeByBatchId(batchId);
	}

	@Override
	public void updateTemporaryBatchData(String batchId, String originalId, String companyId, boolean autoSupplement, Set<String> validateSet) {

		logger.info("------修改明细------计算佣金费用开始-------------------");

		int passNum = 0;
		int failedNum = 0;
		String amountSum = "0.00";
		String batchAmount = "0.00";
		String handleAmount = "0.00";
		String serviceFeeSum = "0.00";
		String profitAmountSum = "0.00";
		String failedAmountSum = "0.00";
		String supplementServiceFeeSum = "0.00";

		List<CommissionTemporary> commissions = temporaryDao.getCommissionsByBatchId(batchId, originalId);
		//此处查询临时批次数据完全是为了拿到companyId
		ChannelInterimBatch interimBatch = interimBatchDao.getChannelInterimBatchByOrderno(batchId, originalId);
		//        ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrigAll(originalId, interimBatch.getRecCustomkey());
		CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfService.getConfByCustomKeyAndCompanyId(originalId, interimBatch.getRecCustomkey());

		// 批次总数
		int batchNum = commissions.size();
		for (CommissionTemporary temporary : commissions) {
			String amount = temporary.getSourceAmount();
			if (temporary.getStatus() == 1) {

				String sumFee = "0";
				String profitAmount = "0";
				String supplementFee = "0";
				//                String ruleType = temporary.getFeeRuleType();
				//                ruleType = StringUtil.isEmpty(ruleType) ? "1" : ruleType;

				if (customCompanyRateConf != null) {
					Map<String, String> commissionFeeInfoMap = calculationFeeService
							.calculationFeeInfo("web", originalId, companyId, batchId, validateSet, "", "",
									autoSupplement, temporary, customCompanyRateConf.getServiceFeeType());
					if (customCompanyRateConf.getServiceFeeType() == ServiceFeeType.ISSUE.getCode()
							|| customCompanyRateConf.getServiceFeeType() == ServiceFeeType.PERSON.getCode()) {

						sumFee = commissionFeeInfoMap.get("sumFee");
						profitAmount = commissionFeeInfoMap.get("profitAmount");
						supplementFee = commissionFeeInfoMap.get("supplementFee");
						if (customCompanyRateConf.getServiceFeeType() == ServiceFeeType.PERSON.getCode()) {
							//下发实时扣税个人承担时，这里的实发金额已被上面 calculationFeeInfo方法修改
							amount = temporary.getAmount();
						}
					}
				}

				//增加下发实时扣税个人承担后可能出现跨档补差价后 本次下发金额不足以抵扣补服务费金额 导致到账金额为负数
				if (ArithmeticUtil.compareTod(amount, "0") != 1) {
					temporary.setStatus(TempStatus.FAILURE.getCode());
					temporary.setStatusDesc(TempStatus.FAILURE.getDesc());
					failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
					failedNum++;
				} else {
					amountSum = ArithmeticUtil.addStr(amountSum, amount);//批次总金额
					serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum, sumFee);//批次总服务费
					profitAmountSum = ArithmeticUtil.addStr(profitAmountSum, profitAmount);//批次总利润
					supplementServiceFeeSum = ArithmeticUtil
							.addStr(supplementServiceFeeSum, supplementFee);//批次补差价总服务费
					passNum++;
				}

			} else if (temporary.getStatus() == 2) {
				failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
				failedNum++;
			}
			batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
		}

		handleAmount = ArithmeticUtil.addStr(amountSum, serviceFeeSum);

		ChannelInterimBatch batch = interimBatchDao.getChannelInterimBatchByOrderno(batchId, originalId);
		batch.setOrderno(batchId);
		batch.setAmount(amountSum);
		batch.setBatchAmount(batchAmount);
		batch.setServiceFee(serviceFeeSum);//服务费
		batch.setSupplementServiceFee(supplementServiceFeeSum);//服务费补差价
		batch.setMfkjServiceFee(profitAmountSum);
		batch.setPassNum(passNum);
		batch.setFailedNum(failedNum);
		batch.setBatchNum(batchNum);
		batch.setHandleAmount(handleAmount);
		batch.setFailedAmount(failedAmountSum);

		if (failedNum > 0) {
			if (passNum == 0) {
				batch.setStatus(2);//全部失败
			} else {
				batch.setStatus(3);//部分失败
			}
		} else if (failedNum == 0) {
			if (passNum == 0) {
				batch.setStatus(2);//全部失败
			} else {
				batch.setStatus(1);//全部成功
			}
		}
		interimBatchDao.updateChannelInterimBatch(batch);
		logger.info("-------修改明细-----计算佣金费用结束-------------------");

	}

	@Override
	public List<UserCommission> commissionResultQuery(Map<String, Object> param) {
		return commissionDao.commissionResultQuery(param);
	}

	@Override
	public List<UserCommission> getUserDealRecord(Map<String, Object> param) {
		return commissionDao.getUserDealRecord(param);
	}

	@Override
	public List<UserCommission> getUserDealDetail(Map<String, Object> param) {
		return commissionDao.getUserDealDetail(param);
	}

	@Override
	public List<UserCommission> commissionDetailResult(Map<String, Object> param) {
		return commissionDao.commissionDetailResult(param);
	}

	@Override
	public List<UserCommission> commissionDetailResult2(Map<String, Object> param) {
		return commissionDao2.commissionDetailResult(param);
	}

	@Override
	public List<UserCommission> commissionByMemuResult(Map<String, Object> param) {
		return commissionDao.commissionByMemuResult(param);
	}

	@Override
	public List<UserCommission> commissionByMemuDetail(Map<String, Object> param) {
		return commissionDao.commissionByMemuDetail(param);
	}

	@Override
	public List<UserCommission> getUserTradeData(Map<String, Object> param) {
		return commissionDao.getUserTradeData(param);
	}

	@Override
	public List<Map> getCommissionsByParams(Map<String, String> map) {
		//成功交易总金额  成功交易总笔数 总服务费 总报税金额
		//        payAmountLevel  全部0,   大于2.8W  1,   小于2.8W  2
		List<UserCommission> list = commissionDao.getSumCommissionsByParams(map);
		Set<String> hashSet = new HashSet<>();
		for (UserCommission userCommission : list) {
			String originalId = userCommission.getOriginalId();
			hashSet.add(originalId);
		}
		boolean level = "-1".equals(map.get("payAmountLevel"));
		List<Map> arrayList = new ArrayList<>();
		for (String s : hashSet) {
			Map<String, Object> hashMap = new HashMap<>(10);
			String customName = "";
			String amountSum = "0";
			String serviceRatesFree = "0";
			String supplementFee = "0";
			int countId = 0;
			int countMore28000UserId = 0;
			String amountMore28000Sum = "0";
			for (UserCommission userCommission : list) {
				String originalId = userCommission.getOriginalId();
				boolean equals = s.equals(originalId);
				if (equals) {
					customName = userCommission.getCustomName();
					String amount = userCommission.getAmount();
					amountSum = ArithmeticUtil.addStr(amountSum, amount);
					serviceRatesFree = ArithmeticUtil.addStr(serviceRatesFree, userCommission.getSumFee());
					supplementFee = ArithmeticUtil.addStr(supplementFee, userCommission.getSupplementFee());
					countId = countId + userCommission.getId();
					if (level) {
						boolean b = ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit()) < 0;
						//大于等于28000
						HashSet<String> set = new HashSet<>();
						if (!b) {
							String userId = userCommission.getUserId();
							amountMore28000Sum = ArithmeticUtil.addStr(amountMore28000Sum, amount);
							set.add(userId);
						}
						countMore28000UserId = set.size();
					}
				}
			}
			hashMap.put("customName", customName);
			hashMap.put("countMore28000UserId", countMore28000UserId);
			hashMap.put("amountMore28000Sum", amountMore28000Sum);
			hashMap.put("sumFee", serviceRatesFree);
			hashMap.put("supplementFee", supplementFee);
			hashMap.put("amountSum", amountSum);
			hashMap.put("countId", countId);
			String amountUpperLimit = map.get("amountUpperLimit");
			String amountLowerLimit = map.get("amountLowerLimit");
			double upper = Double.MAX_VALUE;
			double lower = 0.0;
			if (StringUtil.isNumber(amountUpperLimit)) {
				upper = Double.parseDouble(amountUpperLimit);
			}
			if (StringUtil.isNumber(amountLowerLimit)) {
				lower = Double.parseDouble(amountLowerLimit);
			}
			double sum = 0.0;
			if (StringUtil.isNumber(amountSum)) {
				sum = Double.parseDouble(amountSum);
			}
			if ((lower <= sum) && (sum <= upper)) {
				arrayList.add(hashMap);
			}
		}
		if (StringUtil.isNumber(map.get("page")) && StringUtil.isNumber(map.get("pageSize"))) {

			int page = Integer.parseInt(map.get("page"));
			int pageSize = Integer.parseInt(map.get("pageSize"));
			int size = arrayList.size();
			int pageCount = size / pageSize;
			int fromIndex = pageSize * (page - 1);
			int toIndex = fromIndex + pageSize;
			if (toIndex >= size) {
				toIndex = size;
			}
			if (page > pageCount + 1) {
				fromIndex = 0;
				toIndex = 0;
			}
			return arrayList.subList(fromIndex, toIndex);

		} else {
			return arrayList;
		}
	}

	@Override
	public List<UserCommission> getCommissionsDetailByParams(Map<String, String> map) {
		return commissionDao.getCommissionsDetailByParams(map);
	}

	@Override
	public List<UserCommission> getUserTradeCompany(Map<String, Object> param) {
		return commissionDao.getUserTradeCompany(param);
	}

	@Override
	public List<UserCommission> getUserTradeDetailCompany(Map<String, Object> param) {
		return commissionDao.getUserTradeDetailCompany(param);
	}

	@Override
	public List<UserCommission> getApiListByTypeAndStatus(int status, String type) {
		return commissionDao.getApiListByTypeAndStatus(status, type);
	}

	@Override
	public List<UserCommission> getApiListByTypeAndStatusOnJob(int status, String type) {
		return commissionDao.getApiListByTypeAndStatusOnJob(status, type);
	}

	@Override
	public int updateUserCommissionById(UserCommission userCommission) {
		return commissionDao.updateUserCommissionById(userCommission);
	}

	@Override
	public List<HashMap<String, Object>> getCompanyCommissions(Map<String, Object> params) {
		return commissionDao.getCompanyCommissions(params);
	}

	/**
	 * @return 根据customkeys 查询该角色所能查询的所有信息。
	 */
	@Override
	public List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param) {
		return commissionDao2.listCommissionByCustomKeys(param);
	}

	/**
	 * 根据批次号cha查询  状态为 3的 批次订单
	 *
	 * @param batchId 批次号
	 * @return UserCommission list
	 */
	@Override
	public List<UserCommission> getToBeConfirmedUserCommissionByBatchId(String batchId) {
		return commissionDao.getToBeConfirmedUserCommissionByBatchId(batchId);
	}

	/**
	 * 查询符合条件的条数
	 *
	 * @param paramMap 参数
	 * @return 条数
	 */
	@Override
	public int getCommissionsCountByParams(Map<Object, Object> paramMap) {
		return commissionDao.getCommissionsCountByParams(paramMap);
	}

	/**
	 * 修改订单
	 *
	 * @param paramMap 参数
	 * @return 修改条数
	 */
	@Override
	public int updateUndifiedOrder(HashMap<String, Object> paramMap) {
		return commissionDao.updateUndifiedOrder(paramMap);
	}

	/**
	 * 未落地的预授权交易
	 *
	 * @param params
	 * @return
	 */
	@Override
	public List<UserCommission> getUnClosedPrepareUnifiedCommissions(Map<Object, Object> params) {
		return commissionDao.getUnClosedPrepareUnifiedCommissions(params);
	}

	@Override
	public List<UserCommission> getAutogenerateTaskList(String startTime, String endTime, String customKey, String startAmount, String endAmount, String orderNos) {
		return commissionDao.getAutogenerateTaskList(startTime, endTime, customKey, startAmount, endAmount, orderNos);
	}

	@Override
	public int updateIsTask(Integer id) {
		return commissionDao.updateIsTask(id);
	}

	@Override
	public List<UserCommission> getLdListByTypeAndStatus(int type, String status) {
		return commissionDao.getLdListByTypeAndStatus(type, status);
	}

    @Override
    public List<UserCommission> getLdListByTypeAndStatusOnJob(int status, String type) {
        return commissionDao.getLdListByTypeAndStatusOnJob(status, type);
    }

	@Override
	public int getPayingCount(String customKey, String companyId, Integer payType) {
		return commissionDao2.getPayingCount(customKey, companyId, payType);
	}

	@Override
	public List<UserCommission> getSuccessUserCommission(Map<String, Object> param) {
		return commissionDao2.getSuccessUserCommission(param);
	}

	@Override
	public String getSuccessAmount(Map<String, Object> param) {
		return commissionDao2.getSuccessAmount(param);
	}

	@Override
	public int updateUserCommissionRate(UserCommission commission) {
		return commissionDao2.updateUserCommissionRate(commission);
	}

    @Override
    public PaymentReturn<String> getPaymentReturn(UserCommission userCommission, PaymentConfig paymentConfig) {
        PaymentReturn<String> paymentReturn;
        Map<String, Object> paramMap = new HashMap<>(12);
        paramMap.put("companyId", userCommission.getCompanyId());
        paramMap.put("originalId", userCommission.getOriginalId());
        List<AgreementTemplate> agreementTemplateList = agreementTemplateService.getAgreementTemplateByParam(paramMap);
        if ((!agreementTemplateList.isEmpty()) && "1".equals(agreementTemplateList.get(0).getAgreementPayment())) {
//                    不需要验证
            logger.info("不走二要素验证>>>{}",agreementTemplateList);
            //调用支付通道工厂模式
            Payment payment = PaymentFactory.paymentEntity(paymentConfig);
            PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
            Payment proxy = paymentProxy.getProxy();
            paymentReturn = proxy.paymentTransfer(userCommission);
        } else {
            logger.info("进行二要素验证");
            Map<String, Object> map = new HashMap<>(4);
            map.put("userName",userCommission.getUserName());
            map.put("certId",userCommission.getCertId());
            List<User> userList = userSerivce.getUserByParam(map);
            User user;
            if(userList.isEmpty()){
                //未查询到用户。不进行二要素校验
                user = new User();
                user.setCheckTruth(1);
            }else{
                user = userList.get(0);
            }
            if (user.getCheckTruth() == 0) {
                try {
									Map<String,Object> checkResult = usersAgreementService.checkUserNameAndCertId(userCommission.getUserName(), userCommission.getCertId(), userCommission.getRealCompanyId(), userCommission.getOriginalId());
									int checkCode =(int)checkResult.get("code");
									boolean check = (checkCode==-1||checkCode==1);
                    if (check) {
                        //调用支付通道工厂模式
                        Payment payment = PaymentFactory.paymentEntity(paymentConfig);
                        PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
                        Payment proxy = paymentProxy.getProxy();
                        paymentReturn = proxy.paymentTransfer(userCommission);
                    } else {
                        paymentReturn = new PaymentReturn(PayRespCode.RESP_CHECK_FAIL, String.valueOf(checkResult.get("msg")), userCommission.getOrderNo());
                    }
                } catch (CheckUserNameCertIdCountException e) {
                    paymentReturn = new PaymentReturn(PayRespCode.RESP_CHECK_COUNT_FAIL, e.getMessage(), userCommission.getOrderNo());
                }
            } else if (user.getCheckTruth() == 1) {
                Payment payment = PaymentFactory.paymentEntity(paymentConfig);
                PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
                Payment proxy = paymentProxy.getProxy();
                paymentReturn = proxy.paymentTransfer(userCommission);
            } else {
                paymentReturn = new PaymentReturn(PayRespCode.RESP_CHECK_FAIL, "姓名和身份证不匹配，二要素未校验，请修改后再支付", userCommission.getOrderNo());
            }
        }
        return paymentReturn;
    }

	/**
	 * 根据customkeys 查询该角色所能查询的所有信息总条数
	 *
	 * @return UserCommission list
	 */
	@Override
	public int listCommissionByCustomKeysCount(Map<String, Object> param) {
		return commissionDao2.listCommissionByCustomKeysCount(param);
	}

	@Override
	public int commissionDetailResultCount(Map<String, Object> param) {
		return commissionDao.commissionDetailResultCount(param);
	}


	@Override
	public void updateYmTemporaryBatchData(String batchId, String originalId, String companyId, boolean autoSupplement, Set<String> validateSet) {

		logger.info("------修改明细------计算佣金费用开始-------------------");

		int passNum = 0;
		int failedNum = 0;
		String amountSum = "0.00";
		String batchAmount = "0.00";
		String handleAmount = "0.00";
		String serviceFeeSum = "0.00";
		String profitAmountSum = "0.00";
		String failedAmountSum = "0.00";
		String supplementServiceFeeSum = "0.00";

		List<CommissionTemporary> commissions = temporaryDao.getCommissionsByBatchId(batchId, originalId);
		//此处查询临时批次数据完全是为了拿到companyId
		ChannelInterimBatch interimBatch = interimBatchDao.getChannelInterimBatchByOrderno(batchId, originalId);
		//        ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrigAll(originalId, interimBatch.getRecCustomkey());
		CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfService.getConfByCustomKeyAndCompanyId(originalId, interimBatch.getRecCustomkey());

		// 批次总数
		int batchNum = commissions.size();
		for (CommissionTemporary temporary : commissions) {
			String amount = temporary.getAmount();
			if (temporary.getStatus() == 1) {
				passNum++;

				String sumFee = "0";
				String profitAmount = "0";
				String supplementFee = "0";
				//                String ruleType = temporary.getFeeRuleType();
				//                ruleType = StringUtil.isEmpty(ruleType) ? "1" : ruleType;

				if (customCompanyRateConf != null ) {
					Map<String, String> commissionFeeInfoMap = calculationFeeService.calculationFeeInfo("web", originalId, companyId, batchId, validateSet, "", "", autoSupplement, temporary, customCompanyRateConf.getServiceFeeType());
					if (customCompanyRateConf.getServiceFeeType() == ServiceFeeType.ISSUE.getCode()
							|| customCompanyRateConf.getServiceFeeType() == ServiceFeeType.PERSON.getCode()){
						sumFee = commissionFeeInfoMap.get("sumFee");
						profitAmount = commissionFeeInfoMap.get("profitAmount");
						supplementFee = commissionFeeInfoMap.get("supplementFee");

						if (customCompanyRateConf.getServiceFeeType() == ServiceFeeType.PERSON.getCode()) {
							//下发实时扣税个人承担时，这里的实发金额已被上面 calculationFeeInfo方法修改
							amount = temporary.getAmount();
						}
					}
				}


				//增加下发实时扣税个人承担后可能出现跨档补差价后 本次下发金额不足以抵扣补服务费金额 导致到账金额为负数
				if (ArithmeticUtil.compareTod(amount, "0") != 1) {
					failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
					failedNum++;
				} else {
					amountSum = ArithmeticUtil.addStr(amountSum, amount);//批次总金额
					serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum, sumFee);//批次总服务费
					profitAmountSum = ArithmeticUtil.addStr(profitAmountSum, profitAmount);//批次总利润
					supplementServiceFeeSum = ArithmeticUtil.addStr(supplementServiceFeeSum, supplementFee);//批次补差价总服务费
					passNum++;
				}

			} else if (temporary.getStatus() == 2) {
				failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, amount);
				failedNum++;
			}
			batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
		}

		handleAmount = ArithmeticUtil.addStr(amountSum, serviceFeeSum);

		ChannelInterimBatch batch = interimBatchDao.getChannelInterimBatchByOrderno(batchId, originalId);
		batch.setOrderno(batchId);
		batch.setAmount(amountSum);
		batch.setBatchAmount(batchAmount);
		batch.setServiceFee(serviceFeeSum);//服务费
		batch.setSupplementServiceFee(supplementServiceFeeSum);//服务费补差价
		batch.setMfkjServiceFee(profitAmountSum);
		batch.setPassNum(passNum);
		batch.setFailedNum(failedNum);
		batch.setBatchNum(batchNum);
		batch.setHandleAmount(handleAmount);
		batch.setFailedAmount(failedAmountSum);

		interimBatchDao.updateChannelInterimBatch(batch);
		logger.info("-------修改明细-----计算佣金费用结束-------------------");

	}

	@Override
	public String summaryInfoByMerchant(Map<String, String> params) {
		return commissionDao2.summaryInfoByMerchant(params);
	}

	@Override
	public String summaryInfoByAgent(Map<String, String> params) {
		return commissionDao2.summaryInfoByAgent(params);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupMerchant(Page page) {
		return commissionDao2.summaryInfoGroupMerchant(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupAgent(Page page) {
		return commissionDao2.summaryInfoGroupAgent(page);
	}

	@Override
	public int summaryInfoGroupMerchantCount(Page page) {
		return commissionDao2.summaryInfoGroupMerchantCount(page);
	}

	@Override
	public int summaryInfoGroupAgentCount(Page page) {
		return commissionDao2.summaryInfoGroupAgentCount(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupMerchantNoPage(Page page) {
		return commissionDao2.summaryInfoGroupMerchantNoPage(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupAgentNoPage(Page page) {
		return commissionDao2.summaryInfoGroupAgentNoPage(page);
	}

	@Override
	public Map<String, String> getCommissionDaySumAmonut(String originalId, String companyId, String batchId, String status) {

		List<Map<String, String>> list = commissionDao2.getCommissionDaySumAmonut(originalId, companyId, batchId, status);

		Map<String, String> amoutMap = new HashMap();

		list.forEach(item -> amoutMap.put(item.get("certId"), String.valueOf(item.get("mounthSumAmonut"))));

		return amoutMap;
	}

	@Override
	public Map<String, String> getCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status) {

		List<Map<String, String>> list = commissionDao2.getCommissionMounthSumAmonut(originalId, companyId, batchId, status);

		Map<String, String> amoutMap = new HashMap();

		list.forEach(item -> amoutMap.put(item.get("certId"), String.valueOf(item.get("mounthSumAmonut"))));

		return amoutMap;

	}

	@Override
	public Map<String, String> getCommissionQuarterSumAmonut(String originalId, String companyId, String batchId, String status) {

		List<Map<String, String>> list = commissionDao2.getCommissionQuarterSumAmonut(originalId, companyId, batchId, status);

		Map<String, String> amoutMap = new HashMap();

		list.forEach(item -> amoutMap.put(item.get("certId"), String.valueOf(item.get("mounthSumAmonut"))));

		return amoutMap;

	}

	@Override
	public List<Map<String, Object>> summaryInfoByMerchantMonth(Page page) {
		return commissionDao2.summaryInfoByMerchantMonth(page);
	}

	@Override
	public String summaryInfoByMonth(Page page) {
		return commissionDao2.summaryInfoByMonth(page);
	}

	@Override
	public int summaryInfoByMerchantMonthCount(Page page) {
		return commissionDao2.summaryInfoByMerchantMonthCount(page);
	}

	@Override
	public int summaryInfoGroupMerchantMonthCount(Page page) {
		return commissionDao2.summaryInfoGroupMerchantMonthCount(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupMerchantMonth(Page page) {
		return commissionDao2.summaryInfoGroupMerchantMonth(page);
	}

	@Override
	public String summaryInfoByMonthDetail(Page page) {
		return commissionDao2.summaryInfoByMonthDetail(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupMerchantMonthNoPage(
			Page page) {
		return commissionDao2.summaryInfoGroupMerchantMonthNoPage(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoByMerchantMonthNoPage(Page page) {
		return commissionDao2.summaryInfoByMerchantMonthNoPage(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupAgentMonth(Page page) {
		return commissionDao2.summaryInfoGroupAgentMonth(page);
	}

	@Override
	public int summaryInfoGroupAgentMonthCount(Page page) {
		return commissionDao2.summaryInfoGroupAgentMonthCount(page);
	}

	@Override
	public List<Map<String, Object>> summaryInfoGroupAgentMonthNoPage(Page page) {
		return commissionDao2.summaryInfoGroupAgentMonthNoPage(page);
	}

	@Override
	public List<Map<String, Object>> getInvoiceCustomInfos(CommissionInvoice commissionInvoice) {
		return commissionDao2.getInvoiceCustomInfos(commissionInvoice);
	}

	@Override
	public List<UserCommission> getUserCommissionInvoiceRecord(Map<String, Object> params) {
		return commissionDao2.getUserCommissionInvoiceRecord(params);
	}

	@Transactional
	@Override
	/**计算开票金额，并且更新对应的下发记录*/
	public void calculateInvoiceAmountAndUpdateCommission(int firstIndex,int endIndex,String userId,String companyId,
			Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap,
			List<UserCommission> userCommissionList,CommissionInvoice invoiceItem,QbInvoiceRecord invoiceRecord){
		Map<String,Object> updateParams = new HashMap<>();
		//该用户当月下发总金额
		String userTotalAmount = "0";
		for (int i = firstIndex; i < endIndex; i++) {
			UserCommission userCommission = userCommissionList.get(i);
			if (!userId.equals(userCommission.getUserId())){
				break;
			}
			String amount = userCommission.getAmount();
			userTotalAmount = ArithmeticUtil.addStr(userTotalAmount,amount,2);

			String sumFee = userCommission.getSumFee();
			//开票金额 = 下发金额 + 服务费
			String totalInvoiceAmount = ArithmeticUtil.addStr(amount,sumFee,2);
			//待开票总金额
			if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
				String existTotalInvoiceAmount = invoiceItem.getTotalInvoiceAmount();
				invoiceItem.setTotalInvoiceAmount(ArithmeticUtil.addStr(existTotalInvoiceAmount,totalInvoiceAmount,2));
			}
		}
		//获得个税档位
		String taxRate = getTaxRate(userTotalAmount, companyId,personalTaxRatesMap);
		if (ArithmeticUtil.compareTod(taxRate,"0") <= 0){//个税档位小于0
			//个税税额
			String existTaxAmount = invoiceItem.getTaxAmount();
			if (StringUtil.isEmpty(existTaxAmount)){
				invoiceItem.setTaxAmount("0.00");
			}
			//更改状态
			for (int i = firstIndex; i < endIndex; i++){
				UserCommission userCommission = userCommissionList.get(i);
				if (!userId.equals(userCommission.getUserId())){
					break;
				}
				if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
					//待开实发个税票
					String existTaxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
					invoiceItem.setTaxInvoiceAmount(ArithmeticUtil.addStr(existTaxInvoiceAmount,userCommission.getAmount(),2));
					//更新下发记录:开票流水、开票状态
					updateParams.clear();
					updateParams.put("id",userCommission.getId());
					updateParams.put("invoiceSerialNo",invoiceItem.getInvoiceSerialNo());
					updateParams.put("invoiceStatus2",1);
					commissionDao2.updateCommissionInvoiceInfo(updateParams);
				}
				userCommission.setInvoiceStatus2(9);
			}
		}else{//个税档位大于0
			//个税税额
			String existTaxAmount = invoiceItem.getTaxAmount();
			invoiceItem.setTaxAmount(ArithmeticUtil.addStr(existTaxAmount,ArithmeticUtil.mulStr(userTotalAmount,taxRate,2,
					BigDecimal.ROUND_HALF_UP),2));
			//遍历下发记录，计算已开、待开票 金额
			for (int i = firstIndex; i < endIndex; i++) {
				UserCommission userCommission = userCommissionList.get(i);
				if (!userId.equals(userCommission.getUserId())){
					break;
				}
				// 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票 9 临时状态-开票已计算
				//当用用户a，在商户A下发，第一次提交发票时，实发金额1<个税税率范围，第二次提交发票时，实发金额2+实发金额1>个税税率范围，两次的计算公式分别为：
				//第一次提交：实发个税票=实发金额1      服务费票=实发金额*服务费率
				//第二次提交：实发个税票=实发金额2+（实发金额1+实发金额2）*个税税率     服务费票=实发金额2*服务费率-（实发金额1+实发金额2）*个税税率
				String amount = userCommission.getAmount();
				//待开实发个税票 计算该笔下发记录实际个税和已经开过的个税的差额
				String existTaxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
				String taxAmount = ArithmeticUtil.mulStr(amount,taxRate,2,BigDecimal.ROUND_HALF_UP);
				String individualTax = userCommission.getIndividualTax();
				String individualBackTax = userCommission.getIndividualBackTax();
				String diffTaxAmount = ArithmeticUtil
						.subStr2(ArithmeticUtil.subStr2(taxAmount, individualTax), individualBackTax);
				String taxInvoiceAmount = diffTaxAmount;
				if (userCommission.getInvoiceStatus2() == 1 || userCommission.getInvoiceStatus2() == 2){
					if (ArithmeticUtil.compareTod(diffTaxAmount,"0") > 0){
						//更新下发记录：补个税金额、个税税率
						updateParams.clear();
						updateParams.put("id",userCommission.getId());
						updateParams.put("individualBackTax",diffTaxAmount);
						updateParams.put("taxRate",taxRate);
						updateParams.put("invoiceSerialNo2",invoiceItem.getInvoiceSerialNo());
						commissionDao2.updateCommissionInvoiceInfo(updateParams);
					}
				}
				if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
					//更新下发记录：开票流水、开票状态、个税金额、个税税率
					updateParams.clear();
					updateParams.put("id",userCommission.getId());
					updateParams.put("invoiceSerialNo",invoiceItem.getInvoiceSerialNo());
					updateParams.put("invoiceStatus2",1);
					updateParams.put("individualTax",diffTaxAmount);
					updateParams.put("taxRate",taxRate);
					commissionDao2.updateCommissionInvoiceInfo(updateParams);
					//如果是未开票状态还需要加上下发金额，才能算
					taxInvoiceAmount = ArithmeticUtil.addStr(amount,diffTaxAmount,2);
				}
				invoiceItem.setTaxInvoiceAmount(ArithmeticUtil.addStr(existTaxInvoiceAmount,taxInvoiceAmount,2));
				//待开服务费票-通过计算所得
				userCommission.setInvoiceStatus2(9);
			}
		}

	}

	@Override
	public String getTaxRate(String amount,String companyId,Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap){
		//获取服务公司个税档位
		//开封：0~10w，1.5%个税
		//贵州：0~3w（包含），0个税 ；  3w~10w，0.5%个税
		List<PersonalIncomeTaxRate> taxRateList = personalTaxRatesMap.get(companyId);
		for (PersonalIncomeTaxRate incomeTaxRate : taxRateList) {
			String amountStart = incomeTaxRate.getAmountStart();
			String amountEnd = incomeTaxRate.getAmountEnd();
			if (ArithmeticUtil.compareTod(amount,amountStart) > 0 && ArithmeticUtil.compareTod(amount,amountEnd) <= 0){
				return incomeTaxRate.getTaxRate();
			}
		}
		//如果没有匹配的返回最大值
		return taxRateList.get(taxRateList.size() - 1).getTaxRate();
	}

	@Override
	public List<UserCommission> groupUserCommissionInvoiceRecord(Map<String, Object> params) {
		return commissionDao2.groupUserCommissionInvoiceRecord(params);
	}

	@Override
	public List<String> getCommissionInvoiceSerialNos(Map<String, Object> params) {
		return commissionDao2.getCommissionInvoiceSerialNos(params);
	}

	@Override
	public Map<String, String> getRealCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status) {

		List<Map<String, String>> list = commissionDao2.getRealCommissionMounthSumAmonut(originalId, companyId, batchId, status);

		Map<String, String> amoutMap = new HashMap();

		list.forEach(item -> amoutMap.put(item.get("certId"), String.valueOf(item.get("mounthSumAmonut"))));

		return amoutMap;

	}

	@Override
	public String getRealCommissionMounthSumAmonutByCertId(String companyId, String certId) {
		return commissionDao2.getRealCommissionMounthSumAmonutByCertId(companyId, certId);
	}

	@Override
	public List<UserCommission> getLdListByTypeAndStatusAndBusinessTypeOnJob(int status, String type,
			String businessType) {
		return commissionDao.getLdListByTypeAndStatusAndBusinessTypeOnJob(status, type, businessType);
	}

	@Override
	public Map<String, String> getCommissionMounthSumAmonutByPlatformId(String batchId) {

		List<Map<String, String>> list = commissionDao2
				.getCommissionMounthSumAmonutByPlatformId(batchId);

		Map<String, String> amoutMap = new HashMap();

		list.forEach(
				item -> amoutMap.put(item.get("certId"), String.valueOf(item.get("monthSumAmonut"))));

		return amoutMap;
	}

	@Override
	public String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey) {
		return commissionDao.getTotalUninvoicedAmountByCompanyId(companyId, customKey);
	}

	@Override
	public List<UserCommission> listCommissionByCompanyId(String companyId, String receiptTime) {
		return commissionDao.listCommissionByCompanyId(companyId, receiptTime);
	}
}
