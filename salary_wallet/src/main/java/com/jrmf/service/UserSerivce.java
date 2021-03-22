package com.jrmf.service;

import com.jrmf.domain.User;
import com.jrmf.domain.UserBatch;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
* @author 种路路
* @version 创建时间：2017年8月17日 下午4:25:39
* 类说明 com.jrmf.service.UserSerivce
*/
@Service
public interface UserSerivce {

	int addUser(User user);

	User getUsersCountByCard(Map<String, Object> params);

	User getUserByUserId(int id);

	User getUserByMobilePhone(String mobilePhone);

	User getUserByUserNameAndCertId(String userName, String certId);

	int getUsersCountByParam(Map<String, Object> params);

	List<User> getCompanyByMerchantId(String merchantId);

    void deleteByIds(String ids);

	List<User> getUserRelatedByParam(Map<String, Object> params);

	int getUserRelatedCountByParam(Map<String, Object> params);

	void addWechartId(String id, String wechartId);

    User selectUserByWechartId(String wechartId);

	void deleteByBatcheId(String batcheId,String originalId);

	int addUserBatch(UserBatch user);

	UserBatch getUserBatchByBatchId(String batchId);

	void updateUserBatch(String batchId,int passNum,int batchNum, int errorNum);

	List<UserBatch> getUserBatchByParam(Map<String, Object> params);

	void deleteUserBatch(String id);

	List<User> getUserByParam(Map<String, Object> param);

    List<User> getUserForMerchantByParams(Map<String, Object> paramMap);


	/**
	 * 获取平台用户信息
	 * @param paramMap
	 * @return
	 */
	List<User> getUserForPlatform(Map<String, Object> paramMap);


    Map<String,Object> addUserBatchByExcel(Map<String, Object> paramMap) throws IOException;

    /**
     * 删除用户
     */
    boolean deleteUnSignUser(Map<String, Object> paramMap);

    /**
     * 添加用户，商户-服务公司-用户，签约明细
     * @param userName 姓名
     * @param documentType 证件类型
     * @param certId 证件号
     * @param userNo 商户用户编号（非必填）
     * @param mobileNo 手机号（非必填）
     * @param originalId 商户id
     * @param merchantId 平台id（非必填，默认keqijinyun）
     * @param remark
     * @return state = 1   成功
     */
    Map<String, Object> addUserInfo(String userName, int documentType, String certId, String userNo, String mobileNo, String originalId, String merchantId, String remark);

	/**
	 * 更新userrelated表，用户信息
	 * @param phoneNo
	 * @param originalId
	 */
	void updateUserRelated(String userId, String phoneNo, String originalId);

	/**
	 * 新增服务公司 新增user表
	 * @param user
	 */
	void insertUserInfo(User user);

	/**
	 * 修改用户信息 修改user表
	 * @param user
	 */
	void updateUserInfo(User user);

    int getUserForMerchantByParamsCount(Map<String, Object> paramMap);
}
