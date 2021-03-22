package com.jrmf.splitorder.service;

import com.jrmf.domain.CustomPaymentTotalAmount;

/**
 * @author chonglulu
 * 商户配置限额
 */
public interface SplitOrderCustomLimitConfService {

    public CustomPaymentTotalAmount queryCustomPaymentTotalAmount(String companyId,
                                                                  String customkey,
                                                                  String identityNo);

}

