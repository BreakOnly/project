package com.jrmf.domain;

import com.jrmf.oldsalarywallet.dao.CommissionTemporaryDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.service.BankCardBinService;
import com.jrmf.service.DataService;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ExecuteBatchToPAInput implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatchToPAInput.class);

	private CountDownLatch cb;

	private List<Map<String, String>> param;

	private CommissionTemporaryDao temporaryDao;
	
	private TransferBankDao transferBankDao;
	
	private ChannelRelated channelRelated;
	
	private DataService dataService;
	
	private Set<String> validateSet;
	
	private Map<String, String> batchData;

	private OrderNoUtil orderNoUtil;
	
	private BankCardBinService cardBinService;
	
	
	public ExecuteBatchToPAInput(CountDownLatch cb, List<Map<String, String>> param,OrderNoUtil orderNoUtil,
								 CommissionTemporaryDao temporaryDao, ChannelRelated channelRelated, TransferBankDao transferBankDao,
								 DataService dataService, Map<String, String> batchData, Set<String> validateSet, BankCardBinService cardBinService) {
		super();
		this.param = param;
		this.cb = cb;
		this.validateSet = validateSet;
		this.channelRelated = channelRelated;
		this.temporaryDao = temporaryDao;
		this.transferBankDao = transferBankDao;
		this.dataService = dataService;
		this.batchData = batchData;
		this.orderNoUtil = orderNoUtil;
		this.cardBinService = cardBinService;
	}

	@Override
	public void run() {
		String batchId = batchData.get("batchId");
		logger.info("------------导入银企直联下发佣金明细开始---处理数目"+param.size()+"，临时批次号："+batchId+"---------");
		List<CommissionTemporary> commissionBatch = new ArrayList<CommissionTemporary>();
        for (Map<String, String> map : param) {
        	
        	String userName = map.get("userName");
        	String amount = map.get("amount");
        	String bankCard = map.get("bankCard");
        	String certId = map.get("certId");
        	String bankName = map.get("bankName");
        	String documentType = map.get("documentType");
        	String remark = map.get("remark");
        	String bankNo = "";//银行行号，非必传项
        	if("身份证".equals(documentType)){
        		documentType = "1";
        	}else if("护照".equals(documentType)){
        		documentType = "3";
        	}else if("军官证".equals(documentType)){
        		documentType = "4";
        	}else if("港澳台通行证".equals(documentType)){
        		documentType = "2";
        	}else{
        		documentType = "0";
        	}
        	/**
        	 *  信息非空校验
        	 */
//        	if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(amount) || "0".equals(documentType)
//        			|| StringUtils.isEmpty(bankCard) || StringUtils.isEmpty(certId)) {
//        		createCommission(amount,bankCard,bankName,null, userName, certId,documentType, 2,
//        				"信息不完善", remark, commissionBatch);
//        		continue;
//        	}
        	
        	boolean emptyFlag = false;
        	String emptyMsg = "信息不完善";
        	if (StringUtils.isEmpty(userName)) {
        		emptyMsg = emptyMsg + "-姓名为空";
        		emptyFlag = true;
        	}
        	if (StringUtils.isEmpty(amount)) {
        		emptyMsg = emptyMsg + "-金额为空";
        		emptyFlag = true;
        	}
        	if ("0".equals(documentType)) {
        		emptyMsg = emptyMsg + "-证件类型为空";
        		emptyFlag = true;
        	}
        	if (StringUtils.isEmpty(bankCard)) {
        		emptyMsg = emptyMsg + "-银行卡号为空";
        		emptyFlag = true;
        	}
        	if (StringUtils.isEmpty(certId)) {
        		emptyMsg = emptyMsg + "-证件号为空";
        		emptyFlag = true;
        	}
        	if(emptyFlag){
        		createCommission(amount,
        				bankCard,
        				bankName,
        				null,
        				userName,
        				certId,
        				documentType,
        				2,
        				emptyMsg,
        				remark,
        				commissionBatch);
        		continue;
        	}
        	
        	/**
        	 * 数据重复校验
        	 */
        	if(!validateSet.add(userName+bankCard+amount)){
        		createCommission(amount,bankCard,bankName,null, userName, certId,documentType, 2,
        				"信息重复", remark, commissionBatch);
        		continue;
        	}
        	/**
        	 * 数据格式校验,港澳台暂不校验
        	 */
        	String msg = "";
        	if("1".equals(documentType)){
        		msg = StringUtil.isValidateData(amount,certId,bankCard,null,userName);
        	}else{
        		msg = StringUtil.isValidateData(amount,null,bankCard,null,null);
        	}
        	if(!StringUtil.isEmpty(msg)){
        		createCommission(amount,bankCard,bankName,null, userName, certId,documentType, 2,
        				msg,remark, commissionBatch);
        		continue;
        	}
        	
        	/**
        	 * 单笔资金超过五万
        	 */
        	if (Double.parseDouble(amount) > 50000.00 ) {
        		createCommission(amount,bankCard,bankName,null, userName, certId,documentType, 2,
        				"单笔转账不能超过五万", remark, commissionBatch);
        		continue;
        	}
        	/**
        	 * 银行卡是否支持
        	 */
        	// 1 本地cardbin表查询
        	if(StringUtil.isEmpty(bankName)){
        	    BankCard bankInfo = transferBankDao.getBankInfo(bankCard);
        	    if(bankInfo==null){
        	        // 2 三方接口查询
        	        BankName CardBin =  cardBinService.getBankName(bankCard);
        	        if(CardBin!=null){
        	            bankName = CardBin.getName();
        	            bankNo = CardBin.getSuperNetNo();
        	        }else if(StringUtil.isEmpty(bankName)){
        	            createCommission(amount,bankCard,bankName,null, userName, certId,documentType, 2,
        	                    "不支持该银行卡", remark, commissionBatch);
        	            continue;
        	        }
        	    }else{
        	        bankNo = bankInfo.getBankNo()+"";
        	        bankName = bankInfo.getBankName();
        	    }
        	}
        	createCommission(amount,bankCard,bankName,bankNo, userName, certId,documentType, 1,
        			"校验成功",remark, commissionBatch);
        }
        if(commissionBatch.size() != 0){
        	int count = temporaryDao.addCommissionTemporary(commissionBatch);
        	logger.info("------------导入银企直联下发佣金--插入条数:"+count+"不一致------------");
        	if(count != commissionBatch.size()){
        		logger.info("------------导入银企直联下发佣金--上送条数:"+commissionBatch.size()+"插入条数:"+count+"不一致------------");
        	}
        }
        cb.countDown();
		logger.info("------------导入银企直联下发佣金明细结束------------");
	}
	
	
	private void createCommission(String amount , String bankCard, String bankName, String bankNo,
			String userName, String idCard,String documentType, int status,String statusDesc, 
			String remark, List<CommissionTemporary> commissionBatch){
		String operatorName = batchData.get("operatorName");
		String batchId = batchData.get("batchId");
		String customkey = batchData.get("customkey");
		String menuId = batchData.get("menuId");
		CommissionTemporary commission = new CommissionTemporary();
		commission.setAmount(amount); 
		commission.setBankCardNo(bankCard);
		commission.setIdCard(idCard);
		commission.setUserName(userName);
		commission.setStatus(status);
		commission.setBatchId(batchId);
		commission.setOriginalId(customkey);
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setOrderNo(orderNoUtil.getChannelSerialno());
		commission.setOperatorName(operatorName);
		commission.setRemark(remark);
		commission.setBankName(bankName);
		commission.setStatusDesc(statusDesc);
		commission.setBankNo(bankNo);
		commission.setPayType(4);
		commission.setBankNo(bankNo);
		commission.setMenuId(menuId);
		
		commission.setDocumentType(Integer.parseInt(documentType));
		commissionBatch.add(commission);
		
		/**
		 * 添加缓存
		 */
//		Map<String, Object> data = new HashMap<String, Object>();
//		data.put("userName", userName);
//		data.put("bankCard", bankCard);
//		data.put("certId", idCard);
//		data.put("amount", amount);
//		data.put("bankName", bankName);
//		data.put("msg", remark);
//		dataService.addErrorData(data, batchId);
	}
}
