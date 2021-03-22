package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.service.UserSerivce;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jrmf
 */
@ActionConfig(name = "增加商户下发收款者")
public class AddMerchantTransferReceiverService
		implements Action<AddMerchantTransferReceiverServiceParams, AddMerchantTransferReceiverServiceAttachment> {

	@Autowired
	private UserSerivce userService;

	@Override
	public String getActionType() {
		return APIDefinition.ADD_MERCHANT_TRANSFER_RECEIVER.name();
	}

	@Override
	public ActionResult<AddMerchantTransferReceiverServiceAttachment> execute(
			AddMerchantTransferReceiverServiceParams actionParams) {

        userService.addUserInfo(actionParams.getName(), 1, actionParams.getCertificateNo().toUpperCase(), null,
                actionParams.getMobileNo(), actionParams.getMerchantId(), actionParams.getPartnerId(), "");

        AddMerchantTransferReceiverServiceAttachment attachment = new AddMerchantTransferReceiverServiceAttachment();
        attachment.setAddTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return new ActionResult<>(attachment);
    }

}
