package com.jrmf.service;

import static com.jrmf.common.Constant.AUDIT_FAILED;
import static com.jrmf.common.Constant.AUDIT_SUCCESS;
import static com.jrmf.common.Constant.IN_AUDIT;
import static com.jrmf.common.NodeMessage.AUTH_FAIL;
import static com.jrmf.common.NodeMessage.AUTH_SUCCESS;
import static com.jrmf.common.NodeMessage.WAIT_ENTERPRISE_AUTH;
import static com.jrmf.common.NodeMessage.WAIT_GOVERNMENT_AUTH;
import static com.jrmf.common.NodeMessage.WAIT_USER_UPLOAD;
import static com.jrmf.common.ProcessNode.ALL_SUCCESS;
import static com.jrmf.common.ProcessNode.BUSINESS_LICENSE;
import static com.jrmf.common.ProcessNode.ENTERPRISE;
import static com.jrmf.common.ProcessNode.IDENTITY_AUTH_FRONT;
import static com.jrmf.common.ProcessNode.IDENTITY_AUTH_REVERSE;
import static com.jrmf.common.ProcessNode.SFC;
import static com.jrmf.common.ProcessNode.VIDEO_AUTH;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.Constant;
import com.jrmf.common.ProcessNode;
import com.jrmf.common.RespResult;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.common.YuncrFailNode;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.controller.constant.YuncrFeignClient;
import com.jrmf.domain.ChannelUserRealName;
import com.jrmf.domain.OpenUser;
import com.jrmf.domain.YuncrUserAuthentication;
import com.jrmf.domain.YuncrUserFailNode;
import com.jrmf.domain.YuncrUserBank;
import com.jrmf.domain.dto.YuncrUserAuthenticationRequestDTO;
import com.jrmf.domain.dto.YuncrUserBankDTO;
import com.jrmf.domain.vo.YuncrUserBankVO;
import com.jrmf.persistence.YuncrUserAuthenticationDao;
import com.jrmf.persistence.YuncrUserFailNodeDao;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.gateway.APIDockingGatewayDataUtil;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.utils.OkHttpUtils;
import com.jrmf.utils.PicUtils;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.ThreadPoolUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2020/9/24 16:48
 * @description:
 */
