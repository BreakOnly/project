package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.CompanyAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomBalanceDao {
	
	int updateBalance(Map<String, Object> param);

	//金额字段单位为分
	Integer queryBalance(Map<String, Object> param);

	void initCustomBalance(Map<String,Object> param);

	List<CompanyAccount> queryCompanyAccount(Map<String,Object> param);

	String queryCustomBalance(String customKey,String companyId,Integer payType);

	String queryCustomBalanceForUpdate(String customKey,String companyId,Integer payType);
}
