package com.jrmf.persistence;

import com.jrmf.domain.Contract;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractDao {

  List<Contract> listContract(Map<String, Object> map);

  Contract getContractById(Integer id);

  void updateContractStatusIsDelete(Integer id);

  void updateContract(Contract contract);

  void insertContract(Contract contract);

  List<Map<String, Object>> getProjectByCustomKey(String customKey);

  List<Map<String, Object>> getYuncrUser(@Param("customKey") String customKey);

  String getPlatsrl(Integer channelTaskId);

  List<Map<String, Object>> getCustomerFirm(@Param("customKey") String customKey);

  String getUserAuthenticationById(Integer userId);
}
