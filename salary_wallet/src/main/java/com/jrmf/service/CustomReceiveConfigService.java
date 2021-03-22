package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.Page;

public interface CustomReceiveConfigService {

    public Integer queryRechargeAccountListCount(Page page);

    public List<Map<String, Object>> queryRechargeAccountList(Page page);

    public List<CustomReceiveConfig> queryRechargeAccountListNoPape(Map<String, Object> paramsMap);

    public void insertMerchantRechargeAccount(CustomReceiveConfig config);

    public void updateMerchantRechargeAccount(CustomReceiveConfig config);

    public CustomReceiveConfig getCustomReceiveConfigById(Integer id);

    public Integer checkMerchantRechargeAccountIsExists(CustomReceiveConfig customReceiveConfig);

    public void deleteRechargeAccountConfig(Integer id);

    /**
     * 查询收款账号信息
     *
     * @param params
     * @return
     */
    Map<String, Object> getRechargeInfo(Map<String, Object> params);

    List<CustomReceiveConfig> querySubAccountList(Map<String, Object> paramsMap);

    CustomReceiveConfig getCustomReceiveConfig(String customKey, String companyId, Integer payType);

    String getSubAccount(String customKey, String companyId, Integer payType);

    int checkSubAccountIsExists(String subAccount,String subAccountName);

    CustomReceiveConfig getCustomReceiveConfigBySubAccount(String subAccount);
}
