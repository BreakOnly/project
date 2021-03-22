package com.jrmf.service;

import com.jrmf.domain.Contract;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public interface ContractService {

  List<Contract> listContract(Map<String, Object> map);

  Map<String, Object> configContract(Contract contract) throws IOException;

  Contract getContractById(Integer id);

  void updateContractStatusIsDelete(Integer id);

  List<Map<String, Object>> getProjectByCustomKey(String customKey);

  List<Map<String, Object>> getYuncrUser(String customKey);

  List<Map<String, Object>> getCustomerFirm(String customKey);

  void updateContract(Contract contract);
}
