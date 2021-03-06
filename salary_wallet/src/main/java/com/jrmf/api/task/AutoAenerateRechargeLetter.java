package com.jrmf.api.task;

import static com.jrmf.controller.BaseController.PROCESS;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.api.RechargeLetterType;
import com.jrmf.common.Constant;
import com.jrmf.common.ServiceResponse;
import com.jrmf.common.UserServiceFeignClient;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.RechargeLetterStatusType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.Company;
import com.jrmf.domain.CompanyEsignContractTemplate;
import com.jrmf.domain.dto.ContractDTO;
import com.jrmf.domain.dto.EsignContractDTO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.CompanyService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.PdUtil;
import com.jrmf.utils.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;

@Slf4j
public class AutoAenerateRechargeLetter implements Runnable {

  private String processId;
  private String customKey;
  private String companyId;
  private BigDecimal rechargeAmount;
  private String remark;
  private CompanyService companyService;
  private ChannelCustomService channelCustomService;
  private ChannelHistoryService channelHistoryService;
  private int channelHistoryId;
  private BestSignConfig bestSignConfig;
  private String templateSavetmpPath;
  private String fontsPath;
  private UserServiceFeignClient userServiceFeignClient;

  public AutoAenerateRechargeLetter(String processId, String customKey, String companyId,
      BigDecimal rechargeAmount, String remark, CompanyService companyService,
      ChannelCustomService channelCustomService, ChannelHistoryService channelHistoryService,
      int channelHistoryId, BestSignConfig bestSignConfig, String templateSavetmpPath,
      String fontsPath, UserServiceFeignClient userServiceFeignClient) {
    this.processId = processId;
    this.customKey = customKey;
    this.companyId = companyId;
    this.rechargeAmount = rechargeAmount;
    this.remark = remark;
    this.companyService = companyService;
    this.channelCustomService = channelCustomService;
    this.channelHistoryService = channelHistoryService;
    this.channelHistoryId = channelHistoryId;
    this.bestSignConfig = bestSignConfig;
    this.templateSavetmpPath = templateSavetmpPath;
    this.fontsPath = fontsPath;
    this.userServiceFeignClient = userServiceFeignClient;
  }

  @Override
  public void run() {
    MDC.put(PROCESS, processId);
    //?????????????????????????????????????????????
    ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(customKey);
    if (channelCustom != null && channelCustom.getNeedRechargeLetter()) {
      if (remark != null && !"".equals(remark)) {
        int start = remark.indexOf("???????????????????????????");
        int end = remark.indexOf("???");
        if (start != -1) {
          String amount = remark.substring(start + 9, end).trim();
          if (rechargeAmount.compareTo(new BigDecimal(amount)) == 0) {
            //????????????????????????,??????????????????????????????????????????pdf,??????ftp?????????db
            Company company = companyService.getCompanyByUserId(Integer.valueOf(companyId));
            if (company != null && company.getRechargeLetterType()
                .equals(RechargeLetterType.ESIGN.getRechargeLetterTypeCode())) { //?????????????????????
              if (!generateEsignRechargeLetter(String.valueOf(channelHistoryId),
                  Integer.valueOf(companyId))) {
                generateLocalRechargeLetter(company, channelCustom);
              }
            } else {
              generateLocalRechargeLetter(company, channelCustom);
            }
          } else {
            log.info("?????????????????????????????????--???????????????{}, ????????????????????????{}", rechargeAmount, amount);
            ChannelHistory channelHistory = new ChannelHistory();
            channelHistory.setId(channelHistoryId);
            channelHistory.setRechargeLetterStatus(new Byte("3"));
            channelHistory.setRechargeLetterErrMsg(
                "?????????????????????????????????--???????????????" + rechargeAmount + " ????????????????????????" + amount);
            channelHistoryService.updateChannelHistory(channelHistory);
          }
        } else {
          log.info("???????????????????????????????????????" + remark);
        }
      }
    } else {
      log.info("????????????????????????????????????");
    }

  }

