package com.jrmf.persistence;

import com.jrmf.domain.CustomPaymentTotalAmount;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface SplitOrderCustomPaymentTotalAmountDao {

    void initCustomPaymentTotalAmount(CustomPaymentTotalAmount customPaymentTotalAmount);

    CustomPaymentTotalAmount queryCustomPaymentTotalAmount(Map<String, Object> param);

}
