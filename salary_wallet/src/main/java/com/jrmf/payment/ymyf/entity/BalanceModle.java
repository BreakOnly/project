package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author : chenym
 * @create 2019-09-24 4:35 PM
 **/
public class BalanceModle implements Serializable {
    private String merId;//商户编号
    private List<BalanceQueryItems> balanceQueryItems;

    public static class BalanceQueryItems{

        private String name;
        private String idCard; //身份证号
        private Long levyId;//通道ID
        private int balance;
        private String queryCode = "0"; //返回结果代码，0：成功；-1：失败；
        private String queryMessage; //返回结果描述

        public String getQueryCode() {
            return queryCode;
        }

        public void setQueryCode(String queryCode) {
            this.queryCode = queryCode;
        }

        public String getQueryMessage() {
            return queryMessage;
        }

        public void setQueryMessage(String queryMessage) {
            this.queryMessage = queryMessage;
        }

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public Long getLevyId() {
            return levyId;
        }

        public void setLevyId(Long levyId) {
            this.levyId = levyId;
        }

    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public List<BalanceQueryItems> getBalanceQueryItems() {
        return balanceQueryItems;
    }

    public void setBalanceQueryItems(List<BalanceQueryItems> balanceQueryItems) {
        this.balanceQueryItems = balanceQueryItems;
    }
}