  public boolean generateEsignRechargeLetter(String chanelHistoryId, Integer companyId) {
    boolean rs = false;
    ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(chanelHistoryId);
    if (channelHistory != null && channelHistory.getId() > 0) {
      if (RechargeLetterStatusType.PROCESSING.getCode()
          .equals(channelHistory.getRechargeLetterStatus())) {
        if (channelHistory.getRemark() != null && !"".equals(channelHistory.getRemark())) {
          String remark = channelHistory.getRemark();
          int start = remark.indexOf("???????????????????????????");
          int end = remark.indexOf("???");
          if (start != -1) {
            String amount = remark.substring(start + 9, end).trim();
            if (new BigDecimal(channelHistory.getRechargeAmount()).compareTo(new BigDecimal(amount))
                == 0) {
              CompanyEsignContractTemplate companyEsignContractTemplate = channelHistoryService
                  .getCompanyEsignContractTemplateByParams(companyId, new Byte("1"));
              if (companyEsignContractTemplate != null) {
                EsignContractDTO esignContractDTO = new EsignContractDTO();
                esignContractDTO.setAccountId(companyEsignContractTemplate.getAccountId());
                esignContractDTO.setProxyFlag(true);
                esignContractDTO.setMsgFlag(true);
                esignContractDTO.setProjectCode("wallet-sign");
                esignContractDTO.setSignLocation("Signature2");
                esignContractDTO.setEsignPlatform(companyEsignContractTemplate.getEsignPlatform());

                Map<String, String> textValueInfo = new HashMap<>();
                String date = null;
                try {
                  date = DateUtils
                      .formartDateStr(channelHistory.getCreatetime(), "yyyy-MM-dd", "yyyy???MM???dd");
                } catch (ParseException e) {
                  e.printStackTrace();
                }
                textValueInfo.put("remark", StringUtil.insertSubString(remark, "\r\n", 34));
                textValueInfo.put("date", date);

                esignContractDTO.setTextValueInfo(textValueInfo);
                esignContractDTO.setTemplateId(companyEsignContractTemplate.getTemplateId());
                esignContractDTO.setContractName("??????????????????");
                esignContractDTO.setSealId(companyEsignContractTemplate.getSealId());

                ServiceResponse serviceResponse = userServiceFeignClient
                    .createContract(esignContractDTO);
                if (serviceResponse != null && Constant.SERVICE_RESPONSE_CODE_SUCCESS
                    .equals(serviceResponse.getCode())) {
                  ContractDTO contract = JSONObject
                      .parseObject(JSONObject.toJSONString(serviceResponse.getData()),
                          ContractDTO.class);
                  if (contract != null && contract.getErrorCode() == 0) {
                    ChannelHistory history = new ChannelHistory();
                    history.setId(channelHistory.getId());
                    history.setRechargeLetterUrl(contract.getContractUrl());
                    history.setRechargeLetterStatus(new Byte("2"));
                    history.setRechargeLetterType(new Byte("2"));
                    channelHistoryService.updateChannelHistory(history);
                    rs = true;
                    return rs;
                  }
                  log.info("???????????????????????????--??????????????????????????????");
                  return rs;
                }
                log.info("???????????????????????????--???????????????????????????" + serviceResponse.getMsg());
                return rs;
              }
              log.info("???????????????????????????--??????????????????????????????????????????");
              return rs;
            }
            log.info("???????????????????????????--???????????????");
            return rs;
          }
          log.info("???????????????????????????--?????????????????????");
          return rs;
        }
        log.info("???????????????????????????--????????????");
        return rs;
      }
      log.info("???????????????????????????????????????--" + channelHistory.getRechargeLetterStatus());
      return rs;
    }
    log.info("???????????????????????????--??????????????????????????????");
    return rs;
  }

  public void generateLocalRechargeLetter(Company company, ChannelCustom channelCustom) {
    if (company != null && company.getRechargeLetterTemplate() != null) {
      String template = company.getRechargeLetterTemplate();
      File file = new File(templateSavetmpPath + template + ".html");
      String fileName = channelCustom.getCompanyName() + "-" + DateUtils.getNowTime() + "-??????????????????";
      String pdfFile = templateSavetmpPath + fileName + ".pdf";
      String htmlFile = templateSavetmpPath + fileName + ".html";
      try {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        //???????????????????????????
        while ((line = br.readLine()) != null) {
          line = line.replaceAll("\\{remark\\}",
              StringUtil.insertSubString(remark, "\r\n", 46));
          line = line.replaceAll("\\{date\\}", DateUtils.getNowDayZH());
          stringBuilder.append(line);
          stringBuilder.append(System.getProperty("line.separator"));
        }
        br.close();
        OutputStream fos = new FileOutputStream(htmlFile);
        fos.write(stringBuilder.toString().getBytes("UTF-8"));
        fos.flush();
        fos.close();
        try {
          //HtmlUtil.parseHTML2PDFFile(pdfFile, stringBuilder.toString());
          PdUtil.html2pdf(htmlFile, pdfFile, fontsPath);
          FileInputStream inputStream = new FileInputStream(pdfFile);
          String path = "/download/rechargeletter/";
          FtpTool.uploadFile(path, fileName + ".pdf", inputStream);
          String url = bestSignConfig.getServerNameUrl() + path + fileName + ".pdf";
          ChannelHistory channelHistory = new ChannelHistory();
          channelHistory.setId(channelHistoryId);
          channelHistory.setRechargeLetterStatus(new Byte("2"));
          channelHistory.setRechargeLetterUrl(url);
          channelHistory.setRechargeLetterType(new Byte("1"));
          channelHistoryService.updateChannelHistory(channelHistory);
        } catch (Exception e) {
          log.error("?????????????????????????????????", e);
        }
      } catch (IOException e) {
        log.error("???????????????????????????io??????", e);
      }
    } else {
      log.info("??????????????????????????????--???????????????????????????????????????");
    }
  }

}
