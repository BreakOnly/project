package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.domain.Page;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author chonglulu
 */
@ActionConfig(name = "开票收件人地址查询")
public class QueryInvoiceReceiverService implements Action<QueryInvoiceReceiverServiceParams, QueryInvoiceReceiverServiceAttachment> {


    @Autowired
    private CustomInvoiceService customInvoiceService;

    @Override
    public String getActionType() {
        return APIDefinition.QUERY_INVOICE_RECEIVER.name();
    }

    @Override
    public ActionResult<QueryInvoiceReceiverServiceAttachment> execute(QueryInvoiceReceiverServiceParams actionParams) {
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

        //分页查询发票邮寄地址信息
        List<Map<String, Object>> relationList = customInvoiceService.getMerchantInvoiceAddressByPage(page);
        List<InvoiceAddress> list = new ArrayList<>();
        for (Map<String, Object> objectMap : relationList) {
            InvoiceAddress invoiceAddress = new InvoiceAddress();
            invoiceAddress.setReceiverName(objectMap.get("invoiceUserName")+"");
            invoiceAddress.setReceiverAddress(objectMap.get("invoiceAddress")+"");
            invoiceAddress.setMobileNo(objectMap.get("invoicePhone")+"");
            invoiceAddress.setFixedTelephone(objectMap.get("fixedTelephone")+"");
            invoiceAddress.setEmail(objectMap.get("email")+"");
            invoiceAddress.setAddTime(objectMap.get("createTime")+"");
            invoiceAddress.setReceive_id(objectMap.get("id")+"");
            list.add(invoiceAddress);
        }
        //查询发票邮寄地址总条数
        int total = customInvoiceService.getMerchantInvoiceAddressCount(page);
        QueryInvoiceReceiverServiceAttachment attachment = new QueryInvoiceReceiverServiceAttachment();
        attachment.setList(list);
        attachment.setTotal(total+"");
        return new ActionResult<>(attachment);
    }

}
