package com.jrmf.service;

import static com.jrmf.utils.Base64Utils.remoteFileToBase64;
import static com.jrmf.utils.HtmlUtil.parseHTML2PDFFile;
import static com.jrmf.utils.HtmlUtil.templateFill;
import static com.jrmf.utils.PdUtil.mergePdfFiles;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.PushYuncrNode;
import com.jrmf.common.PushYuncrStatusNode;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.BatchInvoiceAssociation;
import com.jrmf.domain.PushApplyBatchBean;
import com.jrmf.domain.ZhipaiSignTemplate;
import com.jrmf.persistence.ApplyBatchInvoiceDao;
import com.jrmf.persistence.BatchInvoiceAssociationDao;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.PdUtil;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class BatchInvoiceAssociationServiceImpl implements BatchInvoiceAssociationService {
    @Autowired
    BatchInvoiceAssociationDao batchInvoiceAssociationDao;
    @Autowired
    ApplyBatchInvoiceDao applyBatchInvoiceDao;

    @Value("${fonts.path}")
    String fontsPath;

    /*判断系统是否windows*/
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    //linux 模板路径
    private static final String linuxFilePath = "/data/server/salaryboot/static/template/zhipai/";

    @Value("${ftppath}")
    private String ftp_path;

    @Value("${receiptUrl}")
    private String receipt_url;

    @Autowired
    YuncrServiceFeignClient yuncrServiceFeignClient;
    @Override
    public int insert(BatchInvoiceAssociation invoiceAssociation) {
        return batchInvoiceAssociationDao.insert(invoiceAssociation);
    }

    @Override
    public List<BatchInvoiceAssociation> findBatchInvoiceAssociationList(BatchInvoiceAssociation invoiceAssociation) {
        return batchInvoiceAssociationDao.findBatchInvoiceAssociationList(invoiceAssociation);
    }

    @Override
    public List<BatchInvoiceAssociation> findInvoiceCustomAssociationList(BatchInvoiceAssociation invoiceAssociation) {
        return batchInvoiceAssociationDao.findInvoiceCustomAssociationList(invoiceAssociation);
    }

    @Override
    public List<BatchInvoiceAssociation> findInvoiceCustomList(BatchInvoiceAssociation invoiceAssociation) {
        return batchInvoiceAssociationDao.findInvoiceCustomList(invoiceAssociation);
    }

    /**
     * @Description 查询需要推送的数据
     **/
    @Override
    public void batchPush(Integer id) {
        List<PushApplyBatchBean> list = applyBatchInvoiceDao.findApplyBatchListByStatus(id);
        pushContract(list);
    }

    /**
     * @Description 推送合同 结算 结算单
     **/
    public void pushContract(List<PushApplyBatchBean> list) {
        log.info("开始推送:需要推送的数据条数有" + list.size());
        boolean checkData = false;
        JSONObject jsonObject = new JSONObject();
        boolean updateFlag = false;
        int step = 0;
        for (PushApplyBatchBean batchBean : list) {

            log.info("开始推送:原始数据为" + batchBean.toString());
            step = checkStep(batchBean);
            /**
             * @Description 推送完成后需要更新的bean
             **/
            ApplyBatchInvoice updateBean = new ApplyBatchInvoice();
            BeanUtils.copyProperties(batchBean, updateBean);
            String fileName = batchBean.getBusinessLicenseNumber() + System.currentTimeMillis()+".pdf";
            if (step <= PushYuncrNode.PUSH_CONTRACT.getNode()) {
                log.info("开始推送合同");
                ZhipaiSignTemplate zhipaiSignTemplate = new ZhipaiSignTemplate();
                zhipaiSignTemplate.setAddress(batchBean.getAddress());
                zhipaiSignTemplate.setContactInfo(batchBean.getPhone());
                zhipaiSignTemplate.setContacts(batchBean.getIndividualName());
                zhipaiSignTemplate.setUserName(batchBean.getIndividualName());
                zhipaiSignTemplate.setTaxpayerId(batchBean.getBusinessLicenseNumber());
                zhipaiSignTemplate.setDate(batchBean.getGovernmentAuditDate());
                zhipaiSignTemplate.setTaskName(batchBean.getTaskName());
                zhipaiSignTemplate.setTaskDesc(batchBean.getTaskDesc());
                zhipaiSignTemplate.setInvoiceMoney(batchBean.getInvoiceMoney());
                HashMap hashMap = uploadContract(zhipaiSignTemplate, fileName);
                if (ObjectUtils.isEmpty(hashMap) || ObjectUtils.isEmpty(hashMap.get("url")) || ObjectUtils
                    .isEmpty(hashMap.get("base64"))) {
                    updateApplyBatchInvoice(batchBean.getId(),  PushYuncrNode.PUSH_CONTRACT.getNode(),  PushYuncrStatusNode.FAIL.getNode(),"解析合同文件失败!");
                    continue;
                }
                Map<String, Object> contract = new HashMap<>();
                contract.put("firmId", batchBean.getFirmId());
                contract.put("bidPlatsrl", batchBean.getPlatsrl());
                contract.put("fileType", "pdf");
                contract.put("fileName", fileName);
                contract.put("fileBase", hashMap.get("base64"));
                log.info("推送合同数据为" + contract.toString());
                jsonObject = yuncrServiceFeignClient.pushContract(contract);
                checkData = checkYuncrData(jsonObject, PushYuncrNode.PUSH_CONTRACT.getNode(),
                    batchBean.getId());
                if (!checkData) {
                    continue;
                }
                JSONObject dataMsg = jsonObject.getJSONObject("data");
                //合同流水号
                String contractSerialNumber = dataMsg.getString("platsrl");
                //合同编号
                String contractFileNo = dataMsg.getString("fileNo");

                updateBean.setContractSerialNumber(contractSerialNumber);
                updateBean.setContractFileNo(contractFileNo);
                updateBean.setStep(PushYuncrNode.PUSH_CONTRACT.getNode());
                updateBean.setStepStatus(PushYuncrStatusNode.SUCCESS.getNode());
                updateBean.setContractUrl(hashMap.get("url").toString());
                updateBean
                    .setContractName(updateBean.getIndividualName() + updateBean.getTradeMonth() + "合同" +
                        "(" + updateBean.getIdCard().substring(updateBean.getIdCard().length() - 4) + ")"
                        + updateBean.getInAccountNo().substring(updateBean.getInAccountNo().length() - 4)
                        + ".pdf");
                //更新数据
                updateFlag = applyBatchInvoiceDao.updateBatchInvoice(updateBean) > 0 ? true : false;
                if (!updateFlag) {
                    continue;
                }
                step = PushYuncrNode.PUSH_SETTLEMENT.getNode();
            }

            if (step <= PushYuncrNode.PUSH_SETTLEMENT.getNode()) {
                log.info("开始推送结算");
                Map<String, Object> settlement = new HashMap<>();
                settlement.put("firmId", batchBean.getFirmId());
                settlement.put("bidNo", batchBean.getBidNo());
                settlement.put("accZjNo", batchBean.getIdCard());
                settlement.put("accId", batchBean.getInAccountNo());
                settlement.put("settleMoney", batchBean.getInvoiceMoney());
                settlement.put("htplatsrl", updateBean.getContractSerialNumber());
                log.info("推送结算数据为" + settlement.toString());
                jsonObject = yuncrServiceFeignClient.pushSettlement(settlement);
                checkData = checkYuncrData(jsonObject, PushYuncrNode.PUSH_SETTLEMENT.getNode(),
                    batchBean.getId());
                if (!checkData) {
                    continue;
                }
                JSONObject dataMsg = jsonObject.getJSONObject("data");
                //结算编号
                String settlementSerialNumber = dataMsg.getString("platsrl");
                updateBean.setSettlementSerialNumber(settlementSerialNumber);
                updateBean.setStep(PushYuncrNode.PUSH_SETTLEMENT.getNode());
                updateBean.setStepStatus(PushYuncrStatusNode.SUCCESS.getNode());
                //更新数据
                updateFlag = applyBatchInvoiceDao.updateBatchInvoice(updateBean) > 0 ? true : false;
                if (!updateFlag) {
                    continue;
                }
                step = PushYuncrNode.PUSH_FINAL_STATEMENT.getNode();
            }
            if (step <= PushYuncrNode.PUSH_FINAL_STATEMENT.getNode()) {
                log.info("开始推送结算单");
                List<String> commissionList = applyBatchInvoiceDao.selectReceiptUrl(updateBean.getId());
                if (ObjectUtils.isEmpty(commissionList)) {
                    updateApplyBatchInvoice(batchBean.getId(),  PushYuncrNode.PUSH_FINAL_STATEMENT.getNode(),  PushYuncrStatusNode.FAIL.getNode(),"未获取到结算单数据!");
                    continue;
                }
                for (int i = 0; i < commissionList.size(); i++) {
                    commissionList.set(i, receipt_url + commissionList.get(i));
                }
                String base64 = null;
                String finalStatementUrl = null;
                if (commissionList.size() > 1) {
                    HashMap hashMapSettlement = uploadSettlement(commissionList, fileName);
                    if (ObjectUtils.isEmpty(hashMapSettlement) ||
                        ObjectUtils.isEmpty(hashMapSettlement.get("url"))
                        || ObjectUtils.isEmpty(hashMapSettlement.get("base64"))) {
                        updateApplyBatchInvoice(batchBean.getId(),  PushYuncrNode.PUSH_FINAL_STATEMENT.getNode(),  PushYuncrStatusNode.FAIL.getNode(),"解析合并结算单失败!");
                        continue;
                    }
                    base64 = hashMapSettlement.get("base64").toString();
                    finalStatementUrl = hashMapSettlement.get("url").toString();
                } else {
                    base64 = remoteFileToBase64(commissionList.get(0));
                    finalStatementUrl = commissionList.get(0);
                }
                Map<String, Object> settlementUpload = new HashMap<>();
                settlementUpload.put("platsrl", updateBean.getSettlementSerialNumber());
                settlementUpload.put("fileType", "pdf");
                settlementUpload.put("fileName", fileName);
                settlementUpload.put("fileBase", base64);
                log.info("推送结算单数据为" + settlementUpload.toString());
                jsonObject = yuncrServiceFeignClient.pushSettlementUpload(settlementUpload);
                checkData = checkYuncrData(jsonObject, PushYuncrNode.PUSH_FINAL_STATEMENT.getNode(),
                    batchBean.getId());
                if (!checkData) {
                    continue;
                }
                updateBean.setFinalStatementUrl(finalStatementUrl);
                updateBean
                    .setFinalStatementName(updateBean.getIndividualName() + updateBean.getTradeMonth() + "回单" +
                        "(" + updateBean.getIdCard().substring(updateBean.getIdCard().length() - 4) + ")"
                        + updateBean.getInAccountNo().substring(updateBean.getInAccountNo().length() - 4)
                        + ".pdf");
                updateBean.setStep(PushYuncrNode.PUSH_FINAL_STATEMENT.getNode());
                updateBean.setStepStatus(PushYuncrStatusNode.SUCCESS.getNode());
                updateBean.setInvoiceStatusDescribe("结算单上送完成!");

                //更新数据
                applyBatchInvoiceDao.updateBatchInvoice(updateBean);
            }
        }
    }

    /**
     * @Description 填充并上传合同
     **/
    public HashMap uploadContract(ZhipaiSignTemplate zhipaiSignTemplate, String fileName) {
        log.info("进入上传合同文件");
        HashMap hashMap = new HashMap(2);
        String filePath = "/yuncr/file/contract/";
        try {
            String templatePath = "E:\\push\\zhipai-sign.html";
            String generatePdfPath = "E:\\push\\contract.pdf";
            if(!isWindows){
                templatePath = linuxFilePath+"zhipai-sign.html";
                generatePdfPath = linuxFilePath+System.currentTimeMillis()+"contract.pdf";
            }
            String  htmlLocalSavePath = linuxFilePath+System.currentTimeMillis()+"contract.html";
            String html = templateFill(templatePath, zhipaiSignTemplate);
            OutputStream fos = new FileOutputStream(htmlLocalSavePath);
            fos.write(html.getBytes("UTF-8"));
            fos.flush();
            fos.close();
            PdUtil.html2pdf(htmlLocalSavePath,generatePdfPath, fontsPath);

            InputStream is = new FileInputStream(generatePdfPath);
            log.info("开始上传合同文件到FTP");
            boolean upload = FtpTool.uploadFile(filePath, fileName, is);
            if (upload) {
                hashMap.put("url", ftp_path + filePath + fileName);
                InputStream isBase = new FileInputStream(generatePdfPath);
                byte[] bytes = IOUtils.toByteArray(isBase);
                String encoded = Base64.getEncoder().encodeToString(bytes);
                hashMap.put("base64", encoded);
            }
        } catch (Exception e) {
            log.info("ftp上传合同文件异常" + e);
        }

        return hashMap;
    }

    /**
     * @Description 合并并上传结算单
     **/
    public HashMap uploadSettlement(List<String> list, String fileName) {
        log.info("进入上传结算单文件");
        String filePath = "/yuncr/file/settlement/";
        HashMap hashMap = new HashMap(2);
        String[] array = new String[list.size()];
        list.toArray(array);
        String savepath = "C:/push/settlement.pdf";
        if(!isWindows){
            savepath = linuxFilePath+System.currentTimeMillis()+"settlement.pdf";
        }

        mergePdfFiles(array, savepath);
        try {
            log.info("ftp开始上传结算单");
            InputStream is = new FileInputStream(savepath);
            boolean upload = FtpTool.uploadFile(filePath, fileName, is);
            if (upload) {
                log.info("ftp上传结算单成功");
                InputStream isbase = new FileInputStream(savepath);
                byte[] bytes = IOUtils.toByteArray(isbase);
                String encoded = Base64.getEncoder().encodeToString(bytes);
                hashMap.put("base64", encoded);
                hashMap.put("url", ftp_path + filePath + fileName);
            }
        } catch (Exception e) {
            log.info("ftp上传合同文件异常" + e);
        }
        return hashMap;
    }

    /**
     * @return boolean
     * @Author YJY
     * @Description 检查 返回数据是否正确
     * @Date 2020/10/12
     * @Param [result, processNode]
     **/
    public boolean checkYuncrData(JSONObject result, int step, int id) {


        /**
         * @Description 请求 yuncr_service 服务异常
         **/
        if (ObjectUtils.isEmpty(result)) {
            updateApplyBatchInvoice(id, step, PushYuncrStatusNode.FAIL.getNode(),"请求服务接口失败!");
            return false;
        }
        log.info("云控返回的结果数据为" + result.toJSONString());
        /**
         * @Description 请求 yuncr_service 服务异常
         **/
        if (!"00000".equals(result.get("code"))) {
            // updateYuncrUser(userInfo, processNode, processNode.getNodeName() + ":" + result.get("msg"));
            updateApplyBatchInvoice(id, step, PushYuncrStatusNode.FAIL.getNode(),result.get("msg").toString());
            return false;
        }
        /**
         * @Description 请求 云控 服务异常
         **/
        JSONObject yuncr = result.getJSONObject("data");
        if (ObjectUtils.isEmpty(yuncr)) {

            //updateYuncrUser(userInfo, processNode, processNode.getNodeName() + ":yucr服务接口返回数据为空");
            updateApplyBatchInvoice(id, step, PushYuncrStatusNode.FAIL.getNode(),"");
            return false;
        }

        String code = yuncr.getString("code");
        if (!StringUtils.isEmpty(code) && !"0".equals(yuncr.getString("code"))) {
            // updateYuncrUser(userInfo, processNode, processNode.getNodeName() + ":" + yuncr.getString("info"));
            updateApplyBatchInvoice(id, step, PushYuncrStatusNode.FAIL.getNode(),yuncr.getString("info"));
            return false;
        }

        return true;
    }

    /**
     * @Description 获取推送步骤
     **/
    public int checkStep(PushApplyBatchBean batchBean) {

        if (batchBean.getStepStatus().trim().equals(PushYuncrStatusNode.FAIL.getNode()) ||
            batchBean.getStepStatus().trim().equals(PushYuncrStatusNode.IN_HAND.getNode())) {
            return batchBean.getStep();
        }
        return batchBean.getStep() + 1;
    }

    public void updateApplyBatchInvoice(int id, int step, String stepStatus,String invoiceStatusDescribe) {
        ApplyBatchInvoice applyBatchInvoice = new ApplyBatchInvoice();
        applyBatchInvoice.setId(id);
        applyBatchInvoice.setStep(step);
        applyBatchInvoice.setStepStatus(stepStatus);
        applyBatchInvoice.setInvoiceStatusDescribe(invoiceStatusDescribe);
        applyBatchInvoiceDao.updateBatchInvoice(applyBatchInvoice);
    }


}
