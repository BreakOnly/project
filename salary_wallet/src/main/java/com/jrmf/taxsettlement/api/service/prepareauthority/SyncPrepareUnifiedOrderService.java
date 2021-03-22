package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.domain.UserCommission;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * @author chonglulu
 */
@ActionConfig(name = "同步预授权下发结果")
public class SyncPrepareUnifiedOrderService implements Action<SyncPrepareUnifiedOrderServiceParams, SyncPrepareUnifiedOrderServiceAttachment> {


    private static final Logger logger = LoggerFactory.getLogger(SyncPrepareUnifiedOrderService.class);

    private final CustomLimitConfService customLimitConfService;
    private final UserCommissionService userCommissionService;

    @Autowired
    public SyncPrepareUnifiedOrderService(CustomLimitConfService customLimitConfService, UserCommissionService userCommissionService) {
        this.customLimitConfService = customLimitConfService;
        this.userCommissionService = userCommissionService;
    }

    @Override
    public String getActionType() {
        return APIDefinition.SYNC_PREPARE_UNIFIED_ORDER.name();
    }

    @Override
    public ActionResult<SyncPrepareUnifiedOrderServiceAttachment> execute(SyncPrepareUnifiedOrderServiceParams actionParams) {
        HashMap<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("originalId", actionParams.getMerchantId());
        paramMap.put("customOrderNo", actionParams.getCustomOrderNo());
        paramMap.put("status", "S".equals(actionParams.getStatus()) ? 1 : 2);
        paramMap.put("originalStatus", 5);
        paramMap.put("statusDesc", actionParams.getMessage());
        paramMap.put("orderNo", actionParams.getDealNo());
        paramMap.put("regType", "02");
        int i = userCommissionService.updateUndifiedOrder(paramMap);
        if (i != 1) {
            throw new APIDockingException(APIDockingRetCodes.ORDER_IS_DONE_OR_NOT_FOUND.getCode(), APIDockingRetCodes.ORDER_IS_DONE_OR_NOT_FOUND.getDesc());
        }
        if (!"S".equals(actionParams.getStatus())) {
            logger.info("下发失败" + actionParams.getMessage());
            UserCommission userCommission = userCommissionService.getUserCommission(actionParams.getDealNo());
            if (userCommission == null) {
                throw new APIDockingException(APIDockingRetCodes.ORDER_IS_DONE_OR_NOT_FOUND.getCode(), APIDockingRetCodes.ORDER_IS_DONE_OR_NOT_FOUND.getDesc());
            }
            logger.info("扣减余额记录");
            customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(), userCommission.getOriginalId(), userCommission.getCertId(), userCommission.getAmount(), false);
        }
        return new ActionResult<>();
    }

}
