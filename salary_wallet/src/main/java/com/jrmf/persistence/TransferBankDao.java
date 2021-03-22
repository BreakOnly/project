package com.jrmf.persistence;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.BankCard;
import com.jrmf.domain.BankInfo;
import com.jrmf.domain.TransferBank;
import com.jrmf.domain.User;
import com.jrmf.taxsettlement.api.util.BankCode;

/** 
* @author 种路路 
* @version 创建时间：2017年8月17日 下午9:18:59 
* 类说明 
*/
@Mapper
public interface TransferBankDao {

	void creatUserTransferOutBank(@Param("userId")int userId, @Param("recard")String recard, @Param("bankNo")String bankNo);

	List<TransferBank> getTransferOutBankListByUserId(@Param("id")int id);

	List<TransferBank> getTransferInBankListByUserId(@Param("id") int id);
	
	TransferBank getBankByCardNo(@Param("bankCardNo") String bankCardNo,@Param("userId") String userId);

	BankCard getBankInfo(@Param("recard") String recard);

	void creatTransferOutBankReturnId(TransferBank transferBank);
	
	void addTransferBank(TransferBank transferBank);

	void activeCard(@Param("id") int id);
	
	void deleteBatcheId(@Param("batcheId") String batcheId,@Param("originalId") String originalId);

	List<BankInfo> getbanks();

	void creatCompanyBank(TransferBank transferBank);

	List<TransferBank> getCompanyBankList(@Param("id") int id);

	void unbindBankCardById(@Param("id") int id);

	List<TransferBank> getUnActiveCompanyBankList(@Param("id") int id);
	
	List<TransferBank> getBankData(@Param("id") int id);
	
	List<BankCard> getbankcardAll();

	void updateTransferBankInfo(TransferBank transferBank);
	
	void deleteByUserIds(@Param("userIds") String userIds);

	Set<String> getUserNameByCertId(@Param("certId")String certId);
	
	int updateUserNameByCertId(@Param("certId")String certId, @Param("userName")String userName);

	List<User> getUserByCertId(@Param("certId")String certId);
	
	List<BankCode> getAllBankCodes();

	int updateBankCard(BankCard bankCard);

	int addBankCard(BankCard bankCard);

	int isExist(String bankNo);
}
 