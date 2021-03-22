/**
 *
 */
package com.jrmf.persistence;

import com.jrmf.domain.*;

import com.jrmf.interceptor.InterceptPlatformPermissionAnnotation;
import java.util.HashMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author: zhangzehui
 * @date: 2018-8-18
 * @description:
 */
@Mapper
public interface ChannelCustomDao {

  public ChannelCustom getCustomByCustomkey(@Param("customkey") String customkey,
      @Param("platformId") Integer platformId);

  public Map<String, Object> getCustomByCustomkeyMap(@Param("customkey") String customkey);

  /**
   * 更新账户信息
   * @param custom
   */
  public void updateCustomById(ChannelCustom custom);

  public void saveChannelCustom(ChannelCustom custom);

  public ChannelCustom getCustomById(int id);

  public ChannelCustom customUserLogin(@Param("userName") String userName,
      @Param("password") String password);

  public void updatePassword(@Param("id") int id,
      @Param("password") String password);


  public void updateTranPassword(@Param("id") int id,
      @Param("tranPassword") String tranPassword);

  public void updateTranPasswordNew(@Param("customkey") String customkey,
      @Param("tranPassword") String tranPassword);

  /**
   * 启用账户
   * @param id
   * @param enabled
   */
  public void enabledCustom(@Param("id") Integer id,
      @Param("enabled") String enabled);

  void checkCustom(@Param("id") Integer id, @Param("enabled") String enabled,
                   @Param("addAccount") String addAccount);

  public int getCustomCount(Map<String, Object> paramMap);

  public List<ChannelCustom> getCustomList(Map<String, Object> paramMap);

  public ChannelCustom customUsername(@Param("userName") String userName);

  public void deleteById(@Param("id") String id);

  public ChannelConfig getChannelConfigByParam(Map<String, Object> param);

  public void insertCompanyAccountDetail(ChannelConfig channelConfig);

  public void updateCompanyAccountDetail(ChannelConfig channelConfig);

  @InterceptPlatformPermissionAnnotation(aliasName = "m.businessPlatformId")
  public List<ChannelCustom> getCustomListExRoot(Map<String, Object> paramMap);

  /**
   * bankNameDao
   * 说明:
   * @param mark
   * @return:
   */
  public BankName getBankName(@Param("mark") String mark);

  public void insertBankName(Map<String, String> param);

  public List<ChannelCustom> getChannelCustemGroup(String customkey);

  public List<CompanyPayment> getPaymentList(Map<String, Object> paramMap);

  public List<CompanyPayment> getPaymentListDefault(Map<String, Object> paramMap);

  List<ChannelConfig> getChannelConfigListByParam(Map<String, Object> param);

  /**
   * 根据商户key获取服务公司信息。
   * @return originalId, companyId, merchantId, companyName
   */
  List<Map<String, Object>> getPaymentListByOriginalId(String originalId);

  /**
   * 查询商户列表，customType 不传，取customType = 1,3,5
   *
   * @return 商户列表
   */
  @InterceptPlatformPermissionAnnotation(aliasName = "m.businessPlatformId")
  List<ChannelCustom> getListCustom(Map<String, Object> paramMap);

  /**
   * @return 查询商户列表明细
   */
  @InterceptPlatformPermissionAnnotation(aliasName = "aa.businessPlatformId")
  List<ChannelCustom> getListCustomDetail(Map<String, Object> paramMap);

  /**
   * @return 查询商户列表, 传入customkey 字符串（字符串拼接）
   */
  List<ChannelCustom> listCustomByCustomKeys(Map<String, Object> paramMap);

  List<Map<String, Object>> queryGroupInfoByParams(Map<String, Object> params);

  List<Map<String, Object>> queryProxyInfoByParams(Map<String, Object> params);

  void updateCustomTransFerPassword(@Param("customKey") String customKey,
      @Param("password") String password);

  ChannelCustom getCustomByOfficialAccOpenId(@Param("officialAccOpenId") String officialAccOpenId);

  int removeAgentIdById(int id);

  List<OrganizationNode> getAllCompany(Integer platformId);

  OrganizationNode getNodeById(int nodeId, Integer platformId);

  List<ChannelCustom> getAllCustom();

  /**
   * 根据参数  查询  ChannelCustom  sql中没有的 ，可以自行添加
   *
   * @param params 现有
   *               umfId（联动优势id）
   *               customType（商户类别）
   *               umfIdLength（umfId不为空）
   * @return ChannelCustom List
   */
  List<ChannelCustom> getCustomByParam(Map<String, Object> params);

  List<ChannelCustom> queryGroupCustom(String customKeys);

  int removeProxyTypeById(int id);

  List<Map<String, Object>> queryCustom(@Param("customKeys") String customKeys);

  ChannelCustom getBusinessInfoByCustomkey(@Param("customkey") String customkey);

