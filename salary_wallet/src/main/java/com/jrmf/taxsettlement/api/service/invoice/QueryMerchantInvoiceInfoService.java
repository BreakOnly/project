package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.domain.Page;
import com.jrmf.service.QbInvoiceBaseService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author chonglulu
 */
@ActionConfig(name = "商户开票信息查询")
public class QueryMerchantInvoiceInfoService implements Action<QueryMerchantInvoiceInfoServiceParams, QueryMerchantInvoiceInfoServiceAttachment> {


    @Autowired
    private QbInvoiceBaseService qbInvoiceBaseService;

    @Override
    public String getActionType() {
        return APIDefinition.QUERY_MERCHANT_INVOICE_INFO.name();
    }

    @Override
    public ActionResult<QueryMerchantInvoiceInfoServiceAttachment> execute(QueryMerchantInvoiceInfoServiceParams actionParams) {
        String pageNo = actionParams.getPage();
        String size = actionParams.getSize();
        if(!StringUtil.isNumber(pageNo)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
        }
        if(!StringUtil.isNumber(size)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
        }
        int sizeMax = 20;
        if(Integer.parseInt(size)>sizeMax){
            size = "20";
        }
        Page page = new Page(pageNo,size);
        page.getParams().put("customkey", actionParams.getMerchantId());
        page.getParams().put("companyId", actionParams.getTransferCorpId());

        List<Map<String, Object>> relationList = qbInvoiceBaseService.queryInvoiceBaseList(page);
        List<MerchantInvoiceInfo> list = new ArrayList<>();
        for (Map<String, Object> objectMap : relationList) {
            MerchantInvoiceInfo merchantInvoiceInfo = new MerchantInvoiceInfo();
            merchantInvoiceInfo.setTransferCorpId(objectMap.get("companyId")+"");
            merchantInvoiceInfo.setAddTime(objectMap.get("createTime")+"");
            merchantInvoiceInfo.setBillingClass(objectMap.get("billingClass")+"");
            merchantInvoiceInfo.setInfoId(objectMap.get("id")+"");
            merchantInvoiceInfo.setInvoiceType(objectMap.get("invoiceType")+"");
            merchantInvoiceInfo.setStatus(objectMap.get("status")+"");
            merchantInvoiceInfo.setTaxpayerType(objectMap.get("taxpayerType")+"");
            merchantInvoiceInfo.setTaxRegistrationNumber(objectMap.get("taxRegistrationNumber")+"");
            merchantInvoiceInfo.setAccountBankName(objectMap.get("accountBankName")+"");
            merchantInvoiceInfo.setPhone(objectMap.get("phone")+"");
            merchantInvoiceInfo.setAddress(objectMap.get("address")+"");
            merchantInvoiceInfo.setAccountNo(objectMap.get("accountNo")+"");
            list.add(merchantInvoiceInfo);
        }
        int total = qbInvoiceBaseService.queryInvoiceBaseListCount(page);
        QueryMerchantInvoiceInfoServiceAttachment attachment = new QueryMerchantInvoiceInfoServiceAttachment();
        attachment.setList(list);
        attachment.setTotal(total+"");
        return new ActionResult<>(attachment);
    }

}
