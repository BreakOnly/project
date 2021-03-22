package com.jrmf.service;

import com.alibaba.excel.EasyExcel;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelCustomCatalog;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CompanyPayment;
import com.jrmf.domain.CustomMenu;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.Parameter;
import com.jrmf.payment.service.ConfirmGrantService2;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomMenuDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.taxsettlement.api.TaxSettlementInertnessDataCache;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SMSSendUtils;
import com.jrmf.utils.SalaryConfigUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.dto.InputAliBatchData;
import com.jrmf.utils.dto.InputBankBatchData;
import com.jrmf.utils.dto.InputBatchData;
import com.jrmf.utils.eazyexcel.read.listener.InputBatchListener;
import com.jrmf.utils.exception.LoginException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: zhangzehui
 * @date: 2018-8-18
 * @description:
 */
@Service("channelCustomService")
public class ChannelCustomServiceImpl implements ChannelCustomService {

  private static Logger logger = LoggerFactory.getLogger(ChannelCustomService.class);

  @Autowired
  private ChannelCustomDao customDao;
  @Autowired
  private CustomMenuDao customMenuDao;
  @Autowired
  private CustomGroupDao customGroupDao;
  @Autowired
  private CustomProxyDao customProxyDao;
  @Autowired
  private ChannelRelatedDao channelRelatedDao;
  @Autowired
  private TaxSettlementInertnessDataCache taxSettlementInertnessDataCache;

  @Autowired
  private OemConfigService oemConfigService;

  @Autowired
  private ParameterService parameterService;

  @Autowired
  private ConfirmGrantService2 confirmGrantService;
  @Autowired
  private SalaryConfigUtil conf;
  @Autowired
  private ChannelRelatedService channelRelatedService;
  @Autowired
  private OrganizationTreeService organizationTreeService;

  @Override
  public ChannelCustom getCustomByCustomkey(String customkey) {
    return customDao.getCustomByCustomkey(customkey, null);
  }

  @Override
  public Map<String, Object> getCustomByCustomkeyMap(String customkey) {
    return customDao.getCustomByCustomkeyMap(customkey);
  }

  @Override
  public void updateCustomById(ChannelCustom custom) {
    customDao.updateCustomById(custom);
  }

  @Override
  public ChannelCustom getCustomById(int id) {
    return customDao.getCustomById(id);
  }

  @Override
  public ChannelCustom customUserLogin(String userName, String password) {
    return customDao.customUserLogin(userName, password);
  }

  @Override
  public void updatePassword(int id, String password) {
    customDao.updatePassword(id, password);
  }

  @Override
  public void updateTranPassword(int id, String tranPassword) {
    customDao.updateTranPassword(id, tranPassword);
  }

  @Override
  public void enabledCustom(Integer id, String enabled) {
    customDao.enabledCustom(id, enabled);
  }

  @Override
  public void checkCustom(Integer id, String enabled, String addAccount) {
    customDao.checkCustom(id, enabled, addAccount);
  }

  @Override
  public int getCustomCount(Map<String, Object> paramMap) {
    return customDao.getCustomCount(paramMap);
  }

  @Override
  public List<ChannelCustom> getCustomList(Map<String, Object> paramMap) {
    return customDao.getCustomList(paramMap);
  }

    @Override
    public ChannelCustom customUsername(String userName) {
        return customDao.customUsername(userName);
    }

