package com.jrmf.service;

import com.jrmf.domain.*;

import java.util.HashMap;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author: zhangzehui
 * @date: 2018-8-18
 * @description:
 */
@Service
public interface ChannelCustomService {

  public ChannelCustom getCustomByCustomkey(String customkey);

  public Map<String, Object> getCustomByCustomkeyMap(String customkey);

  /**
   * 更新账户信息
   */
  public void updateCustomById(ChannelCustom custom);

  public ChannelCustom getCustomById(int id);

  public ChannelCustom customUserLogin(String userName, String password);

  @Deprecated
  public ChannelCustom customUsername(String userName);

  public void updatePassword(int id, String password);

  public void updateTranPassword(int id, String tranPassword);

  /**
   * 启用账户
   */
  public void enabledCustom(Integer id, String enabled);

  public void checkCustom(Integer id, String enabled, String addAccount);

  public int getCustomCount(Map<String, Object> paramMap);

  public List<ChannelCustom> getCustomList(Map<String, Object> paramMap);

  public void saveChannelCustom(ChannelCustom custom);

  public void deleteById(String id);

  public ChannelConfig getChannelConfigByParam(Map<String, Object> param);

  public List<ChannelConfig> getChannelConfigListByParam(Map<String, Object> param);

  public void insertCompanyAccountDetail(ChannelConfig channelConfig);

  public void updateCompanyAccountDetail(ChannelConfig channelConfig);

  public List<ChannelCustom> getCustomListExRoot(Map<String, Object> paramMap);

  /**
   * customeMenuDao 说明:
   *
   * @return:
   */

  List<ChannelCustomCatalog> getCustomContentByCustomId(String originalId, String level);

  List<CustomMenu> getAllPermission(String originalId);

  List<CustomMenu> getCustomMenuByOriginalId(Map<String, Object> param);

  List<CustomMenu> getCustomMenuByName(String originalId, String contentName);

  List<CustomMenu> getNodeTree(Map<String, Object> param);

  CustomMenu getCustomMenuById(int id);

  public int savePermission(CustomMenu munu);

  public void updatePermission(CustomMenu munu);

  public List<CustomMenu> getCustomMenuList(Map<String, Object> param);

  public int deleteNodeById(String id);

  public List<ChannelCustom> getChannelCustemGroup(String customkey);

  public List<CompanyPayment> getPaymentList(Map<String, Object> param, boolean defaultPayment);

  /**
   * 根据商户key获取服务公司信息。
   *
   * @return originalId, companyId, merchantId, companyName
   */
  List<Map<String, Object>> getPaymentListByOriginalId(String originalId);

  /**
   * 查询商户列表，customType 不传，取customType = 1,3,5
   *
   * @return 商户列表
   */
  List<ChannelCustom> getListCustom(Map<String, Object> paramMap);

  /**
   * @return 查询商户列表明细
   */
  List<ChannelCustom> getListCustomDetail(Map<String, Object> paramMap);

  /**
   * @return 查询商户列表, 传入customkey 字符串（字符串拼接）
   */
  List<ChannelCustom> listCustomByCustomKeys(Map<String, Object> paramMap);

  int enableOrganizationRelation(String customkey);

  List<Map<String, Object>> queryGroupInfoByParams(Map<String, Object> params);

  List<Map<String, Object>> queryProxyInfoByParams(Map<String, Object> params);

  void updateCustomTransFerPassword(String customKey, String password);

  public ChannelCustom getCustomByOfficialAccOpenId(String officialAccOpenId);

  List<ChannelCustom> getAllCustom();

  /**
   * 根据参数  查询  ChannelCustom  sql中没有的 ，可以自行添加
   *
   * @param params 现有 umfId（联动优势id） customType（商户类别） umfIdLength（umfId不为空）
   * @return ChannelCustom List
   */
  List<ChannelCustom> getCustomByParam(Map<String, Object> params);

  List<ChannelCustom> queryGroupCustom(String customKeys);

  public Map<String, Object> inputBatchInfoNew(Integer respstat, String operatorName,
      String nodeCustomKey, String menuId, String payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, String taskAttachmentFile,
      Map<String, Object> result,String realCompanyId);

  public Map<String, Object> inputBatchInfo(Integer respstat, String operatorName,
      String nodeCustomKey, String menuId, String payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, Map<String, Object> result);

