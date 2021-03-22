package com.jrmf.payment.entity;

import com.jrmf.bankapi.*;
import com.jrmf.bankapi.pingan.*;
import com.jrmf.bankapi.pingan.transaction.*;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class PingAnBankYqzl implements Payment<SubmitTransferParams, ActionReturn, String> {

	private Logger logger = LoggerFactory.getLogger(PingAnBankYqzl.class);

	public PaymentConfig payment;

	public PingAnBankYqzl(PaymentConfig payment) {
		super();
		this.payment = payment;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName());

		Map<String, TransReportTemplate<?,?>> templates = new HashMap<>(3);
		templates.put(PinganBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		BankService pinganBankService = new PinganBankService(bankAccountInfo,
				reportFactory,
				exchanger);

		ActionReturn<String> ret = null;
		ret = pinganBankService.submitTransfer(getTransferTemple(userCommission));

        return  getTransferResult(ret);
	}

	@Override
	public SubmitTransferParams getTransferTemple(UserCommission userCommission) {

        SubmitTransferParams params = new SubmitTransferParams();
        params.setTransferAmount(userCommission.getAmount());
        params.setTransferInAccountName(userCommission.getUserName());
        params.setTransferInAccountNo(userCommission.getAccount());
        params.setTransferInBankName(userCommission.getBankName());
        params.setTransferSerialNo(userCommission.getOrderNo());

        //兼容现金管理代理结算时只能输入13个汉字
        String commissionRemark = userCommission.getRemark() == null ? "" : userCommission.getRemark();
		if (commissionRemark.length() > 13) {
			commissionRemark = commissionRemark.substring(0, 13);
		}

        params.setRemark(commissionRemark);

        if(!StringUtil.isEmpty(userCommission.getBankNo())){
            params.setTransferInBankOrgNo(userCommission.getBankNo());
        }

        logger.info("金财银企直联上送下发参数："+params.getTransferSerialNo()+"-------"+
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

//		String retCode = "A0ZU".equals(paramter.getRetCode())?"0001":paramter.getRetCode();
		PaymentReturn<String> transferReturn = new PaymentReturn<>(retCode,
				message,
				String.valueOf(paramter.getAttachment()));

		  logger.info("金财银企直联下发返回参数：" + transferReturn.toString());

		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName());

		Map<String, TransReportTemplate<?,?>> templates = new HashMap<String, TransReportTemplate<?,?>>();
		templates.put(PinganBankTransactions.QUERY_TRANSFER_RESULT, new QueryTransferResultReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		BankService pinganBankService = new PinganBankService(bankAccountInfo,
				reportFactory,
				exchanger);

		ActionReturn<TransferResult> transferResult = pinganBankService.queryTransferResult(orderNo);

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
		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName());
		Map<String, TransReportTemplate<?,?>> templates = new HashMap<String, TransReportTemplate<?,?>>();
		templates.put(PinganBankTransactions.QUERY_BALANCE, new QueryBalanceReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);
		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		BankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);
		ActionReturn<String> paramter = pinganBankService.queryBalace();
		PaymentReturn<String> transferReturn = new PaymentReturn<String>(paramter.getRetCode(),
				paramter.getFailMessage(),
				String.valueOf(paramter.getAttachment()));
		logger.info("银企直联查询返回参数：" + transferReturn.toString());
		return transferReturn;
	}

	@Override
	public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getCorporationName());

		Map<String, TransReportTemplate<?, ?>> templates = new HashMap<>(3);
		templates.put(PinganBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());
		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		BankService pinganBankService = new PinganBankService(bankAccountInfo,
				reportFactory,
				exchanger);

		SubmitTransferParams params = new SubmitTransferParams();
		params.setTransferAmount(transferRecord.getTranAmount());
		params.setTransferInAccountName(transferRecord.getInAccountName());
		params.setTransferInAccountNo(transferRecord.getInAccountNo());
		params.setTransferInBankName(transferRecord.getInBankName());
		params.setTransferSerialNo(transferRecord.getOrderNo());

		String remark = transferRecord.getTranRemark() == null ? "" : transferRecord.getTranRemark();
		params.setRemark(remark);

		if (!StringUtil.isEmpty(transferRecord.getInBankNo())) {
			params.setTransferInBankOrgNo(transferRecord.getInBankNo());
		}

		logger.info("平安银企直联联动交易转账上送参数：" + params.getTransferSerialNo() + "-------" +
				params.getTransferAmount() + "-------" +
				params.getTransferInAccountNo() + "-------" +
				params.getTransferInBankOrgNo() + "-------" +
				params.getTransferInBankName() + "-------" +
				params.getTransferInAccountName() + "-------");

		ActionReturn<String> ret = pinganBankService.submitTransfer(params);

		return getTransferResult(ret);
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory params) {

		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getBankName());

		Map<String, TransReportTemplate<?,?>> templates = new HashMap<String, TransReportTemplate<?,?>>();
		templates.put(PinganBankTransactions.QUERY_HISTORY_TRANSFER_RESULT, new LinkageQueryHistoryTransferResultReportTemplate());

		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		BankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);

		LinkageQueryTransHistoryPageParams queryParams = new LinkageQueryTransHistoryPageParams();
		queryParams.setStartDate(params.getStartDate());
		queryParams.setEndDate(params.getEndDate());
		queryParams.setPageNo(params.getPageNo());
		queryParams.setPageSize(params.getPageSize()+"");

		ActionReturn<LinkageTransHistoryPage> paramter = pinganBankService.queryTransHistoryPageNew(queryParams);
		PaymentReturn<LinkageTransHistoryPage> transferReturn = new PaymentReturn<LinkageTransHistoryPage>(paramter.getRetCode(),
				paramter.getFailMessage(),
				paramter.getAttachment());


		return transferReturn;

	}

	public List<ReceiptFileResult> queryTransHistoryFile(String queryDate) {

		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getBankName());

		Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
		templates.put(PinganBankTransactions.QUERY_HISTORY_TRANSFER_FILE_RESULT, new QueryHistoryTransferFileResultReportTemplate());

		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);


		return pinganBankService.queryTransHistoryFile(queryDate, null);
	}


	public void DowloadQueryTransHistoryFile(String tradeSn, String fileName, String randomPwd) {

		PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
				payment.getCorporationAccountName(),
				payment.getCorpToBankStandardCode(),
				payment.getBankName());

		Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
		templates.put(PinganBankTransactions.DOWNLOAD_HISTORY_TRANSFER_FILE, new DowloadHistoryTransferFileReportTemplate());

		TransReportTemplates reportFactory = new TransReportTemplates(templates);

		DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
				payment.getRemotePort(),
				payment.getReadTimeOut());

		PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);

		pinganBankService.DowloadQueryTransHistoryFile(tradeSn, fileName, null, randomPwd);
//        if (ret.isOk()) {
//            System.out.println("$$$$$$$$$$1-" + ret.getRetCode() + ret.getFailMessage() + ret.getAttachment().getResultMsg());
//        } else {
//            System.out.println("##########2-" + ret.getRetCode() + ret.getFailMessage());
//        }
	}


}
