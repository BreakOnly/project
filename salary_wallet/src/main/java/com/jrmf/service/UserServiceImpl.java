package com.jrmf.service;

import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.*;
import com.jrmf.persistence.UserDao;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @version 创建时间：2017年8月17日 下午4:53:46
 * 类说明
 */
@Service("userSerivce")
public class UserServiceImpl implements UserSerivce {
    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    protected UserDao userDao;
    @Autowired
    private UserRelatedService userRelatedService;
    @Autowired
    private AgreementTemplateService agreementTemplateService;
    @Autowired
    private UsersAgreementService usersAgreementService;
    @Autowired
    private ChannelRelatedService channelRelatedService;

    @Override
    public User getUserByUserId(int id) {
        return userDao.getUserByUserNo(id);
    }

    @Override
    public User getUserByMobilePhone(String mobilePhone) {
        return userDao.getUserByMobilePhone(mobilePhone);
    }

    @Override
    public User getUserByUserNameAndCertId(String userName, String certId) {
        return userDao.getUserByUserNameAndCertId(userName, certId);
    }

    @Override
    public int addUser(User user) {
    	int addCount = 0;
    	try{
            user.setCheckTruth(0);
    		addCount = userDao.addUser(user);
    	}catch(Exception e){
          logger.error(e.getMessage(),e);
    	}
        return addCount;
    }


    @Override
    public void deleteByIds(String ids) {
        userDao.deleteByIds(ids);
    }


    @Override
    public int getUsersCountByParam(Map<String, Object> params) {
        return userDao.getUsersCountByParam(params);
    }

    @Override
    public List<User> getCompanyByMerchantId(String merchantId) {
        return userDao.getCompanyByMerchantId(merchantId);
    }

    @Override
    public List<User> getUserRelatedByParam(Map<String, Object> params) {
        return userDao.getUserRelatedByParam(params);
    }

    @Override
    public int getUserRelatedCountByParam(Map<String, Object> params) {
        return userDao.getUserRelatedCountByParam(params);
    }

    @Override
    public void addWechartId(String id, String wechartId) {
        userDao.addWechartId(id, wechartId);

    }

    @Override
    public User selectUserByWechartId(String wechartId) {
        return userDao.selectUserByWechartId(wechartId);
    }

    @Override
    public User getUsersCountByCard(Map<String, Object> params) {
        return userDao.getUsersCountByCard(params);
    }

    @Override
    public void deleteByBatcheId(String batcheId, String originalId) {
        userDao.deleteByBatcheId(batcheId, originalId);
    }

    @Override
    public int addUserBatch(UserBatch user) {
        return userDao.addUserBatch(user);
    }

    @Override
    public UserBatch getUserBatchByBatchId(String batchId) {
        return userDao.getUserBatchByBatchId(batchId);
    }

    @Override
    public List<UserBatch> getUserBatchByParam(Map<String, Object> params) {
        return userDao.getUserBatchByParam(params);
    }

    @Override
    public void updateUserBatch(String batchId, int passNum, int batchNum, int errorNum) {
        UserBatch userBatch = userDao.getUserBatchByBatchId(batchId);
        userBatch.setPassNum(passNum);
        userBatch.setBatchNum(batchNum);
        userBatch.setErrorNum(errorNum);
        userDao.updateUserBatch(userBatch);
    }

    @Override
    public void deleteUserBatch(String id) {
        userDao.deleteUserBatch(id);
    }

    @Override
    public List<User> getUserByParam(Map<String, Object> param) {
        return userDao.getUserByParam(param);
    }

    @Override
    public List<User> getUserForMerchantByParams(Map<String, Object> paramMap) {
        return userDao.getUserForMerchantByParams(paramMap);
    }

    @Override
    public List<User> getUserForPlatform(Map<String, Object> paramMap){
        return userDao.getUserForPlatform(paramMap);
    }


    @Override
    public Map<String, Object> addUserBatchByExcel(Map<String, Object> paramMap) {
        HashMap<String, Object> resultMap = new HashMap<>(5);
        List<User> users = (List<User>) paramMap.get("users");
        Object remarkObject = paramMap.get("remark");
        String remark = "";
        if(remarkObject != null){
            remark = (String) paramMap.get("remark");
        }

        String customkey = (String) paramMap.get("customkey");
        ArrayList<Map<String, Object>> errorList = new ArrayList<>();
        int success = 0;
        if (!users.isEmpty()) {
            for (int i = 0; i < users.size(); i++) {
                Map<String, Object> checkResult = checkInfo(i + 1, users.get(i), customkey,remark);
                if (checkResult != null) {
                    errorList.add(checkResult);
                } else {
                    success++;
                }
            }
        }
        resultMap.put("success", success);
        resultMap.put("errorList", errorList);
        resultMap.put("hasError", !errorList.isEmpty());
        return resultMap;
    }

