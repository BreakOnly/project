package com.jrmf.service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.RespResult;
import com.jrmf.controller.constant.CustomerFirmStatusEnum;
import com.jrmf.controller.constant.YuncrFeignClient;
import com.jrmf.domain.Contract;
import com.jrmf.domain.dto.YuncrContractFileAttribute;
import com.jrmf.domain.dto.YuncrPushContractDTO;
import com.jrmf.persistence.ContractDao;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service("contractService")
public class ContractServiceImpl implements ContractService {

  @Autowired
  private ContractDao contractDao;

  @Autowired
  private YuncrFeignClient yuncrFeignClient;

  @Override
  public List<Contract> listContract(Map<String, Object> map) {
    return contractDao.listContract(map);
  }

  @Override
  public Map<String, Object> configContract(Contract contract) throws IOException {
    Map<String, Object> result = new HashMap<>();
    YuncrContractFileAttribute fileAttribute = new YuncrContractFileAttribute();

    if (contract.getId() != null) {
      Contract c = contractDao.getContractById(contract.getId());
      if (c == null) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "数据不存在，请刷新页面重试");
        return result;
      }

      if (c.getStatus() != CustomerFirmStatusEnum.FAIL.getCode()) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "非失败状态不可修改");
        return result;
      }
      contractDao.updateContract(contract);
      String fileType = contract.getContractUrl().substring(contract.getContractUrl().lastIndexOf(".") + 1);
      String fileName = contract.getContractUrl().substring(contract.getContractUrl().lastIndexOf("/") + 1);
      fileAttribute.setFileName(fileName);
      fileAttribute.setFileType(fileType);
    } else {
      fileAttribute = getFileAttribute(contract.getContractFile());

      if (!"pdf".equals(fileAttribute.getFileType())) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "文件格式错误，附件格式需为pdf");
        return result;
      }

      result = uploadContract(contract, fileAttribute.getFilePath(), fileAttribute.getFileName());
      if (!result.isEmpty()) {
        return result;
      }
      contract.setContractUrl(fileAttribute.getFileUrl());
      contract.setStatus(CustomerFirmStatusEnum.DOING.getCode());
      contract.setStatusDesc(CustomerFirmStatusEnum.DOING.getDesc());
      contractDao.insertContract(contract);
    }

    ResponseEntity<RespResult<Map<String, String>>> respResult;
    YuncrPushContractDTO yuncrPushContractDTO = this
        .getContractTemplateReq(contract, fileAttribute.getFileName(),
            fileAttribute.getFileType());
    try {
      JSONObject jsonObject = (JSONObject) JSONObject.toJSON(yuncrPushContractDTO);
      respResult = yuncrFeignClient.openContract(jsonObject);
    } catch (Exception e) {
      log.error("请求云控失败：", e);
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "调用云控服务异常, 请联系管理员");
      contract.setStatus(CustomerFirmStatusEnum.FAIL.getCode());
      contract.setStatusDesc("调用云控服务异常, 请联系管理员");
      contractDao.updateContract(contract);
      return result;
    }
    result = checkYuncrResp(respResult, contract);
    return result;
  }

  private YuncrContractFileAttribute getFileAttribute(MultipartFile contractFile) {
    YuncrContractFileAttribute yuncrContractFileAttribute = new YuncrContractFileAttribute();
    String contractFileName = contractFile.getOriginalFilename();
    String fileType = contractFileName.substring(contractFileName.lastIndexOf(".") + 1);
    String contractName = UUID.randomUUID().toString().replace("-", "");
    String filePath = "/yuncr/contract/";
    String fileName =
        contractFileName.substring(0, contractFileName.indexOf(".")) + contractName + "."
            + fileType;
    yuncrContractFileAttribute.setFileType(fileType);
    yuncrContractFileAttribute.setFilePath(filePath);
    yuncrContractFileAttribute.setFileName(fileName);
    yuncrContractFileAttribute.setFileUrl(filePath + fileName);
    return yuncrContractFileAttribute;
  }

  private Map<String, Object> uploadContract(Contract contract, String filePath, String fileName) {
    Map<String, Object> result = new HashMap<>(4);
    boolean backFile;
    try {
      byte[] bytes = contract.getContractFile().getBytes();
      InputStream fileInputStream = new ByteArrayInputStream(bytes);
      backFile = FtpTool.uploadFile(filePath, fileName, fileInputStream);
    } catch (Exception e) {
      log.error("上传合同失败");
      backFile = false;
    }

    if (!backFile) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "上传合同失败");
      return result;
    }
    return result;
  }

  private YuncrPushContractDTO getContractTemplateReq(Contract contract,
      String fileName, String fileType)
      throws IOException {
    YuncrPushContractDTO yuncrPushContractDTO = new YuncrPushContractDTO();
    String firmId = contractDao.getUserAuthenticationById(contract.getUserId());
    yuncrPushContractDTO.setFirmId(firmId);
    String platsrl = contractDao.getPlatsrl(contract.getChannelTaskId());
    yuncrPushContractDTO.setBidPlatsrl(platsrl);
    yuncrPushContractDTO.setFileType(fileType);
    yuncrPushContractDTO.setFileName(fileName);
    byte[] bytes;
    if (contract.getContractFile() != null) {
      bytes = contract.getContractFile().getBytes();
    } else {
      bytes = FtpTool.downloadFtpFile("/yuncr/contract/", fileName);
    }
    String fileBase = java.util.Base64.getEncoder().encodeToString(bytes);
    yuncrPushContractDTO.setFileBase(fileBase);
    return yuncrPushContractDTO;
  }

  private Map<String, Object> checkYuncrResp(
      ResponseEntity<RespResult<Map<String, String>>> respResult, Contract contract) {
    Map<String, Object> result = new HashMap<>(4);
    result.put(RespCode.RESP_STAT, RespCode.success);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

    if (!"00000".equals(respResult.getBody().getCode())) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "通道方返回：" + respResult.getBody().getMsg());
      Contract c = new Contract();
      c.setId(contract.getId());
      c.setStatus(CustomerFirmStatusEnum.FAIL.getCode());
      c.setStatusDesc("通道方返回：" + respResult.getBody().getMsg());
      contractDao.updateContract(c);
      return result;
    }

    LinkedHashMap<String, String> data = (LinkedHashMap<String, String>) respResult.getBody()
        .getData();
    log.info("云控数据响应:{}", data);
    contract.setFileNo(data.get("fileNo"));
    contract.setPlatsrl(data.get("platsrl"));
    contract.setStatus(CustomerFirmStatusEnum.SUCCESS.getCode());
    contract.setStatusDesc(CustomerFirmStatusEnum.SUCCESS.getDesc());
    contractDao.updateContract(contract);
    return result;
  }

  @Override
  public Contract getContractById(Integer id) {
    return contractDao.getContractById(id);
  }

  @Override
  public void updateContractStatusIsDelete(Integer id) {
    contractDao.updateContractStatusIsDelete(id);
  }

  @Override
  public List<Map<String, Object>> getProjectByCustomKey(String customKey) {
    return contractDao.getProjectByCustomKey(customKey);
  }

  @Override
  public List<Map<String, Object>> getYuncrUser(String customKey) {
    return contractDao.getYuncrUser(customKey);
  }

  @Override
  public List<Map<String, Object>> getCustomerFirm(String customKey) {
    return contractDao.getCustomerFirm(customKey);
  }

  @Override
  public void updateContract(Contract contract) {
    contractDao.updateContract(contract);
  }
}
