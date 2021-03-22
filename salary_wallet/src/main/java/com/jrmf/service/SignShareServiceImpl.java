package com.jrmf.service;

import com.jrmf.controller.constant.SignShareStatus;
import com.jrmf.controller.constant.SignShareType;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.domain.*;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.SignShareDao;
import com.jrmf.persistence.UserRelatedDao;
import com.jrmf.persistence.UsersAgreementDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import java.util.*;

/**
 * @Title: SignShareServiceImpl
 * @Description: 共享签约配置实现
 * @create 2020/4/27 10:10
 */
@Service
public class SignShareServiceImpl implements SignShareService {

    private static final Logger logger = LoggerFactory.getLogger(SignShareServiceImpl.class);

    @Autowired
    private SignShareDao signShareDao;
    @Autowired
    private ChannelCustomDao channelCustomDao;
    @Autowired
    private UserSerivce userSerivce;

    @Autowired
    private UsersAgreementService usersAgreementService;

    @Autowired
    private JmsTemplate providerJmsTemplate;
    @Autowired
    private Destination signShareDestination;
    @Autowired
    private AgreementTemplateService agreementTemplateService;
    @Autowired
    private UtilCacheManager utilCacheManager;
    @Autowired
    private UserRelatedDao userRelatedDao;
    @Autowired
    private UsersAgreementDao usersAgreementDao;


    @Override
    public List<SignShare> getSignShareByParam(Map<String, Object> param) {
        return signShareDao.getSignShareByParam(param);
    }

    @Override
    public Map<String, Object> configSignShare(SignShare signShare) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        if (signShare.getId() == 0) {
            //新增 判断限制组名称是否为空（前端校验类型为一对多才会显示限制组名称）
            if (!StringUtil.isEmpty(signShare.getLimitName()) && signShare.getType() == SignShareType.CUSTOMONETOMANY.getCode()) {
                signShare.setLimitGroupId(this.getUUID());
            }
            // 限制名称不存在，则直接新增
            signShareDao.insertSignShare(signShare);
            return result;
        }

        // 当前数据
        SignShare share = signShareDao.getSignShareById(signShare.getId() + "");

        if (StringUtil.isEmpty(signShare.getLimitName()) || signShare.getType() != SignShareType.CUSTOMONETOMANY.getCode()) {
            // 查询该条记录的limitGroupId 不为空则删除limit表的限制组
            if (!StringUtil.isEmpty(share.getLimitGroupId())) {
                List<SignShare> signShareLimitByLimitGroupId = signShareDao.getSignShareLimitByLimitGroupId(share.getLimitGroupId());
                if (!signShareLimitByLimitGroupId.isEmpty() && signShareLimitByLimitGroupId.size() > 0) {
                    result.put(RespCode.RESP_STAT, RespCode.error101);
                    result.put(RespCode.RESP_MSG, "限制组内有配置商户，无法修改");
                    return result;
                }
            }
            signShare.setLimitGroupId("");
            signShare.setLimitName("");
            signShareDao.updateSignShare(signShare);
            return result;
        }

        // 如果商户类型修改为一对多类型或者限制组名称修改，需要新的UUID
        SignShare s1 = new SignShare();
        s1.setOldLimitGroupId(signShare.getLimitGroupId());
        if (!share.getCustomkey().equals(signShare.getCustomkey())) {
            s1.setCustomkey(signShare.getCustomkey());
        }

