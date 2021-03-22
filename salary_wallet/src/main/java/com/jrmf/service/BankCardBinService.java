package com.jrmf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jrmf.domain.BankCard;
import com.jrmf.domain.BankName;

@Service
public interface BankCardBinService {

	public BankName getBankName(String cardNo);
	
	public List<BankCard> getbankcardAll();
	
}