  /**
   * 通过邮箱查询商户信息
   * @param email
   * @return
   */
  ChannelCustom getChannelCustomByEmail(@Param("email") String email);

  /**
   * 通过customKey查询商户信息 统一社会信用码 地址信息
   * @param customKey
   * @return
   */
  ChannelCustom getChannelCustomByKey(@Param("customKey") String customKey);

  /**
   * 通过商户唯一标识查询商户信息
   * @param customkey
   * @return
   */
  ChannelCustom getChannelCustomById(@Param("customkey") String customkey);

  /**
   * 设置登陆密码
   * @param id
   * @param password
   */
  void updateCustomPassword(@Param("id") Integer id, @Param("password") String password);

  /**
   *  新增服务公司 添加channel_custom表
   * @param channelCustom
   */
  void insertChannelCustomInfo(ChannelCustom channelCustom);

  /**
   *  修改服务公司 修改channel_custom表
   * @param channelCustom
   */
  void updateChannelCustomInfo(ChannelCustom channelCustom);


  /**
   * 通过邮箱查询商户信息
   * @param email
   * @return
   */
  ChannelCustom getChannelCustomEmail(@Param("email") String email);

  ChannelCustom getUserByUserNameAndOemUrl(@Param("userName") String userName,
      @Param("oemUrl") String oemUrl);

  int getCountByUserName(String userName);

  /**
   * 通过商户名称查询商户信息
   * @param proxyName
   * @return
   */
  int getChannelcustomByCompanyName(@Param("proxyName") String proxyName);

  /**
   * 通过customkey查询商户信息
   * @param customkey
   * @return
   */
  int getchannelcustomByCustomkey(@Param("customkey") String customkey);

  /**
   * 查询 账号配置管理
   * @param param
   * @return
   */
  @InterceptPlatformPermissionAnnotation(aliasName = "cc.businessPlatformId")
  List<Map<String, Object>> queryAccount(Map<String, Object> param);

  /**
   * 切换账号配置管理-> 根据商户角色类型查询商户
   * @param companyType
   * @return
   */
  @InterceptPlatformPermissionAnnotation(aliasName = "businessPlatformId")
  List<Map<String, Object>> getCustomByCustomType(@Param("companyType") String companyType);

  ChannelCustom getIdByCustomkey(@Param("customkey") String customkey);

  /**
   * 根据customkey查询商户主键ID
   * @param customkey
   * @return
   */
  String getCustomIdByCustomkey(@Param("customkey") String customkey);

  List<Map<String, Object>> getCustomByIds(Map<String, Object> paramMap);

  List<String> getChannelCustomByCustomType(Integer customType);

  List<String> getChannelCustomId();

  /**
   * 通过商户类型和登录类型查找商户信息
   * @param customType
   * @param loginRole
   */
  List<String> getCustomByCustomTypeAndLoginRole(Integer customType, Integer loginRole);

  /**
   * 获取普通代理商商户信息
   */
  List<String> getAgentCustomByCustomTypeAndLoginRole();

  /**
   * 获取OEM代理商商户信息
   */
  List<String> getOemAgentCustomByCustomTypeAndLoginRole();

  /**
   * 查询服务公司下所关联的商户
   * @param companyIds
   * @return
   */
  List<String> getCustomByCompanyId(String companyIds);

  /**
   * 查询服务公司下所关联的代理商
   * @param companyIds
   * @return
   */
  List<String> getProxyByCompanyId(String companyIds);

  /**
   * 通过服务公司的customkey查找id
   * @param companyIds
   * @return
   */
  List<String> getIdByCompanyId(String companyIds);

  public List<Map<String, Object>> getAllList(Map<String, Object> params);

  ChannelCustom getChannelCustomByCompanyName(String companyName);

  int getChannelCustomCompanyNameByCustomType(String companyName);

  List<Map<String, Object>> queryCustom2(@Param("customKeys") String customKeys);

  CompanyPayment getByCompanyIdAndPayType(String companyId, String paymentType);

  ChannelConfig getChannelConfigByCustomKeyAndCompanyId(String companyId,
      Integer payType);

  /**
   * @Description 查询所有平台
   **/
  List<HashMap> selectPlatformList();

  /**
   * @Author YJY
   * @Description 查询该平台名称
   * @Date  2020/9/23
   * @Param [customId]
   * @return java.util.HashMap
   **/
  HashMap selectPlatformByCostomId(Integer customId);

  void updateSubAccountPlatformId(@Param("businessPlatformId") Integer businessPlatformId,
      @Param("masterCustomKey") String masterCustomKey);


  List<String> getCompanyUserIdByBusinessPlatformId(Integer businessPlatformId);

  List<String> getCustomKeyByBusinessPlatformId(Integer platformId);

  List<Map<String, Object>> queryAllCustom(@Param("customKeys") String customKeys);
}
