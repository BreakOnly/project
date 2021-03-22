package com.jrmf.payment;

import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.dulidaypay.DulidayPayment;
import com.jrmf.payment.entity.AygPayment;
import com.jrmf.payment.entity.GsbPayment;
import com.jrmf.payment.entity.HYGPayment;
import com.jrmf.payment.entity.HddPayment;
import com.jrmf.payment.entity.HuiYongGongHnbBank;
import com.jrmf.payment.entity.AliPayPayment;
import com.jrmf.payment.entity.MyBankPayment;
import com.jrmf.payment.entity.PingAnBankYqzl;
import com.jrmf.payment.entity.NewPayPayment;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.entity.PingAnBankKhkf;
import com.jrmf.payment.entity.SyxPayment;
import com.jrmf.payment.entity.YMPayment;
import com.jrmf.payment.entity.YMSHPayment;
import com.jrmf.payment.entity.YeepayPayment;
import com.jrmf.payment.entity.ZJPayment;
import com.jrmf.payment.entity.ZXPayment;

public class PaymentFactory {

  public static final String PAKHKF = "000001";//平安跨行快付
  public static final String PAYQZL = "000002";//平安银企直联
  public static final String YPDFDF = "000003";//易宝代付代发
  public static final String HNYQZL = "000004";//海南农商行银企直联
  public static final String HMZFTD = "000005";//合摩爱员工支付通道
  public static final String ALIPAY = "000006";//支付宝
  public static final String CIBYQZL = "000007";//兴业银企直联
  public static final String CMBYQZL = "000008";//招商银企直联
  public static final String SGXXXF = "000009";//手工线下下发
  public static final String YMPAY = "000010";//溢美
  public static final String ZJPAY = "000011";//中金
  public static final String SYXPAY = "000012";//商银信
  public static final String DLDPAY = "000013";//独立日
  public static final String YMFWSPAY = "000014";//溢美服务商下发
  public static final String ZXPAY = "000016";//众薪下发
  public static final String HDDPAY = "000017";//惠多多
  public static final String YFSH = "000018";//溢美优付普通商户下发
  public static final String GSBPAY = "000019";//公司宝通道下发
  public static final String NEWPAY = "000020";//新生支付下发
  public static final String HYGPAY = "000021";//慧用工
  public static final String MYBANK = "000022";//网商银行

  public static Payment paymentEntity(PaymentConfig payment) {
    switch (payment.getPathNo()) {
      case PAKHKF:
        return new PingAnBankKhkf(payment);
      case PAYQZL:
        return new PingAnBankYqzl(payment);
      case YPDFDF:
        return new YeepayPayment(payment);
      case HNYQZL:
        return new HuiYongGongHnbBank(payment);
      case HMZFTD:
        return new AygPayment(payment);
      case ALIPAY:
        return new AliPayPayment(payment);
      case CIBYQZL:
        return null;
      case YMPAY:
        return new YMPayment(payment);
      case ZJPAY:
        return new ZJPayment(payment);
      case SYXPAY:
        return new SyxPayment(payment);
      case DLDPAY:
        return new DulidayPayment(payment);
      case ZXPAY:
        return new ZXPayment(payment);
      case HDDPAY:
        return new HddPayment(payment);
      case YFSH:
        return new YMSHPayment(payment);
      case GSBPAY:
        return new GsbPayment(payment);
      case NEWPAY:
        return new NewPayPayment(payment);
      case HYGPAY:
        return new HYGPayment(payment);
      case MYBANK:
        return new MyBankPayment(payment);
      default:
        return null;
    }
  }

}
