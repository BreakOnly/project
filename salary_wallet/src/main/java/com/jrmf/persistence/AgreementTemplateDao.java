package com.jrmf.persistence;

import com.jrmf.domain.AgreementTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-12 18:02
 * @desc
 **/
@Mapper
public interface AgreementTemplateDao {

    /**
     * 根据参数（map） 查询模版list
     * @param map 参数集合
     * @return AgreementTemplate list
     */
    List<AgreementTemplate> getAgreementTemplateByParam(Map<String, Object> map);

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
     * @param id
     * @return
     */
    AgreementTemplate getAgreementTemplateById(@Param("id") String id);

    int getAgreementTemplateByParamCount(Map<String, Object> params);

    AgreementTemplate getAgreementPaymentTemplate(String customKey, String companyId);

  List<AgreementTemplate> getNotUploadIdCardTemplateInfoByParam(Map<String, Object> paramMap);
}
