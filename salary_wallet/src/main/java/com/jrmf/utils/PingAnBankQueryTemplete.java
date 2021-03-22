package com.jrmf.utils;

import java.util.HashMap;
import java.util.Map;

import com.jrmf.bankapi.BankService;
import com.jrmf.bankapi.pingan.DataExchanger;
import com.jrmf.bankapi.pingan.PinganBankAccountInfo;
import com.jrmf.bankapi.pingan.PinganBankService;
import com.jrmf.bankapi.pingan.PinganBankTransactions;
import com.jrmf.bankapi.pingan.SocketDataExchanger;
import com.jrmf.bankapi.pingan.TransReportTemplate;
import com.jrmf.bankapi.pingan.TransReportTemplates;
import com.jrmf.bankapi.pingan.transaction.QueryBalanceReportTemplate;
import com.jrmf.bankapi.pingan.transaction.QueryHistoryTransferResultReportTemplate;
import com.jrmf.bankapi.pingan.transaction.QueryTransferResultReportTemplate;
import com.jrmf.bankapi.pingan.transaction.SubmitTransferReportTemplate;

public class PingAnBankQueryTemplete {
	public static BankService pinganBankService;
	public static Map<String, TransReportTemplate<?, ?>> templates;
	public static TransReportTemplates reportFactory;
	public static DataExchanger exchanger;
	public static PinganBankAccountInfo bankAccountInfo;
	static {
		templates = new HashMap<String, TransReportTemplate<?, ?>>();
		templates.put(PinganBankTransactions.QUERY_BALANCE, new QueryBalanceReportTemplate());
		templates.put(PinganBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());
		templates.put(PinganBankTransactions.QUERY_TRANSFER_RESULT, new QueryTransferResultReportTemplate());
		templates.put(PinganBankTransactions.QUERY_HISTORY_TRANSFER_RESULT,
				new QueryHistoryTransferResultReportTemplate());
		reportFactory = new TransReportTemplates(templates);
		exchanger = new SocketDataExchanger("192.168.193.231", 7072, 60000);
		bankAccountInfo = new PinganBankAccountInfo("15000094761748", "泉州金财信息科技有限公司", "00901250000000817000",
				"平安银行泉州分行营业部");
	}

	public static BankService getBankService() {
		if (pinganBankService != null) {
			return pinganBankService;
		}
		pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);
		return pinganBankService;
	}
}
