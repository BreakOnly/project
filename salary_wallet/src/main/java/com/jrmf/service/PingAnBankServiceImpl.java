package com.jrmf.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.QueryTransHistoryPageParams;
import com.jrmf.bankapi.TransHistoryPage;
import com.jrmf.bankapi.TransHistoryRecord;
import com.jrmf.utils.PingAnBankQueryTemplete;

/**
 * @author guoto
 */
@Service("pingAnBankService")
public class PingAnBankServiceImpl implements PingAnBankService {
	private static Logger logger = LoggerFactory.getLogger(PingAnBankServiceImpl.class);

	@Override
	public List<TransHistoryRecord> queryTransHistoryPage(String timeStart, String timeEnd, int pageNo) {
		QueryTransHistoryPageParams params = new QueryTransHistoryPageParams();
		params.setStartDate(timeStart);
		params.setEndDate(timeEnd);
		params.setPageNo(pageNo);
		ActionReturn<TransHistoryPage> ret = PingAnBankQueryTemplete.getBankService().queryTransHistoryPage(params);
		if (ret.isOk()) {
			logger.info("调用平安银行查询交易记录成功");
			List<TransHistoryRecord> transHistoryRecords = ret.getAttachment().getTransHistoryRecords();
			return transHistoryRecords;
		} else {
			logger.info("调用平安银行查询交易记录失败：code="+ret.getRetCode()+" msg=" + ret.getFailMessage());
			List<TransHistoryRecord> transHistoryRecords = new ArrayList<>();
			return transHistoryRecords;
		}
	}
}
