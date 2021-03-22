package com.jrmf.controller.zhipai;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.BankName;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.YuncrUserBank;
import com.jrmf.domain.dto.YuncrUserAuthenticationRequestDTO;
import com.jrmf.domain.vo.YuncrUserBankVO;
import com.jrmf.service.BankCardBinService;
import com.jrmf.service.UserAuthenticationService;
import com.jrmf.taxsettlement.api.service.yuncr.UserAuthenticationBankCardService;
import com.jrmf.utils.RespCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: YJY
 * @date: 2020/9/24 11:18
 * @description:
 */
@RestController
@RequestMapping("zhipai/")
public class UserAuthenticationController extends BaseController {

  @Autowired
  UserAuthenticationService userAuthenticationService;
  @Autowired
  BankCardBinService bankCardBinService;


  /**
   * @Author YJY
   * @Description 获取用户微信信息
   * @Date  2020/9/25
   * @Param [phone]
   * @return com.jrmf.common.APIResponse
   **/
  @PostMapping("wechat/msg")
  public APIResponse getWeChatMsg(String id) {

    if (StringUtils.isBlank(id)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_5009);
    }

    return APIResponse.successResponse(userAuthenticationService.getWeChatMsg(id));
  }

  /**
   * @Author YJY
   * @Description 个体户注册审核查询
   * @Date  2020/9/25
   * @Param [phone]
   * @return com.jrmf.common.APIResponse
   **/
  @PostMapping("user/audit/list")
  public APIResponse userAuditList(@RequestBody YuncrUserAuthenticationRequestDTO requestDTO, HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(!isRootAdmin(customLogin)){
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    return userAuthenticationService.findUserByCondition(requestDTO);
  }

  /**
   * @Author YJY
   * @Description 个体户注册审核
   * @Date  2020/9/25
   * @Param [phone]
   * @return com.jrmf.common.APIResponse
   **/
  @PostMapping("/enterprise/audit")
  public APIResponse enterpriseAudit(@RequestBody JSONObject jsonObject, HttpServletRequest request) {

     ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
     if(!isRootAdmin(customLogin)){

       return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
     }
     jsonObject.put("customkey",customLogin.getCustomkey());
      return userAuthenticationService.enterpriseAudit(jsonObject);
  }

  /**
   * @Author YJY
   * @Description 个体户注册审核 重新提交
   * @Date  2020/9/25
   * @Param [phone]
   * @return com.jrmf.common.APIResponse
   **/
  @PostMapping("/enterprise/resubmit")
  public APIResponse resubmit(@RequestBody JSONObject jsonObject, HttpServletRequest request) {

    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(!isRootAdmin(customLogin)){
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    return userAuthenticationService.resubmit(jsonObject);
  }

  /**
   * @Author wsheng
   * @Description 个体户注册审核- 查询用户银行卡信息
   **/
  @PostMapping("/enterprise/bank/info")
  public Map<String, Object> getBankInfo(@RequestParam Integer id,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer pageSize, HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (!isRootAdmin(customLogin)) {
      return returnFail(RespCode.error101, "权限不足");
    }
    PageHelper.startPage(pageNo, pageSize);
    List<YuncrUserBankVO> list = userAuthenticationService.listBankInfo(id);
    this.getBankCardName(list);
    PageInfo page = new PageInfo(list);
    Map<String, Object> result = new HashMap<>(4);
    result.put("list", page.getList());
    result.put("total", page.getTotal());
    return returnSuccess(result);
  }

  private void getBankCardName(List<YuncrUserBankVO> list) {
    Optional.ofNullable(list)
        .ifPresent(lists -> {
          lists.forEach(yuncrUserBank -> {
            BankName bank = bankCardBinService.getBankName(yuncrUserBank.getBankCardNumber());
            if ("true".equals(bank.getValidated())) {
              yuncrUserBank.setBankCardName(bank.getName());
            }
          });
        });
  }

  /**
   * @Author wsheng
   * @Description 个体户注册审核- 绑定银行卡
   **/
  @PostMapping("/enterprise/binding/bankcard")
  public Map<String, Object> bindingBankCard(YuncrUserBank yuncrUserBank, HttpServletRequest request) {
    /*ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(!isRootAdmin(customLogin)){
      return returnFail(RespCode.error101, "权限不足");
    }*/
    return userAuthenticationService.bindingBankCard(yuncrUserBank);
  }

  private void getBankNamePrefix(String bankName) {
    if (bankName.contains("银行")) {
      // 截取银行
      int bankNameIndex = bankName.indexOf("银行");
      bankName = bankName.substring(0, bankNameIndex);
    }
  }

  /**
   * 查询所有银行
   * @return
   */
  @PostMapping("/enterprise/all/bank")
  public Map<String, Object> getAllBank() {
    List<Map<String, Object>> bankList = userAuthenticationService.getAllBank();
    return returnSuccess(bankList);
  }

  /**
   * @Author wsheng
   * @Description 个体户注册审核- 查询银行卡所属银行
   **/
  @PostMapping("/enterprise/bankcard/info")
  public Map<String, Object> getBankCardInfo(@RequestParam String bankCardNumber, HttpServletRequest request) {
   /* ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(!isRootAdmin(customLogin)){
      return returnFail(RespCode.error101, "权限不足");
    }*/
    Map<String, Object> result = new HashMap<>();
    BankName bank = bankCardBinService.getBankName(bankCardNumber);
    if ("true".equals(bank.getValidated())) {
      String bankName = bank.getName();
      List<Map<String, Object>> subBank = userAuthenticationService
          .getSubBankByBankName(bankName);
      result.put("list", subBank);
    } else {
      return returnFail(RespCode.error101, "银行卡号格式错误");
    }
    return returnSuccess(result);
  }

  /**
   * @Author wsheng
   * @Description 个体户注册审核- 删除银行卡
   **/
  @PostMapping("/enterprise/delete/bankcard")
  public Map<String, Object> deleteBankCard(@RequestParam String id, HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(!isRootAdmin(customLogin)){
      return returnFail(RespCode.error101, "权限不足");
    }
    userAuthenticationService.deleteUserBankCard(id);
    return returnSuccess();
  }
}
