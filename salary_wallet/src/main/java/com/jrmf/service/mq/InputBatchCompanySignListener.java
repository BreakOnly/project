package com.jrmf.service.mq;

import com.jrmf.payment.service.ConfirmGrantService2;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/***
 * @Description: 转包服务公司签约
 * @Auther: wsheng
 * @Version: 1.0
 * @create 2020/8/7 11:51 
 */
@Slf4j
public class InputBatchCompanySignListener implements MessageListener {

  public static final String PROCESS = "process";

  @Autowired
  private ConfirmGrantService2 confirmGrantService2;

  @Override
  public void onMessage(Message message) {
    log.info("------------------------批次导入服务公司联动签约mq开始工作------------------------");
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    TextMessage noticeMessage = (TextMessage) message;
    try {
      String batchId = noticeMessage.getText();
      log.info("批次导入签约mq的 batchId:{}", batchId);
      confirmGrantService2.verifyCompanyAndSendMq(batchId);
    } catch (Exception e) {
      log.error("服务公司签约异常：{}", e);
    }
    log.info("-------------------批次导入服务公司联动签约完成mq结束工作---------------------------");
  }

}
