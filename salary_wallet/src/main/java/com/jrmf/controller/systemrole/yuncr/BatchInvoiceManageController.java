package com.jrmf.controller.systemrole.yuncr;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BatchInvoiceStatusEnum;
import com.jrmf.controller.constant.BatchInvoiceStepEnum;
import com.jrmf.controller.constant.BatchInvoiceStepStatusEnum;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.BatchInvoiceAssociation;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.dto.ApplyBatchInvoiceDTO;
import com.jrmf.service.ApplyBatchInvoiceService;
import com.jrmf.service.BatchInvoiceAssociationService;
import com.jrmf.service.BatchInvoiceCommissionService;
import com.jrmf.taxsettlement.api.util.UUIDTool;
import com.jrmf.utils.*;
import com.mchange.v2.ser.SerializableUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.security.x509.SerialNumber;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/manage/invoice")
public class BatchInvoiceManageController extends BaseController {

    @Autowired
    BatchInvoiceCommissionService batchInvoiceCommissionService;
    @Autowired
    ApplyBatchInvoiceService applyBatchInvoiceService;
    @Autowired
    private BestSignConfig bestSignConfig;
    @Autowired
    YuncrServiceFeignClient yuncrServiceFeignClient;
    @Autowired
    BatchInvoiceAssociationService batchInvoiceAssociationService;

