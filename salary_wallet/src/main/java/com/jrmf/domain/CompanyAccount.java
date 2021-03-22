package com.jrmf.domain;

/**
 * 商户余额信息
 *
 * @author linsong
 * @date 2019/4/8
 */
public class CompanyAccount {

    private String customName; //商户
    private String companyName; //服务公司
    private String waitConfirmedBalance; //充值待确认余额
    private String balanceSum; //充值账户汇总可用余额
    private String bankCardBalance; //银行卡余额
    private String alipayBlance;    //支付宝余额
    private String wechatBalance;   //微信余额
    private String customkey;
    private String companyId;

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWaitConfirmedBalance() {
        return waitConfirmedBalance;
    }

    public void setWaitConfirmedBalance(String waitConfirmedBalance) {
        this.waitConfirmedBalance = waitConfirmedBalance;
    }

    public String getBalanceSum() {
        return balanceSum;
    }

    public void setBalanceSum(String balanceSum) {
        this.balanceSum = balanceSum;
    }

    public String getBankCardBalance() {
        return bankCardBalance;
    }

    public void setBankCardBalance(String bankCardBalance) {
        this.bankCardBalance = bankCardBalance;
    }

    public String getAlipayBlance() {
        return alipayBlance;
    }

    public void setAlipayBlance(String alipayBlance) {
        this.alipayBlance = alipayBlance;
    }

    public String getWechatBalance() {
        return wechatBalance;
    }

    public void setWechatBalance(String wechatBalance) {
        this.wechatBalance = wechatBalance;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
