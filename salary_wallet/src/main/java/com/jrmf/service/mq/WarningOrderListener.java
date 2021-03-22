package com.jrmf.service.mq;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.sms.SmsSignNameEnum;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.EmailUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @author 种路路
 * @create 2019-03-29 16:47
 * @desc 异常订单处理
 **/
public class WarningOrderListener extends BaseController implements MessageListener {

  private static final Logger logger = LoggerFactory.getLogger(WarningOrderListener.class);

  public static final String PROCESS = "process";

  private Destination noticeTopicDestination;

  @Value("${warning.time}")
  private String warningTime;

  @Autowired
  private UserCommissionService userCommissionService;

  public WarningOrderListener(int senderThreadCount, Destination noticeTopicDestination) {
    super();
    this.noticeTopicDestination = noticeTopicDestination;
  }

  @Override
  public void onMessage(Message message) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);
      TextMessage noticeMessage = (TextMessage) message;
      String batchId = null;
      try {
        batchId = noticeMessage.getText();
      } catch (Exception e) {
        logger.error("error occurred in receiving message", e);
      }
      logger.info("mq接收消息" + batchId);
      List<UserCommission> list = userCommissionService
          .getToBeConfirmedUserCommissionByBatchId(batchId);

      if (list.isEmpty()) {
        logger.info("批次：" + batchId + "全部落地");
        return;
      }
      int size = list.size();
      String finalBatchId = batchId;
      ThreadUtil.pdfThreadPool.execute(() -> {
        MDC.put(PROCESS, processId + "send_message");
        String[] mobiletelno = {"13021138882", "18518779859"};
        warningTime = Long.parseLong(warningTime) / 1000 / 60 + "";
        String content = "【智税通】有未落地的订单：批次号为：" + finalBatchId + ",共计" + size + "笔。超时时间"
            + warningTime + "分钟。";
        try {

          final String templateParam =
              "{\"finalBatchId\":\"" + finalBatchId + "\",\"size\":\"" + size
                  + "\",\"warningTime\":\"" + warningTime + "\"}";
          sendContent(mobiletelno, content, SmsSignNameEnum.JRMF.getName(),
              SmsTemplateCodeEnum.WARNING_ORDER.getCode(), templateParam);
        } catch (Exception e) {
          logger.error("短信发送异常", e);
        }
        MDC.remove(PROCESS);
      });
      ThreadUtil.pdfThreadPool.execute(() -> {
        MDC.put(PROCESS, processId + "send_email");
        String url = "zstservice@jrmf360.com";
        String password = "Jrmf#2019";
        String host = "smtp.jrmf360.com";
        StringBuilder context = new StringBuilder(
            "<html><body><table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" style=\"margin:0;padding:0;border:1px solid #cccccc;border-collapse: collapse;\">");
        context.append(
            "<tr>" + "<th style=\"padding:10px;\">支付时间</th>" + "<th style=\"padding:10px;\">金额</th>"
                + "<th style=\"padding:10px;\">订单号</th>" + "<th style=\"padding:10px;\">身份证</th>"
                + "<th style=\"padding:10px;\">姓名</th>" + "<th style=\"padding:10px;\">商户名称</th>"
                + "<th style=\"padding:10px;\">服务公司标号</th>"
                + "<th style=\"padding:10px;\">服务公司名称</th>"
                + "<th style=\"padding:10px;\">银行名称</th>" + "<th style=\"padding:10px;\">创建时间</th>"
                + "<th style=\"padding:10px;\">最后更新时间</th>" + "</tr>");
        for (UserCommission userCommission : list) {
          context.append("<tr>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getPaymentTime())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getAmount())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getOrderNo())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getCertId())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getUserName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getCustomName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getCompanyId())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getCompanyName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getBankName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getCreatetime())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(userCommission.getUpdatetime())
              .append("</td>");
          context.append("</tr>");
        }
        context.append("</table>" + "</body>" + "</html>");
        String[] receivers = {"chonglulu@jrmf360.com", "linsong@jrmf360.com",
            "fangmingyan@jrmf360.com", "lifan@jrmf360.com", "lilei@jrmf360.com",
            "wufujin@jrmf360.com", "chenchen@jrmf360.com"};
        String title = "异常订单邮件通知";
        try {
          EmailUtil.send(url, password, host, receivers, title, context.toString(), null,
              "text/html;charset=GB2312");
        } catch (Exception e) {
          logger.error(e.getMessage());
        } finally {
          MDC.remove(PROCESS);
        }
      });

    } finally {
      MDC.remove(PROCESS);
    }


  }
}
