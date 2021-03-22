package com.jrmf.payment.entity;

import com.jrmf.bankapi.*;
import com.jrmf.bankapi.hnb.*;
import com.jrmf.bankapi.hnb.transaction.QueryBalanceReportTemplate;
import com.jrmf.bankapi.hnb.transaction.QueryTransferResultReportTemplate;
import com.jrmf.bankapi.hnb.transaction.SubmitTransferReportTemplate;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class HuiYongGongHnbBank implements Payment<SubmitTransferParams, ActionReturn, String> {

	private Logger logger = LoggerFactory.getLogger(HuiYongGongHnbBank.class);

	public PaymentConfig payment;

	public HuiYongGongHnbBank(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

		HnBankAccountInfo bankAccountInfo = new HnBankAccountInfo(payment.getParameter1(),
				payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName(),
				payment.getPayPublicKey(),
				payment.getPayPrivateKey(),
				payment.getParameter2());

		Map<String, TransReportTemplate<?,?>> templates = new HashMap<>(3);
		templates.put(HnBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new HttpDataExchanger(payment.getPreHost(), payment.getReadTimeOut());

		BankService hnBankService = new HnBankService(bankAccountInfo,
				reportFactory,
				exchanger);

		ActionReturn<String> ret = null;
		ret = hnBankService.submitTransfer(getTransferTemple(userCommission));

        return  getTransferResult(ret);
	}

	@Override
	public SubmitTransferParams getTransferTemple(UserCommission userCommission) {

        SubmitTransferParams params = new SubmitTransferParams();
        params.setTransferAmount(ArithmeticUtil.mulStr("100", userCommission.getAmount()));
        params.setTransferInAccountName(userCommission.getUserName());
        params.setTransferInAccountNo(userCommission.getAccount());
        params.setTransferInBankName(userCommission.getBankName());
        params.setTransferSerialNo(userCommission.getOrderNo());

        //兼容现金管理代理结算时只能输入13个汉字
        String commissionRemark = userCommission.getRemark() == null ? "" : userCommission.getRemark();
		if (commissionRemark.length() > 20) {
			commissionRemark = commissionRemark.substring(0, 20);
		}
        params.setRemark(commissionRemark);

        if(!StringUtil.isEmpty(userCommission.getBankNo())){
            params.setTransferInBankOrgNo(userCommission.getBankNo());
        }

        logger.info("海南农信银企上送下发参数："+params.getTransferSerialNo()+"-------"+
                params.getTransferAmount()+"-------"+
                params.getTransferInAccountNo()+"-------"+
                params.getTransferInBankOrgNo()+"-------"+
                params.getTransferInBankName()+"-------"+
                params.getTransferInAccountName()+"-------");

		return params;
	}

	@Override
	public PaymentReturn<String> getTransferResult(ActionReturn paramter) {

		String retCode = paramter.getRetCode();
		String message = paramter.getFailMessage();
		if(CommonRetCodes.INVOCATION_NO_RESULT.getCode().equals(retCode)
				|| CommonRetCodes.UNEXPECT_ERROR.getCode().equals(retCode)){
			retCode = PayRespCode.RESP_UNKNOWN;
			message = CommonRetCodes.INVOCATION_NO_RESULT.getDesc();
		}

		PaymentReturn<String> transferReturn = new PaymentReturn<>(retCode,
				message,
				String.valueOf(paramter.getAttachment()));

		  logger.info("海南农信银企下发返回参数：" + transferReturn.toString());

		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

		HnBankAccountInfo bankAccountInfo = new HnBankAccountInfo(payment.getParameter1(),
				payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName(),
				payment.getPayPublicKey(),
				payment.getPayPrivateKey(),
				payment.getParameter2());


		Map<String, TransReportTemplate<?,?>> templates = new HashMap<String, TransReportTemplate<?,?>>();
		templates.put(HnBankTransactions.QUERY_TRANSFER_RESULT, new QueryTransferResultReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new HttpDataExchanger(payment.getPreHost(),
				payment.getReadTimeOut());

		BankService hnBankService = new HnBankService(bankAccountInfo,
				reportFactory,
				exchanger);

		ActionReturn<TransferResult> transferResult = hnBankService.queryTransferResult(orderNo);

		String code = "";
		String massage = "";
		String transCode = "";
		String transMsg = "";
		String transOrderNo = orderNo;

		if(transferResult.isOk()){
			code = PayRespCode.RESP_SUCCESS;
			if(TransferResult.TransferResultType.SUCCESS.equals(transferResult.getAttachment().getResultType())){
				transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
			}else if(TransferResult.TransferResultType.FAIL.equals(transferResult.getAttachment().getResultType())){
				transCode = PayRespCode.RESP_TRANSFER_FAILURE;
			}else{
				transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
			}
		}else if(PayRespCode.PA_RESP_ORDER_NOEXISTS.equals(transferResult.getRetCode())){
			code = PayRespCode.RESP_SUCCESS;
			transCode = PayRespCode.RESP_TRANSFER_FAILURE;
			transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_NETWORK_EXCEPTION);
		}else{
			code = PayRespCode.RESP_FAILURE;
		}
		massage = transferResult.getFailMessage();
		TransferResult transResult = transferResult.getAttachment();
		if(transResult != null){
			transMsg = transResult.getResultMsg();
		}

		TransStatus transStatus = new TransStatus(transOrderNo,
				transCode,
				transMsg);

		PaymentReturn<TransStatus>  paymentReturn = new PaymentReturn<TransStatus>(code,
				massage,
				transStatus);

		return paymentReturn;
	}

	@Override
	public PaymentReturn<String> queryBalanceResult(String type) {

		HnBankAccountInfo bankAccountInfo = new HnBankAccountInfo(payment.getParameter1(),
				payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName(),
				payment.getPayPublicKey(),
				payment.getPayPrivateKey(),
				payment.getParameter2());

		Map<String, TransReportTemplate<?,?>> templates = new HashMap<String, TransReportTemplate<?,?>>();
		templates.put(HnBankTransactions.QUERY_BALANCE, new QueryBalanceReportTemplate());

		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new HttpDataExchanger(payment.getPreHost(),
				payment.getReadTimeOut());

		BankService hnBankService = new HnBankService(bankAccountInfo, reportFactory, exchanger);

		ActionReturn<String> paramter = hnBankService.queryBalace();

		PaymentReturn<String> transferReturn = new PaymentReturn<String>(paramter.getRetCode(),
				paramter.getFailMessage(),
				String.valueOf(paramter.getAttachment()));

		logger.info("海南农信银企查询返回参数：" + transferReturn.toString());

		return transferReturn;
	}

	@Override
	public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
		return null;
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory queryParams) {
		return null;
	}


}
