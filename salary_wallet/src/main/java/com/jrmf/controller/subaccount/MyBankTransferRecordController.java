package com.jrmf.controller.subaccount;

import com.jrmf.bankapi.pingansub.PinganBankTransactionConstants;
import com.jrmf.controller.constant.ConfirmStatus;
import com.jrmf.controller.constant.CustomTransferRecordType;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.common.constant.GatewayConstant;
import com.jrmf.payment.mybankapi.common.util.DataConvertUtil;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomReceiveConfigService;
import com.jrmf.service.CustomTransferRecordService;
import com.jrmf.utils.ArithmeticUtil;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mybanksub")
public class MyBankTransferRecordController {

  private static Logger logger = LoggerFactory.getLogger(PingAnTransferResultController.class);

  @Autowired
  private CustomTransferRecordService customTransferRecordService;
  @Autowired
  CustomReceiveConfigService customReceiveConfigService;
  @Autowired
  private CompanyService companyService;

  /**
   * 自动充值回调
   */
  @RequestMapping(value = "/receiveTransferRecord")
  @ResponseBody
  public String receiveTransferRecord(HttpServletRequest request) {

    try {

      logger.info("网商银行通知参数 : " + request.getParameterMap().toString());

      Map<String, String> paramCharsetConvert = DataConvertUtil
          .paramCharsetConvert(request.getParameterMap(),
              GatewayConstant.charset_utf_8);
      // 验证签名
//      boolean verify = securityService
//          .verify(paramCharsetConvert, GatewayConstant.charset_utf_8,
//              paramCharsetConvert.get("sign"),
//              paramCharsetConvert.get("sign_type"));
//
//      if (verify) {

      String notifyType = paramCharsetConvert.get("notify_type");

      if (!BaseRequestConstant.NOTIFY_TYPE.equals(notifyType)) {
        return "success";
      }
      String flag = CustomTransferRecordType.SUBACCOUNTINTO.getFlag();
      String subAccount = paramCharsetConvert.get("subAccount");
      //根据子账户号获取商户下子账户信息
      CustomReceiveConfig customReceiveConfig = customReceiveConfigService.getCustomReceiveConfigBySubAccount(subAccount);
      if (customReceiveConfig == null){
        logger.info("获取网商银行子账户失败,子账户号:{}",subAccount);
        return "success";
      }
      //获取主账户信息-根据服务公司和通道
      Map<String,String> map = companyService.getCompanyPayChannelRelation(customReceiveConfig.getCompanyId(),PaymentFactory.MYBANK);
      if (map == null){
        logger.info("获取网商银行通道信息失败");
        return "success";
      }

      String oppAccountNo = paramCharsetConvert.get("payerCardNo");
      String oppAccountName = paramCharsetConvert.get("payerCardName");

      String tranTime = paramCharsetConvert.get("gmt_create");
      String mainAccount = map.get("corporationAccount");
      String mainAccountName = map.get("corporationAccountName");
      String tranAmount = ArithmeticUtil.addZeroAndDot(paramCharsetConvert.get("remitAmount"));
      String oppBankName = "";
      String oppBankNo = paramCharsetConvert.get("payerBankOrgId");
      String remark = paramCharsetConvert.get("payerRemark");
      String bizFlowNo = paramCharsetConvert.get("txId");
      String subAccoutName = paramCharsetConvert.get("subAccountName");

      CustomTransferRecord record = new CustomTransferRecord();
      record.setCustomKey(customReceiveConfig.getCustomkey());
      record.setCompanyId(customReceiveConfig.getCompanyId());
      record.setPathNo(PaymentFactory.MYBANK);
      record.setSubAccount(subAccount);
      record.setSubAccoutName(subAccoutName);
      record.setTranTime(tranTime);
      record.setMainAccount(mainAccount);
      record.setMainAccountName(mainAccountName);
      record.setFlag(flag);
      record.setTranAmount(tranAmount);
      record.setOppAccountNo(oppAccountNo);
      record.setOppAccountName(oppAccountName);
      record.setOppBankName(oppBankName);
      record.setOppBankNo(oppBankNo);
      record.setRemark(remark);
      record.setBizFlowNo(bizFlowNo);
      record.setIsConfirm(ConfirmStatus.FAILURE.getCode());
      record.setTranType(CustomTransferRecordType.codeOfFlag(record.getFlag()).getCode());
      customTransferRecordService.insertWithPathNo(record);

//      } else {
//        logger.error("网商银行充值回调验签失败");
//      }
      return "success";


    } catch (DuplicateKeyException e) {
      logger.info("-----------网商银行充值回调转账记录流水号重复---------");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return "fail";

  }

}
