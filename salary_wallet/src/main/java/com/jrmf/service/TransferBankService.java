package com.jrmf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jrmf.domain.BankCard;
import com.jrmf.domain.BankInfo;
import com.jrmf.domain.TransferBank;

/** 
* @author 种路路 
* @version 创建时间：2017年8月17日 下午9:06:47 
* 类说明 
*/
@Service
public interface TransferBankService {

	void creatUserTransferOutBank(int id, String recard, String bankNo);

	List<TransferBank> getTransferOutBankListByUserId(int id);

	List<TransferBank> getTransferInBankListByUserId(int id);

	BankCard getBankInfo(String recard);

	void creatTransferOutBankReturnId(TransferBank transferBank);

	void addTransferBank(TransferBank transferBank);

	void activeCard(int id);

	List<BankInfo> getbanks();

	void creatCompanyBank(TransferBank transferBank);

	List<TransferBank> getCompanyBankList(int id);
	
	List<TransferBank> getBankData(int id);

	void unbindBankCardById(int id);

	List<TransferBank> getUnActiveCompanyBankList(int id);

	void updateTransferBankInfo(TransferBank transferBank);
	
	void deleteByUserIds(String userIds);
	
	TransferBank getBankByCardNo(String bankCardNo,String userId);
	
	void deleteByBatcheId(String batcheId,String originalId);
	
}
 