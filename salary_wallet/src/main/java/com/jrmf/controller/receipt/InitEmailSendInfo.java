package com.jrmf.controller.receipt;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.EmailSendStatus;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ReceiptDownLoad;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.ReceiptService;
import com.jrmf.utils.EmailUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/28 17:35
 * Version:1.0
 *
 * @author guoto
 */
public class InitEmailSendInfo implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(InitEmailSendInfo.class);

    private static final String url = "zstservice@jrmf360.com";
    private static final String password = "Jrmf#2019";
    private static final String host = "smtp.jrmf360.com";
    private String[] receivers;
    private String title;
    private OrganizationTreeService organizationTreeService;
    private Map<String, Object> params;
    private Integer customType;
    private Integer nodeId;
    private String timeStart;
    private String timeEnd;
    private String email;
    private HttpSession session;
    private ChannelCustomService customService;
    private ReceiptService receiptService;
    private String serverName;

    public InitEmailSendInfo(String[] receivers, OrganizationTreeService organizationTreeService, Map<String, Object> params, Integer customType, Integer nodeId, String timeStart, String timeEnd, String email, HttpSession session, ChannelCustomService customService, ReceiptService receiptService, String serverName,String title) {
        this.receivers = receivers;
        this.organizationTreeService = organizationTreeService;
        this.params = params;
        this.customType = customType;
        this.nodeId = nodeId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.email = email;
        this.session = session;
        this.customService = customService;
        this.receiptService = receiptService;
        this.serverName = serverName;
        this.title = title;
    }

    @Override
    public void run() {
        ChannelCustom custom = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        ChannelCustom byCustomkey = customService.getCustomByCustomkey((String) session.getAttribute(CommonString.CUSTOMKEY));
        StringBuilder customKeys = new StringBuilder();
        if (!CommonString.ROOT.equals(session.getAttribute(CommonString.CUSTOMKEY))) {
            List<String> strings = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
            for (String str : strings) {
                customKeys.append(str).append(",");
            }
            params.put("customKey", customKeys.toString());
        }
        long time = System.currentTimeMillis();
        String fileName = custom.getId() + "_" + timeStart.replaceAll("-", "") + "_" + timeEnd.replaceAll("-", "")+"_"+time;
        ReceiptDownLoad receiptDownLoad = new ReceiptDownLoad();
        receiptDownLoad.setFileName(fileName);
        receiptDownLoad.setOrgAccount(custom.getUsername());
        receiptDownLoad.setCustomType(byCustomkey.getCustomType());
        receiptDownLoad.setOrgName(byCustomkey.getCompanyName());
        receiptDownLoad.setReceivingMail(email);
        receiptDownLoad.setStatus(EmailSendStatus.WAITING.getCode());
        receiptDownLoad.setStatusDesc(EmailSendStatus.codeOf(EmailSendStatus.WAITING.getCode()).getDesc());
        receiptService.addReceiptDownload(receiptDownLoad);
        String zipPath = receiptService.listPdfPathByParam(params, fileName, receiptDownLoad.getId());
        String context = "尊敬的客户： \n" + "您好！ \n" + "链接地址是根据贵公司的申请生成的批量回单文件，请点击链接下载。";

        if(StringUtil.isEmpty(zipPath)){
            context = "尊敬的客户：\n" + "您好！\n" + "您申请的批量回单文件尚未生成，请联系工作人员上传回单后再下载。";
        }else{
            try {
                FileInputStream inputStream = new FileInputStream(new File(zipPath));
                String path = "/download/receipt/";
                String filename = System.currentTimeMillis()+".zip";
                FtpTool.uploadFile(path, filename, inputStream);
                String url = serverName+path+filename;
                context = context + "\n下载地址："+url;
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
                logger.error("文件生成失败");
                context = "尊敬的客户：\n" + "您好！\n" + "您申请的批量回单文件生成失败，请联系工作人员。";
            }
        }
        try {
            EmailUtil.send(url, password, host, receivers, title, context, null, null);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }
}
