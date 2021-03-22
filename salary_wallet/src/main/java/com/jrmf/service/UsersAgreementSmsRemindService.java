package com.jrmf.service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.CommissionGroup;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.TempCommission;
import com.jrmf.domain.WhiteUser;
import com.jrmf.utils.dto.InputBankBatchData;
import com.jrmf.utils.dto.InputBatchData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * @author: YJY
 * @date: 2020/10/28 11:22
 * @description:
 */
@Service
public interface UsersAgreementSmsRemindService {


  /**
   * @return java.util.HashMap<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description 查询发送短信所需参数
   * @Date 2020/10/28
   * @Param domainName:域名 originalId:商户key userIds:需要发送的用户ID needAll:是否全选发送
   **/
  Map<String,Object> findSmsTemplate(String domainName,String originalId,String userIds);

  /**
   * @return java.util.HashMap<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description 查询发送短信所需参数
   * @Date 2020/10/28
   * @Param originalId:商户key smsSign:短信签名 content:短信内容  ids:需要发送的签约用户ID needAll:是否全选发送
   **/
  boolean sendSms(String originalId, String smsSign, String content, String ids);

  /**
   * @Author YJY
   * @Description  检查用户ID是否是同一个所属商户下
   * @Date  2020/10/29
   * @Param  batchId:批次ID, customkey:商户key, companyId:服务公司ID
   * @return void
   **/
  JSONObject checkUserId(List userIds);

  /**
   * @Author YJY
   * @Description 工商户效验 个体户效验
   * @Date  2020/10/29
   * @Param  batchId:批次ID, customkey:商户key, companyId:服务公司ID realCompanyId:实际下发服务公司 payType:下发方式
   * @return void
   **/
  void checkUser(String batchId,String customkey,String companyId,String realCompanyId,int payType, List<InputBatchData> inputBatchData);


}
