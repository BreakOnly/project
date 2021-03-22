package com.jrmf.service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.UserCommission;
import com.jrmf.utils.EmailUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import com.umf.api.service.UmfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @version 创建时间：2019年4月22日17:02:03
 */
@Service("umfCommissionService")
public class UmfCommissionServiceImpl implements UmfCommissionService {

	private static Logger logger = LoggerFactory.getLogger(UmfCommissionServiceImpl.class);

    private final ChannelCustomService channelCustomService;
    private final UserSerivce userSerivce;
    private final UserCommissionService userCommissionService;
    private final OrderNoUtil orderNoUtil;

    @Autowired
    public UmfCommissionServiceImpl(ChannelCustomService channelCustomService, UserSerivce userSerivce, UserCommissionService userCommissionService, OrderNoUtil orderNoUtil) {
        this.channelCustomService = channelCustomService;
        this.userSerivce = userSerivce;
        this.userCommissionService = userCommissionService;
        this.orderNoUtil = orderNoUtil;
    }


    /**
     * 下载对账文件
     *
     * @param merId  商户号
     * @param time  下载文件时间
     */
    @Override
    public void downloadUserCommission(String merId,String time) {
        String path = "/data/server/key/"+merId+"_.key.p8";
        UmfService instance =  new com.umf.api.service.UmfServiceImpl(merId,path);
        Map<String,Object> reqMap = new HashMap<>(8);
        reqMap.put("mer_id",merId);
        //对账日期
        reqMap.put("settle_date",time);
        //对账类型
        reqMap.put("settle_type","TAXPAY");
        String dir = "/data/server/salaryboot/reconciliation/umfintech";
        File file = new File(dir);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            logger.info("创建多级目录："+mkdirs);
        }
        reqMap.put("settle_path",dir);
        boolean result = instance.reconciliationDownloadMap(reqMap);
        logger.info("----------对账文件是否下载成功："+result);
        if(!result){
            //发送邮件告知
            sendEmail(merId,time,"下载");
        }else{
            try {
//                "52325_"+time+".taxpay"
                Map<String,Object> params = new HashMap<>(2);
                params.put("umfId",merId);
                List<ChannelCustom> channelCustoms  = channelCustomService.getCustomByParam(params);
                ChannelCustom channelCustom = channelCustoms.get(0);
                String companyId = channelCustom.getCustomkey();
                String companyName = channelCustom.getCompanyName();
                readFile(companyId,companyName,dir,merId,time);
            } catch (IOException e) {
                sendEmail(merId, time,"读取"+e.getMessage());
                logger.error(e.getMessage(),e);
            }
        }
    }


    private void readFile(String companyId, String companyName, String dir, String merId, String time) throws IOException {
        String file = dir+"/"+merId+"_"+time+".taxpay.txt";
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
        BufferedReader bufferedReader = new BufferedReader(isr);
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            logger.info(s);
            if (s.startsWith("TRADEDETAIL-START")) {
                if (!checkFileStart(merId, time, s)){
                    return;
                }
            } else if (s.startsWith("TRADEDETAIL-END")) {
                if (!checkFileEnd(merId, time, s)){
                    return;
                }
            } else {
                String[] split = s.split(",");

                String customOrderNo = split[0];
                String tradeTime = split[1];
                String updateTime = split[2];
                String amount = split[3];
                String channelHandlingFee = split[4];
                String stateDesc = split[6];
                String channelOrderNo = split[7];
                //发起商户号
                String merchantCustom = split[8];
                Map<String,Object> params = new HashMap<>(2);
                params.put("umfId",merchantCustom);
                List<ChannelCustom> channelCustoms  = channelCustomService.getCustomByParam(params);
                if(channelCustoms.isEmpty()){
                    sendEmail(merId, time,"ChannelCustom未找到");
                    return;
                }

                String bankRemark = split[11];
                String customRemark = split[12];
                String[] splits = customRemark.split("\\|");
                String certId = splits[0];
                String userName = splits[1];
                String bankCardNo = splits[2];

                ChannelCustom channelCustom = channelCustoms.get(0);
                //创建用户
                String customkey = channelCustom.getCustomkey();
                Map<String, Object> objectMap = userSerivce.addUserInfo(userName, CertType.ID_CARD.getCode(), certId, null, null,customkey , null, "");
                int state = (int) objectMap.get("state");

                if(RespCode.success != state){
                    sendEmail(merId, time,"用户插入失败："+ JSONObject.toJSONString(objectMap));
                }else{
                    int userId = (int) objectMap.get("userId");
                    createUserCommission(companyId, companyName, customOrderNo, tradeTime, updateTime, amount, channelHandlingFee, stateDesc, channelOrderNo, bankRemark, customRemark, certId, bankCardNo, channelCustom, customkey, userId+"",userName);
                }
            }
        }
    }

    /**
     * 插入下发明细表
     */
    private void createUserCommission(String companyId, String companyName, String customOrderNo, String tradeTime, String updateTime, String amount, String channelHandlingFee, String stateDesc, String channelOrderNo, String bankRemark, String customRemark, String certId, String bankCardNo, ChannelCustom channelCustom, String customkey, String userId, String userName) {

        logger.error("添加用户成功。用户Id是："+userId);
        Map<Object, Object> paramMap = new HashMap<>(2);
        paramMap.put("channelOrderNo",channelOrderNo);
        int size = userCommissionService.getCommissionsCountByParams(paramMap);
        if(size != 0){
            logger.error("订单已存在！支付通道订单号是："+channelOrderNo);
            return;
        }
        UserCommission userCommission = new UserCommission();
        userCommission.setAccountDate(tradeTime.substring(0,10));
        userCommission.setPayType(PayType.PINGAN_BANK.getCode());
        userCommission.setOriginalId(customkey);
        userCommission.setUserName(userName);
        userCommission.setOrderNo(orderNoUtil.getChannelSerialno());
        userCommission.setAmount(amount);
        userCommission.setUserId(userId);
        userCommission.setStatus(1);
        userCommission.setStatusDesc(StringUtil.isEmpty(stateDesc)?"成功":stateDesc);
        userCommission.setUpdatetime(updateTime.substring(0,19));
        userCommission.setRemark(customRemark);
        userCommission.setCompanyId(companyId);
        userCommission.setCompanyName(companyName);
        userCommission.setCertId(certId);
        userCommission.setCustomName(channelCustom.getCompanyName());
        userCommission.setAccount(bankCardNo);
        userCommission.setDocumentType(CertType.ID_CARD.getCode());
        userCommission.setDescription(bankRemark);
        userCommission.setPaymentTime(updateTime.substring(0,19));
        userCommission.setChannelHandlingFee(channelHandlingFee);
        userCommission.setChannelOrderNo(channelOrderNo);
        userCommission.setCustomOrderNo(customOrderNo);
        userCommissionService.addUserCommission(userCommission);
    }

    /**
     * 校验文件结尾
     */
    private boolean checkFileEnd(String merId, String time, String s) {
        logger.info("文件汇总信息：");
        String[] split = s.split(",");
        String merchantId = split[1];
        if(!merchantId.equals(merId)){
            sendEmail(merId, time,"文件商户号为："+merchantId+",配置商户号为："+merId);
            return false;
        }
        String tradeTime = split[2];
        if(!tradeTime.equals(time)){
            sendEmail(merId, time,"交易时间为："+tradeTime+"当前时间为："+time);
            return false;
        }
        return true;
    }

    /**
     * 校验文件开头
     */
    private boolean checkFileStart(String merId, String time, String s) {
        logger.info("文件头信息：");
        String[] split = s.split(",");
        String merchantId = split[1];
        if(!merchantId.equals(merId)){
            sendEmail(merId, time,"文件商户号为："+merchantId+",配置商户号为："+merId);
            return false;
        }
        String fileTime = split[2];
        if(!fileTime.equals(time)){
            sendEmail(merId, time,"文件时间为："+fileTime+"当前时间为："+time);
            return false;
        }
        String code = split[4];
        String success = "0000";
        if(!success.equals(code)){
            String message = "";
            int startLength = 5;
            if (split.length > startLength) {
                message = split[5];
            }
            sendEmail(merId, time,"文件状态码为："+code+"，信息为："+message);
            return false;
        }
        return true;
    }

    private void sendEmail(String merId, String time, String message) {
        ThreadUtil.pdfThreadPool.execute(() -> {
            String url = "zstservice@jrmf360.com";
            String password = "Jrmf#2019";
            String host = "smtp.jrmf360.com";
            String context = "联动优势支付对账文件下载失败，商户号："+merId+"。日期："+time+"。错误信息："+message;
            logger.error(context);
//            String[] receivers = {"chonglulu@jrmf360.com", "zhanghuan@jrmf360.com", "lilili@jrmf360.com", "lilei@jrmf360.com", "wufujin@jrmf360.com", "liuxiaoming@jrmf360.com"};
            String[] receivers = {"chonglulu@jrmf360.com"};
            String title = "联动优势支付对账文件异常邮件通知";
            try {
                EmailUtil.send(url, password, host, receivers, title, context, null, "text/html;charset=GB2312");
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        });
    }

}
