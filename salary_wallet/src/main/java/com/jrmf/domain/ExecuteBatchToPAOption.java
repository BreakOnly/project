package com.jrmf.domain;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.BankService;
import com.jrmf.bankapi.SubmitTransferParams;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.PayType;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.persistence.UserRelatedDao;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.transaction.TransactionRunner;
import com.jrmf.utils.transaction.TransactionSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ExecuteBatchToPAOption implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatchToPAOption.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private String batchId;

	private String operatorName;

	private String customName;

	private String companyName;

	private int companyId;

	private String fileName;

	private String batchDesc;

	private String batchName;

	private int menuId;

	private String originalId;

	private String menuName;

	private OrderNoUtil orderNoUtil;

	private CyclicBarrier cb;

	private List<CommissionTemporary> param;

	private BankService bankService;

	private UserSerivce userSerivce;

	private UserCommissionDao userCommissionDao;

	private TransferBankDao transferBankdDao;

	private ChannelRelated channelRelated;

	private UserRelatedDao userRelatedDao;

	private CustomBalanceDao customBalanceDao;

	private TransactionRunner transactionRunner;
	
	private BaseInfo baseInfo;

	public ExecuteBatchToPAOption(CyclicBarrier cb,OrderNoUtil orderNoUtil, List<CommissionTemporary> param,UserCommissionDao userCommissionDao,UserSerivce userSerivce,ChannelRelated channelRelated,
								  TransferBankDao transferBankdDao,UserRelatedDao userRelatedDao,TransactionRunner transactionRunner,Map<String, Object> batchData,
								  BaseInfo baseInfo) {
		super();
		this.param = param;
		this.cb = cb;
		this.orderNoUtil = orderNoUtil;
		this.userCommissionDao = userCommissionDao;
		this.transferBankdDao = transferBankdDao;
		this.userSerivce = userSerivce;
		this.channelRelated = channelRelated;
		this.userRelatedDao = userRelatedDao;
		this.transactionRunner = transactionRunner;
		this.baseInfo = baseInfo;
		batchId = (String) batchData.get("batchId");
		operatorName = (String) batchData.get("operatorName");
		fileName = (String) batchData.get("fileName");
		batchDesc = (String) batchData.get("batchDesc");
		batchName = (String) batchData.get("batchName");
		originalId = (String) batchData.get("originalId");
		menuName = (String) batchData.get("menuName");
		customName = (String) batchData.get("customName");
		companyName = (String) batchData.get("companyName");
		menuId = (Integer) batchData.get("menuId");
		companyId = (Integer) batchData.get("companyId");
	}

	@Override
	public void run() {
		try {
			logger.info("------------????????????--??????????????????---????????????"+param.size()+"---------");
			List<UserCommission> commissionBatch = new ArrayList<UserCommission>();
			for (CommissionTemporary commission: param) {
				try {
					/**
					 * ??????????????????
					 */
					Map<String,Object> paramMap= new HashMap<String,Object>();
					paramMap.put("userName",commission.getUserName());
					paramMap.put("certId",commission.getIdCard());
					User user = userSerivce.getUsersCountByCard(paramMap);
					if (user == null) {
						int userId= createSuccessUser(commission);
						commission.setUserId(userId);
					}else{
						/**
						 * ????????????????????????????????????????????????????????????
						 */
						commission.setUserId(user.getId());
						paramMap.clear();
						paramMap.put("merchantId",channelRelated.getMerchantId());
						paramMap.put("certId",commission.getIdCard());
						paramMap.put("userName",commission.getUserName());
						paramMap.put("originalId",channelRelated.getOriginalId());
						int size = userSerivce.getUsersCountByParam(paramMap);
						if(size==0){
							UserRelated userRelated = new UserRelated();
							userRelated.setStatus(0);//????????????????????????
							userRelated.setCreateTime(DateUtils.getNowDate());
							userRelated.setOriginalId(channelRelated.getOriginalId());
							userRelated.setUserId(user.getId());
							userRelated.setCompanyId(channelRelated.getCompanyId());
	                    	try{
	                    		userRelatedDao.createUserRelated(userRelated);
	                    	}catch(Exception e){
													logger.error(e.getMessage(),e);
	                    	}
						}

						TransferBank bank = transferBankdDao.
								getBankByCardNo(commission.getBankCardNo(), user.getId()+"");

						if(bank == null){
							transferBankdDao.deleteByUserIds(user.getId()+"");
							/**
							 *  ???????????????????????????
							 */
							TransferBank transferBank = new TransferBank();
							transferBank.setBankNo(commission.getBankNo());
							transferBank.setUser_id(user.getId()+"");
							transferBank.setBankCardNo(commission.getBankCardNo());
							transferBank.setTransferType("2");
							transferBank.setStatus(1);
							transferBank.setBankName(commission.getBankName());
							transferBankdDao.addTransferBank(transferBank);
						}
					}
					/**
					 * ????????????????????????--????????????????????????
					 */
					if(commission.getStatus()==2){
						createErrorCommion(commission,commissionBatch);
						continue;
					}else if(commission.getStatus()==1){
						createSuccessUserCommission(commission, commissionBatch);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					createErrorCommion(commission,commissionBatch);
				}
			}
			/**
			 * ?????????????????????????????????
			 */
			if(commissionBatch.size() != 0){
				String costBalance = "";
				for(UserCommission commission : commissionBatch){

					String costAmount = ArithmeticUtil.addStr(commission.getSumFee(), commission.getAmount());
					costBalance = ArithmeticUtil.addStr(costBalance, costAmount);
				}
				Map<String,Object> params= new HashMap<>();
				params.put("customkey", originalId);
				params.put("companyId", companyId);
				params.put("payType", String.valueOf(PayType.PINGAN_BANK.getCode()));
				params.put("amount", new BigDecimal(costBalance).multiply(new BigDecimal(100)).intValue());

				Map<String, Object> context = new HashMap<>();
				context.put("arg1", params);
				context.put("arg2", commissionBatch);

				if(transactionRunner.runTransaction(TransactionDefinition.PROPAGATION_REQUIRED, context,
						new TransactionSection() {

							@Override
							public void doInATransaction(Map<String, Object> paramContext) {

								customBalanceDao.updateBalance((Map<String, Object>)paramContext.get("arg1"));
								List<UserCommission> commissionBatch = (List<UserCommission>) paramContext.get("arg2");
								int result = userCommissionDao.addUserCommissionBatch(commissionBatch);
								/**
								 * ????????????????????????????????????
								 */
								logger.info("------------??????????????????????????????--????????????:"+result+"------------------");
								if(result != commissionBatch.size()){
									logger.info("------------??????????????????????????????--????????????:"+commissionBatch.size()+"????????????:"+result+"?????????------------");
								}
							}
						})) {
					logger.info("------------??????????????????------------");
				} else {
                    for (UserCommission userCommission : commissionBatch) {
                        userCommission.setStatus(2);
                        userCommission.setStatusDesc("????????????");
                    }
                    int result = userCommissionDao.addUserCommissionBatch(commissionBatch);
                    logger.info("------------??????????????????:"+result+"------------------");
                    return;
                }
			}

			/**
			 * ?????????????????????
			 */
			for (UserCommission userCommission : commissionBatch) {

				try {
					if(userCommission.getStatus() != 0){
						continue;
					}

					SubmitTransferParams params = new SubmitTransferParams();
					params.setTransferAmount(userCommission.getAmount());
					params.setTransferInAccountName(userCommission.getUserName());
					params.setTransferInAccountNo(userCommission.getAccount());
					params.setTransferInBankName(userCommission.getBankName());
					params.setTransferSerialNo(userCommission.getOrderNo());

					//?????????????????????????????????????????????13?????????
					String commissionRemark = userCommission.getRemark() == null ? "" : userCommission.getRemark();
					if (commissionRemark.length() > 13) {
						commissionRemark = commissionRemark.substring(0, 13);
					}

					params.setRemark(commissionRemark);

					if(!StringUtil.isEmpty(userCommission.getBankNo())){
						params.setTransferInBankOrgNo(userCommission.getBankNo());
					}

					logger.info("?????????????????????????????????"+params.getTransferSerialNo()+"-------"+
							params.getTransferAmount()+"-------"+
							params.getTransferInAccountNo()+"-------"+
							params.getTransferInBankOrgNo()+"-------"+
							params.getTransferInBankName()+"-------"+
							params.getTransferInAccountName()+"-------");

					ActionReturn<String> ret = bankService.submitTransfer(params);

					if(ret.isOk()) {
						// ?????????????????????
						String bankOrderNo = ret.getAttachment();
						logger.info("?????????????????????????????????????????????????????????"+bankOrderNo+"-------");
						if(bankOrderNo.length()>100){
							bankOrderNo = bankOrderNo.substring(0, 50);
						}
						userCommission.setStatus(3);
						userCommission.setAygOrderNo(bankOrderNo);
						userCommission.setStatusDesc("???????????????");
					} else {
						userCommission.setStatus(2);
						String failMessage = ret.getFailMessage();
						if(failMessage.length()>200){
							failMessage = failMessage.substring(0, 200);
						}
						userCommission.setStatusDesc(ret.getFailMessage());
					}
					userCommissionDao.updateUserCommissionByOrderNo(userCommission);
				} catch (Exception e) {
					logger.error("????????????????????????????????? ??????????????? ???"+userCommission.getOrderNo());
					logger.error(e.getMessage(),e);
				}
			}
			logger.info("------------????????????--??????????????????------------");
			cb.await();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(),e);
			logger.error(e.toString());
		} catch (BrokenBarrierException e) {
			logger.error(e.getMessage(),e);
			logger.error(e.toString());
		}
	}


	private void createErrorCommion(CommissionTemporary temporary,List<UserCommission> commissionBatch){
		UserCommission commission = new UserCommission();
		commission.setAmount(temporary.getAmount());
		commission.setCreatetime(DateUtils.getNowDate());
		commission.setUserId(temporary.getUserId()+"");
		commission.setStatus(2);
		commission.setStatusDesc(temporary.getStatusDesc());
		commission.setRemark(temporary.getRemark());
		commission.setBatchId(batchId);
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setOrderNo(StringUtil.getBankOrderNO());
		commission.setProfiltFree("0");
		commission.setCalculationRates("0");
		commission.setSumFee("0");
		commission.setOriginalId(channelRelated.getOriginalId());
		commission.setMerchantId(channelRelated.getMerchantId());
		commission.setOperatorName(operatorName);
		commission.setPayType(4);
		commission.setAccount(temporary.getBankCardNo());
		commission.setInvoiceStatus(2);//?????????

		commission.setUserName(temporary.getUserName());
		commission.setAccount(temporary.getBankCardNo());
		commission.setBankNo(temporary.getBankNo());
		commission.setBankName(temporary.getBankName());
		commission.setContentName(menuName);
		commission.setMenuId(menuId);
		commission.setBatchFileName(fileName);
		commission.setBatchDesc(batchDesc);
		commission.setBatchName(batchName);
		commission.setDocumentType(temporary.getDocumentType());
		commission.setCertId(temporary.getIdCard());
		commission.setBankNo(temporary.getBankNo());
		commission.setCustomName(customName);
		commission.setCompanyName(companyName);
		commissionBatch.add(commission);
	}

	private int createSuccessUser(CommissionTemporary commission){
		/**
		 *  ????????????????????????
		 */
		User user = new User();
		user.setCertId(commission.getIdCard());
		user.setUserName(commission.getUserName());
		user.setMerchantId(channelRelated.getMerchantId());
		user.setUserType(11);//??????????????????
		user.setCompanyUserNo(channelRelated.getCompanyId());
		user.setAccount(commission.getBankCardNo());
		userSerivce.addUser(user);
		int userid = user.getId();
		/**
		 *  ???????????????????????????
		 */
		TransferBank transferBank = new TransferBank();
		transferBank.setBankNo(commission.getBankNo());
		transferBank.setStatus(1);
		transferBank.setUser_id(user.getId()+"");
		transferBank.setBankCardNo(commission.getBankCardNo());
		transferBank.setBankName(commission.getBankName());
		transferBank.setTransferType("2");
		transferBankdDao.addTransferBank(transferBank);

		/**
		 * ???????????????????????????
		 */
		UserRelated userRelated = new UserRelated();
		userRelated.setStatus(0);//????????????????????????
		userRelated.setCreateTime(DateUtils.getNowDate());
		userRelated.setOriginalId(originalId);
		userRelated.setUserId(userid);
		userRelated.setCompanyId(channelRelated.getCompanyId());
    	try{
    		userRelatedDao.createUserRelated(userRelated);
    	}catch(Exception e){
				logger.error(e.getMessage(),e);
    	}
		return userid;
	}

	private void createSuccessUserCommission(CommissionTemporary temporary, List<UserCommission> commissionBatch){
		String amount  = temporary.getAmount();
		/**
		 * ???????????????
		 */
		UserCommission commission = new UserCommission();
		String commissionMfkjFree = "0";
		if(ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit())<0){
			commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltLower(), 2);
			commission.setProfilt(channelRelated.getProfiltLower());
		}else{
			commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltUpper(), 2);
			commission.setProfilt(channelRelated.getProfiltUpper());
		}
		//?????????
		String commissionAygFree = ArithmeticUtil.mulStr(amount, channelRelated.getServiceRates(), 2);

		commission.setAmount(amount);
		commission.setCreatetime(DateUtils.getNowDate());
		commission.setUserId(temporary.getUserId() + "");
		commission.setStatus(0);
		commission.setAccount(temporary.getBankCardNo());
		commission.setBatchId(batchId);
		commission.setOriginalId(originalId);
		commission.setMerchantId(channelRelated.getMerchantId());
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setOrderNo(StringUtil.getBankOrderNO());
		commission.setOperatorName(operatorName);
		commission.setStatusDesc("????????????????????????");
		commission.setSumFee(commissionAygFree);
		commission.setProfiltFree(commissionMfkjFree);
		commission.setCalculationRates(channelRelated.getServiceRates());
		commission.setPayType(4);
		commission.setInvoiceStatus(2);//?????????
		commission.setContentName(menuName);
		commission.setMenuId(menuId);
		commission.setBatchFileName(fileName);
		commission.setBatchDesc(batchDesc);
		commission.setBatchName(batchName);
		commission.setAccountDate(sdf.format(new Date()));

		commission.setCreatetime(DateUtils.getNowDate());
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setUserName(temporary.getUserName());
		commission.setReceiptNo(orderNoUtil.getReceiptNo());
		commission.setRemark(commission.getReceiptNo()+temporary.getRemark());
		commission.setBankNo(temporary.getBankNo());
		commission.setBankName(temporary.getBankName());
		commission.setDocumentType(temporary.getDocumentType());
		commission.setCertId(temporary.getIdCard());
		commission.setCustomName(customName);
		commission.setCompanyName(companyName);
		commissionBatch.add(commission);
	}
}
