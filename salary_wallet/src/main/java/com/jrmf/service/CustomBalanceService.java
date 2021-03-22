package com.jrmf.service;

import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.*;
import com.jrmf.splitorder.domain.Custom;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface CustomBalanceService {

    Integer queryBalance(Map<String, Object> param);

    void initCustomBalance(Map<String, Object> param);

    List<CompanyAccount> queryCompanyAccount(Map<String, Object> param);

    boolean confirmBalance(ChannelHistory info, CustomTransferRecord record);

    int updateCustomBalanceAndSubBalance(String customKey, String companyId, Integer payType, String amount, int operating, TradeType changeType, String operatorName, String refundOrderNo);

    String queryCustomBalance(String customKey, String companyId, Integer payType);

    boolean manualConfirmBalance(ChannelHistory info, List<CustomTransferRecord> records);

    boolean rechargeRefund(ChannelHistory history, String amount, String operatorName);

    void updateSubAccountBalance(String customKey, String companyId, Integer payType, String amount, int operating, TradeType changeType, String operatorName);

    void updateCustomBalance(int operating, CustomBalanceHistory balanceHistory);

    List<CustomBalanceHistory> listCustomBalanceHistory(CustomBalanceHistory balanceHistory);

    void updateCustomAndCompanyBalance(UserCommission transferParam);
}
