package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.ChannelRelated;
import org.springframework.stereotype.Service;

import com.jrmf.domain.UserRelated;

/** 
* @author zhangzehui
* @version 创建时间：2018年04月25日
*/
@Service
public interface UserRelatedService {

	void createUserRelated(UserRelated userRelated);
	
	void updateUserRelated(UserRelated userRelated);
	
	List<UserRelated> getRelatedByParam(Map<String, Object> param);
	
	UserRelated getRelatedByUserId(String userId,String originalId);
	
	UserRelated getRelatedByUserNo(String userNo,String originalId);
	
	UserRelated getRelatedById(String id);
	
	void updateUserRelatedStatus(String userId,String originalId,int status);
	
	void deleteByOriginalId(String userId,String originalId);
	
	void deleteByBatchId(String batcheId,String originalId);

    /**
     * 根据商户id,用户id  去修改商户用户id
     * @param userRelated
     */
    void updateUserRelatedUserNo(UserRelated userRelated);

    /**
     * 添加关系，如果存在就更新userNo
     * @param channelRelated
     * @param userNo
     * @param userId
     */
    void addUserRelated(ChannelRelated channelRelated, String userNo, int userId,String mobileNo);

	UserRelated selectIsWhiteList(String customKey,String companyId,String certId);
}
 