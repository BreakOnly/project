package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.domain.vo.CustomInvoiceInfoVO;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 种路路
 * @create 2019-10-12 10:20
 * @desc 新增开票收件人地址
 **/
@ActionConfig(name = "新增开票收件人地址")
public class AddInvoiceReceiverService
        implements Action<AddInvoiceReceiverServiceParams, AddInvoiceReceiverServiceAttachment> {

    @Autowired
    private CustomInvoiceService customInvoiceService;

    @Override
    public String getActionType() {
        return APIDefinition.ADD_INVOICE_RECEIVER.name();
    }

    @Override
    public ActionResult<AddInvoiceReceiverServiceAttachment> execute(
            AddInvoiceReceiverServiceParams actionParams) {
        String mobileNo = actionParams.getMobileNo();
        if(!StringUtil.isMobileNOBy11(mobileNo)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc()+","+mobileNo);
        }
        CustomInvoiceInfoVO customInvoiceInfoVO = new CustomInvoiceInfoVO();
        customInvoiceInfoVO.setInvoiceAddress(actionParams.getReceiverAddress());
        customInvoiceInfoVO.setInvoicePhone(mobileNo);
        customInvoiceInfoVO.setInvoiceUserName(actionParams.getReceiverName());
        customInvoiceInfoVO.setFixedTelephone(actionParams.getFixedTelephone());
        customInvoiceInfoVO.setEmail(actionParams.getEmail());
        customInvoiceInfoVO.setCustomkey(actionParams.getMerchantId());

        //添加发票地址信息
        customInvoiceService.addCustomInvoiceInfo(customInvoiceInfoVO);
        AddInvoiceReceiverServiceAttachment attachment = new AddInvoiceReceiverServiceAttachment();
        attachment.setAddTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return new ActionResult<>(attachment);
    }

}
