package com.jrmf.persistence;

import com.jrmf.domain.Partner;
import com.jrmf.domain.PartnerShip;
import com.jrmf.domain.User;
import com.jrmf.domain.UserBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 种路路
* @version 创建时间：2017年8月17日 下午7:00:05
* 类说明
*/
@Mapper
public interface UserDao {
	int addUser(User user);

	int addUserBatch(UserBatch user);

	UserBatch getUserBatchByBatchId(@Param("batchId") String batchId);

	User getUserByUserNo(@Param("id") int id);

	User getUserByMerchant(@Param("userNo")String userNo, @Param("merchantId")String merchantId);

	User getUserByMobilePhone(@Param("mobilePhone") String mobilePhone);

	User getUserByUserNameAndCertId(String userName, String certId);

	User getActiveUser(@Param("userNo") String userNo, @Param("merchantId") String merchantId);

	String getTokenByUserId(@Param("id") int id);

	User getMerchant(@Param("userNo") String userNo, @Param("merchantId") String merchantId);

	User getUserByUserNoAndMerchant(@Param("userNo") String userNo, @Param("merchantId") String merchantId);

	String checkToken(@Param("id") int id, @Param("token") String token);

	String getAccountNum(Map<String,Object> param);

	User getUserByPhoneAndMerchant(@Param("mobilePhone") String mobilePhone, @Param("merchantId") String merchantId);

	List<User> getAllProgressUsers();

	List<User> getUserList(@Param("ids") String ids);

	int getUsersCountByParam(Map<String, Object> params);

	User getUsersCountByCard(Map<String, Object> params);

	List<User> getUsersByParam(Map<String, Object> params);

	List<UserBatch> getUserBatchByParam(Map<String, Object> params);

	List<User> getUsersToNoth(Map<String, Object> params);

	List<User> getUserRelatedByParam(Map<String, Object> params);

	List<Partner> getPartnerByComNo_Name_Cert(@Param("comUserNo") String comUserNo,
			@Param("userName") String userName,
			@Param("certId") String certId);

	int operatePartnerShip(@Param("id") String id, @Param("partnerComNo") String partnerComNo);

	List<PartnerShip> getPartnerShips( @Param("customkey") String customkey);

	List<Map<String, Object>> getUserAccountByPerson(Map<String, Object> params);

	List<Map<String, Object>> getUserAccountByCompany(Map<String, Object> params);

	void updateUserByParam(Map<String,Object> param);

	void deleteByIds(@Param("ids") String ids);

	void deleteByBatcheId(@Param("batcheId") String batcheId,@Param("originalId") String originalId);

	void updateUserSignType(User user);

	List<User> getCompanyByMerchantId(@Param("merchantId")  String merchantId);

	int getUserRelatedCountByParam(Map<String, Object> params);

	void addWechartId(@Param("id") String id,@Param("wechartId") String wechartId);

	User selectUserByWechartId(@Param("wechartId") String wechartId);

	void updateUserBatch(UserBatch batch);

	void deleteUserBatch(@Param("id")String id);

	List<User> getUserByParam(Map<String, Object> param);

    List<User> getUserForMerchantByParams(Map<String, Object> paramMap);

    List<User> getUserForPlatform(Map<String, Object> paramMap);

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

	void updateMobileNoByUserId(User user);
}
