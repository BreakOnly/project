package com.jrmf.taxsettlement.api.gateway.restful;

import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.gateway.APIDockingAccesserProfile;
import com.jrmf.taxsettlement.api.gateway.APIDockingGateway;
import com.jrmf.taxsettlement.api.gateway.APIKeyMapper;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.taxsettlement.util.file.FileRepository;
import com.jrmf.taxsettlement.util.json.JsonDataTransformer;

@Controller
public class APIRestfulController {

  private static final Logger logger = LoggerFactory.getLogger(APIRestfulController.class);

  private static final String DEFAULT_NO_FILE_DOWNLOAD_TIP = "no file available for download";

  @Autowired
  private JsonDataTransformer jsonDataTransformer;

  @Autowired
  private APIDockingGateway apiDockingGateway;

  @Autowired
  private APIKeyMapper apiKeyMapper;

  @Autowired
  private UtilCacheManager utilCacheManager;

  @Autowired
  private FileRepository fileRepository;

  @RequestMapping(value = "/api/contract/signagreement.do", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> signagreementHandle(HttpServletRequest request) {

    String jsonStr;
    try {
      jsonStr = new String(readInputStream(request.getInputStream()), "utf-8");
    } catch (IOException e) {
      throw new APIDockingException(APIDockingRetCodes.COMMUNICATION_ABORT.getCode(),
          e.getMessage());
    }

    try {
      Map<String, Object> mapData = jsonDataTransformer.transformIn(jsonStr);
      logger.debug("receive api invocation[{}] with json", request.getRequestURI());
      for (String s : mapData.keySet()) {
        String param = (String) mapData.get(s);
        if (param.length() < 1000) {
          logger.debug(s + ":" + param);
        }
      }

      String merchantId = (String) mapData.get(APIDefinitionConstants.CFN_MERCHANT_ID);
      String partnerId = (String) mapData.get(APIDefinitionConstants.CFN_PARTNER_ID);

      request.setAttribute(APIDefinitionConstants.CFN_MERCHANT_ID, merchantId);
      request.setAttribute(APIDefinitionConstants.CFN_PARTNER_ID, partnerId);

      APIDockingAccesserProfile profile = new APIDockingAccesserProfile();
      profile.setMerchantId(merchantId);
      profile.setPartnerId(partnerId);
      profile.setApiKey(apiKeyMapper.map(request.getRequestURI()));
      profile.setAccesserIP(getAccessIP(request));

      return apiDockingGateway.signagreementApiHandle(profile, mapData);
    } catch (APIDockingException e) {
      throw e;
    } catch (Exception e) {
      logger.error("unhandle exception occured in api docking", e);
      throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
    }
  }

  /**
   * @return java.util.Map<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description API请求接口 统一入口
   * @Date 2020/10/19
   * @Param [request]
   **/
  @RequestMapping(value = "/api/**/*.do", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> receiveInvocationAndHandle(HttpServletRequest request) {

    String jsonStr;
    try {
      jsonStr = new String(readInputStream(request.getInputStream()), "utf-8");
    } catch (IOException e) {
      throw new APIDockingException(APIDockingRetCodes.COMMUNICATION_ABORT.getCode(),
          e.getMessage());
    }
    /**
     * @Description 将json转为 Map
     **/
    Map<String, Object> mapData = jsonDataTransformer.transformIn(jsonStr);
    logger.debug("receive api invocation[{}] with json", request.getRequestURI());
    for (String s : mapData.keySet()) {
      String param = mapData.get(s).toString();
      if (param.length() < 1000) {
        logger.debug(s + ":" + param);
      }
    }

    try {

      /**
       * @Description  merchantId:商户ID  partnerId:代理渠道ID
       **/
      String merchantId = (String) mapData.get(APIDefinitionConstants.CFN_MERCHANT_ID);
      String partnerId = (String) mapData.get(APIDefinitionConstants.CFN_PARTNER_ID);

      request.setAttribute(APIDefinitionConstants.CFN_MERCHANT_ID, merchantId);
      request.setAttribute(APIDefinitionConstants.CFN_PARTNER_ID, partnerId);

      APIDockingAccesserProfile profile = new APIDockingAccesserProfile();
      profile.setMerchantId(merchantId);
      profile.setPartnerId(partnerId);
      String url = request.getRequestURI();
      profile.setApiKey(apiKeyMapper.map(url));
      profile.setAccesserIP(getAccessIP(request));

      return apiDockingGateway.apiHandle(profile, mapData);
    } catch (APIDockingException e) {
      throw e;
    } catch (Exception e) {
      logger.error("unhandle exception occured in api docking", e);
      throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
    }
  }

  @RequestMapping(value = "/download.do")
  public void download(HttpServletRequest request, HttpServletResponse response, String file) {

    logger.debug("receive file download request for key[{}]", file);
    String storePath = (String) utilCacheManager.get(file);

    try {

      OutputStream out = response.getOutputStream();
      if (StringUtil.isEmpty(storePath) || !FtpTool.checkFile(storePath)) {
        out.write(DEFAULT_NO_FILE_DOWNLOAD_TIP.getBytes());
      } else {
        storePath = storePath.substring(0, storePath.lastIndexOf(file));
        byte[] fileBytes = FtpTool.downloadFtpFile(storePath, file + ".txt.gzip");
        out.write(fileBytes);
      }
      out.flush();
    } catch (Exception e) {
      logger.error("error occured in response file data", e);
    }
  }

  @RequestMapping(value = "/batch/**/*.do", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> receiveBatchFileAndHandle(HttpServletRequest request,
      @RequestParam("merchant_id") String merchantId,
      @RequestParam(value = "partner_id", required = false) String partnerId,
      @RequestParam(value = "batch_no", required = false) String batchNo,
      @RequestParam(value = "transfer_corp_id", required = false) String transferCorpId,
      String timestamp,
      String sign, MultipartFile file) {

    logger.debug("receive batch[{}] invocation[{}] from merchant[{}]",
        new Object[]{batchNo, request.getRequestURI(), merchantId});

    byte[] fileBytes;
    try {
      fileBytes = readInputStream(file.getInputStream());
    } catch (IOException e) {
      throw new APIDockingException(APIDockingRetCodes.COMMUNICATION_ABORT.getCode(),
          e.getMessage());
    }

    request.setAttribute(APIDefinitionConstants.CFN_MERCHANT_ID, merchantId);
    request.setAttribute(APIDefinitionConstants.CFN_PARTNER_ID, partnerId);

    APIDockingAccesserProfile profile = new APIDockingAccesserProfile();
    profile.setMerchantId(merchantId);
    profile.setPartnerId(partnerId);
    String url = request.getRequestURI();
    profile.setApiKey(apiKeyMapper.map(url));
    profile.setAccesserIP(getAccessIP(request));

    Map<String, Object> inData = new HashMap<String, Object>();
    inData.put(APIDefinitionConstants.CFN_TRANSFER_CORP_ID, transferCorpId);
    inData.put(APIDefinitionConstants.CFN_TIMESTAMP, timestamp);
    inData.put(APIDefinitionConstants.CFN_SIGN, sign);
    inData.put(APIDefinitionConstants.CFN_BATCH_NO, batchNo);
    inData.put(APIDefinitionConstants.CFN_BATCH_DATA, fileBytes);

    return apiDockingGateway.batchHandle(profile, inData);
  }


  private byte[] readInputStream(InputStream in) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] byteBuffer = new byte[256];
    while (true) {
      int readLen = in.read(byteBuffer);
      if (readLen < 0) {
        break;
      } else if (readLen == 0) {
        continue;
      } else {
        bytes.write(byteBuffer, 0, readLen);
      }
    }
    return bytes.toByteArray();
  }

  private String getAccessIP(HttpServletRequest request) {

    String ip = request.getHeader("x-forwarded-for");
    if (ip != null && "".equals(ip) && !"unknown".equals(ip)) {
      String[] ips = ip.split(",");
      ip = ips[ips.length - 1];
    }
    if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
  }

}
