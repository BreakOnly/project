package com.jrmf.service;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.UserRelated;
import com.jrmf.persistence.UserRelatedDao;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
* @author zhangzehui
* @version 创建时间：2017年12月16日
* 
*/
@Service("userRelatedService")
public class UserRelatedServiceImpl implements UserRelatedService {

    private static Logger logger = LoggerFactory.getLogger(UserRelatedServiceImpl.class);

    @Autowired
	private UserRelatedDao userRelatedDao;

	@Override
	public void createUserRelated(UserRelated userRelated) {
    	try{
    		userRelatedDao.createUserRelated(userRelated);
    	}catch(Exception e){
    	    logger.error(e.getMessage());
    	}
	}

	@Override
	public void updateUserRelated(UserRelated userRelated) {
		userRelatedDao.updateUserRelated(userRelated);
	}

	@Override
	public List<UserRelated> getRelatedByParam(Map<String, Object> param) {
		return userRelatedDao.getRelatedByParam(param);
	}

	@Override
	public UserRelated getRelatedByUserId(String userId,String originalId) {
		return userRelatedDao.getRelatedByUserId(userId,originalId);
	}

	@Override
	public void updateUserRelatedStatus(String userId, String originalId,
			int status) {
		userRelatedDao.updateUserRelatedStatus(userId, originalId, status);
	}

	@Override
	public void deleteByOriginalId(String userId, String originalId) {
		userRelatedDao.deleteByOriginalId(userId, originalId);
	}

	@Override
	public UserRelated getRelatedByUserNo(String userNo, String originalId) {
		return userRelatedDao.getRelatedByUserNo(userNo, originalId);
	}

	@Override
	public UserRelated getRelatedById(String id) {
		return userRelatedDao.getRelatedById(id);
	}

	@Override
	public void deleteByBatchId(String batcheId, String originalId) {
		userRelatedDao.deleteRelatedByBatchId(batcheId, originalId);
	}

    /**
     * 根据商户id,用户id  去修改商户用户id
     *
     * @param userRelated
     */
    @Override
    public void updateUserRelatedUserNo(UserRelated userRelated) {
        userRelatedDao.updateUserRelatedUserNo( userRelated);
    }

    /**
     * 添加关系，如果存在就更新userNo
     *
     * @param channelRelated
     * @param userNo
     * @param userId
     */
    @Override
    public void addUserRelated(ChannelRelated channelRelated, String userNo, int userId,String mobileNo) {
		logger.info("添加用户商户服务公司关系：userId {}，userNo {},mobileNo {}", userId,userNo,mobileNo);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", userId);
        hashMap.put("originalId", channelRelated.getOriginalId());
        hashMap.put("companyId", channelRelated.getCompanyId());
        List<UserRelated> userRelatedList = userRelatedDao.getRelatedByParam(hashMap);
        if(userRelatedList.isEmpty()){
            UserRelated userRelated = new UserRelated();
            userRelated.setOriginalId(channelRelated.getOriginalId());
            userRelated.setUserId(userId);
            userRelated.setUserNo(userNo);
            userRelated.setCompanyId(channelRelated.getCompanyId());
            userRelated.setCreateTime(DateUtils.getNowDate());
			userRelated.setMobileNo(mobileNo);
            try {
                userRelatedDao.createUserRelated(userRelated);
            }catch (Exception e){
                logger.error("添加异常",e.getMessage());
            }
        }else{
            UserRelated userRelated = userRelatedList.get(0);
            if (StringUtil.isEmpty(userNo) && StringUtil.isEmpty(mobileNo)){
            	return;
			}
            if (!StringUtil.isEmpty(userNo) && !userNo.equals(userRelated.getUserNo())){
            	userRelated.setUserNo(userNo);
			}
            if (!StringUtil.isEmpty(mobileNo) && !mobileNo.equals(userRelated.getMobileNo())){
            	userRelated.setMobileNo(mobileNo);
			}
			userRelatedDao.updateUserRelated(userRelated);
        }
    }

	@Override
	public UserRelated selectIsWhiteList(String customKey, String companyId, String certId) {
		return userRelatedDao.selectIsWhiteList(customKey, companyId, certId);
	}

}
 