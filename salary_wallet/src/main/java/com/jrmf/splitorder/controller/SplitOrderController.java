package com.jrmf.splitorder.controller;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.*;
import com.jrmf.persistence.SplitOrderConfDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.splitorder.domain.*;
import com.jrmf.splitorder.service.CustomService;
import com.jrmf.splitorder.service.CustomSplitOrderService;
import com.jrmf.splitorder.service.CustomSplitSuccessOrderService;
import com.jrmf.splitorder.service.SplitOrderService;
import com.jrmf.splitorder.util.FileUtil;
import com.jrmf.splitorder.util.SplitOrderThreadPool;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/split/order")
public class SplitOrderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SplitOrderController.class);

    @Autowired
    private SplitOrderService splitOrderServiceImpl;
    @Autowired
    private CustomService customServiceImpl;
    @Autowired
    private CustomSplitOrderService customSplitOrderService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private OrganizationTreeService organizationTreeService;
    @Autowired
    private CustomSplitSuccessOrderService customSplitSuccessOrderService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private SplitOrderConfDao splitOrderConfDao;
    @Autowired
    private final BestSignConfig bestSignConfig;

    public SplitOrderController(BestSignConfig bestSignConfig) {
        this.bestSignConfig = bestSignConfig;
    }

    @RequestMapping("/info")
    @ResponseBody
    public Map<String, Object> info(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                    String timeStart, String timeEnd, String customName, String fileName, Integer status, String splitOrderName) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        Map<String, Object> params = new HashMap<>();

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            if (LoginRole.OPERATE_ACCOUNT.getCode() == loginUser.getLoginRole()) {
                params.put("operatorName", loginUser.getUsername());
            }
            ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
            if (masterCustom != null) {
                loginUser = masterCustom;
            }
        }

        if (!CommonString.ROOT.equals(loginUser.getCustomkey())) {
            if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
                params.put("customKey", loginUser.getCustomkey());
            } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
                int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
                List<String> customKeys = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
                if (customKeys == null || customKeys.size() == 0) {
                    return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
                }
                params.put("customKey", String.join(",", customKeys));
            } else {
                return returnFail(RespCode.error101, "权限错误");
            }
        }


        params.put("customName", customName);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        params.put("fileName", fileName);
        params.put("status", status);
        params.put("splitOrderName", splitOrderName);
        params.put("notDelete", true);
        int total = customSplitOrderService.selectSplitOrder(params).size();
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<CustomSplitOrder> result = customSplitOrderService.selectSplitOrder(params);

        return returnSuccess(result, total);
    }


    @RequestMapping("/successInfo")
    @ResponseBody
    public Map<String, Object> successInfo(@RequestParam String splitOrderNo, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();

        params.put("splitOrderNo", splitOrderNo);

        int total = customSplitSuccessOrderService.selectAll(params).size();
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);

        List<CustomSplitSuccessOrder> result = customSplitSuccessOrderService.selectAll(params);
        return returnSuccess(result, total);
    }

    @RequestMapping(value = "/input", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> inputExcel(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam("customKey") String customKey,
                                          @RequestParam("payType") Integer payType, @RequestParam("splitOrderName") String splitOrderName) {

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        logger.info("请求参数======> customKey={},payType={},splitOrderName={}", customKey, payType, splitOrderName);

        List<ChannelRelated> relateds = customServiceImpl.getRelatedsByCustomKey(customKey);
        if (relateds == null || relateds.size() == 0) {
            return returnFail(RespCode.error101, "请先配置服务公司");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customKey", customKey);
        params.put("status", 0);
        List<CustomSplitOrder> result = customSplitOrderService.selectSplitOrder(params);
        if (result != null && result.size() > 0) {
            return returnFail(RespCode.error101, "该商户有进行中的拆单,请稍后再试");
        }

        List<SplitOrderConf> splitOrderConfs = splitOrderConfDao.listSplitOrderConf(params);
        if (splitOrderConfs == null || splitOrderConfs.size() == 0) {
            return returnFail(RespCode.error101, "请先配置拆单规则");
        }

        InputStream inputStream;
        Workbook workbook;
        ByteArrayOutputStream bytesOut;
        try {
            inputStream = file.getInputStream();
            int readLen;
            byte[] byteBuffer = new byte[1024];
            bytesOut = new ByteArrayOutputStream();
            while ((readLen = inputStream.read(byteBuffer)) > -1) {
                bytesOut.write(byteBuffer, 0, readLen);
            }
            byte[] fileData = bytesOut.toByteArray();
            try {
                workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
            } catch (Exception ex) {
                workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
            }

            Sheet sheet = workbook.getSheetAt(0);
            XSSFRow row = (XSSFRow) sheet.getRow(0);
            XSSFRow row1 = (XSSFRow) sheet.getRow(1);

            Integer templateNo;

            if (row == null || row1 == null) {
                logger.info("数据行不可以为空");
                return returnFail(RespCode.error101, "数据行不可以为空");
            } else {
                String inputTemplate = StringUtil.getXSSFCell(row.getCell(0))
                        + StringUtil.getXSSFCell(row.getCell(1))
                        + StringUtil.getXSSFCell(row.getCell(2))
                        + StringUtil.getXSSFCell(row.getCell(3))
                        + StringUtil.getXSSFCell(row.getCell(4))
                        + StringUtil.getXSSFCell(row.getCell(5))
                        + StringUtil.getXSSFCell(row.getCell(6))
                        + StringUtil.getXSSFCell(row.getCell(7))
                        + StringUtil.getXSSFCell(row.getCell(8));

                String inputTemplate2 = StringUtil.getXSSFCell(row1.getCell(0))
                        + StringUtil.getXSSFCell(row1.getCell(1))
                        + StringUtil.getXSSFCell(row1.getCell(2))
                        + StringUtil.getXSSFCell(row1.getCell(3))
                        + StringUtil.getXSSFCell(row1.getCell(4))
                        + StringUtil.getXSSFCell(row1.getCell(5))
                        + StringUtil.getXSSFCell(row1.getCell(6))
                        + StringUtil.getXSSFCell(row1.getCell(7))
                        + StringUtil.getXSSFCell(row1.getCell(8));
                logger.info("Input Template Head = {}", inputTemplate);
//                logger.info("Config Template Head 1 = {}", CommonString.INPUTTEMPLATECONF_FIRST);
//                logger.info("Config Template Head 2 = {}", CommonString.INPUTTEMPLATECONF_SECEND);
                if ((CommonString.INPUTTEMPLATECONF_BANKCARD_FIRST.equals(inputTemplate) && CommonString.INPUTTEMPLATECONF_BANKCARD_SECEND.equals(inputTemplate2) && payType == 4) || (CommonString.INPUTTEMPLATECONF_ALIPAY_FIRST.equals(inputTemplate) && CommonString.INPUTTEMPLATECONF_ALIPAY_SECEND.equals(inputTemplate2) && payType == 2)) {
                    templateNo = CommonString.INPUTTEMPLATECONF_BANKCARD_FIRST.equals(inputTemplate) ? 1 : 2;
                    logger.info("模板正确======>");
                } else {
                    logger.info("导入模板不正确");
                    return returnFail(RespCode.error101, "导入模板不正确");
                }
            }

            String splitOrderNo = orderNoUtil.getChannelSerialno();

            CustomSplitOrder customSplitOrder = new CustomSplitOrder();
            customSplitOrder.setCustomKey(customKey);
            customSplitOrder.setSplitOrderName(splitOrderName);
            customSplitOrder.setPayType(payType);
            customSplitOrder.setSplitOrderNo(splitOrderNo);
            customSplitOrder.setStatus(0);
            customSplitOrder.setStatusDesc("拆单处理中");
            customSplitOrder.setOperatorName(customLogin.getUsername());

//            //设置保存数据的目录
//            String path = CommonString.EXECLPATH + "/" + splitOrderNo + "/" + customKey;
//            //创建保存数据的目录
//            FileUtil.mkdirs(path);

            String fileName = file.getOriginalFilename();
//            String fileUrl = path + "/" + fileName;

            String uploadPath = "/splitOrder/" + splitOrderNo + "/" + customKey + "/";
            boolean state = FtpTool.uploadFile(uploadPath, fileName, file.getInputStream());

            if (!state) {
                logger.info("--------拆单源文件保存失败--------");
                return returnFail(RespCode.error101, RespCode.EXPORT_FAILIURE);
            }

//            //保存源文件
//            File sourceFile = new File(fileUrl);
//            file.transferTo(sourceFile);

            customSplitOrder.setSourceFileName(fileName);
            customSplitOrder.setSourceFileUrl(uploadPath + "/" + fileName);

            //先保存到数据库，其他在线程中处理
            customSplitOrderService.insert(customSplitOrder);

            Workbook finalWorkbook = workbook;
            SplitOrderThreadPool.cashThreadPool.execute(() -> {
                splitOrderServiceImpl.splitOrder(finalWorkbook, customSplitOrder, templateNo);
            });

            return returnSuccess(RespCode.EXPORT_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return returnFail(RespCode.error101, RespCode.EXPORT_FAILIURE);
    }


    @RequestMapping("/updateStatus")
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestParam String splitOrderNo, @RequestParam Integer status) {
        if (status != 3 && status != 4) {
            return returnFail(RespCode.error101, "非法参数");
        }

        CustomSplitOrder splitOrder = customSplitOrderService.selectBySplitOrderNo(splitOrderNo);
        if (splitOrder != null) {
            splitOrder.setStatus(status);
            customSplitOrderService.updateBySplitOrderNo(splitOrder);
        } else {
            return returnFail(RespCode.error101, "非法参数");
        }

        return returnSuccess();
    }


    @RequestMapping("/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadFile(String fileUrl, String fileName) {

        HttpHeaders headers = null;
        byte[] bytes = null;
        if (!StringUtil.isEmpty(fileUrl) && !StringUtil.isEmpty(fileName)) {
            headers = new HttpHeaders();
            String filePath = fileUrl.substring(0, fileUrl.lastIndexOf("/"));
            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);


            bytes = FtpTool.downloadFtpFile(filePath, fileName);

            if (bytes != null) {
                try {
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
                    headers.add("Content-Disposition", "attachment;filename=" + fileName);

                    headers.setContentDispositionFormData("attachment", fileName);

                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }

        return new ResponseEntity<>(bytes,
                headers, HttpStatus.OK);
    }


    @RequestMapping("/inputBatchInfo")
    @ResponseBody
    public Map<String, Object> inputBatchInfo(HttpServletRequest request, @RequestParam String splitOrderNo, @RequestParam String customKey,
                                              @RequestParam String menuId, @RequestParam String companyId,
                                              @RequestParam String batchName, String batchDesc, @RequestParam String payType) {

        int respstat = RespCode.success;

        CustomSplitSuccessOrder successOrder = customSplitSuccessOrderService.selectBySplitOrderNoAndCompanyId(splitOrderNo, companyId);

        if (successOrder.getHasSynchrodata() == 1) {
            return returnFail(RespCode.error101, "已同步批次准备");
        } else if (successOrder.getHasSynchrodata() == 3) {
            return returnFail(RespCode.error101, "同步批次准备处理中");
        }
        //处理中
        successOrder.setHasSynchrodata(3);
        customSplitSuccessOrderService.updateByPrimaryKey(successOrder);

        Map<String, Object> result = new HashMap<>();

        ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        String operatorName = loginUser.getUsername();// 操作人


        try {

            String filePath = successOrder.getFileUrl().substring(0, successOrder.getFileUrl().lastIndexOf("/"));
            String fileName = successOrder.getFileUrl().substring(successOrder.getFileUrl().lastIndexOf("/") + 1);

            byte[] bytes = FtpTool.downloadFtpFile(filePath, fileName);;
//            File file = new File(successOrder.getFileUrl());
            if (bytes != null) {
                InputStream input = new ByteArrayInputStream(bytes);
//                if (channelCustom.getCustomType() == 5) {
                    if ("2".equals(payType)) {
                        result = customService.alipayInputBatchInfoNew(respstat, operatorName, customKey, menuId, Integer.valueOf(payType), companyId, batchName, batchDesc, input, fileName, result, null);
                    } else if ("4".equals(payType)) {
                        result = customService.inputBatchInfoNew(respstat, operatorName, customKey, menuId, payType, companyId, batchName, batchDesc, input, fileName, null,result,null);
                    }
//                } else {
//                    if ("2".equals(payType)) {
//                        result = customService.alipayInputBatchInfo(respstat, operatorName, customKey, menuId, Integer.valueOf(payType), companyId, batchName, batchDesc, input, fileName, result);
//                    } else if ("4".equals(payType)) {
//                        result = customService.inputBatchInfo(respstat, operatorName, customKey, menuId, payType, companyId, batchName, batchDesc, input, fileName, result);
//                    }
//                }
            }

            successOrder.setHasSynchrodata(1);
            customSplitSuccessOrderService.updateByPrimaryKey(successOrder);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        Integer state = (Integer) result.get("state");
        if (state == RespCode.error101) {
            return returnFail(RespCode.error101, String.valueOf(result.get(RespCode.RESP_MSG)));
        }

        return returnSuccess(result);
    }


}
