package com.jrmf.service;

import java.util.Map;
import java.util.Set;

import com.jrmf.domain.CommissionTemporary;

public interface CalculationFeeService {
	
	public Map<String, String> calculationFeeInfo(String callType,
			String originalId,
			String companyId,
			String batchId,
			Set<String> validateSet,
			String certId,
			String amountTemp,
			boolean autoSupplement,
			CommissionTemporary temporary,
			Integer serviceFeeType);

    /**
     * 检查服务费
     * @param originalId 商户id
     * @param sumFee 服务费
     * @param calculationRates 费率
     * @return 服务费
     */
    String checkSumFee(String originalId, String sumFee, String calculationRates);

    void locationCustomCompanyRateConf(String companyId,
                                              String customkey,
                                              String batchId);
}
