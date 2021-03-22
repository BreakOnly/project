package com.jrmf.splitorder.service;

import com.jrmf.domain.CustomPaymentTotalAmount;
import com.jrmf.persistence.SplitOrderCustomPaymentTotalAmountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SplitOrderCustomLimitConfServiceImpl implements SplitOrderCustomLimitConfService {

    @Autowired
    private SplitOrderCustomPaymentTotalAmountDao customPaymentTotalAmountDao;

    @Override
    public CustomPaymentTotalAmount queryCustomPaymentTotalAmount(String companyId, String customkey, String identityNo) {
        Map<String, Object> params = new HashMap<>(10);
        params.put("companyId", companyId);
        params.put("originalId", customkey);
        params.put("identityNo", identityNo);
        CustomPaymentTotalAmount customPaymentTotalAmount = customPaymentTotalAmountDao.queryCustomPaymentTotalAmount(params);

        if (customPaymentTotalAmount == null) {
            customPaymentTotalAmount = new CustomPaymentTotalAmount();
            customPaymentTotalAmount.setCompanyId(companyId);
            customPaymentTotalAmount.setOriginalId(customkey);
            customPaymentTotalAmount.setIdentityNo(identityNo);
            customPaymentTotalAmount.setLastDayTotal(0);
            customPaymentTotalAmount.setTodayTotal(0);
            customPaymentTotalAmount.setLastMonthTotal(0);
            customPaymentTotalAmount.setCurrentMonthTotal(0);
            customPaymentTotalAmountDao.initCustomPaymentTotalAmount(customPaymentTotalAmount);
            customPaymentTotalAmount = customPaymentTotalAmountDao.queryCustomPaymentTotalAmount(params);
        }

        return customPaymentTotalAmount;
    }
}
