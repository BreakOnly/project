package com.jrmf.payment.openapi.model.response.deliver;

import java.math.BigDecimal;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class PayBalanceDetailQueryResult implements IBizResult {

	private String account;
    private String accountName;
    private BigDecimal totalBalance;
    private BigDecimal bankBalance;
    private BigDecimal alipayBalance;
    private BigDecimal wxBalance;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance;
    }

    public BigDecimal getAlipayBalance() {
        return alipayBalance;
    }

    public void setAlipayBalance(BigDecimal alipayBalance) {
        this.alipayBalance = alipayBalance;
    }

    public BigDecimal getWxBalance() {
        return wxBalance;
    }

    public void setWxBalance(BigDecimal wxBalance) {
        this.wxBalance = wxBalance;
    }
	
}
