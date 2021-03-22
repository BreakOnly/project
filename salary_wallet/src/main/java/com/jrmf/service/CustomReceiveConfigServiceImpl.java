package com.jrmf.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jrmf.controller.constant.RechargeConfirmType;
import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.Page;
import com.jrmf.persistence.CustomReceiveConfigDao;

@Service
public class CustomReceiveConfigServiceImpl implements CustomReceiveConfigService {

    @Autowired
    private CustomReceiveConfigDao customReceiveConfigDao;
    @Autowired
    private ChannelCustomDao channelCustomDao;
    @Autowired
    private CompanyService companyService;


    @Override
    public Integer queryRechargeAccountListCount(Page page) {
        return customReceiveConfigDao.queryRechargeAccountListCount(page);
    }

    @Override
    public List<Map<String, Object>> queryRechargeAccountList(Page page) {
        return customReceiveConfigDao.queryRechargeAccountList(page);
    }

    @Override
    public List<CustomReceiveConfig> queryRechargeAccountListNoPape(
            Map<String, Object> paramsMap) {
        return customReceiveConfigDao.queryRechargeAccountListNoPape(paramsMap);
    }

    @Override
    public void insertMerchantRechargeAccount(CustomReceiveConfig config) {
        customReceiveConfigDao.insertMerchantRechargeAccount(config);
    }

    @Override
    public void updateMerchantRechargeAccount(CustomReceiveConfig config) {
        customReceiveConfigDao.updateMerchantRechargeAccount(config);
    }

    @Override
    public CustomReceiveConfig getCustomReceiveConfigById(Integer id) {
        return customReceiveConfigDao.getCustomReceiveConfigById(id);
    }

    @Override
    public Integer checkMerchantRechargeAccountIsExists(
            CustomReceiveConfig customReceiveConfig) {
        return customReceiveConfigDao.checkMerchantRechargeAccountIsExists(customReceiveConfig);
    }

    @Override
    public void deleteRechargeAccountConfig(Integer id) {
        customReceiveConfigDao.deleteRechargeAccountConfig(id);
    }

    /**
     * 查询收款账号信息
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getRechargeInfo(Map<String, Object> params) {
        List<CustomReceiveConfig> receiveConfigList = queryRechargeAccountListNoPape(params);
        Map<String, Object> data = new HashMap<>(8);
        if (receiveConfigList != null && receiveConfigList.size() > 0) {
            CustomReceiveConfig receiveConfig = receiveConfigList.get(0);
            Integer status = receiveConfig.getStatus();
            if (status != null && status == 2) {
                throw new APIDockingException(APIDockingRetCodes.COMPANY_RECHARGE_ACCOUNT_DISABLED.getCode(), APIDockingRetCodes.COMPANY_RECHARGE_ACCOUNT_DISABLED.getDesc());
            }
            data.put("inAccountNo", receiveConfig.getReceiveAccount());
            data.put("inAccountBankName", receiveConfig.getReceiveBank());
            data.put("inAccountName", receiveConfig.getReceiveUser());
            data.put("rechargeConfirmType", receiveConfig.getRechargeConfirmType());
            return data;
        }
        List<ChannelConfig> channelConfigList = channelCustomDao.getChannelConfigListByParam(params);
        if (channelConfigList != null && channelConfigList.size() > 0) {
            ChannelConfig channelConfig = channelConfigList.get(0);
            Integer status = channelConfig.getStatus();
            if (status != null && status == 2) {
                throw new APIDockingException(APIDockingRetCodes.COMPANY_RECHARGE_ACCOUNT_DISABLED.getCode(), APIDockingRetCodes.COMPANY_RECHARGE_ACCOUNT_DISABLED.getDesc());
            }
            data.put("inAccountNo", channelConfig.getAccountNum());
            data.put("inAccountBankName", channelConfig.getBankName());
            data.put("inAccountName", channelConfig.getAccountName());
            data.put("rechargeConfirmType", channelConfig.getRechargeConfirmType());
            return data;
        }
        return null;
    }

    @Override
    public List<CustomReceiveConfig> querySubAccountList(Map<String, Object> paramsMap) {
        return customReceiveConfigDao.querySubAccountList(paramsMap);
    }

    @Override
    public CustomReceiveConfig getCustomReceiveConfig(String customKey, String companyId, Integer payType) {
        return customReceiveConfigDao.getCustomReceiveConfig(customKey, companyId, payType);
    }

    @Override
    public String getSubAccount(String customKey, String companyId, Integer payType) {

        String subAcctNo = "";

        PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(payType), customKey, companyId);
        if (paymentConfig.getIsSubAccount() != null && paymentConfig.getIsSubAccount() == 1 && PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo())) {
            CustomReceiveConfig receiveConfig = getCustomReceiveConfig(customKey, companyId, payType);
            if (receiveConfig != null) {
                subAcctNo = receiveConfig.getReceiveAccount();
            }
        }

        return subAcctNo;
    }

    @Override
    public int checkSubAccountIsExists(String subAccount, String subAccountName) {
        return customReceiveConfigDao.checkSubAccountIsExists(subAccount, subAccountName);
    }

    @Override
    public CustomReceiveConfig getCustomReceiveConfigBySubAccount(String subAccount) {
        return customReceiveConfigDao.getCustomReceiveConfigBySubAccount(subAccount);
    }
}
