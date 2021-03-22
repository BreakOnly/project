package com.jrmf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jrmf.bankapi.TransHistoryRecord;
@Service
public interface PingAnBankService {

	public List<TransHistoryRecord> queryTransHistoryPage(String timeStart,String timeEnd,int pageNo);
}
