package com.jrmf.service;

import static com.jrmf.common.Constant.SAME_NAME;
import static com.jrmf.common.Constant.UN_AUDIT;
import static com.jrmf.common.Constant.UN_BIND_BANK;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.Constant;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.CommissionGroup;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.Company;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.domain.SendSmsHistoryRecord;
import com.jrmf.domain.TempCommission;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.WhiteUser;
import com.jrmf.domain.YuncrUserAuthentication;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.persistence.OemConfigDao;
import com.jrmf.persistence.SendSmsHistoryRecordDao;
import com.jrmf.persistence.UsersAgreementDao;
import com.jrmf.persistence.YuncrUserAuthenticationDao;
import com.jrmf.utils.dto.InputBatchData;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.SMSChannelFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2020/10/28 11:22
 * @description:
 */
@Slf4j
@Service
public class UsersAgreementSmsRemindServiceImpl implements UsersAgreementSmsRemindService {

  @Autowired
  OemConfigDao oemConfigDao;
  @Autowired
  UsersAgreementDao usersAgreementDao;
  @Autowired
  SendSmsHistoryRecordDao sendSmsHistoryRecordDao;
  @Autowired
  CompanyService companyService;

  @Autowired
  CommissionTemporary2Dao temporaryDao2;

  @Autowired
  WhiteUserService whiteUserService;

  @Autowired
  YuncrUserAuthenticationDao yuncrUserAuthenticationDao;

  /**
   * @return java.util.Map<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description 查询发送短信参数
   * @Date 2020/10/28
   * @Param [domainName, originalId, userIds, needAll]
   **/
  @Override
  public Map<String, Object> findSmsTemplate(String domainName, String originalId, String userIds) {

    Map<String, Object> resultData = new HashMap<>();

    String smsSign = "智税通";
    int sendSmsNumber = 0;
    int noPhoneNumber = 0;
    int limitNumber = 0;
    String shortClientDomain = "";
    Map<String, Object> map = new HashMap<>();
    map.put("portalDomain", domainName);
    map.put("status", 1);
    List<OemConfig> list = oemConfigDao.getOemByParam(map);
    if (CollectionUtils.isNotEmpty(list)) {
      smsSign = list.get(0).getSmsSignature();
      shortClientDomain = list.get(0).getShortClientDomain();

    }
    if (StringUtils.isEmpty(shortClientDomain)) {
      shortClientDomain = "";
    }
    String content = "XXXXX公司提示您，请关注xx公众号或点击地址" + shortClientDomain + "进行签约否则会影响您的收入发放。";
    List ids = Arrays.asList(userIds.split(","));

    /**
     * @Description 计算用户有无手机号数量
     **/
    List<UsersAgreement> usersData = usersAgreementDao
        .findSendSmsUsers(originalId, null, ids);

    noPhoneNumber = usersData.size();
    if (CollectionUtils.isNotEmpty(usersData)) {
      for (UsersAgreement user : usersData) {

        if (!StringUtils.isEmpty(user.getMobilePhone())) {

          sendSmsNumber++;
        }
      }

      noPhoneNumber = usersData.size() - sendSmsNumber;
    }

    /**
     * @Description 计算有无超限手机号
     **/
    List<SendSmsHistoryRecord> records = sendSmsHistoryRecordDao.findCountLimit(originalId);
    if (CollectionUtils.isNotEmpty(records)) {

      for (SendSmsHistoryRecord record : records) {
        for (UsersAgreement user : usersData) {
          if (!StringUtils.isEmpty(user.getMobilePhone()) && user.getMobilePhone()
              .equals(record.getReceivePhone())) {

            limitNumber++;
          }

        }
      }

      sendSmsNumber = sendSmsNumber - limitNumber;
    }

    resultData.put("content", content);
    resultData.put("smsSign", smsSign);
    resultData.put("sendSmsNumber", sendSmsNumber);
    resultData.put("noPhoneNumber", noPhoneNumber);
    resultData.put("limitNumber", limitNumber);
    return resultData;
  }

