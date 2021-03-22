package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.InvoiceType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.QbInvoiceBase;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.QbInvoiceBaseService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.util.HexStringUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * @author chonglulu
 */
@ActionConfig(name = "新增商户开票信息")
public class AddMerchantInvoiceInfoService implements Action<AddMerchantInvoiceInfoServiceParams, AddMerchantInvoiceInfoServiceAttachment> {


    @Autowired
    private BestSignConfig bestSignConfig;
    @Autowired
    private QbInvoiceBaseService qbInvoiceBaseService;
    @Autowired
    private ChannelCustomService channelCustomService;
    @Autowired
    private ChannelRelatedService channelRelatedService;

    @Override
    public String getActionType() {
        return APIDefinition.ADD_MERCHANT_INVOICE_INFO.name();
    }

    @Override
    public ActionResult<AddMerchantInvoiceInfoServiceAttachment> execute(AddMerchantInvoiceInfoServiceParams actionParams) {

        String invoiceType = actionParams.getInvoiceType();
        int count = qbInvoiceBaseService.queryInvoiceClassCount(actionParams.getBillingClass());
        if(count != 1){
            throw new APIDockingException(APIDockingRetCodes.INVOICE_CLASS_NOT_FOUND.getCode(), APIDockingRetCodes.INVOICE_CLASS_NOT_FOUND.getDesc());
        }
        InvoiceType type = InvoiceType.codeOfReal(Integer.parseInt(invoiceType));
        if(type == null){
            throw new APIDockingException(APIDockingRetCodes.INVOICE_TYPE_NOT_FOUND.getCode(), APIDockingRetCodes.INVOICE_TYPE_NOT_FOUND.getDesc());
        }
        QbInvoiceBase qbInvoiceBase = new QbInvoiceBase();
        ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(actionParams.getMerchantId(),actionParams.getTransferCorpId());
        if(channelRelated == null){
            throw new APIDockingException(APIDockingRetCodes.SERVICE_COMPANY_NOT_FOUND.getCode(), APIDockingRetCodes.SERVICE_COMPANY_NOT_FOUND.getDesc());
        }
        qbInvoiceBase.setCompanyId(actionParams.getTransferCorpId());
        qbInvoiceBase.setInvoiceType(Integer.parseInt(invoiceType));

        qbInvoiceBase.setBillingClass(Integer.parseInt(actionParams.getBillingClass()));
        qbInvoiceBase.setServiceType(Integer.parseInt(actionParams.getBillingClass()));
        qbInvoiceBase.setStatus(0);
        String nowDate = DateUtils.getNowDate();
        qbInvoiceBase.setCreateTime(nowDate);
        qbInvoiceBase.setCustomkey(actionParams.getMerchantId());

        if(Integer.parseInt(invoiceType) == InvoiceType.DEDICATED_TYPE.getCode()){

            if(StringUtil.isEmpty(actionParams.getAccountBankName())){
                throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), APIDockingRetCodes.FIELD_LACK.getDesc());
            }
            if(StringUtil.isEmpty(actionParams.getAccountNo())){
                throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), APIDockingRetCodes.FIELD_LACK.getDesc());
            }
            if(StringUtil.isEmpty(actionParams.getAddress())){
                throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), APIDockingRetCodes.FIELD_LACK.getDesc());
            }
            if(StringUtil.isEmpty(actionParams.getPhone())){
                throw new APIDockingException(APIDockingRetCodes.FIELD_LACK.getCode(), APIDockingRetCodes.FIELD_LACK.getDesc());
            }

            String uploadPath = "/taxFile/";
            String taxPicUrl = actionParams.getTaxPicUrl();
            String filePath = uploadPath+qbInvoiceBase.getCustomkey()+"/";
            String uploadError = "error";
            if(!StringUtil.isEmpty(taxPicUrl)){
                byte[] file = HexStringUtil.hexStringToBytes(taxPicUrl);
                String name = UUID.randomUUID().toString().replaceAll("-", "");
                //设置文件上传路径
                String fileName =name+".jpg";
                String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, new ByteArrayInputStream(file), bestSignConfig.getUsername(), bestSignConfig.getPassword());
                if (!uploadError.equals(uploadFile)) {
                    qbInvoiceBase.setTaxPicUrl(filePath+fileName);
                }
            }
            String taxpayerPicUrl = actionParams.getTaxpayerPicUrl();
            if(!StringUtil.isEmpty(taxpayerPicUrl)){
                byte[] taxpayerFile = HexStringUtil.hexStringToBytes(taxpayerPicUrl);
                String taxpayerName = UUID.randomUUID().toString().replaceAll("-", "");
                //设置文件上传路径
                String taxpayerFileName =taxpayerName+".jpg";
                String taxpayerUploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, taxpayerFileName, new ByteArrayInputStream(taxpayerFile), bestSignConfig.getUsername(), bestSignConfig.getPassword());
                if (!uploadError.equals(taxpayerUploadFile)) {
                    qbInvoiceBase.setTaxpayerPicUrl(filePath+taxpayerFileName);
                }
            }

        }

        ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(actionParams.getMerchantId());
        qbInvoiceBase.setAccountBankName(actionParams.getAccountBankName());
        qbInvoiceBase.setAccountNo(actionParams.getAccountNo());
        qbInvoiceBase.setAddress(actionParams.getAddress());
        qbInvoiceBase.setPhone(actionParams.getPhone());
        qbInvoiceBase.setTaxpayerType(channelCustom.getTaxpayerType());
        qbInvoiceBase.setTaxRegistrationNumber(actionParams.getTaxRegistrationNumber());
        qbInvoiceBase.setCompanyName(channelCustom.getCompanyName());
        qbInvoiceBase.setUpdateTime(DateUtils.getNowDate());
        qbInvoiceBaseService.insert(qbInvoiceBase);
        AddMerchantInvoiceInfoServiceAttachment addMerchantInvoiceInfoServiceAttachment = new AddMerchantInvoiceInfoServiceAttachment();
        addMerchantInvoiceInfoServiceAttachment.setAddTime(nowDate);
        return new ActionResult<>(addMerchantInvoiceInfoServiceAttachment);
    }

}
