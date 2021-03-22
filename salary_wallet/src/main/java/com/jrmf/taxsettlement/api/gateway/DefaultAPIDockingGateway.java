package com.jrmf.taxsettlement.api.gateway;

import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.APIDockingMode;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.MerchantAPITransferBatchDao;
import com.jrmf.taxsettlement.api.TaxSettlementInertnessDataCache;
import com.jrmf.taxsettlement.api.gateway.batch.BatchDealResult;
import com.jrmf.taxsettlement.api.gateway.batch.BatchTransferRequestDealer;
import com.jrmf.taxsettlement.api.gateway.batch.BatchTransferRequestDealers;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class DefaultAPIDockingGateway implements APIDockingGateway {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAPIDockingGateway.class);

  private static final String BATCH_API_KEY_PREFIX = "BATCH_";

  @Autowired
  private SignWorkers signWorkers;

  @Autowired
  private APIDockingManager apiDockingManager;

  @Autowired
  private MerchantAPITransferBatchDao apiTransferBatchDao;

  @Autowired
  private TaxSettlementInertnessDataCache dataCache;

  @Autowired
  private BatchTransferRequestDealers requestDealers;

  @Autowired
  private TransferDealStatusNotifier statusNotifier;

  private static final Map<Class,List<String>> CACHED_IGNORE_SIGN_MAPPING=new HashMap<>();

  @Override
  public Map<String, Object> apiHandle(APIDockingAccesserProfile profile,
      Map<String, Object> inData)
      throws APIDockingException {

    String merchantId = profile.getMerchantId();
    String partnerId = profile.getPartnerId();
    String apiKey = profile.getApiKey();
    /**
     * @Description 根据商户KEY 获取商户配置信息
     **/
    MerchantAPIDockingConfig dockingConfig = checkAccessAuthorizationAndGetAPIDockingConfig(
        merchantId, partnerId,
        apiKey, profile.getAccesserIP());

    try {

      /**
       * @Description 根据apiKey获取对应的 service
       **/
      Action<ActionParams, ActionAttachment> dockingService = apiDockingManager
              .getDockingService(apiKey);

      Type genericType = null;
      Type[] genericInterfaces = dockingService.getClass().getGenericInterfaces();
      if (genericInterfaces == null || genericInterfaces.length == 0) {
        genericType = dockingService.getClass().getGenericSuperclass();
      } else {
        genericType = genericInterfaces[0];
      }
      /**
       * @Description 转化参数
       **/
      Class<? extends ActionParams> exactalParamClass = (Class<? extends ActionParams>) ((ParameterizedType) genericType)
              .getActualTypeArguments()[0];
      /**
      * @Description 效验签名
      **/
      String signType = dockingConfig.getSignType();
      Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(inData);
      filterIgnoreSignFields(toSignMap,exactalParamClass);
      checkSign(toSignMap, signType, dockingConfig.getSignVerificationKey());

      /**
      * @Description 执行具体方法
      **/
      ActionParams actionParams = (ActionParams) APIDockingGatewayDataUtil
              .checkAndTransform(inData, exactalParamClass);
      ActionResult<ActionAttachment> actionResult = dockingService
          .execute(actionParams);
      /**
      * @Description 解析返回参数
      **/
      Map<String, Object> outData = APIDockingGatewayDataUtil.parseAndTransform(actionResult);

      /**
      * @Description 根据返回code 查询对应提示语 新增了云控接口 兼容云控提示语
      **/
      String retCode = (String) outData.get(APIDefinitionConstants.CFN_RET_CODE);
      outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
      if(StringUtils.isEmpty(dataCache.getErrorMsg(retCode))){
        outData.put(APIDefinitionConstants.CFN_RET_MSG,outData.get(APIDefinitionConstants.CFN_RET_MSG));
      }else{
        outData.put(APIDefinitionConstants.CFN_RET_MSG,dataCache.getErrorMsg(retCode));
      }
      /**
      * @Description 返回加密数据
      **/
      outData.put(APIDefinitionConstants.CFN_TIMESTAMP,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

      if(outData.get(APIDefinitionConstants.SERIAL_NUMBER)==null){
        outData.put(APIDefinitionConstants.SERIAL_NUMBER,UUID.randomUUID().toString().replaceAll("-",""));
      }

      toSignMap = APIDockingGatewayDataUtil.toSignMap(outData);
      String sign = generateSign(toSignMap, signType, dockingConfig.getSignGenerationKey());
      outData.put(APIDefinitionConstants.CFN_SIGN, sign);

      return outData;
    } finally {
      apiDockingManager.releaseFlux(merchantId, apiKey);
    }
  }

  @Override
  public Map<String, Object> signagreementApiHandle(APIDockingAccesserProfile profile,
      Map<String, Object> inData) throws APIDockingException {
    String merchantId = profile.getMerchantId();
    String partnerId = profile.getPartnerId();
    String apiKey = profile.getApiKey();

    MerchantAPIDockingConfig dockingConfig = checkAccessAuthorizationAndGetAPIDockingConfig(
        merchantId, partnerId,
        apiKey, profile.getAccesserIP());

    try {
      String signType = dockingConfig.getSignType();

      Action<ActionParams, ActionAttachment> dockingService = apiDockingManager
          .getDockingService(apiKey);

      Type genericType;
      Type[] genericInterfaces = dockingService.getClass().getGenericInterfaces();
      if (genericInterfaces == null || genericInterfaces.length == 0) {
        genericType = dockingService.getClass().getGenericSuperclass();
      } else {
        genericType = genericInterfaces[0];
      }

      Class<? extends ActionParams> exactalParamClass = (Class<? extends ActionParams>) ((ParameterizedType) genericType)
          .getActualTypeArguments()[0];
      ActionResult<ActionAttachment> actionResult = dockingService
          .execute((ActionParams) APIDockingGatewayDataUtil
              .checkAndTransform(inData, exactalParamClass));
      Map<String, Object> outData = APIDockingGatewayDataUtil.parseAndTransform(actionResult);

      String retCode = (String) outData.get(APIDefinitionConstants.CFN_RET_CODE);
      outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
      outData.put(APIDefinitionConstants.CFN_RET_MSG, dataCache.getErrorMsg(retCode));
      outData.put(APIDefinitionConstants.CFN_TIMESTAMP,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

      Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(outData);
      String sign = generateSign(toSignMap, signType, dockingConfig.getSignGenerationKey());
      outData.put(APIDefinitionConstants.CFN_SIGN, sign);

      return outData;
    } finally {
      apiDockingManager.releaseFlux(merchantId, apiKey);
    }
  }

  @Override
  public Map<String, Object> batchHandle(APIDockingAccesserProfile profile,
      Map<String, Object> inData)
      throws APIDockingException {

    String merchantId = profile.getMerchantId();
    String partnerId = profile.getPartnerId();
    String batchApiKey = profile.getApiKey();

    String batchNo = (String) inData.get(APIDefinitionConstants.CFN_BATCH_NO);
    String transferCorpId = (String) inData.get(APIDefinitionConstants.CFN_TRANSFER_CORP_ID);
    String timestamp = (String) inData.get(APIDefinitionConstants.CFN_TIMESTAMP);
    String sign = (String) inData.get(APIDefinitionConstants.CFN_SIGN);
    byte[] fileBytes = (byte[]) inData.get(APIDefinitionConstants.CFN_BATCH_DATA);

    MerchantAPIDockingConfig dockingConfig = checkAccessAuthorizationAndGetAPIDockingConfig(
        merchantId, partnerId,
        batchApiKey, profile.getAccesserIP());
    try {
      String signType = dockingConfig.getSignType();
      checkSign(fileBytes, profile.getMerchantId(), batchNo, timestamp, sign, signType,
          dockingConfig.getSignVerificationKey());

      String apiKey = getSingleApiKey(batchApiKey);
      Action<ActionParams, ActionAttachment> dockingService = apiDockingManager
          .getDockingService(apiKey);

      BatchTransferRequestDealer dealer = requestDealers.getDealerFor(merchantId, partnerId);

      Map<String, Object> outData = new HashMap<String, Object>();
      String retCode = CommonRetCodes.ACTION_DONE.getCode();
      try {
        BatchDealResult batchResult = dealer
            .batchSubmit(profile.getMerchantId(), profile.getPartnerId(),
                dockingConfig.getNotifyUrl(), batchNo, transferCorpId, fileBytes, dockingService,
                apiTransferBatchDao, statusNotifier);

        outData.put(APIDefinitionConstants.CFN_RET_DATA,
            APIDockingGatewayDataUtil.parseAndTransform(batchResult));
      } catch (APIDockingException e) {
        throw e;
      } catch (Exception e) {
        logger.error("error occured in batch submit", e);
        retCode = CommonRetCodes.UNEXPECT_ERROR.getCode();
      }

      outData.put(APIDefinitionConstants.CFN_RET_CODE, retCode);
      outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
      outData.put(APIDefinitionConstants.CFN_RET_MSG, dataCache.getErrorMsg(retCode));
      outData.put(APIDefinitionConstants.CFN_TIMESTAMP,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

      Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(outData);
      String retSign = generateSign(toSignMap, signType, dockingConfig.getSignGenerationKey());
      outData.put(APIDefinitionConstants.CFN_SIGN, retSign);
      return outData;
    } finally {
      apiDockingManager.releaseFlux(merchantId, batchApiKey);
    }
  }

  private String getSingleApiKey(String batchApiKey) {
    return batchApiKey.substring(BATCH_API_KEY_PREFIX.length());
  }

  private String generateSign(Map<String, Object> mapData, String signType,
      String signGenerationKey) {
    SignWorker generator = signWorkers.get(signType);
    try {
      return generator.generateSign(mapData, signGenerationKey);
    } catch (Exception e) {
      throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
    }
  }

  private void checkSign(byte[] fileBytes, String merchantId, String batchNo, String timestamp,
      String sign,
      String signType, String verificationKey) {

    if (sign == null || "".equals(sign)) {
      throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), "sign is lack");
    }

    SignWorker verifier = signWorkers.get(signType);
    boolean signVerified = false;
    try {
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      byteBuffer.write(fileBytes);
      byteBuffer.write(merchantId.getBytes());
      byteBuffer.write(batchNo.getBytes());
      byteBuffer.write(timestamp.getBytes());

      signVerified = verifier.verifySign(byteBuffer.toByteArray(), verificationKey, sign);
    } catch (Exception e) {
      throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
    }
    if (!signVerified) {
      throw new APIDockingException(APIDockingRetCodes.SIGN_ERROR.getCode(), "sign is wrong");
    }
  }

  /**
   * @Description 按照规则解析map 根据签名方效验商户传过来的秘钥
   **/
  private void checkSign(Map<String, Object> mapData, String signType, String verificationKey) {
    String sign = (String) mapData.remove(APIDefinitionConstants.CFN_SIGN);
    if (sign == null || "".equals(sign)) {
      throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), "sign is lack");
    }
    /**
    * @Description 获取配置文件签名方式
    **/
    SignWorker verifier = signWorkers.get(signType);
    boolean signVerified = false;
    try {
      /**
      * @Description 效验签名
      **/
      signVerified = verifier.verifySign(mapData, verificationKey, sign);
    } catch (Exception e) {
      throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), e.getMessage());
    }
    if (!signVerified) {
      throw new APIDockingException(APIDockingRetCodes.SIGN_ERROR.getCode(), "sign is wrong");
    }
  }

  private MerchantAPIDockingConfig checkAccessAuthorizationAndGetAPIDockingConfig(String merchantId,
      String partnerId,
      String apiKey, String accessIP) {

    if (merchantId == null || "".equals(merchantId)) {
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(),
          "merchant id is null");
    }

    MerchantAPIDockingConfig dockingConfig = apiDockingManager
        .getMerchantAPIDockingConfig(merchantId);
    if (APIDockingMode.CLOSED.equals(dockingConfig.getAPIDockingMode())) {
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(),
          "api docking mode is closed");
    }

    if (!dockingConfig.checkIPAccessable(accessIP)) {
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(),
          "ip[" + accessIP + "] is not in the specified acceptable ip white list");
    }

    Integer accessableConcurrentCount = dockingConfig.checkInterfaceAccessable(apiKey);
    if (accessableConcurrentCount == null) {
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(),
          "api[" + apiKey + "] is not open");
    }

    if (!apiDockingManager.applyFluxFor(merchantId, apiKey, accessableConcurrentCount)) {
      throw new APIDockingException(APIDockingRetCodes.ACCESS_BEYOND_LIMIT.getCode(),
          "api[" + apiKey + "] access is beyond limit");
    }

    return dockingConfig;
  }

  private void filterIgnoreSignFields(Map<String,Object> toSignMap,Class exactalParamClass){

      List<String> ignoreList=getIgnoreSignFileds(exactalParamClass);
      if(ignoreList==null){
        return;
      }
      for(String fieldName:ignoreList){
        toSignMap.remove(fieldName);
      }
  }

  private List<String> getIgnoreSignFileds(Class exactalParamClass){
    if(CACHED_IGNORE_SIGN_MAPPING.containsKey(exactalParamClass)){
      return CACHED_IGNORE_SIGN_MAPPING.get(exactalParamClass);
    }

    Field[] fields= exactalParamClass.getDeclaredFields();
    if(fields==null){
      return Collections.emptyList();
    }

    List<String> ignoreList=new ArrayList<>();
    for(Field field:fields){
      if(field.getAnnotation(IgnoreSign.class)!=null){
        ignoreList.add(field.getName());
      }
    }
    CACHED_IGNORE_SIGN_MAPPING.put(exactalParamClass,ignoreList);
    return ignoreList;
  }



}
