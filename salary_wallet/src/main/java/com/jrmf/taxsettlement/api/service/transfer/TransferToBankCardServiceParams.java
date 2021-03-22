package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.gateway.NotNull;

public class TransferToBankCardServiceParams extends AbstractTransferServiceParams {
	
	@NotNull
	private String bankCardNo;
		
	private String reservedMobile;

	public String getBankCardNo() {
		return bankCardNo;
	}

	public String getReservedMobile() {
		return reservedMobile;
	}
	
	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}

	public void setReservedMobile(String reservedMobile) {
		this.reservedMobile = reservedMobile;
	}
}