    /**
     * 删除用户
     */
    @Override
    public boolean deleteUnSignUser(Map<String, Object> paramMap) {
        String originalId = (String) paramMap.get("customkey");
        List<String> ids = (List<String>) paramMap.get("ids");
        for (String id : ids) {
            User user = userDao.getUserByUserNo(Integer.parseInt(id));
            if (user == null) {
                continue;
            }
            paramMap.clear();
            paramMap.put("userId", id);
            paramMap.put("originalId", originalId);
            List<UsersAgreement> usersAgreements = usersAgreementService.getUsersAgreementsByParams(paramMap);

            //删除用户商户服务公司关系
            List<UserRelated> userRelatedList = userRelatedService.getRelatedByParam(paramMap);
            for (UserRelated userRelated : userRelatedList) {
                userRelatedService.deleteByOriginalId(userRelated.getUserId() + "", userRelated.getOriginalId());
            }
            //删除协议
            deleteUserAgreement(usersAgreements);
        }

        return true;
    }

    private void deleteUserAgreement(List<UsersAgreement> usersAgreements) {
        for (UsersAgreement usersAgreement : usersAgreements) {
            int signStatus = usersAgreement.getSignStatus();
            if(UsersAgreementSignType.SIGN_SUCCESS.getCode() == signStatus){
                usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FORBIDDEN.getCode());
                usersAgreement.setSignStatusDes(UsersAgreementSignType.SIGN_FORBIDDEN.getDesc());
                usersAgreementService.updateUsersAgreement(usersAgreement);
            }else{
                usersAgreementService.deleteUsersAgreement(usersAgreement);
            }
        }
    }

    /**
     * 检查输入格式是否有问题
     */
    private Map<String, Object> checkInfo(int num, User importUser, String customkey, String remark) {
        HashMap<String, Object> map = new HashMap<>(2);
        String userName = importUser.getUserName();
        if (StringUtil.isEmpty(userName)) {
            map.put("errorInfo", "第" + num + "条记录，姓名为空");
            return map;
        }
        String documentType = importUser.getDocumentType();
        CertType certType = CertType.descOfDefault(documentType);
        if (certType == null) {
            map.put("errorInfo", "第" + num + "条记录，证件类型错误");
            return map;
        }
        String certId = importUser.getCertId();
        if(certType.getCode() == CertType.ID_CARD.getCode()){
            if (!StringUtil.checkCertId(certId)) {
                logger.error("身份证号码错误：" + certId);
                map.put("errorInfo", "第" + num + "条记录，身份证号码校验失败");
                return map;
            }
        }
        String userNo = importUser.getUserNo();
        if (StringUtil.isEmpty(userNo)) {
            logger.error("商户用户编号为空：" + certId);
            map.put("errorInfo", "第" + num + "条记录，商户用户编号为空");
            return map;
        }
        //excel 校验完成，开始信息插入，总共需要6个信息  userName,documentType,certId,userNo,mobileNo,originalId。merchantId 现在默认keqijinyun
        Map<String, Object> resultMap = addUserInfo(userName, certType.getCode(), certId, userNo, importUser.getMobilePhone(), customkey, null,remark);
        int state = (Integer)resultMap.get("state");
        if(RespCode.success == state){
            logger.info("添加完成");
            return null;
        }else{
            String respmsg = (String)resultMap.get("respmsg");
            map.put("errorInfo", "第" + num + "条记录，"+respmsg);
            return map;
        }

    }

    /**
     * 添加用户，商户-服务公司-用户，签约明细
     * @param userName 姓名
     * @param documentType 证件类型
     * @param certId 证件号
     * @param userNo 商户用户编号（非必填）
     * @param mobileNo 手机号（非必填）
     * @param originalId 商户id
     * @param merchantId 平台id（非必填，默认keqijinyun）
     * @param remark 签约明细备注
     * @return state = 1   成功
     */
    @Override
    public Map<String, Object> addUserInfo(String userName, int documentType, String certId, String userNo, String mobileNo, String originalId, String merchantId, String remark){

        Map<String, Object> paramMap = new HashMap<>(12);
        paramMap.put("userName", userName);
        paramMap.put("certId", certId);
        List<User> users = userDao.getUserByParam(paramMap);
        int userId ;
        if(users.isEmpty()){
            //用户信息不存在，插入user表
            User user = new User();
            user.setMobilePhone(mobileNo);
            user.setUserType(11);
            user.setUserNo(userNo);
            user.setCertId(certId);
            user.setUserName(userName);
            user.setDocumentType(documentType+"");
            user.setCheckTruth(0);
            try {
                userDao.addUser(user);
                userId = user.getId();
            }catch (Exception e){
                logger.error("添加异常{}",e.getMessage());
                users = userDao.getUserByParam(paramMap);
                userId = users.get(0).getId();
            }
        }else{
            User user = users.get(0);
            userId = user.getId();
            if (!StringUtil.isEmpty(mobileNo) && StringUtil.isEmpty(user.getMobilePhone())){
                user.setMobilePhone(mobileNo);
                userDao.updateMobileNoByUserId(user);
            }
        }
        //插入商户-服务公司-用户关系表
        //查询userNo是否重复
        if(!StringUtil.isEmpty(userNo)){
            paramMap.put("userNo", userNo);
            paramMap.put("originalId", originalId);
            List<UserRelated> userRelatedList = userRelatedService.getRelatedByParam(paramMap);
            if (!userRelatedList.isEmpty()) {
                for (UserRelated userRelated : userRelatedList) {
                    int userId1 = userRelated.getUserId();
                    if(userId != userId1){
                        int code = RespCode.USERNO_ALREADY_EXIST;
                        logger.error("用户信息编号重复:{}",paramMap);
                        return returnMap(code,RespCode.codeMaps.get(code)+"。"+userNo,userId);
                    }
                }
            }
        }
        List<ChannelRelated> channelRelatedList = channelRelatedService.getRelatedByOriginalId(null,originalId);
        if(channelRelatedList.isEmpty()){
            int code = RespCode.CHANNEL_RELATED_NOT_FOUND;
            return returnMap(code,RespCode.codeMaps.get(code),userId);
        }
        for (ChannelRelated channelRelated : channelRelatedList) {
            userRelatedService.addUserRelated(channelRelated,userNo,userId,mobileNo);
        }

        //插入用户协议表
        paramMap.clear();
        paramMap.put("originalId", originalId);
        List<AgreementTemplate> agreementTemplateList = agreementTemplateService.getAgreementTemplateByParam(paramMap);
        for (AgreementTemplate agreementTemplate : agreementTemplateList) {
            usersAgreementService.addUserAgreement(agreementTemplate,userId,originalId,userName,certId,documentType,remark,null);
        }
        int code = RespCode.success;
        return returnMap(code,RespCode.codeMaps.get(code),userId);
    }

    @Override
    public void updateUserRelated(String userId, String phoneNo, String originalId) {
        List<ChannelRelated> channelRelatedList = channelRelatedService.getRelatedByOriginalId(null,originalId);
        if(channelRelatedList.isEmpty()){
            return;
        }
        for (ChannelRelated channelRelated : channelRelatedList) {
            if (!StringUtil.isEmpty(userId)){
                userRelatedService.addUserRelated(channelRelated,"",Integer.parseInt(userId),phoneNo);
            }
        }
    }

    /**
     * 新增服务公司 新增user表
     */
    @Override
    public void insertUserInfo(User user) {
        userDao.insertUserInfo(user);
    }

    /**
     * 修改用户信息 修改user表
     */
    @Override
    public void updateUserInfo(User user) {
        userDao.updateUserInfo(user);
    }


    @Override
    public int getUserForMerchantByParamsCount(Map<String, Object> paramMap) {
        return userDao.getUserForMerchantByParamsCount(paramMap);
    }

    private Map<String, Object> returnMap(int state,String message,int userId) {
        Map<String, Object> model = new HashMap<>(4);
        model.put(RespCode.RESP_STAT, state);
        model.put(RespCode.RESP_MSG, message);
        model.put("userId", userId);
        logger.info("路路的用户服务返回的model{}",model);
        return model;
    }
}
