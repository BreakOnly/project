package com.jrmf.controller.agreement;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.User;
import com.jrmf.domain.UserRelated;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.UserRelatedService;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.ZipUtils;
import com.jrmf.utils.exception.ImportException;
import com.jrmf.utils.ocr.OCRUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 种路路
 * @create 2019-07-05 10:28
 * @desc 协议迁移控制层
 **/
@RestController
@RequestMapping("/copy/agreement")
public class AgreementController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(AgreementController.class);

    private final ChannelCustomService channelCustomService;
    private final AgreementTemplateService agreementTemplateService;
    private final UsersAgreementService usersAgreementService;
    private final UserSerivce userSerivce;
    private final BestSignConfig bestSignConfig;
    private final UserRelatedService userRelatedService;
    private final ChannelRelatedService channelRelatedService;

    @Autowired
    public AgreementController(ChannelCustomService channelCustomService, AgreementTemplateService agreementTemplateService, UsersAgreementService usersAgreementService, UserSerivce userSerivce, BestSignConfig bestSignConfig, UserRelatedService userRelatedService, ChannelRelatedService channelRelatedService) {
        this.channelCustomService = channelCustomService;
        this.agreementTemplateService = agreementTemplateService;
        this.usersAgreementService = usersAgreementService;
        this.userSerivce = userSerivce;
        this.bestSignConfig = bestSignConfig;
        this.userRelatedService = userRelatedService;
        this.channelRelatedService = channelRelatedService;
    }

    /**
     * 查询所有签约的商户列表
     * @return 商户列表
     */
    @RequestMapping("/merchants")
    public Map<String, Object> merchants(HttpServletRequest request) {
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        List<ChannelCustom> listCustom = channelCustomService.getAgreementMerchants(channelCustom);
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("channelCustomList", listCustom);
        return returnSuccess(resultMap);
    }
    /**
     * 查询所有该商户的服务公司
     * @param customKey 选择的商户key
     * @return 服务公司列表
     */
    @RequestMapping("/companies")
    public Map<String, Object> companies(HttpServletRequest request,String customKey) {
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        List<ChannelCustom> listCustom = channelCustomService.getAgreementCompanies(channelCustom,customKey);
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("companyList", listCustom);
        return returnSuccess(resultMap);
    }

    /**
     * 查询转包服务公司的完税服务公司
     */
    @RequestMapping("/realCompanys")
    public Map<String, Object> realCompanys(@RequestParam String companyId) {
        List<Map<String, Object>> listCustom = channelRelatedService.listCompanyByOriginalId(companyId);
        if (listCustom == null || listCustom.isEmpty()) {
            return returnFail(RespCode.error101, "未配置完税服务公司");
        }
        return returnSuccess(listCustom);
    }

    /**
     * 查询协议模版列表
     * @param customKey 选择的商户key
     * @param companyId 服务公司
     * @return 协议模版列表
     */
    @RequestMapping("/template")
    public Map<String, Object> template(String customKey,String companyId) {
        Map<String, Object> hashMap = new HashMap<>(2);
        hashMap.put("originalId",customKey);
        hashMap.put("companyId",companyId);
        List<AgreementTemplate> agreementTemplates = agreementTemplateService.getAgreementTemplateByParam(hashMap);
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("agreementTemplates", agreementTemplates);
        return returnSuccess(resultMap);
    }

    /**
     * 查询转包服务公司和实际服务公司的协议模版列表
     */
    @RequestMapping("/realTemplate")
    public Map<String, Object> realTemplate(String companyId, String realCompanyId) {
        Map<String, Object> hashMap = new HashMap<>(2);
        hashMap.put("originalId",companyId);
        hashMap.put("companyId",realCompanyId);
        List<AgreementTemplate> agreementTemplates = agreementTemplateService.getAgreementTemplateByParam(hashMap);
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("agreementTemplates", agreementTemplates);
        return returnSuccess(resultMap);
    }

    /**
     * 查询符合条件的 结果条数
     * @param fromTemplateId 原下发模版
     * @param toTemplateId 新下发模版
     * @return 结果条数
     */
    @RequestMapping("/detail")
    public Map<String, Object> detail(String fromTemplateId,String toTemplateId) {
        Map<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("signStatus", UsersAgreementSignType.SIGN_SUCCESS.getCode());
        hashMap.put("agreementTemplateId",fromTemplateId);
        List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(hashMap);
        int countFrom = agreements.size();
        StringBuilder certIds = new StringBuilder();
        for(Iterator agreementIterator = agreements.iterator(); agreementIterator.hasNext(); certIds.append(((UsersAgreement)agreementIterator.next()).getCertId())) {
            if (certIds.length() != 0) {
                certIds.append(",");
            }
        }

        hashMap.put("agreementTemplateId",toTemplateId);
        hashMap.put("certIds", certIds.toString());
        int countTo = usersAgreementService.getUsersAgreementsCountByParams(hashMap);
        int left = countFrom - countTo;
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("countFrom", countFrom);
        resultMap.put("countTo", countTo);
        resultMap.put("left", Math.max(left, 0));
        return returnSuccess(resultMap);
    }

    /**
     * 签约复制
     * @param fromTemplateId 原下发模版
     * @param toTemplateId 新下发模版
     * @return 协议模版列表
     */
    @RequestMapping("/copies")
    public Map<String, Object> copies(String fromTemplateId,String toTemplateId) {
        usersAgreementService.copyAgreementsByTemplateId(fromTemplateId,toTemplateId);
        return returnSuccess();
    }

    /**
     * 导入遗漏数据
     */
    @RequestMapping("/user/related")
    public Map<String, Object> related() {
        List<UsersAgreement> usersAgreementsByParams = usersAgreementService.getUsersAgreementsByParams(new HashMap<>(2));
        for (UsersAgreement usersAgreement : usersAgreementsByParams) {
            UserRelated userRelated = new UserRelated();
            userRelated.setStatus(0);
            userRelated.setCreateTime(DateUtils.getNowDate());
            userRelated.setOriginalId(usersAgreement.getOriginalId());
            userRelated.setUserId(Integer.parseInt(usersAgreement.getUserId()));
            userRelated.setCompanyId(usersAgreement.getCompanyId());
            try {
                userRelatedService.createUserRelated(userRelated);
            }catch (Exception e){
                logger.info("插入重复："+e.getMessage());
            }
        }
        System.out.println("完成");
        return returnSuccess();
    }

    /**
     * 签约导入数据
     */
    @RequestMapping("/import")
    public Map<String, Object> importAgreement(String customkey,String templateId, MultipartFile userInfo,MultipartFile photos,String remark) throws IOException {
        HashMap<String, Object> paramMap = new HashMap<>(3);
        AgreementTemplate template = agreementTemplateService.getAgreementTemplateById(templateId);
        if(template == null){
            return returnFail(RespCode.AGREEMENT_TEMPLATE_NOT_FOUND, RespCode.codeMaps.get(RespCode.AGREEMENT_TEMPLATE_NOT_FOUND));
        }
        Workbook workbook ;
        ByteArrayOutputStream bytesOut;
        InputStream is = userInfo.getInputStream();

        byte[] byteBuffer = new byte[1024];
        bytesOut = new ByteArrayOutputStream();

        int readLen;
        while ((readLen = is.read(byteBuffer)) > -1) {
            bytesOut.write(byteBuffer, 0, readLen);
        }
        byte[] fileData = bytesOut.toByteArray();

        try {
            workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
        } catch (Exception ex) {
            workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
        }
        int num = 0;
        Sheet sheet = workbook.getSheetAt(0);
        for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
            XSSFRow row = (XSSFRow) sheet.getRow(j);
            if (row == null) {
                continue;
            }
            ++num;
            if (num > 500 || num < 0) {
                throw new ImportException(RespCode.codeMaps.get(RespCode.IMPORT_NUMBER_ERROR_500));
            }
        }
        List<User> users = new ArrayList<>();

        getUserList(sheet, users);

        paramMap.put("users", users);
        paramMap.put("customkey", customkey);
        paramMap.put("remark", remark);
        byte[] bytes = null;
        if(template.getChannelType() != 2 && photos == null){
            return returnFail(RespCode.FILE_NOT_FOUND, RespCode.codeMaps.get(RespCode.FILE_NOT_FOUND));
        }
        if(template.getChannelType() == 2 ){
            photos = null;
        }
        if(photos != null){
            bytes = photos.getBytes();
        }
        String processId = MDC.get(PROCESS);
        //导入用户信息
        byte[] finalBytes = bytes;
        ThreadUtil.pdfThreadPool.execute(() -> {
            try {
                MDC.put(PROCESS,processId+"_addUser");
                logger.info("导入用户信息开始");
                userSerivce.addUserBatchByExcel(paramMap);
                logger.info("导入用户信息结束");
            } catch (Exception e) {
                logger.info("导入用户信息报错，{}",e.getMessage());
            } finally {
                MDC.remove(PROCESS);
            }
            try {
                MDC.put(PROCESS,processId+"_ocrProcess");
                logger.info("导入身份证开始");
                ocrProcess(customkey, template, finalBytes, users);
                logger.info("导入身份证结束");
                MDC.remove(PROCESS);
            } catch (Exception e) {
                logger.error("导入身份证报错",e);
            } finally {
                MDC.remove(PROCESS);
            }
        });

        return returnSuccess();
    }

    private void ocrProcess(String customkey, AgreementTemplate template, byte[] bytes, List<User> users) throws Exception {
        String tempPath = "/data/server/salaryboot/temp/import/photo/"+System.currentTimeMillis()+"/";
        String path = tempPath+"/agreement/";
        String uploadPath = "/companyImg/" + customkey + "/" ;
        if(bytes != null){
            logger.info("开始上传身份证证件照---");
            File file1 = new File(tempPath + "1.txt");
            File fileParent = file1.getParentFile();
            if (!fileParent.exists()) {
                boolean mkDirs = fileParent.mkdirs();
                logger.info("创建文件夹状态：" + mkDirs);
            }
            File file = new File(tempPath + "photo.zip");
            if(!file.exists()){
                boolean newFile = file.createNewFile();
                logger.info("创建文件:"+newFile);
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            logger.info("导入身份证开始解压");
            ZipUtils.unzip(file,tempPath,"agreement");

            File[] f1=new File(path).listFiles();
            if(f1 == null){
                logger.info("导入身份证文件不存在");
                throw new ImportException(RespCode.codeMaps.get(RespCode.ZIP_FILE_NOT_FOUND));
            }else{
                logger.info("导入身份证文件："+f1.length+"条");
            }
            String ftpURL = bestSignConfig.getFtpURL();
            String userName = bestSignConfig.getUsername();
            String password = bestSignConfig.getPassword();

            for (File file2 : f1) {
                String certId = file2.getName().substring(0,18);
                String type = "1".equals(file2.getName().substring(19, 20)) ? OCRUtil.IDCARD_SIDE_FRONT:OCRUtil.IDCARD_SIDE_BACK;

                FtpTool.uploadFile(ftpURL, 21, uploadPath, certId + "_" + type + ".jpg", new FileInputStream(file2), userName, password);

            }
            logger.info("上传身份证证件照结束---");
        }
        logger.info("导入身份证文件：{}",users);
        for (User user : users) {
            logger.info("签约开始："+user.toString());
            String certId = user.getCertId();
            HashMap<String, Object> hashMap = new HashMap<>(4);
            hashMap.put("agreementTemplateId",template.getId());
            hashMap.put("certId",certId);
            logger.info("查询{}",hashMap);
            List<UsersAgreement> usersAgreements = usersAgreementService.getUsersAgreementsByParams(hashMap);
            if(usersAgreements.isEmpty()){
                logger.info("查询没有查询到。");
                continue;
            }
            UsersAgreement usersAgreement = usersAgreements.get(0);
            String agreementType = usersAgreement.getAgreementType();
            String thirdPartSign = "2";
            boolean thirdSign = thirdPartSign.equals(agreementType);
            try {
                logger.info("开始签约步骤");
                usersAgreementService.signAgreement(template,usersAgreement, SignSubmitType.IMPORT.getCode(), "", user.getMobilePhone(),thirdSign,bytes!=null, null);
                if(bytes != null){
                    String frontUrl = uploadPath+certId + "_front.jpg";
                    String backUrl = uploadPath+certId + "_back.jpg";
                    File frontPic = new File(path + certId + "-1.jpg");
                    File backPic = new File(path + certId + "-2.jpg");
                    logger.info("开始身份证步骤");
                    usersAgreementService.uploadPic(template,usersAgreement, frontUrl, backUrl, thirdSign,backPic, frontPic);
                }
                if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
                    usersAgreementService.checkSignStatus(usersAgreement);
                }
            }catch (Exception e){
                logger.info("签约报错{}",e.getMessage());
            }
        }
    }


}
