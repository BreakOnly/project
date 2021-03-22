package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.gateway.NotNull;

public class TransferToAlipayAccountServiceParams extends AbstractTransferServiceParams {
	
	@NotNull
	private String alipayAccountNo;

	@NotNull
	private String reservedMobile;
	
	public String getAlipayAccountNo() {
		return alipayAccountNo;
	}

	public void setAlipayAccountNo(String alipayAccountNo) {
		this.alipayAccountNo = alipayAccountNo;
	}

	public String getReservedMobile() {
		return reservedMobile;
	}

	public void setReservedMobile(String reservedMobile) {
		this.reservedMobile = reservedMobile;
	}
}