        if (!signShare.getLimitName().equals(share.getLimitName())) {
            String uuid = this.getUUID();
            s1.setLimitGroupId(uuid);
            signShare.setLimitGroupId(uuid);
        }
        signShareDao.updateSignShareLimitByGroupId(s1);
        signShareDao.updateSignShare(signShare);
        return result;
    }

    @Override
    public Map<String, Object> deleteSignShare(String id) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        SignShare signShare = signShareDao.getSignShareById(id);
        if (signShare != null) {
            if (!StringUtil.isEmpty(signShare.getLimitGroupId())) {
                List<SignShare> list = signShareDao.getSignShareLimitByLimitGroupId(signShare.getLimitGroupId() + "");
                if (!list.isEmpty()) {
                    result.put(RespCode.RESP_STAT, RespCode.error101);
                    result.put(RespCode.RESP_MSG, "已绑定限制组，无法删除");
                    return result;
                } else {
                    signShareDao.deleteSignShare(id);
                }
            } else {
                signShareDao.deleteSignShare(id);
            }
        }

        return result;
    }

    @Override
    public List<SignShare> getSignShareLimitByLimitGroupId(String limitGroupId) {
        return signShareDao.getSignShareLimitByLimitGroupId(limitGroupId);
    }

    @Override
    public Map<String, Object> updateSignShareLimitStatus(String id, String status) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        signShareDao.updateSignShareLimitStatus(id, status);
        return result;
    }

    @Override
    public Map<String, Object> deleteSignShareLimit(String id) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        signShareDao.deleteSignShareLimit(id);
        return result;
    }

    @Override
    public Map<String, Object> insertSignShareLimitNameInfo(SignShare signShare) {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        SignShare share = signShareDao.getSignShareByLimitName(signShare.getLimitName(), 0);
        SignShare s = signShareDao.getSignShareLimitByShareCustomkey(signShare.getShareCustomkey(), share.getLimitGroupId());
        if (s != null) {
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "商户已配置");
            return result;
        }
        signShare.setLimitGroupId(share.getLimitGroupId());
        signShare.setCustomkey(share.getCustomkey());
        signShareDao.insertSignShareLimitInfo(signShare);
        return result;
    }

    @Override
    public List<SignElementRule> getSignElementRule(Map<String, Object> param) {
        return signShareDao.getSignElementRule(param);
    }

    @Override
    public Map<String, Object> configSignElementRule(SignElementRule signElementRule) {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        SignElementRule rule = signShareDao.getSignElementRuleByCompanyIdAndId(signElementRule.getCompanyId(), signElementRule.getId());
        if (rule != null) {
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "请勿重复配置服务公司");
            return result;
        }

        if (signElementRule.getId() != 0) {
            signShareDao.updateSignElementRule(signElementRule);
        } else {
            signShareDao.insertSignElementRule(signElementRule);
        }
        return result;
    }

    @Override
    public Map<String, Object> deleteSignElementRule(String id) {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        signShareDao.deleteSignElementRule(id);
        return result;
    }

    @Override
    public SignShare getSignShareByLimitName(String limitName, int id) {
        return signShareDao.getSignShareByLimitName(limitName, id);
    }

    @Override
    public SignShare getSignShareByLimitName(Integer limitName, int id) {
        return null;
    }

    @Override
    public List<SignShare> getSignShareByCustomKey(String customKey, String companyId) {

        List<SignShare> list = new ArrayList<>();
        for (SignShareType signShareType : SignShareType.values()) {
            //服务公司共享暂不开发
//            customKey = signShareType.getCode() == SignShareType.COMPANYTYPE.getCode() ? companyId : customKey;
            SignShare signShare = signShareDao.getSignShareByCustomKey(signShareType.getCode(), customKey);
            if (null != signShare) {
                if (SignShareType.CUSTOMONETOMANY.getCode() == signShareType.getCode()) {
                    String customKeys = signShareDao.getSignShareLimitCustomKeysByLimitGroupId(signShare.getLimitGroupId());
                    signShare.setGroupCustomKeys(customKeys);
                }
                list.add(signShare);
            }
        }

        logger.info("获取共享签约规则 customKey:{} companyId:{} result:{}", customKey, companyId, list.toString());

        return list;
    }

    @Override
    public UsersAgreement shareSignAgreementByUser(List<SignShare> list, SignElementRule signElementRule, AgreementTemplate template, int signSubmitType, String userName, String certId) {

        User user = userSerivce.getUserByUserNameAndCertId(userName, certId);

        logger.info("开始共享签约 userName:{} certId:{} user:{}", userName, certId, user);

        //没有创建用户不进行签约共享
        if (user == null) {
            return null;
        }

        ShareSignRequest shareSignRequest = new ShareSignRequest();
        shareSignRequest.setUser(user);
        shareSignRequest.setAgreementTemplate(template);
        shareSignRequest.setSignSubmitType(signSubmitType);

        for (SignShare signShare : list) {
            UsersAgreement usersAgreement = null;

            switch (Objects.requireNonNull(SignShareType.codeOf(signShare.getType()))) {
//                case COMPANYTYPE:
//                    UsersAgreement usersAgreement = usersAgreementService.getCompanySignShare(template.getCompanyId(), userId);
//                    //服务公司共享，如果用户在服务公司下没有签约成功记录直接路由下一个规则
//                    if (null == usersAgreement) {
//                        canNext = true;
//                    }
//                    usersAgreementService.shareSuccessSignUserAgreement(template, user, usersAgreement);
//                    break;
                case CUSTOMTYPE:
                    usersAgreement = usersAgreementService.getCustomSignShare(signShare.getCustomkey(), user.getId(), signElementRule.getSignLevel(), signElementRule.getPapersRequire());
                    logger.info("商户共享获取成功共享签约记录 userName:{} certId:{} usersAgreement:{}", userName, certId, usersAgreement);
                    break;
                case CUSTOMONETOMANY:
                    usersAgreement = usersAgreementService.getCustomSignShare(signShare.getGroupCustomKeys(), user.getId(), signElementRule.getSignLevel(), signElementRule.getPapersRequire());
                    logger.info("商户一对多共享获取成功共享签约记录 userName:{} certId:{} usersAgreement:{}", userName, certId, usersAgreement);
                    break;

            }

            if (null != usersAgreement) {
                shareSignRequest.setUsersAgreement(usersAgreement);
                providerJmsTemplate.send(signShareDestination, session -> {
//                            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, Long.parseLong(time));
                    return session.createObjectMessage(shareSignRequest);
                });
                return usersAgreement;
            }
        }

        return null;
    }

    @Override
    public List<SignShare> getSignShareByCustomKey2(String customkey, int id) {
        return signShareDao.getSignShareByCustomKey2(customkey, id);
    }

    @Override
    public SignElementRule getSignElementRuleByCompanyId(String companyId) {
        return signShareDao.getSignElementRuleByCompanyId(companyId);
    }

    @Override
    public int checkUsersAgreement(List<SignShare> signShareList, SignElementRule signElementRule, int signSubmitType, String customKey, String companyId, String certId, String userName) {

        logger.info("批次导入校验用户是否签约开始 userName:{} certId:{} ", userName, certId);
        try {

            AgreementTemplate template = agreementTemplateService.getAgreementPaymentTemplate(customKey, companyId);


            //存在配置先签约后下发的签约模板
            if (template != null) {

                HashMap paramMap = new HashMap();
                paramMap.put("certId", certId);
                paramMap.put("signStatus", "5");
                paramMap.put("userName", userName);
                paramMap.put("agreementTemplateId", template.getId());
                //是否成功签约
                int userAgreeCount = agreementTemplateService.getUserAgreementCountByParam(paramMap);

                logger.info("校验用户是否签约 userName:{} certId:{} userAgreeCount:{}", userName, certId, userAgreeCount);

                if (userAgreeCount > 0) {
                    return SignShareStatus.SIGN_SUCCESS.getCode();
                } else if (signElementRule != null && signShareList != null && signShareList.size() > 0) { //校验服务公司签约要素、商户共享签约配置是否存在
                    UsersAgreement usersAgreement = this.shareSignAgreementByUser(signShareList, signElementRule, template, signSubmitType, userName, certId);
                    if (null != usersAgreement) {
                        //查询是否有可以共享签约的签约记录,存在返回true跳过签约校验
                        return SignShareStatus.SIGN_SHARE_SUCCESS.getCode();
                    }
                } else {
                    return SignShareStatus.SIGN_SHARE_FAIL.getCode();
                }
            } else {
                return SignShareStatus.NO_SIGN_PAY.getCode();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


        return SignShareStatus.SIGN_FAIL.getCode();
    }


    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


    /**
     * 获取需要共享签约的签约数量
     *
     * @param originalId
     * @return
     */
    public int getNeedShareAgreementNum(String originalId) {
        // 查该商户用户总数
        int userNum = userRelatedDao.countByOriginalId(originalId);
        logger.info("[{}]商户总用户数：{}",originalId,userNum);
        //查需要共享签约的模板
        List<AgreementTemplate> templates= getNeedSignShareTemplateByOriginalId(originalId);
        int templateNum=templates==null ? 0 : templates.size();
        logger.info("[{}]商户下可共享签约的模板数：{}",originalId,templateNum);
        if (userNum * templateNum ==0 ) {
            return 0;
        }

        String agreementTemplateIds="";
       for(AgreementTemplate template:templates){
            agreementTemplateIds+=","+template.getId();
       }
        //查已迁移了多少记录
        int signedCount = usersAgreementDao.countByTemplateId(agreementTemplateIds.substring(1));
        logger.info("[{}]已签约总数为：{}",originalId,signedCount);
        return userNum * templateNum - signedCount;
    }

    /**
     * 后台签约共享
     *
     * @param originalId
     * @param shareType
     */
    public void triggerShareAgreement(String originalId, int shareType) {
        logger.info("开始平台发起的共享签约，originalId:{},shareType:{}",originalId,shareType);
        //1、查先签约后支付的签约模板
        List<AgreementTemplate> agreementTemplateList=getNeedSignShareTemplateByOriginalId(originalId);
        if (agreementTemplateList == null || agreementTemplateList.isEmpty()) {
            logger.info("先签约后支付迁移模板不存在");
            return;
        }
        logger.info("需要共享签约的模板: {}"+agreementTemplateList);
        //2、查签约共享配置
        SignShare signShare = signShareDao.getSignShareByCustomKey(shareType, originalId);
        if (signShare == null) {
            String errorMsg = "未配置共享签约范围，originalId：" + originalId;
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (SignShareType.CUSTOMONETOMANY.getCode() == shareType) {
            String customKeys = signShareDao.getSignShareLimitCustomKeysByLimitGroupId(signShare.getLimitGroupId());
            signShare.setGroupCustomKeys(customKeys);
        }
        List<SignShare> signShareList = Arrays.asList(signShare);

        // 3、查该商户下先签约后支付的所有用户
        List<Integer> userList = userRelatedDao.getByOriginalId(originalId);

        if (userList == null || userList.isEmpty()) {
            logger.info("未发现先签约后支付用户");
            return;
        }
        //4、遍历每个用户，查是否还需要共享签约
        for (Integer userId : userList) {
            Map queryParam = new HashMap();
            queryParam.put("userId", userId);

            for (AgreementTemplate template : agreementTemplateList) {
                queryParam.put("agreementTemplateId", template.getId());
                queryParam.put("signStatus",5);//签约成功
                int count = usersAgreementService.getUsersAgreementsCountByParams(queryParam);
                if (count <= 0) {
                    User user = userSerivce.getUserByUserId(userId);
                    if(user==null){
                        logger.error("未找到用户"+userId);
                        continue;
                    }
                    SignElementRule signElementRule = getSignElementRuleByCompanyId(template.getCompanyId());
                    logger.info("开始对用户"+userId+"执行共享签约（平台）");
                    //5、执行共享签约
                    shareSignAgreementByUser(signShareList, signElementRule, template, SignSubmitType.PLATFORM.getCode(), user.getUserName(), user.getCertId());
                }
            }

        }
        logger.info("平台发起共享签约完成 originalId:{} shareType:{}",originalId,shareType);
    }

    /**
     *  查先签约后支付的签约模板
     * @param originalId
     * @return
     */
    private List<AgreementTemplate> getNeedSignShareTemplateByOriginalId(String originalId){

        Map param = new HashMap();
        param.put("originalId", originalId);
        param.put("agreementPayment", "1");

        List<AgreementTemplate> agreementTemplateList = agreementTemplateService.getAgreementTemplateByParam(param);

        List<AgreementTemplate> result=new ArrayList<>();
        if(agreementTemplateList!=null){
            for(AgreementTemplate template:agreementTemplateList){
                SignElementRule signElementRule= getSignElementRuleByCompanyId(template.getCompanyId());
                if(signElementRule!=null){
                    result.add(template);
                }
            }
        }
        return result;
    }

    @Override
    public List<ChannelCustom>  getMerhcantListByUserId(int userid){


        List<String> userRelatedList= userRelatedDao.getMerchantListByUserId(userid);
        if(userRelatedList==null){
            return Collections.emptyList();
        }

        List<ChannelCustom> channelCustomList=new ArrayList<>();
        for(String originalId:userRelatedList){
            ChannelCustom channelCustom= channelCustomDao.getChannelCustomById(originalId);
            if(channelCustom!=null){
                channelCustomList.add(channelCustom);
            }
        }
        return channelCustomList;
    }

    /**
     * 根据共享类型查询共享签约商户
     * @param type
     * @return
     */
    @Override
    public List<SignShare> getSignShareByType(String type) {
        return signShareDao.getSignShareByType(type);
    }
}
