package com.jrmf.controller.systemrole.merchant.account;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.bankapi.pingansub.PinganBankTransactionConstants;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.Page;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.AccountSystemFactory;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.AccountSystem;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.entity.PingAnBankAccountSystem;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomReceiveConfigService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/merchant/RechargeAccount")
public class MerchantRechargeAccountController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(MerchantRechargeAccountController.class);

  @Autowired
  private CustomReceiveConfigService customReceiveConfigService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private ChannelCustomService customService;

  /**
   * 查询商户充值账户配置信息
   *
   * @param request
   * @return
   */
  @RequestMapping("/queryRechargeAccountList")
  @ResponseBody
  public Map<String, Object> queryRechargeAccountList(HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    Page page = new Page(request);
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (!isMFKJAccount(customLogin) && !isPlatformAccount(customLogin)) {
      return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
    }
    int total = customReceiveConfigService.queryRechargeAccountListCount(page);
    //查询商户充值账户配置信息
    List<Map<String, Object>> relationList = customReceiveConfigService.queryRechargeAccountList(page);
    result.put("total", total);
    result.put("relationList", relationList);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }

  /**
   * 添加/修改商户充值配置信息
   *
   * @param customReceiveConfig
   */
  @ResponseBody
  @RequestMapping("/SaveRechargeAccountConfig")
  public Map<String, Object> SaveRechargeAccountConfig(CustomReceiveConfig customReceiveConfig,
      HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    try {
      if (customReceiveConfig.getIsSubAccount() == null) {
        customReceiveConfig.setIsSubAccount(0);
      }
      if (customReceiveConfig.getId() != null) {
        //修改
        customReceiveConfig.setUpdateTime(DateUtils.getNowDate());
        customReceiveConfigService.updateMerchantRechargeAccount(customReceiveConfig);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      } else {
        //添加
        //判断是否存在该记录
        Integer count = customReceiveConfigService.checkMerchantRechargeAccountIsExists(customReceiveConfig);
        if (count > 0) {
          result.put(RespCode.RESP_STAT, RespCode.MERCHNT_ONLYONE_ACCOUNT);
          result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.MERCHNT_ONLYONE_ACCOUNT));
        } else {
          PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(customReceiveConfig.getPayType()),
                  customReceiveConfig.getCustomkey(), customReceiveConfig.getCompanyId());
          if (paymentConfig != null && (paymentConfig.getIsSubAccount() != null && paymentConfig.getIsSubAccount() == 1)
                  && (customReceiveConfig.getIsSubAccount() != null && customReceiveConfig.getIsSubAccount() == 1)) {
            ChannelCustom custom = customService.getCustomByCustomkey(customReceiveConfig.getCustomkey());
            customReceiveConfig.setCustomId(custom.getId());

            customReceiveConfig.setContractCompanyName(custom.getContractCompanyName());

            AccountSystem accountSystem = AccountSystemFactory.accountSystemEntity(paymentConfig);
            ActionReturn<String> res = accountSystem.addSubAccount(customReceiveConfig);

            customReceiveConfig.setReceiveUser(paymentConfig.getCorporationAccountName());
            customReceiveConfig.setReceiveBank(paymentConfig.getCorporationName());
            customReceiveConfig.setReceiveBankNo(paymentConfig.getParameter1());

            if (res.getRetCode().equals(CommonRetCodes.ACTION_DONE.getCode())) {
              customReceiveConfig.setReceiveAccount(res.getAttachment());
            } else {
              if (PinganBankTransactionConstants.SUBACCOUNT_STATE_EXIST.equals(res.getRetCode())) {
                String subAccount = res.getFailMessage().substring(14, 28);
                customReceiveConfig.setReceiveAccount(subAccount);

                res = accountSystem.recoverySubAccount(customReceiveConfig);
                if (res.getRetCode().equals(CommonRetCodes.ACTION_DONE.getCode())) {
                  customReceiveConfig.setReceiveAccount(subAccount);

                } else if (PinganBankTransactionConstants.SUBACCOUNT_STATE_SUCCESS
                    .equals(res.getRetCode())) {
                  subAccount = res.getFailMessage().substring(12, 26);
                  customReceiveConfig.setReceiveAccount(subAccount);
                } else {
                  result.put(RespCode.RESP_STAT, RespCode.INSERT_ACCOUNT_FAIL);
                  result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INSERT_ACCOUNT_FAIL));
                  return result;
                }
              } else {
                result.put(RespCode.RESP_STAT, RespCode.INSERT_ACCOUNT_FAIL);
                result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INSERT_ACCOUNT_FAIL));
                return result;
              }
            }
          }

          customReceiveConfig.setAddUser(customLogin.getUsername());
          customReceiveConfig.setCreateTime(DateUtils.getNowDate());

          customReceiveConfigService.insertMerchantRechargeAccount(customReceiveConfig);
          result.put(RespCode.RESP_STAT, respstat);
          result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        }


      }
    } catch (Exception e) {
      result.put(RespCode.RESP_STAT, RespCode.INSERT_ACCOUNT_EXCEPTION);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INSERT_ACCOUNT_EXCEPTION));
      logger.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * 删除商户充值配置信息
   *
   * @param customReceiveConfig
   */
  @ResponseBody
  @RequestMapping("/deleteRechargeAccountConfig")
  public Map<String, Object> deleteRechargeAccountConfig(CustomReceiveConfig customReceiveConfig) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    try {
      if (customReceiveConfig.getId() != null) {
        //修改
        customReceiveConfig = customReceiveConfigService
            .getCustomReceiveConfigById(customReceiveConfig.getId());
        if (customReceiveConfig != null) {
          if (customReceiveConfig.getIsSubAccount() != null
              && customReceiveConfig.getIsSubAccount() == 1) {
            PaymentConfig paymentConfig = companyService
                .getPaymentConfigInfo(String.valueOf(customReceiveConfig.getPayType()),
                    customReceiveConfig.getCustomkey(), customReceiveConfig.getCompanyId());
            PingAnBankAccountSystem pingAnBankAccountSystem = new PingAnBankAccountSystem(
                paymentConfig);

//                        ActionReturn<String> res = pingAnBankZZH.deleteSubAccount(customReceiveConfig);
//                        if (res.getRetCode().equals(CommonRetCodes.ACTION_DONE.getCode())) {
            customReceiveConfigService.deleteRechargeAccountConfig(customReceiveConfig.getId());
            return returnSuccess();
//                        } else {
//                            logger.error(res.getFailMessage());
//                            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
//                        }
          } else {
            customReceiveConfigService.deleteRechargeAccountConfig(customReceiveConfig.getId());
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
          }
        } else {
          result.put(RespCode.RESP_STAT, RespCode.NO_ACCOUNT_INFO);
          result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.NO_ACCOUNT_INFO));
        }
      } else {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, RespCode.INSERT_ACCOUNT_EXCEPTION);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INSERT_ACCOUNT_EXCEPTION));
    }
    return result;
  }


  /**
   * 查询是否子账号模式
   */
  @RequestMapping("/queryIsSubAccount")
  @ResponseBody
  public Map<String, Object> queryIsSubAccount(String customkey, String companyId, String payType) {

    if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(companyId) || StringUtil
        .isEmpty(payType)) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfo(payType, customkey, companyId);

    Map<String, Object> result = new HashMap<>();

    if (paymentConfig != null && paymentConfig.getIsSubAccount() != null
        && paymentConfig.getIsSubAccount() == 1) {
      result.put("isSubAccount", paymentConfig.getIsSubAccount());
    } else {
      result.put("isSubAccount", "0");
    }

    return returnSuccess(result);

  }
}