  /**
   * @return boolean
   * @Author YJY
   * @Description 发送短信
   * @Date 2020/10/28
   * @Param [originalId, smsSign, content, userIds, needAll]
   **/
  @Override
  public boolean sendSms(String originalId, String smsSign, String content, String ids) {

    boolean dataFlag = false;

    List idList = new ArrayList();
    /**
     * @Description 该商户未签约用户全部发短信提醒
     **/
    if (!StringUtils.isEmpty(ids)) {

      idList = Arrays.asList(ids.split(","));
    }

    /**
     * @Description 查询有手机号的数据
     **/
    List<UsersAgreement> usersData = usersAgreementDao
        .findSendSmsUsers(originalId, "yes", idList);
    String phone = "";
    List<SendSmsHistoryRecord> record = new ArrayList();
    List phoneList = new ArrayList();

    /**
     * @Description 计算有无超限手机号
     **/
    List<SendSmsHistoryRecord> records = sendSmsHistoryRecordDao.findCountLimit(originalId);
    if (CollectionUtils.isNotEmpty(usersData) && CollectionUtils.isNotEmpty(records)) {
      for (int send = 0; send < usersData.size(); send++) {
        for (int limit = 0; limit < records.size(); limit++) {
          if (records.get(limit).getReceivePhone().equals(usersData.get(send).getMobilePhone())) {
            records.remove(limit);
            usersData.remove(send);
            send--;
            continue;
          }
        }
      }
    }

    if (CollectionUtils.isNotEmpty(usersData)) {

      for (UsersAgreement data : usersData) {
        SendSmsHistoryRecord sendSmsHistoryRecord = new SendSmsHistoryRecord();
        sendSmsHistoryRecord.setCustomKey(data.getOriginalId());
        sendSmsHistoryRecord.setIsSuccess("2");
        sendSmsHistoryRecord.setReceivePhone(data.getMobilePhone());
        sendSmsHistoryRecord.setSendContent(content);
        sendSmsHistoryRecord.setSendType("1");
        sendSmsHistoryRecord.setReceiveUserId(data.getUserId());
        record.add(sendSmsHistoryRecord);
        phone += data.getMobilePhone() + ",";

        phoneList.add(data.getMobilePhone());
      }
    }

    if (CollectionUtils.isNotEmpty(record)) {
      dataFlag = sendSmsHistoryRecordDao.batchInsert(record) > 0 ? true : false;
    }
    if (!dataFlag) {
      return false;
    }

    boolean flag = sms(phone, content, smsSign, SmsTemplateCodeEnum.YXY_UNSIGN_NOTICE.getCode(),
        null);
    if (flag) {

      sendSmsHistoryRecordDao.batchUpdate(originalId, phoneList);
    }

    return true;
  }


  public boolean sms(String phoneNo, String content, String signName, String templateCode,
      String templateParam) {
    content = "【" + signName + "】" + content;
    SMSChannelConfig channelConfig = companyService.getSmsConfig();
    SMSChannel smsChannel = SMSChannelFactory.createChannel(channelConfig);

    boolean flag = smsChannel
        .sendSMS(new String[]{phoneNo}, content, signName,
            templateCode, templateParam);
    return flag;
  }

  @Override
  public JSONObject checkUserId(List userIds) {

    JSONObject data = new JSONObject();
    List<UsersAgreement> agreementList = usersAgreementDao.findByIds(userIds);
    if (CollectionUtils.isNotEmpty(agreementList)) {
      String customKey = agreementList.get(0).getOriginalId();
      data.put("customKey", customKey);
      data.put("isOne", "yes");
      data.put("companyName", agreementList.get(0).getCompanyName());
      for (UsersAgreement ua : agreementList) {

        if (!customKey.equals(ua.getOriginalId())) {

          data.put("isOne", "no");
        }

      }

    }
    return data;
  }