  @Override
  public void saveChannelCustom(ChannelCustom custom) {
    customDao.saveChannelCustom(custom);
    try {
      taxSettlementInertnessDataCache.init();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  @Override
  public void deleteById(String id) {
    customDao.deleteById(id);
  }

  @Override
  public ChannelConfig getChannelConfigByParam(Map<String, Object> param) {
    return customDao.getChannelConfigByParam(param);
  }

  @Override
  public List<ChannelConfig> getChannelConfigListByParam(Map<String, Object> param) {
    return customDao.getChannelConfigListByParam(param);
  }

  @Override
  public void insertCompanyAccountDetail(ChannelConfig channelConfig) {
    customDao.insertCompanyAccountDetail(channelConfig);
  }

  @Override
  public void updateCompanyAccountDetail(ChannelConfig channelConfig) {
    customDao.updateCompanyAccountDetail(channelConfig);
  }

  @Override
  public List<ChannelCustom> getCustomListExRoot(Map<String, Object> paramMap) {
    return customDao.getCustomListExRoot(paramMap);
  }

  @Override
  public List<ChannelCustomCatalog> getCustomContentByCustomId(String originalId, String level) {
    return null;
  }

  @Override
  public List<CustomMenu> getAllPermission(String originalId) {
    return customMenuDao.getAllPermission(originalId);
  }

  @Override
  public List<CustomMenu> getCustomMenuByOriginalId(Map<String, Object> param) {
    return customMenuDao.getCustomMenuByOriginalId(param);
  }

  @Override
  public CustomMenu getCustomMenuById(int id) {
    return customMenuDao.getCustomMenuById(id);
  }

  @Override
  public int savePermission(CustomMenu munu) {
    return customMenuDao.savePermission(munu);
  }

  @Override
  public void updatePermission(CustomMenu munu) {
    customMenuDao.updatePermission(munu);
  }

  @Override
  public List<CustomMenu> getCustomMenuByName(String originalId, String contentName) {
    return customMenuDao.getCustomMenuByName(originalId, contentName);
  }

  @Override
  public List<CustomMenu> getCustomMenuList(Map<String, Object> param) {
    return customMenuDao.getCustomMenuList(param);
  }

  @Override
  public List<CustomMenu> getNodeTree(Map<String, Object> param) {
    return customMenuDao.getNodeTree(param);
  }

  @Override
  public int deleteNodeById(String id) {
    return customMenuDao.deleteNodeById(id);
  }

  @Override
  public List<ChannelCustom> getChannelCustemGroup(String customkey) {
    return customDao.getChannelCustemGroup(customkey);
  }

  @Override
  public List<CompanyPayment> getPaymentList(Map<String, Object> param, boolean defaultPayment) {
    if (defaultPayment) {
      return customDao.getPaymentListDefault(param);
    } else {
      return customDao.getPaymentList(param);
    }
  }

  /**
   * 根据商户key获取服务公司信息。
   *
   * @return originalId, companyId, merchantId, companyName
   */
  @Override
  public List<Map<String, Object>> getPaymentListByOriginalId(String originalId) {
    return customDao.getPaymentListByOriginalId(originalId);
  }

  /**
   * 查询商户列表，customType 不传，取customType = 1,3,5
   *
   * @return 商户列表
   */
  @Override
  public List<ChannelCustom> getListCustom(Map<String, Object> paramMap) {
    return customDao.getListCustom(paramMap);
  }

  /**
   * @return 查询商户列表明细
   */
  @Override
  public List<ChannelCustom> getListCustomDetail(Map<String, Object> paramMap) {
    return customDao.getListCustomDetail(paramMap);
  }

  /**
   * @return 查询商户列表, 传入customkey 字符串（字符串拼接）
   */
  @Override
  public List<ChannelCustom> listCustomByCustomKeys(Map<String, Object> paramMap) {
    return customDao.listCustomByCustomKeys(paramMap);
  }

  @Override
  public int enableOrganizationRelation(String customkey) {
    return customGroupDao.enable(customkey) + customProxyDao.enable(customkey);
  }

  @Override
  public List<Map<String, Object>> queryGroupInfoByParams(Map<String, Object> params) {
    return customDao.queryGroupInfoByParams(params);
  }

  @Override
  public List<Map<String, Object>> queryProxyInfoByParams(Map<String, Object> params) {
    return customDao.queryProxyInfoByParams(params);
  }

  @Override
  public void updateCustomTransFerPassword(String customKey, String password) {
    customDao.updateCustomTransFerPassword(customKey, password);
  }

  @Override
  public ChannelCustom getCustomByOfficialAccOpenId(String officialAccOpenId) {
    return customDao.getCustomByOfficialAccOpenId(officialAccOpenId);
  }

  @Override
  public List<ChannelCustom> getAllCustom() {
    return customDao.getAllCustom();
  }

  @Override
  public List<ChannelCustom> queryGroupCustom(String customKeys) {
    return customDao.queryGroupCustom(customKeys);
  }

  /**
   * 根据参数  查询  ChannelCustom  sql中没有的 ，可以自行添加
   *
   * @param params 现有 umfId（联动优势id） customType（商户类别） umfIdLength（umfId不为空）
   * @return ChannelCustom List
   */
  @Override
  public List<ChannelCustom> getCustomByParam(Map<String, Object> params) {
    return customDao.getCustomByParam(params);
  }

  @Override
  public Map<String, Object> inputBatchInfoNew(Integer respstat,
      String operatorName,
      String nodeCustomKey,
      String menuId,
      String payType,
      String companyId,
      String batchName,
      String batchDesc,
      InputStream is,
      String fileName,
      String taskAttachmentFile,
      Map<String, Object> result,
      String realCompanyId) {
    ChannelRelated channelRelated = channelRelatedService
        .getRelatedByCompAndOrig(nodeCustomKey, companyId);
    if (channelRelated == null) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "未配置服务公司！");
      return result;
    }
    if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName) && StringUtil
        .isEmpty(batchDesc)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "请补全批次信息！");
      return result;
    }
    CompanyPayment paymentList = customDao.getByCompanyIdAndPayType(companyId, payType);
    if (paymentList == null) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "该服务公司不支持当前下发通道！");
      return result;
    }

    ByteArrayOutputStream bytesOut = null;
    InputStream saveFileInputStream = null;
    try {

      InputBatchListener<InputBatchData> batchListener = new InputBatchListener<>();
      EasyExcel.read(is, InputBankBatchData.class, batchListener).sheet().doRead();

      List<InputBatchData> bankBatchDataList = batchListener.getList();

      String confCardTemple1;
      String confCardTemple2;
      String cardpayTemple;
      if (bankBatchDataList == null || bankBatchDataList.size() < 1) {
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
        return result;
      } else {
        InputBankBatchData templateRow = (InputBankBatchData) bankBatchDataList.get(0);
        cardpayTemple = templateRow.toValueString();
        confCardTemple1 = conf.getBankTempleFormat1();
        confCardTemple2 = conf.getBankTempleFormat2();
        logger.info("-------导入模板头：" + cardpayTemple);
        logger.info("-------配置模板头1：" + confCardTemple1);
        logger.info("-------配置模板头2：" + confCardTemple2);
        if ("aiyuangong".equals(channelRelated.getMerchantId())) {
          // 爱员工
          if (!cardpayTemple.equals(confCardTemple2)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
            return result;
          }
        } else {
          // 金财
          if (!cardpayTemple.equals(confCardTemple1) && !cardpayTemple.equals(confCardTemple2)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
            return result;
          }
        }
      }
      Map<String, String> batchaData = new HashMap<>();
      batchaData.put("operatorName", operatorName);
      batchaData.put("batchName", batchName);
      batchaData.put("menuId", menuId);
      batchaData.put("companyId", companyId);
      batchaData.put("batchDesc", batchDesc);
      batchaData.put("customkey", nodeCustomKey);
      batchaData.put("payType", payType);
      batchaData.put("fileName", fileName);
//            batchaData.put("fileUrl", uploadPath + uploadFileName);
      batchaData.put("template", cardpayTemple.equals(confCardTemple1) ? "1" : "2");
      batchaData.put("taskAttachmentFile", taskAttachmentFile);
      batchaData.put("realCompanyId",realCompanyId);
      /**
       * 处理批次信息
       */
      result = confirmGrantService
          .inputCommissionData(Integer.valueOf(payType), bankBatchDataList, batchaData);

    } catch (Exception e) {
      respstat = RespCode.error101;
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入失败");
      return result;
    } finally {
      try {
        if (bytesOut != null) {
          bytesOut.close();
        }
        if (is != null) {
          is.close();
        }
        if (saveFileInputStream != null) {
          saveFileInputStream.close();
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    logger.info("返回结果：" + result);
    return result;
  }

  @Override
  @Deprecated
  public Map<String, Object> inputBatchInfo(Integer respstat, String operatorName, String customkey,
      String menuId, String payType, String companyId, String batchName, String batchDesc,
      InputStream is, String fileName, Map<String, Object> result) {
    ChannelRelated channelRelated = channelRelatedService
        .getRelatedByCompAndOrig(customkey, companyId);
    if (channelRelated == null) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "未配置服务公司！");
      return result;
    }
    if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName) && StringUtil
        .isEmpty(batchDesc)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "请补全批次信息！");
      return result;
    }
    String confCardTemple1;
    String confCardTemple2;
    String cardpayTemple;
    Workbook workbook;
    ByteArrayOutputStream bytesOut = null;
    InputStream saveFileInputStream = null;
    try {

      int readLen;
      byte[] byteBuffer = new byte[1024];
      bytesOut = new ByteArrayOutputStream();

      while ((readLen = is.read(byteBuffer)) > -1) {
        bytesOut.write(byteBuffer, 0, readLen);
      }
      byte[] fileData = bytesOut.toByteArray();

      try {
        workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
      } catch (Exception ex) {
        workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
      }

      Sheet sheet = workbook.getSheetAt(0);
      XSSFRow row = (XSSFRow) sheet.getRow(1);
      if (row == null) {
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
        return result;
      } else {
        cardpayTemple =
            StringUtil.getXSSFCell(row.getCell(0))
                + StringUtil.getXSSFCell(row.getCell(1))
                + StringUtil.getXSSFCell(row.getCell(2))
                + StringUtil.getXSSFCell(row.getCell(3))
                + StringUtil.getXSSFCell(row.getCell(4))
                + StringUtil.getXSSFCell(row.getCell(5))
                + StringUtil.getXSSFCell(row.getCell(6))
                + StringUtil.getXSSFCell(row.getCell(7))
                + StringUtil.getXSSFCell(row.getCell(8));
        confCardTemple1 = conf.getBankTempleFormat1();
        confCardTemple2 = conf.getBankTempleFormat2();
        logger.info("-------导入模板头：" + cardpayTemple);
        logger.info("-------配置模板头1：" + confCardTemple1);
        logger.info("-------配置模板头2：" + confCardTemple2);
        if ("aiyuangong".equals(channelRelated.getMerchantId())) {
          // 爱员工
          if (!cardpayTemple.equals(confCardTemple2)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
            return result;
          }
        } else {
          // 金财
          if (!cardpayTemple.equals(confCardTemple1) && !cardpayTemple.equals(confCardTemple2)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
            return result;
          }
        }
      }

      String uploadPath = "/batchFile/" + customkey + "/" + companyId + "/";
      String uploadFileName = System.currentTimeMillis() + fileName;
      saveFileInputStream = new ByteArrayInputStream(fileData);
      boolean state = FtpTool.uploadFile(uploadPath, uploadFileName, saveFileInputStream);

      if (!state) {
        logger.error("--------批次导入源文件保存失败--------");
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.EXPORT_FAILIURE);
        return result;
      }

      Map<String, String> batchaData = new HashMap<>();
      batchaData.put("operatorName", operatorName);
      batchaData.put("batchName", batchName);
      batchaData.put("menuId", menuId);
      batchaData.put("companyId", companyId);
      batchaData.put("batchDesc", batchDesc);
      batchaData.put("customkey", customkey);
      batchaData.put("payType", payType);
      batchaData.put("fileName", fileName);
      batchaData.put("fileUrl", uploadPath + uploadFileName);
      batchaData.put("template", cardpayTemple.equals(confCardTemple1) ? "1" : "2");

      /**
       * 处理批次信息
       */
      result = confirmGrantService.inputCommissionData(Integer.valueOf(payType), null, batchaData);

    } catch (Exception e) {
      logger.error("", e);
      respstat = RespCode.error101;
      logger.error(e.getMessage());
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入失败");
      return result;
    } finally {
      try {
        if (bytesOut != null) {
          bytesOut.close();
        }
        if (is != null) {
          is.close();
        }
        if (saveFileInputStream != null) {
          saveFileInputStream.close();
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    logger.info("返回结果：" + result);
    return result;
  }


  @Override
  public Map<String, Object> alipayInputBatchInfoNew(Integer respstat, String operatorName,
      String customkey, String menuId, Integer payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, Map<String, Object> result,String realCompanyId) {

    if (PayType.ALI_PAY.getCode() != payType) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入文件与支付通道不一致");
      return result;
    }

    if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName)
        && StringUtil.isEmpty(batchDesc)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "请补全批次信息！");
      return result;
    }

    CompanyPayment paymentList = customDao.getByCompanyIdAndPayType(companyId, payType.toString());
    if (paymentList == null) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "该服务公司不支持当前下发通道！");
      return result;
    }

    ByteArrayOutputStream bytesOut = null;
    InputStream saveFileInputStream = null;
    try {

      InputBatchListener<InputBatchData> batchListener = new InputBatchListener<>();
      EasyExcel.read(is, InputAliBatchData.class, batchListener).sheet().doRead();

      List<InputBatchData> bankBatchDataList = batchListener.getList();

      if (bankBatchDataList == null || bankBatchDataList.size() < 1) {
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
        return result;
      } else {
        InputAliBatchData templateRow = (InputAliBatchData) bankBatchDataList.get(0);
        String alipayTemple = templateRow.toValueString();
        String confAliTemple = conf.getAlipayTempleFormat();
        logger.info("-------导入模板头：" + alipayTemple);
        logger.info("-------配置模板头：" + confAliTemple);
        if (!alipayTemple.equals(confAliTemple)) {
          respstat = RespCode.error101;
          result.put(RespCode.RESP_STAT, respstat);
          result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
          return result;
        }
      }

//            String uploadPath = "/batchFile/" + customkey + "/" + companyId + "/";
//            String uploadFileName = System.currentTimeMillis() + fileName;

//            int readLen;
//            byte[] byteBuffer = new byte[1024];
//            bytesOut = new ByteArrayOutputStream();
//
//            while ((readLen = is.read(byteBuffer)) > -1) {
//                bytesOut.write(byteBuffer, 0, readLen);
//            }
//            byte[] fileData = bytesOut.toByteArray();
//
//            saveFileInputStream = new ByteArrayInputStream(fileData);
//            boolean state = FtpTool.uploadFile(uploadPath, uploadFileName, saveFileInputStream);
//
//            if (!state) {
//                logger.error("--------批次导入源文件保存失败--------");
//                respstat = RespCode.error101;
//                result.put(RespCode.RESP_STAT, respstat);
//                result.put(RespCode.RESP_MSG, RespCode.EXPORT_FAILIURE);
//                return result;
//            }

      Map<String, String> batchData = new HashMap<>();
      batchData.put("operatorName", operatorName);
      batchData.put("batchName", batchName);
      batchData.put("menuId", menuId);
      batchData.put("fileName", fileName);
      batchData.put("batchDesc", batchDesc);
      batchData.put("customkey", customkey);
      batchData.put("companyId", companyId);
      batchData.put("realCompanyId",realCompanyId);

      result = confirmGrantService.inputCommissionData(payType, bankBatchDataList, batchData);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入失败");
      return result;
    } finally {
      try {
        if (bytesOut != null) {
          bytesOut.close();
        }
        if (is != null) {
          is.close();
        }
        if (saveFileInputStream != null) {
          saveFileInputStream.close();
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    logger.info("返回结果：" + result);
    return result;
  }

  @Override
  @Deprecated
  public Map<String, Object> alipayInputBatchInfo(Integer respstat, String operatorName,
      String customkey, String menuId, Integer payType, String companyId, String batchName,
      String batchDesc, InputStream is, String fileName, Map<String, Object> result) {

    if (payType != 2) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入文件与支付通道不一致");
      return result;
    }

    if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName)
        && StringUtil.isEmpty(batchDesc)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "请补全批次信息！");
      return result;
    }

    Workbook workbook = null;
    ByteArrayOutputStream bytesOut = null;
    try {

      int readLen = -1;
      byte[] byteBuffer = new byte[1024];
      bytesOut = new ByteArrayOutputStream();

      while ((readLen = is.read(byteBuffer)) > -1) {
        bytesOut.write(byteBuffer, 0, readLen);
      }
      byte[] fileData = bytesOut.toByteArray();

      try {
        workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
      } catch (Exception ex) {
        workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
      }

      Sheet sheet = workbook.getSheetAt(0);
      XSSFRow row = (XSSFRow) sheet.getRow(1);
      if (row == null) {
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
        return result;
      } else {
        String alipayTemple = StringUtil.getXSSFCell(row.getCell(0))
            + StringUtil.getXSSFCell(row.getCell(1))
            + StringUtil.getXSSFCell(row.getCell(2))
            + StringUtil.getXSSFCell(row.getCell(3))
            + StringUtil.getXSSFCell(row.getCell(4))
            + StringUtil.getXSSFCell(row.getCell(5))
            + StringUtil.getXSSFCell(row.getCell(6));
        String confAliTemple = conf.getAlipayTempleFormat();
        logger.info("-------导入模板头：" + alipayTemple);
        logger.info("-------配置模板头：" + confAliTemple);
        if (!alipayTemple.equals(confAliTemple)) {
          respstat = RespCode.error101;
          result.put(RespCode.RESP_STAT, respstat);
          result.put(RespCode.RESP_MSG, "导入的数据模板格式不正确，请重新下载模板");
          return result;
        }
      }

      Map<String, String> batchData = new HashMap<>();
      batchData.put("operatorName", operatorName);
      batchData.put("batchName", batchName);
      batchData.put("menuId", menuId);
      batchData.put("fileName", fileName);
      batchData.put("batchDesc", batchDesc);
      batchData.put("customkey", customkey);
      batchData.put("companyId", companyId);

      result = confirmGrantService.inputCommissionData(payType, null, batchData);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "导入失败");
      return result;
    } finally {
      try {
        if (bytesOut != null) {
          bytesOut.close();
        }
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    logger.info("返回结果：" + result);
    return result;
  }

  @Override
  public int removeProxyTypeById(int id) {
    return customDao.removeProxyTypeById(id);
  }

  /**
   * 查询所有签约的商户列表
   *
   * @param channelCustom 当前登录者
   * @return 商户列表
   */
  @Override
  public List<ChannelCustom> getAgreementMerchants(ChannelCustom channelCustom) {
    String masterCustom = channelCustom.getMasterCustom();
    if (channelCustom.getCustomType() == CustomType.ROOT.getCode() && !StringUtil
        .isEmpty(masterCustom)) {
      channelCustom = customDao.getCustomByCustomkey(masterCustom, null);
    }
    int customType = channelCustom.getCustomType();
    String customTypeStr = "125";
    if (!customTypeStr.contains("" + customType)) {
      throw new LoginException("没有权限");
    }
    Map<String, Object> hashMap = new HashMap<>(2);
    List<ChannelCustom> listCustom;

    if (customType == CustomType.COMPANY.getCode()) {
      List<String> customKeys = channelRelatedDao
          .queryCustomKeysByCompanyId(channelCustom.getCustomkey());
      if (customKeys == null || customKeys.size() == 0) {
        throw new LoginException("没有配置商户");
      }
      listCustom = queryGroupCustom(String.join(",", customKeys));
    } else {
      if (!CommonString.ROOT.equals(channelCustom.getCustomkey())) {
        String customKey = channelCustom.getCustomkey();
        if (channelCustom.getCustomType() == CustomType.ROOT.getCode()) {
          customKey = channelCustom.getMasterCustom();
        }
        hashMap.put("customkey", customKey);
      }
      listCustom = getListCustom(hashMap);
    }
    return listCustom;
  }

  /**
   * 查询所有该商户的服务公司
   *
   * @param channelCustom 当前登录者
   * @param customKey 选择的商户key
   * @return 服务公司列表
   */
  @Override
  public List<ChannelCustom> getAgreementCompanies(ChannelCustom channelCustom, String customKey) {
    String masterCustom = channelCustom.getMasterCustom();
    if (channelCustom.getCustomType() == CustomType.ROOT.getCode() && !StringUtil
        .isEmpty(masterCustom)) {
      channelCustom = customDao.getCustomByCustomkey(masterCustom, null);
    }
    int customType = channelCustom.getCustomType();
    String customTypeStr = "125";
    if (!customTypeStr.contains("" + customType)) {
      throw new LoginException("没有权限");
    }
    List<ChannelCustom> listCustom = new ArrayList<>();

    if (customType == CustomType.COMPANY.getCode()) {
      listCustom.add(channelCustom);
    } else {
      List<ChannelRelated> relatedList = channelRelatedDao.getRelatedList(customKey);
      if (relatedList.isEmpty()) {
        throw new LoginException("没有配置服务公司");
      }
      List<String> customKeys = new ArrayList<>();
      for (ChannelRelated channelRelated : relatedList) {
        String companyId = channelRelated.getCompanyId();
        customKeys.add(companyId);
      }
      listCustom = queryGroupCustom(String.join(",", customKeys));
    }
    return listCustom;
  }

  @Override
  public List<Map<String, Object>> queryCustom(String customKeys) {
    return customDao.queryCustom(customKeys);
  }

  @Override
  public ChannelCustom getBusinessInfoByCustomkey(String customkey) {
    return customDao.getBusinessInfoByCustomkey(customkey);
  }

  @Override
  public String getCustomKeysByLoginMerchant(ChannelCustom channelCustom) {
    String customKeys = "";
    if (channelCustom.getCustomType() == 4 && channelCustom.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customDao
          .getCustomByCustomkey(channelCustom.getMasterCustom(), null);
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT
              .equals(masterChannelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        //超管
      } else if (masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        customKeys = masterChannelCustom.getCustomkey();
      } else if (masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
        List<String> customKeyList = organizationTreeService
            .queryNodeCusotmKey(masterChannelCustom.getCustomType(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        customKeys = String.join(",", customKeyList);
      } else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        List<String> stringList = channelRelatedService
            .queryCustomKeysByCompanyId(masterChannelCustom.getCustomkey());
        customKeys = Joiner.on(",").join(stringList);
      } else if (masterChannelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //判断是不是关联性代理商
        if (masterChannelCustom.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(masterChannelCustom.getCustomkey(), null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey, null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            customKeys = Joiner.on(",").join(customStringList);
            customKeys = String.join(",", customKeys);
          }
        } else {
          OrganizationNode node = customProxyDao
              .getNodeByCustomKey(masterChannelCustom.getCustomkey(), null);
          List<String> customStringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          customKeys = Joiner.on(",").join(customStringList);
          customKeys = String.join(",", customKeys);
        }
      }
    } else {
      if ((CommonString.ROOT.equals(channelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == channelCustom.getCustomType() && CommonString.ROOT
              .equals(channelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == channelCustom.getLoginRole()))) {
        //超管
      } else if (channelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        customKeys = channelCustom.getCustomkey();
      } else if (channelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(channelCustom.getCustomkey());
        List<String> customStringList = organizationTreeService
            .queryNodeCusotmKey(channelCustom.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        customKeys = String.join(",", customStringList);
      } else if (channelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        List<String> stringList = channelRelatedService
            .queryCustomKeysByCompanyId(channelCustom.getCustomkey());
        customKeys = Joiner.on(",").join(stringList);
      } else if (channelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //判断是不是关联性代理商
        if (channelCustom.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(channelCustom.getCustomkey(), null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey, null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            customKeys = Joiner.on(",").join(customStringList);
            customKeys = String.join(",", customKeys);
          }
        } else {
          OrganizationNode node = customProxyDao
              .getNodeByCustomKey(channelCustom.getCustomkey(), null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          customKeys = Joiner.on(",").join(stringList);
          customKeys = String.join(",", customKeys);
        }
      }
    }
    return customKeys;
  }

  /**
   * 通过邮箱查询商户信息
   */
  @Override
  public ChannelCustom getChannelCustomByEmail(String email) {
    return customDao.getChannelCustomByEmail(email);
  }

  @Override
  public ChannelCustom getChannelCustomByKey(String customKey) {
    return customDao.getChannelCustomByKey(customKey);
  }

  /**
   * 通过商户唯一标识查询商户信息
   */
  @Override
  public ChannelCustom getChannelCustomById(String customkey) {
    return customDao.getChannelCustomById(customkey);
  }

  /**
   * 设置登陆密码
   */
  @Override
  public void updateCustomPassword(Integer id, String password) {
    customDao.updateCustomPassword(id, password);
  }

  /**
   * 新增服务公司 添加channel_custom表
   */
  @Override
  public void insertChannelCustomInfo(ChannelCustom channelCustom) {
    customDao.insertChannelCustomInfo(channelCustom);
  }

  /**
   * 修改服务公司 修改channel_custom表
   */
  @Override
  public void updateChannelCustomInfo(ChannelCustom channelCustom) {
    customDao.updateChannelCustomInfo(channelCustom);
  }


  /**
   * 通过邮箱查询商户信息
   */
  @Override
  public ChannelCustom getChannelCustomEmail(String email) {
    return customDao.getChannelCustomEmail(email);
  }

  @Override
  public ChannelCustom getUserByUserNameAndOemUrl(String userName, String oemUrl) {
    int userCount = getCountByUserName(userName);
    ChannelCustom customUser = null;
    if (userCount == 1) {
      customUser = customDao.customUsername(userName);
    } else if (userCount > 1) {
      customUser = customDao.getUserByUserNameAndOemUrl(userName, oemUrl);
    }
    return customUser;
  }

  @Override
  public int getCountByUserName(String userName) {
    return customDao.getCountByUserName(userName);
  }


  /**
   * 校验是否有权限根据商户类型
   */
  @Override
  public boolean getCustomKeysByType(Map<String, String> paramsMap, Integer[] customTypes,
      ChannelCustom customLogin) {
    boolean checkFlag = false;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      customLogin = customDao.getCustomByCustomkey(customLogin.getMasterCustom(), null);
    }
    //校验是否有权限
    for (Integer customType : customTypes) {
      if (CommonString.ROOT.equals(customLogin.getCustomkey()) && customType == 6) {
        checkFlag = true;
        break;
      } else {
        if (customType == customLogin.getCustomType()) {
          checkFlag = true;
          break;
        }
      }
    }
    if (checkFlag) {
      if (CommonString.ROOT.equals(customLogin.getCustomkey())) {
        //超管
      } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
        //下发公司
        paramsMap.put("companyId", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
        List<String> customKeyList = organizationTreeService
            .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        String customKeys = String.join(",", customKeyList);
        paramsMap.put("customkey", customKeys);
      } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        paramsMap.put("customkey", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
        // 代理商
        paramsMap.put("customkey", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PLATFORM.getCode()) {
        paramsMap.put("businessPlatformId", String.valueOf(customLogin.getBusinessPlatformId()));
      }
    }
    return checkFlag;
  }

  /**
   * 通过商户名称查询商户信息
   */
  @Override
  public int getChannelcustomByCompanyName(String proxyName) {
    return customDao.getChannelcustomByCompanyName(proxyName);
  }

  /**
   * 通过customkey查询商户信息
   */
  @Override
  public int getchannelcustomByCustomkey(String customkey) {
    return customDao.getchannelcustomByCustomkey(customkey);
  }

  /**
   * 忘记密码->验证
   */
  @Override
  public Map<String, Object> forgetPassword(HttpServletRequest request, String userName,
      String password, String againPassword, String code, String phoneNo) {

    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    Integer id;
    ChannelCustom channelCustomByEmail = null;
    if (!StringUtil.isEmpty(userName)) {
      channelCustomByEmail = getUserByUserNameAndOemUrl(userName, request.getServerName());
      if (channelCustomByEmail != null) {
        id = channelCustomByEmail.getId();
      } else {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "账号不存在，请输入正确账号！");
        return result;
      }
    } else {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "请输入账号！");
      return result;
    }

    if (!StringUtil.isEmpty(phoneNo)) {
      if (!phoneNo.equals(channelCustomByEmail.getPhoneNo())) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "您输入的预留手机号与登录账号不匹配！");
        return result;
      }
    } else {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "请输入手机号！");
      return result;
    }

    if (StringUtil.isEmpty(code)) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "请输入验证码！");
      return result;
    } else {
      String sessionCode = (String) request.getSession().getAttribute("code");
      logger.info("sessionCode:" + sessionCode);
      if (sessionCode == null || !sessionCode.equalsIgnoreCase(code)) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "验证码错误！");
        return result;
      }
    }

    if (StringUtil.isEmpty(password) && StringUtil.isEmpty(againPassword)) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "请输入密码！");
      return result;
    } else {
      if (!password.equals(againPassword)) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "您输入的密码不匹配");
        return result;
      } else {
        if (password.indexOf(" ") != -1) {
          result.put(RespCode.RESP_STAT, RespCode.error101);
          result.put(RespCode.RESP_MSG, "密码存在非法字符！");
          return result;
        }
        request.getSession().setAttribute("password", password);
        request.getSession().setAttribute("phoneNo", phoneNo);
        request.getSession().setAttribute("id", id);
      }
    }

    return result;
  }

  /**
   * 忘记密码->修改密码
   */
  @Override
  public Map<String, Object> validMobileCodeAndForgetPassword(HttpServletRequest request,
      String code) {
    String password = (String) request.getSession().getAttribute("password");
    String phoneNo = (String) request.getSession().getAttribute("phoneNo");
    Integer id = (Integer) request.getSession().getAttribute("id");
    logger.info(
        "validMobileCode 方法： code=" + code + ", phoneNo=" + phoneNo + ", password=" + password
            + ", id=" + id);

    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(phoneNo) || StringUtil.isEmpty(password)
        || id == null) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "参数异常");
      return result;
    }

    try {
      code = code.trim();
      Parameter param;
      Map<String, Object> map = new HashMap<>(4);
      map.put("portalDomain", request.getServerName());
      OemConfig oemConfig = oemConfigService.getOemByParam(map);
      if (oemConfig.getSmsStatus() != 1) {
        param = new Parameter();
      } else {
        param = parameterService.valiMobiletelno(phoneNo, code, SMSSendUtils.VALI_MOBILE);
      }
      if (param == null) {
        result.put(RespCode.RESP_STAT, RespCode.error141);
        result.put(RespCode.RESP_MSG, "验证码错误");
        return result;
      }

      ChannelCustom customByCustomkey = customDao.getCustomById(id);
      String customkey = customByCustomkey.getCustomkey();
      if (StringUtil.isEmpty(customByCustomkey.getCustomkey())) {
        customkey = customByCustomkey.getMasterCustom();
      }
      String newPassword = CipherUtil.generatePassword(password, customkey);
      customDao.updateCustomPassword(id, newPassword);

    } catch (Exception e) {
      logger.error("验证验证码，修改密码异常：", e);
      result.put(RespCode.RESP_STAT, RespCode.error000);
      result.put(RespCode.RESP_MSG, "修改密码失败，请联系管理员！");
    }
    return result;
  }

  /**
   * 查询 账号配置管理
   */
  @Override
  public List<Map<String, Object>> queryAccount(Map<String, Object> param) {
    return customDao.queryAccount(param);
  }

  /**
   * 切换账号配置管理-> 根据商户角色类型查询商户
   */
  @Override
  public List<Map<String, Object>> getCustomByCustomType(String companyType) {
    return customDao.getCustomByCustomType(companyType);
  }

  /**
   * 根据customkey查询商户主键ID
   */
  @Override
  public String getCustomIdByCustomkey(String customkey) {
    return customDao.getCustomIdByCustomkey(customkey);
  }


  @Override
  public List<Map<String, Object>> getAllList(Map<String, Object> params) {
    return customDao.getAllList(params);
  }

  @Override
  public List<Map<String, Object>> getCustomByIds(Map<String, Object> paramMap) {
    return customDao.getCustomByIds(paramMap);
  }

  @Override
  public ChannelCustom getChannelCustomByCompanyName(String companyName) {
    return customDao.getChannelCustomByCompanyName(companyName);
  }

  /**
   * 查询类型为2的商户名称
   */
  @Override
  public boolean getChannelCustomCompanyNameByCustomType(String companyName) {
    return customDao.getChannelCustomCompanyNameByCustomType(companyName) > 0;
  }

  @Override
  public List<Map<String, Object>> queryCustom2(String customKeys) {
    return customDao.queryCustom2(customKeys);
  }

  @Override
  public ChannelConfig getChannelConfigByCustomKeyAndCompanyId(String companyId, Integer payType) {
    return customDao.getChannelConfigByCustomKeyAndCompanyId(companyId, payType);
  }

  @Override
  public List<HashMap> selectPlatformList() {

    return customDao.selectPlatformList();
  }

  /**
   * @return java.util.HashMap
   * @Author YJY
   * @Description 查询该平台名称
   * @Date 2020/9/23
   * @Param [customId]
   **/
  @Override
  public HashMap selectPlatformByCostomId(Integer customId) {
    return customDao.selectPlatformByCostomId(customId);
  }

  @Override
  public void updateSubAccountPlatformId(Integer businessPlatformId, String masterCustomKey) {
    customDao.updateSubAccountPlatformId(businessPlatformId, masterCustomKey);
  }

  @Override
  public List<String> getCustomKeysByLoginCustomExtent(ChannelCustom loginCustom) {

    List<String> customList = new ArrayList<>();

    ChannelCustom masterCustom = loginCustom;
    if (CustomType.ROOT.getCode() == loginCustom.getCustomType() && !StringUtil
        .isEmpty(loginCustom.getMasterCustom())) {
      masterCustom = this.getCustomByCustomkey(loginCustom.getMasterCustom());
    }

    if (CommonString.ROOT.equals(masterCustom.getCustomkey())) {
      customList.add(CommonString.ROOT);
      return customList;
    } else if (CustomType.CUSTOM.getCode() == masterCustom.getCustomType()) {
      customList.add(masterCustom.getCustomkey());
      return customList;
    } else if (CustomType.GROUP.getCode() == masterCustom.getCustomType()) {
      int nodeId = organizationTreeService.queryNodeIdByCustomKey(masterCustom.getCustomkey());
      return organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
              nodeId);
    } else if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
      return channelRelatedService
          .queryCustomKeysByCompanyId(masterCustom.getCustomkey());
    } else if (CustomType.PROXY.getCode() == masterCustom.getCustomType()) {
      //判断是不是关联性代理商
      if (masterCustom.getProxyType() == 1) {
        OrganizationNode node = customProxyDao
            .getProxyChildenNodeByCustomKey(masterCustom.getCustomkey(), null);
        List<String> stringList = organizationTreeService
            .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

        customList.addAll(stringList);
        if (stringList.size() > 0) {
          for (String customKey : stringList) {
            OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey, null);
            List<String> itemCustomStringList = organizationTreeService
                .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                    QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
            if (itemCustomStringList != null) {
              customList.addAll(itemCustomStringList);
            }
          }
        }
        return customList;
      } else {
        OrganizationNode node = customProxyDao
            .getNodeByCustomKey(masterCustom.getCustomkey(), null);
        return organizationTreeService
            .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                node.getId());
      }
    } else if (CustomType.PLATFORM.getCode() == masterCustom.getCustomType()) {
      Integer businessPlatformId = masterCustom.getId();
      return customDao.getCustomKeyByBusinessPlatformId(businessPlatformId);
    }

    return customList;
  }

  @Override
  public List<Map<String, Object>> queryAllCustom(String customKeys) {
    return customDao.queryAllCustom(customKeys);
  }
}
