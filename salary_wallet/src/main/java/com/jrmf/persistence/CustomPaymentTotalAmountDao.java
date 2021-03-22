package com.jrmf.persistence;

import com.jrmf.domain.CustomPaymentTotalAmount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomPaymentTotalAmountDao {

	void updateCustomPaymentTotalAmount(Map<String, Object> param);

	int initDayMonthPaymentTotalAmount(Map<String, Object> param);

	CustomPaymentTotalAmount queryCustomPaymentTotalAmount(Map<String, Object> param);

	void initCustomPaymentTotalAmount(CustomPaymentTotalAmount customPaymentTotalAmount);

	List<CustomPaymentTotalAmount> listCustomPaymentTotalAmountByParam(Map<String, Object> param);

	CustomPaymentTotalAmount queryCompanyPaymentTotalAmount(@Param("companyId") String companyId,@Param("identityNo") String identityNo);

	Map<String, Object> queryCompanyPaymentTotalAmountByRealCompany(@Param("companyId") String realCompanyId,@Param("identityNo") String certificateNo);

    int listCustomPaymentTotalAmountByParamCount(Map<String, Object> param);
}
