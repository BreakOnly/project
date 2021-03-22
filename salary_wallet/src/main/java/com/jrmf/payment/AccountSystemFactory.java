package com.jrmf.payment;

import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.entity.AccountSystem;
import com.jrmf.payment.entity.MyBankAccountSystem;
import com.jrmf.payment.entity.PingAnBankAccountSystem;

public class AccountSystemFactory {

  public static final String PAKHKF = "000001";//平安跨行快付
  public static final String PAYQZL = "000002";//平安银企直联
  public static final String MYBANK = "000022";//网商银行

  public static AccountSystem accountSystemEntity(PaymentConfig payment) {
    switch (payment.getPathNo()) {
      case PAKHKF:
      case PAYQZL:
        return new PingAnBankAccountSystem(payment);
      case MYBANK:
        return new MyBankAccountSystem(payment);
      default:
        return null;
    }
  }

}
