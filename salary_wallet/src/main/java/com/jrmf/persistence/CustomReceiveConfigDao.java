package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.Page;

@Mapper
public interface CustomReceiveConfigDao {

    public Integer queryRechargeAccountListCount(Page page);

    public List<Map<String, Object>> queryRechargeAccountList(Page page);

    public List<CustomReceiveConfig> queryRechargeAccountListNoPape(Map<String, Object> paramsMap);

    public void insertMerchantRechargeAccount(CustomReceiveConfig config);

    public void updateMerchantRechargeAccount(CustomReceiveConfig config);

    public CustomReceiveConfig getCustomReceiveConfigById(Integer id);

    public Integer checkMerchantRechargeAccountIsExists(CustomReceiveConfig customReceiveConfig);

    public void deleteRechargeAccountConfig(Integer id);

    List<CustomReceiveConfig> querySubAccountList(Map<String, Object> paramsMap);

    CustomReceiveConfig getCustomReceiveConfig(String customKey, String companyId,Integer payType);

    int checkSubAccountIsExists(String subAccount,String subAccountName);

    CustomReceiveConfig getCustomReceiveConfigBySubAccount(String subAccount);
}