@Slf4j
@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

  @Autowired
  YuncrUserAuthenticationDao yuncrUserAuthenticationDao;

  @Autowired
  YuncrServiceFeignClient yuncrServiceFeignClient;
  @Autowired
  ChannelUserRealNameServiceImpl channelUserRealNameService;
  @Autowired
  YuncrUserFailNodeDao yuncrUserFailNodeDao;
  @Autowired
  YuncrFeignClient yuncrFeignClient;

  @Autowired
  APIDockingManager apiDockingManager;

  @Autowired
  private SignWorkers signWorkers;


  @Override
  public OpenUser getWeChatMsg(String id) {
    return yuncrUserAuthenticationDao.findWeChatInfo(id);
  }

  /**
   * @return com.jrmf.common.APIResponse
   * @Author YJY
   * @Description ?????????????????? ??????????????????
   * @Date 2020/9/25
   * @Param [requestDTO]
   **/
  @Override
  public APIResponse findUserByCondition(
      YuncrUserAuthenticationRequestDTO requestDTO) {
    PageHelper.startPage(1, 10);
    if (!ObjectUtils.isEmpty(requestDTO)) {

      if ((!ObjectUtils.isEmpty(requestDTO.getPageNo())) && (!ObjectUtils
          .isEmpty(requestDTO.getPageSize()))) {
        PageHelper.startPage(requestDTO.getPageNo(), requestDTO.getPageSize());
      }

      List idCard = new ArrayList();

      String customName = requestDTO.getCustomName();
      if (!StringUtils.isEmpty(customName)) {
        List<HashMap> mapList = yuncrUserAuthenticationDao.findIdCardByCompanyName(customName);
        /**
         * @Description ???????????? ???????????????
         **/
        if (CollectionUtils.isEmpty(mapList)) {
          return APIResponse.successResponse(new ArrayList<YuncrUserAuthentication>(), 0L);
        }
        for (HashMap hashMap : mapList) {
          idCard.add(hashMap.get("certId"));
        }
      }
      requestDTO.setIdCardList(idCard);
    }

    PageInfo<YuncrUserAuthentication> pageInfo = new PageInfo<YuncrUserAuthentication>(
        yuncrUserAuthenticationDao.findUserByCondition(requestDTO));

    List<YuncrUserAuthentication> dataList = pageInfo.getList();
    //????????????
    setUserStatus(dataList);
    //????????????
    searchCustom(dataList);
    //??????????????????
    searchFailNode(dataList);
    return APIResponse.successResponse(dataList, pageInfo.getTotal());
  }

  /**
   * @return void
   * @Author YJY
   * @Description ????????????
   * @Date 2020/9/25
   * @Param [jsonArray]
   **/
  @Override
  public APIResponse enterpriseAudit(JSONObject jsonObject) {

    if (!ObjectUtils.isEmpty(jsonObject)) {
      String customkey = jsonObject.getString("customkey");
      Integer id = jsonObject.getInteger("id");
      YuncrUserAuthentication data = yuncrUserAuthenticationDao.findUserInfoById(id);
      if (BUSINESS_LICENSE.getProcessNode() != data.getProcessNode()) {
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_505);
      }
      data.setCustomKey(customkey);
      Integer enterpriseAudit = jsonObject.getInteger("enterpriseAudit");
      String enterpriseRefuseReason = jsonObject.getString("enterpriseRefuseReason");

      boolean flag = false;
      data.setEnterpriseAuditDate(new Date());
      data.setEnterpriseAudit(enterpriseAudit);
      data.setEnterpriseRefuseReason(enterpriseRefuseReason);
      flag = yuncrUserAuthenticationDao.updateByExampleSelective(data) > 0 ? true : false;
      if (!flag) {
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_502);
      }
      /**
       * @Description ??????????????????
       **/
      if (Constant.AUDIT_FAILED == enterpriseAudit) {
        if (ObjectUtils.isEmpty(jsonObject.get("failNode"))) {
          return APIResponse.errorResponse(ResponseCodeMapping.ERR_546);
        }
        List<YuncrUserFailNode> failNodeList = new ArrayList<>();
        List<String> failNode = Arrays.asList(jsonObject.getString("failNode").split(","));
        for (String node : failNode) {
          YuncrUserFailNode userFailNode = new YuncrUserFailNode();
          userFailNode.setAuditNode(node);
          userFailNode.setAuthenticationId(id);
          userFailNode.setStatus(1);
          failNodeList.add(userFailNode);
        }
        boolean bathInsert = yuncrUserFailNodeDao.batchInsert(failNodeList) > 0 ? true : false;
        if (!bathInsert) {
          return APIResponse.errorResponse(ResponseCodeMapping.ERR_547);
        }

      }
      /**
       * @Description ??????????????????????????????????????????
       **/
      ThreadPoolUtils.getThread().execute(() -> {
        if (AUDIT_SUCCESS == enterpriseAudit.intValue()) {
          goToAuth(data.getPhone(), data.getApplyNumber());
        }
      });

      return APIResponse.successResponse();
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_5009);
  }

  /**
   * @Author YJY
   * @Description ??????????????????
   * @Date 2020/9/24
   * @Param [JSONArray]
   **/
  @Override
  public APIResponse resubmit(JSONObject jsonObject) {

    if (ObjectUtils.isEmpty(jsonObject)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_5009);
    }
    Integer id = jsonObject.getInteger("id");
    YuncrUserAuthentication data = yuncrUserAuthenticationDao.findUserInfoById(id);
    if (ObjectUtils.isEmpty(data)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_5009);
    }
    Integer errNode = data.getYuncrErrNode();
    if (ObjectUtils.isEmpty(errNode) || errNode.intValue() > ENTERPRISE.getProcessNode()) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_501);
    }
    /**
     * @Description ??????????????????????????????????????????
     **/
    ThreadPoolUtils.getThread().execute(() -> {
      goToAuth(data.getPhone(), data.getApplyNumber());
    });
    return APIResponse.successResponse();
  }

  /**
   * @return void
   * @Author YJY
   * @Description ????????????????????? ?????????
   * @Date 2020/9/25
   * @Param [dataList]
   **/
  public void searchCustom(List<YuncrUserAuthentication> dataList) {

    if (CollectionUtils.isEmpty(dataList)) {
      return;
    }
    List idCards = new ArrayList();
    for (YuncrUserAuthentication data : dataList) {
      if (!StringUtils.isEmpty(data.getIdCard())) {
        idCards.add(data.getIdCard());
      }
    }

    if (!CollectionUtils.isEmpty(idCards)) {

      List<HashMap> customList = yuncrUserAuthenticationDao.findCustomByUserIdCard(idCards);

      if (!CollectionUtils.isEmpty(customList)) {

        for (YuncrUserAuthentication data : dataList) {
          List<HashMap> customs = new ArrayList<>();
          for (int i = 0; i < customList.size(); i++) {
            /**
             * @Description ??????????????? ?????????????????????
             **/
            if (data.getIdCard().equals(customList.get(i).get("certId"))) {
              customs.add(customList.get(i));
              customList.remove(i);
              i--;
            }
          }
          data.setCustomName(customs);
        }
      }
    }

  }

  /**
   * @return void
   * @Author YJY
   * @Description ?????????????????? ??????
   * @Date 2020/9/25
   * @Param [dataList]
   **/
  public void searchFailNode(List<YuncrUserAuthentication> dataList) {

    if (CollectionUtils.isEmpty(dataList)) {
      return;
    }
    List authenticationId = new ArrayList();
    for (YuncrUserAuthentication data : dataList) {
      authenticationId.add(data.getId());
    }

    if (!CollectionUtils.isEmpty(authenticationId)) {

      List<YuncrUserFailNode> failNodes = yuncrUserFailNodeDao
          .findByAuthenticationId(authenticationId);

      if (!CollectionUtils.isEmpty(failNodes)) {

        for (YuncrUserAuthentication data : dataList) {
          String failNode = "";
          for (int i = 0; i < failNodes.size(); i++) {
            /**
             * @Description ??????????????? ?????????????????????
             **/
            if (data.getId() == failNodes.get(i).getAuthenticationId()) {
              failNode += YuncrFailNode.getByType(failNodes.get(i).getAuditNode()) + ",";
              failNodes.remove(i);
              i--;
            }
          }
          data.setFailNodes(failNode);
        }
      }
    }

  }

  /**
   * @return void
   * @Author YJY
   * @Description ????????????
   * @Date 2020/12/23
   * @Param [dataList]
   **/
  public void setUserStatus(List<YuncrUserAuthentication> dataList) {
    /**
     * @Description ???????????????????????? ????????????????????? ??????????????????
     **/
    if (!CollectionUtils.isEmpty(dataList)) {

      for (YuncrUserAuthentication data : dataList) {
        data.setAuditStatus(WAIT_USER_UPLOAD.getNode());
        if (data.getProcessNode().intValue() == BUSINESS_LICENSE.getProcessNode()) {

          //??????????????????
          if (data.getEnterpriseAudit().intValue() == AUDIT_FAILED) {
            data.setAuditStatus(AUTH_FAIL.getNode());
            data.setStatusDescription(data.getEnterpriseRefuseReason());
          }
          //???????????????
          if (data.getEnterpriseAudit().intValue() == IN_AUDIT) {
            data.setAuditStatus(WAIT_ENTERPRISE_AUTH.getNode());
            data.setStatusDescription(data.getEnterpriseRefuseReason());
          }
          //??????????????????
          if (data.getEnterpriseAudit().intValue() == AUDIT_SUCCESS) {

            //???????????????
            if (data.getGovernmentAudit().intValue() == IN_AUDIT) {
              data.setAuditStatus(WAIT_GOVERNMENT_AUTH.getNode());
              data.setStatusDescription(data.getEnterpriseRefuseReason());
            }
            //????????????
            if (data.getGovernmentAudit().intValue() == AUDIT_SUCCESS) {
              data.setAuditStatus(AUTH_SUCCESS.getNode());
              data.setStatusDescription(data.getEnterpriseRefuseReason());
            }
            //???????????? ?????????????????????
            if (data.getGovernmentAudit().intValue() == AUDIT_FAILED) {
              data.setAuditStatus(WAIT_GOVERNMENT_AUTH.getNode());
              data.setStatusDescription(data.getEnterpriseRefuseReason());
            }
          }
        }
      }

    }
  }

  /**
   * @return void
   * @Author YJY
   * @Description ?????????????????? ?????????
   * @Date 2020/9/25
   * @Param [dataList]
   **/
  public void searchBankInfo(List<YuncrUserAuthentication> dataList) {
    if (CollectionUtils.isEmpty(dataList)) {
      return;
    }
    List ids = new ArrayList();
    for (YuncrUserAuthentication data : dataList) {
      /**
       * @Description ?????????????????????
       **/
      //data.setPhone(StringUtil.rePhone(data.getPhone()));
      // data.setIdCard(StringUtil.desensitizedIdNumber(data.getIdCard()));
      ids.add(data.getId());
    }
    List<HashMap> bankList = yuncrUserAuthenticationDao.findBankInfoByUserId(ids);
    if (!CollectionUtils.isEmpty(bankList)) {

      for (YuncrUserAuthentication data : dataList) {

        List<HashMap> banks = new ArrayList<>();
        for (int i = 0; i < bankList.size(); i++) {
          /**
           * @Description ID ????????????????????????
           **/
          if (data.getId().toString().equals(bankList.get(i).get("authentication_id").toString())) {

            //HashMap hashMap = bankList.get(i);
            /**
             * @Description ?????? --> ?????????????????????
             **/
//            hashMap.put("bank_card_number",
//                StringUtil.desensitizedBankNo(hashMap.get("bank_card_number").toString()));
//            hashMap.put("bank_card_phone",
//                StringUtil.rePhone(hashMap.get("bank_card_phone").toString()));
            banks.add(bankList.get(i));
            bankList.remove(i);
            i--;
          }
        }
        data.setBankInfo(banks);
      }
    }

  }


  /**
   * @return void
   * @Author YJY
   * @Description ????????????
   * @Date 2020/10/9
   * @Param
   **/
  public void goToAuth(String phoneNumber, String applyNumber) {
    log.info("??????????????????:" + "?????????:" + phoneNumber + "?????????" + applyNumber);
    List<YuncrUserAuthentication> authenticationList = new ArrayList<>();
    authenticationList = getUserAuthentication(null, null, phoneNumber, applyNumber);

    if (CollectionUtils.isEmpty(authenticationList)) {
      log.error("???????????????????????????");
      return;
    }
    log.info("????????????" + authenticationList.get(0).getEnterpriseAudit());
    Integer errorNode = authenticationList.get(0).getYuncrErrNode();

    JSONObject result = new JSONObject();

    if (ObjectUtils.isEmpty(errorNode) || errorNode <= IDENTITY_AUTH_FRONT.getProcessNode()) {

      log.info("??????????????????????????????");
      JSONObject identityAuth = new JSONObject();
      identityAuth
          .put("photo", PicUtils.encryptToBase64(authenticationList.get(0).getIdCardFrontUrl()));
      identityAuth.put("side", "0");
      result = yuncrServiceFeignClient.sfc(identityAuth);
      if (!checkResultData(result, authenticationList.get(0),
          IDENTITY_AUTH_FRONT.getProcessNode())) {
        log.error("????????????????????? ????????????????????????");
        return;
      }
    }
    if (ObjectUtils.isEmpty(errorNode) || errorNode <= IDENTITY_AUTH_REVERSE.getProcessNode()) {

      log.info("??????????????????????????????");
      JSONObject identityAuthRever = new JSONObject();
      identityAuthRever
          .put("photo", PicUtils.encryptToBase64(authenticationList.get(0).getIdCardReverseUrl()));
      identityAuthRever.put("side", "1");
      result = yuncrServiceFeignClient.sfc(identityAuthRever);
      if (!checkResultData(result, authenticationList.get(0),
          IDENTITY_AUTH_REVERSE.getProcessNode())) {
        log.error("????????????????????? ????????????????????????");
        return;
      }
    }
    if (ObjectUtils.isEmpty(errorNode) || errorNode <= SFC.getProcessNode()) {
      /**
       * @Description ?????????????????? ????????????????????????
       **/
      authenticationList = getUserAuthentication(null, null, phoneNumber, applyNumber);

      log.info("????????????????????????");
      JSONObject trueName = new JSONObject();
      trueName.put("name", authenticationList.get(0).getName());
      trueName.put("certId", authenticationList.get(0).getIdCard());
      trueName.put("certIdFrontSerialNo", authenticationList.get(0).getIdCardFrontNumber());
      result = yuncrServiceFeignClient.nameAuth(trueName);
      if (!checkResultData(result, authenticationList.get(0), SFC.getProcessNode())) {
        log.error("???????????? ????????????????????????");
        return;
      }
    }
    if (ObjectUtils.isEmpty(errorNode) || errorNode <= VIDEO_AUTH.getProcessNode()) {

      log.info("????????????????????????");
      JSONObject requestObj = new JSONObject();
      requestObj.put("certIdFrontSerialNo", authenticationList.get(0).getIdCardFrontNumber());
      requestObj.put("video", PicUtils.encryptToBase64(authenticationList.get(0).getLiveTestUrl()));
      result = yuncrServiceFeignClient.videoAuth(requestObj);
      if (!checkResultData(result, authenticationList.get(0), VIDEO_AUTH.getProcessNode())) {
        log.error("???????????? ????????????????????????");
        return;
      }
    }
    if (ObjectUtils.isEmpty(errorNode) || errorNode <= BUSINESS_LICENSE.getProcessNode()) {
      /**
       * @Description ????????????????????????
       **/
      authenticationList = getUserAuthentication(null, null, phoneNumber, applyNumber);

      log.info("???????????????????????????");
      JSONObject businessLicense = new JSONObject();
      businessLicense.put("certIdBackSerialNo", authenticationList.get(0).getIdCardReverseNumber());
      businessLicense.put("certIdFrontSerialNo", authenticationList.get(0).getIdCardFrontNumber());
      businessLicense.put("realNameSerialNo", authenticationList.get(0).getTrueNameNumber());
      businessLicense.put("videoSerialNo", authenticationList.get(0).getLiveTestNumber());
      businessLicense.put("mobileNo", authenticationList.get(0).getPhone());
      businessLicense
          .put("sign", PicUtils.encryptToBase64(authenticationList.get(0).getSignatureUrl()));
      result = yuncrServiceFeignClient.businessLicense(businessLicense);
      if (!checkResultData(result, authenticationList.get(0), BUSINESS_LICENSE.getProcessNode())) {
        log.error("??????????????? ????????????????????????");
        return;
      }
    }

    if (ObjectUtils.isEmpty(errorNode) || errorNode <= ENTERPRISE.getProcessNode()) {

      authenticationList = getUserAuthentication(null, null, phoneNumber, applyNumber);
      if (authenticationList.isEmpty() || authenticationList.size() > 1) {
        log.error("???????????????????????????");
        return;
      }

      /**
       * @Description ???????????????????????????  ??????????????????
       **/
      JSONObject requestData = new JSONObject();
      requestData.put("firmId", authenticationList.get(0).getFirmId());
      requestData.put("status", "1");
      result = yuncrServiceFeignClient.approvalQy(requestData);
      if (!checkResultData(result, authenticationList.get(0), ENTERPRISE.getProcessNode())) {
        log.error("?????????????????? ????????????????????????");
        return;
      }
    }

    log.info("???????????????????????????,??????????????????");
  }


  /**
   * @return boolean
   * @Author YJY
   * @Description ??????????????????   ?????????????????? true ???????????? false ???????????????
   * @Date 2020/10/9
   * @Param [result]
   **/
  public boolean checkResultData(JSONObject result,
      YuncrUserAuthentication yuncrUserAuthentication, int processNode) {

    switch (processNode) {
      case 1:
        return identityAuth(result, yuncrUserAuthentication, IDENTITY_AUTH_FRONT);
      case 2:
        return identityAuth(result, yuncrUserAuthentication, IDENTITY_AUTH_REVERSE);
      case 3:
        return sfc(result, yuncrUserAuthentication, SFC);
      case 4:
        return videoAuth(result, yuncrUserAuthentication, VIDEO_AUTH);
      case 5:
        return businessLicense(result, yuncrUserAuthentication, BUSINESS_LICENSE);
      case 6:
        return enterpriseAudit(result, yuncrUserAuthentication, ENTERPRISE);
      default:
        return false;

    }
  }

  /**
   * @return void
   * @Author YJY
   * @Description ??????????????????????????????
   * @Date 2020/10/9
   * @Param []
   **/
  public boolean identityAuth(JSONObject result, YuncrUserAuthentication user,
      ProcessNode processNode) {

    if (!checkYuncrData(result, user, processNode)) {
      return false;
    }

    JSONObject dataMsg = result.getJSONObject("data");
    /**
     * @Description ????????????????????????
     **/
    boolean flag = false;

    if (IDENTITY_AUTH_FRONT.getProcessNode() == processNode.getProcessNode()) {
      /**
       * @Description ????????????
       **/
      user.setAddress(dataMsg.getString("address"));
      user.setBirthday(dataMsg.getString("birthday"));
      user.setIdCard(dataMsg.getString("certId"));
      user.setNation(dataMsg.getString("fork"));
      user.setName(dataMsg.getString("name"));
      user.setSex(dataMsg.getString("sex"));
      user.setIdCardFrontNumber(dataMsg.getString("serialNo"));
    } else {
      user.setVaildPriod(dataMsg.getString("vaildPriod"));
      user.setIssueAuthority(dataMsg.getString("issueAuthority"));
      user.setIdCardReverseNumber(dataMsg.getString("serialNo"));
    }

    flag = yuncrUserAuthenticationDao.updateByExampleSelective(user) > 0 ? true : false;
    return flag;
  }

  /**
   * @return boolean
   * @Author YJY
   * @Description ????????????
   * @Date 2020/10/9
   * @Param []
   **/
  public boolean sfc(JSONObject result, YuncrUserAuthentication userInfo,
      ProcessNode processNode) {

    if (!checkYuncrData(result, userInfo, processNode)) {
      return false;
    }
    JSONObject nameAuth = result.getJSONObject("data");
    String realNameSerialNo = nameAuth.getString("realNameSerialNo");
    userInfo.setTrueNameNumber(realNameSerialNo);
    /**
     * @Description ??????????????????
     **/
    Boolean flag =
        yuncrUserAuthenticationDao.updateByExampleSelective(userInfo) > 0 ? true : false;
    return flag;

  }


  /**
   * @return boolean
   * @Author YJY
   * @Description ????????????
   * @Date 2020/10/9
   * @Param [jsonObject, userInfo]
   **/
  public boolean videoAuth(JSONObject jsonObject, YuncrUserAuthentication userInfo,
      ProcessNode processNode) {

    if (!checkYuncrData(jsonObject, userInfo, processNode)) {
      return false;
    }
    JSONObject yuncrData = jsonObject.getJSONObject("data");
    userInfo.setLiveTestNumber(yuncrData.getString("videoSerialNo"));
    Boolean flag =
        yuncrUserAuthenticationDao.updateByExampleSelective(userInfo) > 0 ? true
            : false;

    return flag;
  }


  /**
   * @return boolean
   * @Author YJY
   * @Description ???????????????
   * @Date 2020/10/9
   * @Param [result, userInfo]
   **/
  public boolean businessLicense(JSONObject result, YuncrUserAuthentication userInfo,
      ProcessNode processNode) {

    if (!checkYuncrData(result, userInfo, processNode)) {
      return false;
    }

    JSONObject yuncrData = result.getJSONObject("data");
    String firmId = yuncrData.getString("firmId");
    if (StringUtils.isEmpty(firmId)) {
      updateYuncrUser(userInfo, processNode, "???????????????????????????firmId??????");
      return false;
    }
    userInfo.setFirmId(firmId);
    Boolean flag =
        yuncrUserAuthenticationDao.updateByExampleSelective(userInfo) > 0 ? true
            : false;

    return flag;
  }


  /**
   * @return boolean
   * @Author YJY
   * @Description ??????????????????
   * @Date 2020/10/13
   * @Param [result, userInfo, node]
   **/
  public boolean enterpriseAudit(JSONObject result, YuncrUserAuthentication userInfo,
      ProcessNode processNode) {
    if (!checkYuncrData(result, userInfo, processNode)) {
      return false;
    }
    userInfo.setYuncrErrNode(ALL_SUCCESS.getProcessNode());
    userInfo.setYuncrErrMessage(ALL_SUCCESS.getMsg());
    Boolean flag =
        yuncrUserAuthenticationDao.updateByExampleSelective(userInfo) > 0 ? true
            : false;

    return flag;

  }

  /**
   * @return void
   * @Author YJY
   * @Description ????????????????????????????????????
   * @Date 2020/10/9
   * @Param []
   **/
  public void updateYuncrUser(YuncrUserAuthentication userAuthentication, ProcessNode processNode,
      String msg) {

    userAuthentication.setYuncrErrNode(processNode.getProcessNode());
    userAuthentication.setYuncrErrMessage(msg);
    yuncrUserAuthenticationDao.updateByExampleSelective(userAuthentication);

  }


  /**
   * @return boolean
   * @Author YJY
   * @Description ?????? ????????????????????????
   * @Date 2020/10/12
   * @Param [result, processNode]
   **/
  public boolean checkYuncrData(JSONObject result,
      YuncrUserAuthentication userInfo, ProcessNode processNode) {

    /**
     * @Description ?????? yuncr_service ????????????
     **/
    if (ObjectUtils.isEmpty(result)) {
      updateYuncrUser(userInfo, processNode, processNode.getMsg() + ":user??????????????????????????????");
      return false;
    }

    /**
     * @Description ?????? yuncr_service ????????????
     **/
    if (!"00000".equals(result.get("code"))) {

      updateYuncrUser(userInfo, processNode, processNode.getMsg() + ":" + result.get("msg"));
      return false;
    }
    /**
     * @Description ?????? ?????? ????????????
     **/
    JSONObject yuncr = result.getJSONObject("data");
    if (ObjectUtils.isEmpty(yuncr)) {

      updateYuncrUser(userInfo, processNode, processNode.getMsg() + ":yucr??????????????????????????????");
      return false;
    }

    String code = yuncr.getString("code");
    if (!StringUtils.isEmpty(code) && !"0".equals(yuncr.getString("code"))) {

      updateYuncrUser(userInfo, processNode, processNode.getMsg() + ":" + yuncr.getString("info"));
      return false;
    }

    return true;
  }


  /**
   * @return java.util.List<com.jrmf.domain.yuncr.YuncrUserAuthentication>
   * @Author YJY
   * @Description ????????????????????????
   * @Date 2020/9/11
   * @Param [idCard, idCardFrontNumber]
   **/
  public List<YuncrUserAuthentication> getUserAuthentication(String idCard,
      String idCardFrontNumber, String phoneNumber, String applyNumber) {

    /**
     * @Description ?????????????????????????????? ???????????? null
     **/
    if (StringUtils.isEmpty(idCard) && StringUtils.isEmpty(idCardFrontNumber) && StringUtils
        .isEmpty(phoneNumber) && StringUtils.isEmpty(applyNumber)) {

      return new ArrayList<YuncrUserAuthentication>();
    }

    return yuncrUserAuthenticationDao
        .selectByCondition(idCard, idCardFrontNumber, phoneNumber, applyNumber);

  }


  /**
   * @Description ??????????????? ?????????????????????????????????
   **/
  public void inertChannelRealName(YuncrUserAuthentication userInfo) {
    try {

      if (ObjectUtils.isEmpty(userInfo) || StringUtils.isEmpty(userInfo.getPhone())) {
        return;
      }
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("state", "0");
      paramMap.put("userPhoneNo", userInfo.getPhone());
      List<ChannelUserRealName> list = channelUserRealNameService.selectAll(paramMap);
      ChannelUserRealName userRealName = new ChannelUserRealName();
      if (!CollectionUtils.isEmpty(list)) {
        userRealName = list.get(0);
      }
      userRealName.setUserId(userInfo.getId());
      userRealName.setUserPhoneNo(userInfo.getPhone());
      userRealName.setName(userInfo.getName());
      userRealName.setCertType(1);
      userRealName.setCertId(userInfo.getIdCard());
      userRealName.setLinkPhoneNo(userInfo.getPhone());
      userRealName.setCertFrontUrl(userInfo.getIdCardFrontUrl());
      userRealName.setCertBackUrl(userInfo.getIdCardReverseUrl());
      userRealName.setState(0);
      if (!CollectionUtils.isEmpty(list)) {
        channelUserRealNameService.updateByPrimaryKey(userRealName);
      } else {
        channelUserRealNameService.insert(userRealName);
      }

    } catch (Exception e) {
      log.info("???????????????????????????ChannelRealName?????????", e);
    }
  }


  /**
   * @Description ????????????
   **/
  @Override
  public void callBack(String param) {

    String idCard = null;
    JSONObject jsonObject = JSON.parseObject(param);
    if (!ObjectUtils.isEmpty(jsonObject)) {
      if (!ObjectUtils.isEmpty(jsonObject.get("idCard"))) {
        idCard = jsonObject.getString("idCard");
      }
    }
    List<YuncrUserAuthentication> list = yuncrUserAuthenticationDao.findCallBackData(idCard);
    log.info("??????????????????"+list);
    if (CollectionUtils.isEmpty(list)) {
      return;
    }

    for (YuncrUserAuthentication userAuthentication : list) {
      log.info("????????????????????????"+userAuthentication.toString());
      String status = AUDIT_FAILED + "";
      if(AUDIT_SUCCESS == userAuthentication.getEnterpriseAudit().intValue() &&
          (IN_AUDIT == userAuthentication.getGovernmentAudit().intValue() ||
           AUDIT_FAILED == userAuthentication.getGovernmentAudit().intValue())){
        log.info("???????????????"+userAuthentication.toString());
        continue;
      }
      //????????????
      if (AUDIT_SUCCESS == userAuthentication.getEnterpriseAudit().intValue() &&
          AUDIT_SUCCESS == userAuthentication.getGovernmentAudit().intValue()) {
        status = AUDIT_SUCCESS + "";
      }
      JSONObject returnData = new JSONObject();
      returnData.put("status", status);
      returnData.put("apply_number", userAuthentication.getApplyNumber());
      if (AUDIT_FAILED == userAuthentication.getEnterpriseAudit().intValue()) {
        List<YuncrUserFailNode> failNodes = yuncrUserAuthenticationDao.findErrNodeList(userAuthentication.getId());
        log.info("??????????????????"+failNodes);
        List<String> fail_nodes = new ArrayList<>();
        if(!CollectionUtils.isEmpty(failNodes)) {
          for (YuncrUserFailNode yuncrUserFailNode : failNodes) {
            if (yuncrUserFailNode.getStatus() == 1) {
              log.info("??????????????????" + yuncrUserFailNode.getAuditNode());
              fail_nodes.add(yuncrUserFailNode.getAuditNode());
            }
          }
        }
        returnData.put("fail_nodes", fail_nodes);
      }
      signCallback(returnData, userAuthentication.getMerchantId());
      log.info("????????????????????????");
      RequestBody requestBody = RequestBody
          .create(MediaType.parse("application/json"), returnData.toJSONString());
      Request req = new Request.Builder()
          .url(userAuthentication.getCallbackAddress()).post(requestBody).build();
      log.info("Request??????????????????");
      try {
        log.info("???????????????????????????????????????????????????,?????????" + returnData.toJSONString());
        Response resp = OkHttpUtils.callHttp(req);
        String resultData = resp.body().string();
        log.info("???????????????" + resultData);
        /**
         * @Description ???????????? ?????????????????????
         **/
        if (Constant.CALL_BACK_SUCCESS.equals(resultData)) {
          log.info("???????????????????????????????????????" + resultData);
          userAuthentication.setCallbackNumber(1);
          userAuthentication.setCallbackStatus(Constant.CALLBACK_STATUS_SUCCESS);
          yuncrUserAuthenticationDao.updateCallBack(userAuthentication);
        } else {
          log.info("???????????????????????????????????????" + resultData);
          userAuthentication.setCallbackNumber(1);
          userAuthentication.setCallbackStatus(Constant.CALLBACK_STATUS_FAILED);
          yuncrUserAuthenticationDao.updateCallBack(userAuthentication);
        }

      } catch (Exception e) {
        userAuthentication.setCallbackNumber(1);
        userAuthentication.setCallbackStatus(Constant.CALLBACK_STATUS_FAILED);
        yuncrUserAuthenticationDao.updateCallBack(userAuthentication);
        log.info("????????????" + e);
      }

    }
  }

  private void signCallback(JSONObject returnData, String merchantId) {

    log.info("????????????----------------");
    MerchantAPIDockingConfig merchantAPIDockingConfig = apiDockingManager
        .getMerchantAPIDockingConfig(merchantId);
    if (merchantAPIDockingConfig == null) {
      log.error("????????????????????????????????????id " + merchantId);
      throw new RuntimeException("????????????????????????????????????id " + merchantId);
    }
    String signType = merchantAPIDockingConfig.getSignType();
    returnData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
    Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(returnData);
    log.info("????????????signType----------------"+signType);
    try {
      String sign = signWorkers.get(signType)
          .generateSign(toSignMap, merchantAPIDockingConfig.getSignGenerationKey());
      returnData.put(APIDefinitionConstants.CFN_SIGN, sign);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("????????????????????????????????????????????????.");
    }
    log.info("??????????????????----------------");
  }


  @Override
  public Map<String, Object> bindingBankCard(YuncrUserBank yuncrUserBank) {
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, RespCode.success);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

    YuncrUserAuthentication yuncrUserAuthentication = yuncrUserAuthenticationDao
        .findUserInfoById(yuncrUserBank.getAuthenticationId());
    if (yuncrUserAuthentication.getProcessNode() != ProcessNode.BUSINESS_LICENSE.getProcessNode() ||
        yuncrUserAuthentication.getEnterpriseAudit() != 1
        || yuncrUserAuthentication.getGovernmentAudit() != 1) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
      return result;
    }

    if (StringUtils.isEmpty(yuncrUserBank.getSubBankId())) {
      Map<String, Object> map = yuncrUserAuthenticationDao
          .getSubBankByBankId(yuncrUserBank.getBankId());
      yuncrUserBank.setSubBankId(map.get("subBankId").toString());
    }

    ResponseEntity<RespResult<Map<String, String>>> respResult;
    YuncrUserBankDTO yuncrUserBankDTO = this
        .getYuncrUserBankReq(yuncrUserBank, yuncrUserAuthentication);
    try {
      JSONObject jsonObject = (JSONObject) JSONObject.toJSON(yuncrUserBankDTO);
      log.info("??????????????????:{}", jsonObject);
      respResult = yuncrFeignClient.saveBankInfo(jsonObject);
    } catch (Exception e) {
      log.error("?????????????????????????????????:", e);
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "????????????????????????");
      return result;
    }

    if (!"00000".equals(respResult.getBody().getCode())) {
      log.info("???????????????:" + respResult.getBody().getMsg());
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "??????????????????" + respResult.getBody().getMsg());
      return result;
    }

    if (yuncrUserBank.getId() != null) {
      yuncrUserAuthenticationDao.updateBank(yuncrUserBank);
    } else {
      yuncrUserAuthenticationDao.insertBank(yuncrUserBank);
    }
    return result;
  }

  private YuncrUserBankDTO getYuncrUserBankReq(YuncrUserBank yuncrUserBank,
      YuncrUserAuthentication yuncrUserAuthentication) {
    YuncrUserBankDTO yuncrUserBankDTO = new YuncrUserBankDTO();
    yuncrUserBankDTO.setBankId(yuncrUserBank.getBankId());
    yuncrUserBankDTO.setSubBankId(yuncrUserBank.getSubBankId());
    yuncrUserBankDTO.setAccountNo(yuncrUserBank.getBankCardNumber());
    yuncrUserBankDTO.setAccountType("2");
    yuncrUserBankDTO.setFirmId(yuncrUserAuthentication.getFirmId());
    return yuncrUserBankDTO;
  }

  @Override
  public List<YuncrUserBankVO> listBankInfo(Integer id) {
    return yuncrUserAuthenticationDao.listBankInfo(id);
  }

  @Override
  public List<Map<String, Object>> getSubBankByBankName(String bankName) {
    return yuncrUserAuthenticationDao.getSubBankByBankName(bankName);
  }

  @Override
  public void deleteUserBankCard(String id) {
    yuncrUserAuthenticationDao.deleteUserBankCard(id);
  }

  @Override
  public List<Map<String, Object>> getAllBank() {
    return yuncrUserAuthenticationDao.getAllBank();
  }
}
