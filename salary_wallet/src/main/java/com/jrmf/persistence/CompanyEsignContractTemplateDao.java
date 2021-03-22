package com.jrmf.persistence;

import com.jrmf.domain.CompanyEsignContractTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyEsignContractTemplateDao {

  int deleteByPrimaryKey(Integer id);

  int insert(CompanyEsignContractTemplate record);

  int insertSelective(CompanyEsignContractTemplate record);

  CompanyEsignContractTemplate selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(CompanyEsignContractTemplate record);

  int updateByPrimaryKey(CompanyEsignContractTemplate record);

  CompanyEsignContractTemplate getCompanyEsignContractTemplateByParams(int companyId,
      Byte bizType);
}
