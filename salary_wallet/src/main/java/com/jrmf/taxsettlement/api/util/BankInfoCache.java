package com.jrmf.taxsettlement.api.util;

import com.jrmf.domain.BankCard;
import com.jrmf.persistence.TransferBankDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;

public class BankInfoCache {

	@Autowired
	private TransferBankDao transferBankDao;

	private Map<String, String> bankTable = new HashMap<String, String>();

	private Map<String, String> bankBinTable = new HashMap<String, String>();

	private List<Integer> binLengthList = new ArrayList<Integer>();

	@PostConstruct
	public void test() {

		for(BankCode bankCode : transferBankDao.getAllBankCodes()) {
            bankTable.put(bankCode.getBankName(), bankCode.getBankCode());
        }

		Set<Integer> binLengthSet = new HashSet<Integer>();

		for(BankCard bankCard : transferBankDao.getbankcardAll()) {
			bankBinTable.put(bankCard.getStart(), bankCard.getBankName());
			binLengthSet.add(bankCard.getStartLength());
		}

		binLengthList.addAll(binLengthSet);
		binLengthList.sort(new Comparator<Integer>(){

			@Override
			public int compare(Integer arg0, Integer arg1) {
				return arg1.intValue() - arg0.intValue();
			}

		});
	}

	public String[] searchBankInfo(String cardNo) {

		for(Integer len : binLengthList) {
			String cardBin = cardNo.substring(0, len.intValue());
			String bankName = bankBinTable.get(cardBin);
			if(StringUtils.isNotEmpty(bankName)) {
				return new String[]{bankName, bankTable.get(bankName)};
			}
		}

		return null;
	}
}
