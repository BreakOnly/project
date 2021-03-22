package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.domain.Page;
import com.jrmf.service.QbInvoiceRecordService;
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
 * @author 种路路
 * @create 2019年8月22日16::53
 * @desc 发票申请记录
 **/
@ActionConfig(name = "发票申请记录")
public class QueryInvoiceHistoryService
        implements Action<QueryInvoiceHistoryServiceParams, QueryInvoiceHistoryServiceAttachment> {

    @Autowired
    private QbInvoiceRecordService qbInvoiceRecordService;

    @Override
    public String getActionType() {
        return APIDefinition.QUERY_INVOICE_HISTORY.name();
    }

    @Override
    public ActionResult<QueryInvoiceHistoryServiceAttachment> execute(
            QueryInvoiceHistoryServiceParams actionParams) {
        String pageNo = actionParams.getPage();
        String size = actionParams.getSize();
        if(!StringUtil.isNumber(pageNo)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
        }
        if(!StringUtil.isNumber(size)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
        }
        Page page = new Page(pageNo,size);
        page.getParams().put("customkey", actionParams.getMerchantId());
        page.getParams().put("status", actionParams.getStatus());
        page.getParams().put("companyId", actionParams.getTransferCorpId());
        List<Integer> countArray = qbInvoiceRecordService.queryRecordListWithPicCount(page);

        List<Map<String, Object>> relationList = qbInvoiceRecordService.queryRecordWithPicList(page);
        List<InvoiceHistory> list = new ArrayList<>();
        for (Map<String, Object> objectMap : relationList) {
            InvoiceHistory invoiceHistory = new InvoiceHistory();
            invoiceHistory.setInvoiceSerialNo(objectMap.get("invoiceSerialNo")+"");
            invoiceHistory.setCompanyName(objectMap.get("companyName")+"");
            invoiceHistory.setTaxpayerType(objectMap.get("taxpayerType")+"");
            invoiceHistory.setTaxRegistrationNumber(objectMap.get("taxRegistrationNumber")+"");
            invoiceHistory.setAccountBankName(objectMap.get("accountBankName")+"");
            invoiceHistory.setAccountNo(objectMap.get("accountNo")+"");
            invoiceHistory.setAddress(objectMap.get("address")+"");
            invoiceHistory.setPhone(objectMap.get("phone")+"");
            invoiceHistory.setBillingClassName(objectMap.get("billingClassName")+"");
            invoiceHistory.setInvoiceAmount(objectMap.get("invoiceAmount")+"");
            invoiceHistory.setInvoiceType(objectMap.get("invoiceType")+"");
            invoiceHistory.setReceiveUser(objectMap.get("receiveUser")+"");
            invoiceHistory.setExpressNo(objectMap.get("expressNo")+"");
            invoiceHistory.setInvoiceTime(objectMap.get("invoiceTime")+"");
            invoiceHistory.setServiceName(objectMap.get("serviceName")+"");
            invoiceHistory.setServiceTypeName(objectMap.get("serviceTypeName")+"");
            invoiceHistory.setOrderNo(objectMap.get("orderNo")+"");
            invoiceHistory.setCreateTime(objectMap.get("createTime")+"");
            invoiceHistory.setInvoiceType(objectMap.get("invoiceType")+"");
            invoiceHistory.setStatus(objectMap.get("status")+"");
            invoiceHistory.setRejectionReason(objectMap.get("rejectionReason")+"");
            invoiceHistory.setIsDiscard(objectMap.get("isDiscard")+"");
            invoiceHistory.setUpdateTime(objectMap.get("updateTime")+"");
            invoiceHistory.setInvoicePicUrl(objectMap.get("invoicePicUrl")+"");
            list.add(invoiceHistory);
        }
        QueryInvoiceHistoryServiceAttachment attachment = new QueryInvoiceHistoryServiceAttachment();
        attachment.setList(list);
        attachment.setTotal(countArray.size()+"");
        return new ActionResult<>(attachment);
    }

}
