package com.jrmf.persistence;


import com.jrmf.domain.SubcontractRouter;
import com.jrmf.domain.dto.SubcontractRouterQueryDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubcontractRouterDao {

  int deleteByPrimaryKey(Integer id);

  int insert(SubcontractRouter record);

  int insertSelective(SubcontractRouter record);

  SubcontractRouter selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(SubcontractRouter record);

  int updateByPrimaryKey(SubcontractRouter record);

  List<SubcontractRouter> listSubcontractRouter(
      SubcontractRouterQueryDTO subcontractRouterQueryDTO);

  List<String> listPayTypesOfCompanyDefaultPayChannel(Integer companyId);
}
