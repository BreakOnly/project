package com.jrmf.service;

import com.jrmf.domain.CustomLimitConf;
import com.jrmf.domain.CustomPaymentTotalAmount;

import java.util.List;
import java.util.Map;

/**
 * @author chonglulu
 * 商户配置限额
 */
public interface CustomLimitConfService {

    /**
     * 获取商户配置限额列表
     * @param hashMap 入参
     *                pageNo 页码
     *                pageSize 单页条数
     * @return list
     */
    List<CustomLimitConf> listLimitConfByParams(Map<String, Object> hashMap);

    /**
     * 添加配置
     * @param customLimitConf 新增配置
     */
    void addConfig(CustomLimitConf customLimitConf);

    /**
     * 删除配置
     * @param id 配置id
     */
    void deleteConfig(String id);

    /**
     * 修改配置
     * @param customLimitConf 修改配置
     */
    void updateConfig(CustomLimitConf customLimitConf);

    CustomLimitConf getCustomLimitConf(Map<String, Object> param);

    public boolean autoSupplement(String companyId, String customkey);

	public void updateCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo,
			String transAmount,
			boolean signFlay);

	public CustomPaymentTotalAmount queryCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo);

	public void initCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo);

	List<CustomPaymentTotalAmount> listCustomPaymentTotalAmountByParam(Map<String, Object> param);

	public int initDayMonthPaymentTotalAmount(Map<String, Object> param);


	CustomPaymentTotalAmount queryCompanyPaymentTotalAmount(String companyId,String identityNo);

	Map<String, Object> queryCompanyPaymentTotalAmountByRealCompany(String realCompanyId,String certificateNo);

    int listCustomPaymentTotalAmountByParamCount(Map<String, Object> param);

	void customAmountLimit2(String companyId, String customkey, String batchId);

	void platformAmountLimit(String batchId);
}