  /**
   * @Description 下发效验是否完成个体工商户注册 和绑定银行卡
   **/
  @Override
  public void checkUser(String batchId, String customkey, String companyId, String realCompanyId,
      int payType, List<InputBatchData> inputBatchData) {
    log.info(
        "进入工商户效验参数为" + "batchId:" + batchId + "customkey:" + customkey + "companyId" + companyId
            + "realCompanyId:" + realCompanyId + "payType:" + payType);
    List<CommissionGroup> commissionGroupList = temporaryDao2
        .getCommissionGroupByCertId(batchId, "1", null);
    Company company = new Company();
    if (StringUtils.isEmpty(realCompanyId)) {
      company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
      if(1 == company.getCompanyType()){
        company = companyService.getCompanyByUserId(Integer.parseInt(company.getRealCompanyId()));
      }
    } else {
      company = companyService.getCompanyByUserId(Integer.parseInt(realCompanyId));
    }

    for (CommissionGroup commission : commissionGroupList) {

      List<TempCommission> commissionList = commission.getCommissionList();

      if (CollectionUtils.isNotEmpty(commissionList)) {

        //工商户同名效验
        checkSameName(commission.getCertId(), customkey, companyId, commissionList);

        if (company.getCheckUserAuth() == 1) {
          //个体工商户效验
          checkUserAuth(commission.getCertId(), commissionList);
        }


      }


    }

  }

  /**
   * @Description 工商户同名效验
   **/
  public void checkSameName(String certId, String customkey, String companyId,
      List<TempCommission> commissionList) {

    WhiteUser whiteUser = new WhiteUser();
    whiteUser.setCertId(certId);
    whiteUser.setDocumentType(1);
    whiteUser.setCustomkey(customkey);
    whiteUser.setCompanyId(companyId);
    Integer isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);
    /**
     * @Description 如果是白名单则跳过用户验证
     **/
    if (isWhiteUser < 1) {

      for (TempCommission data : commissionList) {
        Integer count = sendSmsHistoryRecordDao.checkByUserName(data.getUserName(), customkey);
        if (count > 0) {
          CommissionTemporary commissionFail = getCommissionTemporary(data.getId(), SAME_NAME);
          temporaryDao2.updateCommissionTemporary(commissionFail);
        }
      }

    }
  }

  /**
   * @Description 个体工商户效验
   **/
  public void checkUserAuth(String idCard, List<TempCommission> commissionList) {

    List<YuncrUserAuthentication> list = yuncrUserAuthenticationDao
        .selectByCondition(idCard, null, null, null);
    if (CollectionUtils.isEmpty(list)
        || list.get(0).getGovernmentAudit() != Constant.AUDIT_SUCCESS) {

      for (TempCommission data : commissionList) {

        CommissionTemporary commissionFail = getCommissionTemporary(data.getId(), UN_AUDIT);
        temporaryDao2.updateCommissionTemporary(commissionFail);

      }

    } else {
      //查询银行卡号
      for (TempCommission data : commissionList) {
        int count = yuncrUserAuthenticationDao.selectBank(idCard, data.getBankCardNo());
        if (count == 0) {
          CommissionTemporary commissionFail = getCommissionTemporary(data.getId(), UN_BIND_BANK);
          temporaryDao2.updateCommissionTemporary(commissionFail);
        }
      }
    }
  }


  public CommissionTemporary getCommissionTemporary(int id, String desc) {

    CommissionTemporary commissionFail = new CommissionTemporary();
    commissionFail.setId(id);
    commissionFail.setStatus(2);
    commissionFail.setStatusDesc(desc);
    commissionFail.setSumFee("0.00");
    commissionFail.setCalculationRates("0.00");
    commissionFail.setSupplementAmount("0.00");
    commissionFail.setSupplementFee("0.00");

    return commissionFail;
  }
}
