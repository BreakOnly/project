package com.jrmf.service;

import java.util.Map;

import org.springframework.stereotype.Service;
@Service
public interface AlipayService {

	public String singleTransferAccounts(Map<String, Object> map);

//	public void batchTransferAccounts(List<Map<String, Object>> list, ExecuteBatch eb);
}
