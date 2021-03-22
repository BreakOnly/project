package com.jrmf.service;

import com.jrmf.domain.AgreementTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-12 17:34
 * @desc 协议配置相关参数  模版
 **/
public interface AgreementTemplateService {

  /**
   * 根据参数（map） 查询模版list
   *
   * @param map 参数集合
   * @return AgreementTemplate list
   */
  List<AgreementTemplate> getAgreementTemplateByParam(Map<String, Object> map);

  int getUserAgreementCountByParam(Map<String, Object> map);

  List<AgreementTemplate> listUserAgreementTemplates(Map<String, Object> params);

  /**
   * 添加AgreementTemplate
   */
  void addAgreementTemplate(AgreementTemplate agreementTemplate);

  /**
   * 修改协议模板
   */
  void updateAgreementTemplate(AgreementTemplate agreementTemplate);

  /**
   * 删除协议模板
   */
  void deleteAgreementTemplate(String id);

  /**
   * 根据ID查询协议模板内容
   */
  AgreementTemplate getAgreementTemplateById(String id);

  int listUserAgreementTemplatesCount(Map<String, Object> params);

  AgreementTemplate getAgreementPaymentTemplate(String customKey, String companyId);

  List<AgreementTemplate> getNotUploadIdCardTemplateInfoByParam(Map<String, Object> paramMap);

  String selectAgreementTemplateByConsumerKey(String agreementTemplateConsumer);

  String selectAgreementTemplate(String agreementTemplateConsumer);
}
