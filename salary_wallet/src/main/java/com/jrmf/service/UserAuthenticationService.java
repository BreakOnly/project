package com.jrmf.service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.APIResponse;
import com.jrmf.domain.OpenUser;
import com.jrmf.domain.YuncrUserAuthentication;
import com.jrmf.domain.YuncrUserBank;
import com.jrmf.domain.YuncrUserFailNode;
import com.jrmf.domain.dto.YuncrUserAuthenticationRequestDTO;
import com.jrmf.domain.vo.YuncrUserBankVO;
import java.util.List;
import java.util.Map;

/**
 * @author: YJY
 * @date: 2020/9/24 11:25
 * @description:
 */
public interface UserAuthenticationService {


  /**
   * @Author YJY
   * @Description 获取用户微信信息
   * @Date  2020/9/24
   * @Param [phoneNumber]
   * @return com.alibaba.fastjson.JSONObject
   **/
  OpenUser getWeChatMsg(String id);

  /**
   * @Author YJY
   * @Description 查询用户注册认证列表
   * @Date  2020/9/24
   * @Param [phoneNumber]
   * @return com.alibaba.fastjson.JSONObject
   **/
  APIResponse findUserByCondition(YuncrUserAuthenticationRequestDTO requestDTO);


  /**
   * @Author YJY
   * @Description 企业审核
   * @Date  2020/9/24
   * @Param [JSONArray]
   **/
  APIResponse enterpriseAudit(JSONObject jsonObject);

  /**
   * @Author YJY
   * @Description 重新提交审核
   * @Date  2020/9/24
   * @Param [JSONArray]
   **/
  APIResponse resubmit(JSONObject jsonObject);

  /**
   * 绑定银行卡
   */
  Map<String, Object> bindingBankCard(YuncrUserBank yuncrUserBank);

  /**
   * 查询用户所绑定的银行卡信息
   */
  List<YuncrUserBankVO> listBankInfo(Integer id);

  /**
   * 通过银行名称前缀获取支行
   */
  List<Map<String, Object>> getSubBankByBankName(String bankName);

  /**
   * 删除用户银行卡
   */
  void deleteUserBankCard(String id);

  /**
   * 查询所有银行
   * @return
   */
  List<Map<String, Object>> getAllBank();

  /**
  * @Description 回调接口
  **/
  void callBack(String param);
}
