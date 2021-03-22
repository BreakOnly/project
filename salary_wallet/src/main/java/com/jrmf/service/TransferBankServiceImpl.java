package com.jrmf.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.BankCard;
import com.jrmf.domain.BankInfo;
import com.jrmf.domain.TransferBank;
import com.jrmf.persistence.TransferBankDao;

/** 
* @author 种路路 
* @version 创建时间：2017年8月17日 下午9:12:30 
* 类说明 
*/
@Service("TransferBankService")
public class TransferBankServiceImpl implements TransferBankService {
	@Autowired
	protected TransferBankDao transferBankDao;
	@Override
	public void creatUserTransferOutBank(int id, String recard, String bankNo) {
		// TODO Auto-generated method stub
		transferBankDao.creatUserTransferOutBank(id,recard,bankNo);
	}
	@Override
	public List<TransferBank> getTransferOutBankListByUserId(int id) {
		// TODO Auto-generated method stub
		return transferBankDao.getTransferOutBankListByUserId(id);
	}
	@Override
	public List<TransferBank> getTransferInBankListByUserId(int id) {
		// TODO Auto-generated method stub
		return transferBankDao.getTransferInBankListByUserId(id);
	}
	@Override
	public BankCard getBankInfo(String recard) {
		// TODO Auto-generated method stub
		return transferBankDao.getBankInfo(recard);
	}
	@Override
	public void creatTransferOutBankReturnId(TransferBank transferBank) {
		// TODO Auto-generated method stub
		transferBankDao.creatTransferOutBankReturnId(transferBank);
	}
	@Override
	public void activeCard(int id) {
		// TODO Auto-generated method stub
		transferBankDao.activeCard( id);
	}
	@Override
	public List<BankInfo> getbanks() {
		// TODO Auto-generated method stub
		return transferBankDao.getbanks();
	}
	@Override
	public void creatCompanyBank(TransferBank transferBank) {
		// TODO Auto-generated method stub
		transferBankDao.creatCompanyBank( transferBank);
	}
	@Override
	public List<TransferBank> getCompanyBankList(int id) {
		// TODO Auto-generated method stub
		return transferBankDao.getCompanyBankList(id);
	}
	@Override
	public void unbindBankCardById(int id) {
		transferBankDao.unbindBankCardById( id);
	}
	@Override
	public List<TransferBank> getUnActiveCompanyBankList(int id) {
		return transferBankDao.getUnActiveCompanyBankList(id);
	}
	@Override
	public void updateTransferBankInfo(TransferBank transferBank) {
		transferBankDao.updateTransferBankInfo(transferBank);
	}
	@Override
	public TransferBank getBankByCardNo(String bankCardNo,String userId) {
		return transferBankDao.getBankByCardNo(bankCardNo,userId);
	}
	@Override
	public void deleteByUserIds(String userIds) {
		transferBankDao.deleteByUserIds(userIds);
	}
	@Override
	public void addTransferBank(TransferBank transferBank) {
		transferBankDao.addTransferBank(transferBank);
	}
	@Override
	public void deleteByBatcheId(String batcheId, String originalId) {
		transferBankDao.deleteBatcheId(batcheId, originalId);
	}
	@Override
	public List<TransferBank> getBankData(int id) {
		// TODO Auto-generated method stub
		return transferBankDao.getBankData(id);
	}

}
 