    @PostMapping(value = "/list")
    public Map<String,Object> invoiceList(HttpServletRequest request, ApplyBatchInvoiceDTO applyBatchInvoiceDTO){
        Map<String,Object> resultMap = new HashMap<>();
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, RespCode.PERMISSION_ERROR);
            return resultMap;
        }

        PageHelper.startPage(applyBatchInvoiceDTO.getPageNo(),applyBatchInvoiceDTO.getPageSize());
        List<ApplyBatchInvoice> invoiceList = applyBatchInvoiceService.getInvoiceList(applyBatchInvoiceDTO);
        BatchInvoiceAssociation batchInvoiceAssociation = new BatchInvoiceAssociation();
        for (ApplyBatchInvoice applyBatchInvoice : invoiceList) {
            String inAccountNo = applyBatchInvoice.getInAccountNo();
            int step = applyBatchInvoice.getStep();
            String stepStatus = applyBatchInvoice.getStepStatus();
            //1:???????????? 2:???????????? 3:??????????????? 4:???????????? 5:????????????
            boolean cardStatus = (step > 2 || (step == 2 && "1".equals(stepStatus)));
            applyBatchInvoice.setSettlementCard(inAccountNo + "-" + (cardStatus ? "??????" : "??????"));

            //?????????????????????????????????
            batchInvoiceAssociation.setApplyBatchInvoiceId(applyBatchInvoice.getId());
            List<BatchInvoiceAssociation> invoiceCustomList = batchInvoiceAssociationService.findInvoiceCustomList(batchInvoiceAssociation);
            if (invoiceCustomList != null && invoiceCustomList.size() > 0){
                List<String> companyNames = invoiceCustomList.stream().map((Function<BatchInvoiceAssociation, String>) BatchInvoiceAssociation::getCompanyName).collect(Collectors.toList());
                applyBatchInvoice.setCompanyName(Joiner.on("???").join(companyNames));
            }
        }
        PageInfo<ApplyBatchInvoice> pageInfo = new PageInfo<>(invoiceList);
        resultMap.put("total",pageInfo.getTotal());
        resultMap.put("invoiceList",pageInfo.getList());
        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }

    @GetMapping(value = "/list/export")
    public void invoiceExport(HttpServletRequest request,HttpServletResponse response,ApplyBatchInvoiceDTO applyBatchInvoiceDTO){
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
           return;
        }
        List<ApplyBatchInvoice> invoiceList = applyBatchInvoiceService.getInvoiceList(applyBatchInvoiceDTO);
        BatchInvoiceAssociation batchInvoiceAssociation = new BatchInvoiceAssociation();
        for (ApplyBatchInvoice applyBatchInvoice : invoiceList) {
            String inAccountNo = applyBatchInvoice.getInAccountNo();
            int step = applyBatchInvoice.getStep();
            String stepStatus = applyBatchInvoice.getStepStatus();
            //1:???????????? 2:???????????? 3:??????????????? 4:???????????? 5:????????????
            boolean cardStatus = (step > 2 || (step == 2 && "1".equals(stepStatus)));
            applyBatchInvoice.setSettlementCard(inAccountNo + "-" + (cardStatus ? "??????" : "??????"));
            //?????????????????????????????????
            batchInvoiceAssociation.setApplyBatchInvoiceId(applyBatchInvoice.getId());
            List<BatchInvoiceAssociation> invoiceCustomList = batchInvoiceAssociationService.findInvoiceCustomList(batchInvoiceAssociation);
            if (invoiceCustomList != null && invoiceCustomList.size() > 0){
                List<String> companyNames = invoiceCustomList.stream().map((Function<BatchInvoiceAssociation, String>) BatchInvoiceAssociation::getCompanyName).collect(Collectors.toList());
                applyBatchInvoice.setCompanyName(Joiner.on(",").join(companyNames));
            }
        }
        String[] colunmName = new String[]{"??????????????????", "??????????????????", "????????????", "????????????", "????????????", "????????????",
                "????????????", "????????????", "???????????????", "????????????", "????????????(????????????)", "????????????", "?????????", "?????????(????????????)",
                "????????????","????????????","????????????","????????????","????????????(??????1)","????????????(??????2)","???????????????(??????3)","??????????????????(??????4)"};
        String filename = "????????????????????????";
        List<Map<String, Object>> data = new ArrayList<>();
        for (ApplyBatchInvoice applyBatchInvoice : invoiceList) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("1", applyBatchInvoice.getCreateTime());
            dataMap.put("2", applyBatchInvoice.getApplyBatchRemark());
            dataMap.put("3", applyBatchInvoice.getTradeMonth());
            String invoiceStatus = applyBatchInvoice.getInvoiceStatus() == 1? BatchInvoiceStatusEnum.FINISH.getDesc() : applyBatchInvoice.getInvoiceStatus() == 2 ?
                    BatchInvoiceStatusEnum.PROCESSING.getDesc(): BatchInvoiceStatusEnum.FAIL.getDesc();
            dataMap.put("4", invoiceStatus);
            dataMap.put("5", applyBatchInvoice.getInvoiceStatusDescribe());
            int step = applyBatchInvoice.getStep();
            String invoiceStep = "";
            if (step == BatchInvoiceStepEnum.PUSH_CONTRACT.getCode()){
                invoiceStep = BatchInvoiceStepEnum.PUSH_CONTRACT.getDesc();
            }else if (step == BatchInvoiceStepEnum.PUSH_SETTLEMENT.getCode()){
                invoiceStep = BatchInvoiceStepEnum.PUSH_SETTLEMENT.getDesc();
            } else if (step == BatchInvoiceStepEnum.PUSH_FINAL_STATEMENT.getCode()){
                invoiceStep = BatchInvoiceStepEnum.PUSH_FINAL_STATEMENT.getDesc();
            }else if (step == BatchInvoiceStepEnum.PUSH_INVOICE.getCode()){
                invoiceStep = BatchInvoiceStepEnum.PUSH_INVOICE.getDesc();
            }
            dataMap.put("6", invoiceStep);
            String stepStatus = applyBatchInvoice.getStepStatus();
            if (BatchInvoiceStepStatusEnum.SUCCESS.getCode().equals(stepStatus)) {
                stepStatus = BatchInvoiceStepStatusEnum.SUCCESS.getDesc();
            }else if (BatchInvoiceStepStatusEnum.PROCESSING.getCode().equals(stepStatus)){
                stepStatus = BatchInvoiceStepStatusEnum.PROCESSING.getDesc();
            }else if (BatchInvoiceStepStatusEnum.FAIL.getCode().equals(stepStatus)){
                stepStatus = BatchInvoiceStepStatusEnum.FAIL.getDesc();
            }

            dataMap.put("7", stepStatus);
            dataMap.put("8", applyBatchInvoice.getCompanyName());
            dataMap.put("9", applyBatchInvoice.getIndividualName());
            dataMap.put("10", applyBatchInvoice.getTradeMoney());
            dataMap.put("11", applyBatchInvoice.getInvoiceMoney());
            dataMap.put("12", applyBatchInvoice.getTradeNumber());
            dataMap.put("13", applyBatchInvoice.getIdCard());
            dataMap.put("14", applyBatchInvoice.getCustomFirmName());
            dataMap.put("15", applyBatchInvoice.getTaskName());//????????????
            dataMap.put("16", applyBatchInvoice.getFullEcoCateName());//????????????
            dataMap.put("17", applyBatchInvoice.getFullInvoiceCategoryName());//????????????
            dataMap.put("18", applyBatchInvoice.getUpdateTime());
            dataMap.put("19", applyBatchInvoice.getContractName());//????????????
            dataMap.put("20", applyBatchInvoice.getSettlementCard());//???????????? ??????-??????
            dataMap.put("21", applyBatchInvoice.getFinalStatementName());//???????????????
            dataMap.put("22", applyBatchInvoice.getInvoiceName());//??????????????????
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @PostMapping(value = "/upload")
    public Map<String,Object> invoiceUpload(HttpServletRequest request, MultipartFile invoiceFile,String id,String invoiceNum){
        Map<String,Object> resultMap = new HashMap<>();
        if (invoiceFile == null || StringUtil.isEmpty(invoiceNum) || StringUtil.isEmpty(id)){
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return resultMap;
        }
        //????????????????????? - ???????????????????????????(jpeg/jpg/png)
        String ext = FilenameUtils.getExtension(invoiceFile.getOriginalFilename());
        if (!"jpeg".equals(ext) && !"jpg".equals(ext) && !"png".equals(ext)){
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, RespCode.INVOICE_FILE_FORMAT_ERROR);
            return resultMap;
        }
        ApplyBatchInvoice applyBatchInvoice = applyBatchInvoiceService.findById(id);
        String settlementSerialNumber = applyBatchInvoice.getSettlementSerialNumber();
        if (StringUtil.isEmpty(settlementSerialNumber)){
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, "???????????????????????????");
            return resultMap;
        }
        String uploadPath = "/invoiceFile/";
        //??????????????????
        String individualName = applyBatchInvoice.getIndividualName();
        String idCard = applyBatchInvoice.getIdCard();
        String tradeMonth = applyBatchInvoice.getTradeMonth();
        if (!StringUtil.isEmpty(tradeMonth)){
            tradeMonth = tradeMonth.substring(tradeMonth.length() - 2);
        }
        String inAccountNo = applyBatchInvoice.getInAccountNo();
        //?????????????????????FTP?????????
        String uploadFileName = UUID.randomUUID().toString().replace("-","");
        String fileName = uploadFileName + "." + ext;
        String filePath = uploadPath + applyBatchInvoice.getCompanyId() + "/";
        try {
            String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, invoiceFile.getInputStream(),
                    bestSignConfig.getUsername(), bestSignConfig.getPassword());
            if ("error".equals(uploadFile)) {
                resultMap.put(RespCode.RESP_STAT, RespCode.error101);
                resultMap.put(RespCode.RESP_MSG, "????????????????????????????????????!");
                return resultMap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, "????????????????????????????????????!");
            return resultMap;
        }

        //?????????????????????base64?????????
        String fileBase = "";
        try {
            fileBase = PicUtils.encryptToBase64(invoiceFile.getBytes());
        }catch (Exception e) {
            e.printStackTrace();
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error000));
            return resultMap;
        }

        //??????????????????????????????
        Map<String,Object> params = new HashMap<>();
        params.put("fileBase",fileBase);
        params.put("platsrl",applyBatchInvoice.getSettlementSerialNumber());
        params.put("fileType",ext);
        params.put("fileName",fileName);
        params.put("invoicenumber",invoiceNum);
        JSONObject jsonObject = yuncrServiceFeignClient.openSettlement(params);
        String code = (String) jsonObject.get("code");
        String msg = (String) jsonObject.get("msg");
        String status = (String) jsonObject.get("data");
        if (!"00000".equals(code) || !"true".equals(status)){
            //?????????????????????
            applyBatchInvoice.setStep(BatchInvoiceStepEnum.PUSH_INVOICE.getCode());
            applyBatchInvoice.setStepStatus(BatchInvoiceStepStatusEnum.FAIL.getCode());
            applyBatchInvoice.setInvoiceStatusDescribe("??????????????????");
            applyBatchInvoice.setInvoiceName("");
            applyBatchInvoice.setInvoiceUrl("");
            applyBatchInvoice.setInvoiceNumber("");
            applyBatchInvoice.setUpdateInvoiceInfo(1);
            int row = applyBatchInvoiceService.updateBatchInvoice(applyBatchInvoice);

            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, msg);
            return resultMap;
        }
        //???????????????????????????????????????
        String formatInvoiceFileName = individualName + "(" + idCard.substring(idCard.length() - 4) + ")" + tradeMonth + "?????????" + "(" +  inAccountNo.substring(inAccountNo.length() - 4) + ")" + "." + ext;
        applyBatchInvoice.setInvoiceUrl(bestSignConfig.getServerNameUrl() + filePath + fileName);
        applyBatchInvoice.setInvoiceName(formatInvoiceFileName);
        applyBatchInvoice.setInvoiceNumber(invoiceNum);
        applyBatchInvoiceService.batchInvoiceSuccess(applyBatchInvoice);

        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }

    @PostMapping(value = "/set/fail")
    public Map<String,Object> setInvoiceFail(HttpServletRequest request,String id,String desc){
        Map<String,Object> resultMap = new HashMap<>();
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
            resultMap.put(RespCode.RESP_STAT, RespCode.error101);
            resultMap.put(RespCode.RESP_MSG, RespCode.PERMISSION_ERROR);
            return resultMap;
        }
        ApplyBatchInvoice applyBatchInvoice = applyBatchInvoiceService.findById(id);
        applyBatchInvoice.setInvoiceStatusDescribe(desc);
        applyBatchInvoiceService.setInvoiceFail(applyBatchInvoice);

        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }

    @PostMapping(value = "/resubmit/channel")
    public Map<String,Object> resubmitChannel(Integer id){
        Map<String,Object> resultMap = new HashMap<>();
        batchInvoiceAssociationService.batchPush(id);
        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }
}



















