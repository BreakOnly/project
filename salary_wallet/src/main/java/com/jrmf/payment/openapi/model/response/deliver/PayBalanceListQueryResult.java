/**
 *
 */
package com.jrmf.payment.openapi.model.response.deliver;

import com.jrmf.payment.openapi.model.response.IBizResult;

import java.math.BigDecimal;

/**
 * @author Napoleon.Chen
 * @date 2019年2月21日
 */
public class PayBalanceListQueryResult implements IBizResult {

    private String account;
    private String accountName;
    private Long serviceCompanyId;
    private String serviceCompanyName;
    private BigDecimal totalBalance;
    private BigDecimal bankBalance;
    private BigDecimal alipayBalance;
    private BigDecimal wxBalance;

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @return the accountName
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * @param accountName the accountName to set
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * @return the serviceCompanyId
     */
    public Long getServiceCompanyId() {
        return serviceCompanyId;
    }

    /**
     * @param serviceCompanyId the serviceCompanyId to set
     */
    public void setServiceCompanyId(Long serviceCompanyId) {
        this.serviceCompanyId = serviceCompanyId;
    }

    /**
     * @return the serviceCompanyName
     */
    public String getServiceCompanyName() {
        return serviceCompanyName;
    }

    /**
     * @param serviceCompanyName the serviceCompanyName to set
     */
    public void setServiceCompanyName(String serviceCompanyName) {
        this.serviceCompanyName = serviceCompanyName;
    }

    /**
     * @return the totalBalance
     */
    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    /**
     * @param totalBalance the totalBalance to set
     */
    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    /**
     * @return the bankBalance
     */
    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    /**
     * @param bankBalance the bankBalance to set
     */
    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance;
    }

    /**
     * @return the alipayBalance
     */
    public BigDecimal getAlipayBalance() {
        return alipayBalance;
    }

    /**
     * @param alipayBalance the alipayBalance to set
     */
    public void setAlipayBalance(BigDecimal alipayBalance) {
        this.alipayBalance = alipayBalance;
    }

    /**
     * @return the wxBalance
     */
    public BigDecimal getWxBalance() {
        return wxBalance;
    }

    /**
     * @param wxBalance the wxBalance to set
     */
    public void setWxBalance(BigDecimal wxBalance) {
        this.wxBalance = wxBalance;
    }

}
