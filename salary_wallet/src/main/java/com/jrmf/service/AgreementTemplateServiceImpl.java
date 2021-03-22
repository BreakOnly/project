package com.jrmf.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.persistence.AgreementTemplateDao;
import com.jrmf.persistence.AgreementTemplateMappingDao;
import com.jrmf.persistence.UsersAgreementDao;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-12 17:57
 * @desc
 **/
@Service
public class AgreementTemplateServiceImpl implements AgreementTemplateService {

//    private static Logger logger = LoggerFactory.getLogger(AgreementTemplateServiceImpl.class);

  private final AgreementTemplateDao agreementTemplateDao;

  private final UsersAgreementDao usersAgreementDao;

  Cache<String, String> cache = Caffeine.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .maximumSize(1000)
      .build();

  @Autowired
  public AgreementTemplateServiceImpl(AgreementTemplateDao agreementTemplateDao,
      UsersAgreementDao usersAgreementDao) {
    this.agreementTemplateDao = agreementTemplateDao;
    this.usersAgreementDao = usersAgreementDao;
  }

  @Autowired
  AgreementTemplateMappingDao agreementTemplateMappingDao;

  /**
   * 根据参数（map） 查询模版list
   *
   * @param map 参数集合
   * @return AgreementTemplate list
   */
  @Override
  public List<AgreementTemplate> getAgreementTemplateByParam(Map<String, Object> map) {
    return agreementTemplateDao.getAgreementTemplateByParam(map);
  }

  @Override
  public int getUserAgreementCountByParam(Map<String, Object> map) {
    return usersAgreementDao.getUserAgreementCountByParam(map);
  }

  @Override
  public List<AgreementTemplate> listUserAgreementTemplates(Map<String, Object> params) {
    return agreementTemplateDao.getAgreementTemplateByParam(params);
  }

  /**
   * 添加AgreementTemplate
   */
  @Override
  public void addAgreementTemplate(AgreementTemplate agreementTemplate) {
    agreementTemplateDao.addAgreementTemplate(agreementTemplate);
  }

  /**
   * 修改协议模板
   */
  @Override
  public void updateAgreementTemplate(AgreementTemplate agreementTemplate) {
    agreementTemplateDao.updateAgreementTemplate(agreementTemplate);
  }

  /**
   * 删除协议模板
   */
  @Override
  public void deleteAgreementTemplate(String id) {
    agreementTemplateDao.deleteAgreementTemplate(id);
  }

  /**
   * 根据ID查询协议模板内容
   */
  @Override
  public AgreementTemplate getAgreementTemplateById(String id) {
    return agreementTemplateDao.getAgreementTemplateById(id);
  }

  @Override
  public int listUserAgreementTemplatesCount(Map<String, Object> params) {
    return agreementTemplateDao.getAgreementTemplateByParamCount(params);
  }

  @Override
  public AgreementTemplate getAgreementPaymentTemplate(String customKey, String companyId) {
    return agreementTemplateDao.getAgreementPaymentTemplate(customKey, companyId);
  }

  @Override
  public List<AgreementTemplate> getNotUploadIdCardTemplateInfoByParam(
      Map<String, Object> paramMap) {
    return agreementTemplateDao.getNotUploadIdCardTemplateInfoByParam(paramMap);
  }

  @Override
  public String selectAgreementTemplateByConsumerKey(String agreementTemplateConsumer) {
    return agreementTemplateMappingDao.selectAgreementTemplateProvider(agreementTemplateConsumer);
  }

  @Override
  public String selectAgreementTemplate(String agreementTemplateConsumer) {
    return cache.get(agreementTemplateConsumer, this::selectAgreementTemplateByConsumerKey);
  }


}