  public Map<String, Object> alipayInputBatchInfoNew(Integer respstat, String operatorName,
      String nodeCustomKey, String menuId, Integer payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, Map<String, Object> result,String realCompanyId);

  public Map<String, Object> alipayInputBatchInfo(Integer respstat, String operatorName,
      String nodeCustomKey, String menuId, Integer payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, Map<String, Object> result);

  int removeProxyTypeById(int id);

  /**
   * 查询所有签约的商户列表
   *
   * @param channelCustom 当前登录者
   * @return 商户列表
   */
  List<ChannelCustom> getAgreementMerchants(ChannelCustom channelCustom);

  /**
   * 查询所有该商户的服务公司
   *
   * @param channelCustom 当前登录者
   * @param customKey 选择的商户key
   * @return 服务公司列表
   */
  List<ChannelCustom> getAgreementCompanies(ChannelCustom channelCustom, String customKey);

  List<Map<String, Object>> queryCustom(String customKeys);

  ChannelCustom getBusinessInfoByCustomkey(String customkey);

  public String getCustomKeysByLoginMerchant(ChannelCustom channelCustom);

  /**
   * 通过邮箱查询商户信息
   */
  ChannelCustom getChannelCustomByEmail(String email);

  /**
   * 通过customKey查询商户信息 统一社会信用码 地址信息
   * @param customKey
   * @return
   */
  ChannelCustom getChannelCustomByKey(String customKey);

  /**
   * 通过商户唯一标识查询商户信息
   */
  ChannelCustom getChannelCustomById(String customkey);

  /**
   * 设置登陆密码
   */
  void updateCustomPassword(Integer id, String password);

  /**
   * 新增服务公司 添加channel_custom表
   */
  void insertChannelCustomInfo(ChannelCustom channelCustom);

  /**
   * 修改服务公司 修改channel_custom表
   */
  void updateChannelCustomInfo(ChannelCustom channelCustom);

  /**
   * 通过邮箱查询商户信息
   */
  ChannelCustom getChannelCustomEmail(String email);

  ChannelCustom getUserByUserNameAndOemUrl(String userName, String oemUrl);

  int getCountByUserName(String userName);

  /**
   * 通过商户名称查询商户信息
   */
  int getChannelcustomByCompanyName(String proxyName);

  /**
   * 通过customkey查询商户信息
   */
  int getchannelcustomByCustomkey(String customkey);

  boolean getCustomKeysByType(Map<String, String> paramsMap, Integer[] customTypes,
      ChannelCustom customLogin);

  /**
   * 忘记密码->验证
   */
  Map<String, Object> forgetPassword(HttpServletRequest request, String userName, String password,
      String againPassword, String code, String phoneNo);

  /**
   * 忘记密码->修改密码
   */
  Map<String, Object> validMobileCodeAndForgetPassword(HttpServletRequest request, String code);

  /**
   * 查询 账号配置管理
   */
  List<Map<String, Object>> queryAccount(Map<String, Object> param);

  /**
   * 切换账号配置管理-> 根据商户角色类型查询商户
   */
  List<Map<String, Object>> getCustomByCustomType(String companyType);

  /**
   * 根据customkey查询商户主键ID
   */
  String getCustomIdByCustomkey(String customkey);

  /**
   * 自定义组织名称查询
   */
  List<Map<String, Object>> getAllList(Map<String, Object> params);

  List<Map<String, Object>> getCustomByIds(Map<String, Object> paramMap);

  ChannelCustom getChannelCustomByCompanyName(String companyName);

  /**
   * 查询类型为2的商户名称
   */
  boolean getChannelCustomCompanyNameByCustomType(String companyName);

  List<Map<String, Object>> queryCustom2(String customKeys);

  ChannelConfig getChannelConfigByCustomKeyAndCompanyId(String companyId,
      Integer payType);

  List<HashMap> selectPlatformList();

  /**
   * @return java.util.HashMap
   * @Author YJY
   * @Description 查询该平台名称
   * @Date 2020/9/23
   * @Param [customId]
   **/
  HashMap selectPlatformByCostomId(Integer customId);

  void updateSubAccountPlatformId(Integer businessPlatformId, String masterCustomKey);

  List<String> getCustomKeysByLoginCustomExtent(ChannelCustom loginCustom);

  List<Map<String, Object>> queryAllCustom(String customKeys);
}
