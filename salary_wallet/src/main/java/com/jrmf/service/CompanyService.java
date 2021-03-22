package com.jrmf.service;

import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.Company;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.SMSChannelConfig;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author ZHANGZEHUI 类说明
 */
@Service
public interface CompanyService {

  void creatCompanyInfo(Company company);

  Company getCompanyByUserId(int id);

  void addPicturePath(Company company);

  void updateCompanyInfo(Company company);

  List<Company> getCompanyList(Map<String, Object> params);

  List<Company> getCompanyListByProxy(String customKey);

  PaymentConfig getPaymentConfigInfo(String paymentType, String originalId, String companyId);

  PaymentConfig getPaymentConfigInfo(String paymentType, String originalId, String companyId,
      String pathNo);

  List<PaymentConfig> getSubAccountPaymentConfig();

  List<Company> getCompanyByUserIds(String ids);

  /**
   * 查询下发公司名称
   *
   * @param companyName
   * @return
   */
  boolean getCompanyName(String companyName);

  /**
   * 新增下发公司
   *
   * @param company
   * @return
   */
  void addCompany(Company company);

  /**
   * 查询下发公司邮箱
   *
   * @param email
   * @return
   */
  Company getCompanyEmail(String email);

  int getSubAccountList(String companyId);

  /**
   * 条件查询下发公司
   *
   * @param paramMap
   * @return
   */
  List<Company> getCompanyListByParam(Map<String, Object> paramMap);

  /**
   * 通过服务公司名称查询服务公司信息
   *
   * @param companyName
   * @return
   */
  Company getCompanyByCompanyName(String companyName);

  String getMerchantIdByUserId(Integer companyId);

  int checkIsExist(ChannelConfig channelConfig);

  /**
   * 条件查询下发公司总数
   *
   * @param paramMap
   * @return
   */
  int getCompanyListByParamCount(Map<String, Object> paramMap);

  SMSChannelConfig getSmsConfig();

  List<Company> getAllCompanyList(Integer companyType);

  Company getCompanyById(String id);

  PaymentConfig getPaymentConfigInfoPlus(String paymentType, String originalId, String companyId,String realCompanyId);

  PaymentConfig getPaymentConfigInfoPlus(String paymentType, String originalId, String companyId,String realCompanyId,String pathNo);

  /**
   * 根据服务公司id查询是否是转包服务公司
   * @return
   */
  Integer getCompanyCountByIdAndCompanyType(String companyId, int companyType);

  /**
   * 查询companyType = 1 的实际下发公司
   */
  List<Company> listRealityCompany();

  /**
  * @Description 根据平台获取服务公司
  **/
  List<Company>  selectCompanyByPlatform(Integer customId);

  Company getLikeCompanyByCompanyName(String companyName);

  Map<String, String> getCompanyPayChannelRelation(String companyId, String pathNo);

  List<Company> getIndividualCompanys(String userId);
}
