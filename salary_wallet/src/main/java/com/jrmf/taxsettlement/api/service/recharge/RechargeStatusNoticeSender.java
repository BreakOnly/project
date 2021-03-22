package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.taxsettlement.api.util.HttpPostUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.activemq.ScheduledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

public class RechargeStatusNoticeSender implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(RechargeStatusNoticeSender.class);

	private static final String SUCCESS_FLAG = "SUCCESS";

    public static final String PROCESS = "process";

	private Map<String, Integer> levelDelayTable ;

	private JmsTemplate providerJmsTemplate;

	private Destination noticeTopicDestination;

	public RechargeStatusNoticeSender(int senderThreadCount, JmsTemplate providerJmsTemplate,
                                Destination noticeTopicDestination, Map<String, Integer> levelDelayTable) {
		super();
		this.levelDelayTable = levelDelayTable;
		this.providerJmsTemplate = providerJmsTemplate;
		this.noticeTopicDestination = noticeTopicDestination;
	}

	@Override
	public void onMessage(Message message) {
		try {
            String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            MDC.put(PROCESS, processId);
			ObjectMessage noticeMessage = (ObjectMessage) message;
            RechargeStatusNotice notice = (RechargeStatusNotice) noticeMessage.getObject();
			ThreadUtil.pdfThreadPool.execute(new Runnable() {

				@Override
				public void run() {
				    try {
                        MDC.put(PROCESS, processId);
                        if (!sendAndReceiveReceipt(notice)) {
                            int level = notice.increaseAndGetLevel();
                            Integer delaySeconds = levelDelayTable.get(String.valueOf(level));
                            if (delaySeconds == null) {
                                logger.error("fail to notify url[{}] finally", notice.getNotifyUrl());
                            } else {
                                providerJmsTemplate.send(noticeTopicDestination, new MessageCreator() {
                                    @Override
                                    public Message createMessage(Session session) throws JMSException {
                                        Message message = session.createObjectMessage(notice);
                                        message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delaySeconds * 1000);
                                        return message;
                                    }
                                });
                            }
                        }
                    }finally {
                        MDC.remove(PROCESS);
                    }

				}
			});

		} catch (Exception e) {
			logger.error("error occured in receiving message", e);
		} finally {
            MDC.remove(PROCESS);
        }

	}

	private boolean sendAndReceiveReceipt(RechargeStatusNotice notice) {
	    if(StringUtil.isEmpty(notice.getNotifyUrl())){
	        logger.info("回调地址不存在，不需要回调");
	        return true;
        }

	    logger.info("充值回调，通知参数{}",notice.getNoticeData());
	    logger.info("充值回调，通知地址{}",notice.getNotifyUrl());


        HashMap<String, Object> post = HttpPostUtil.httpPost(notice.getNotifyUrl(), notice.getNoticeData());
        String message = (String)post.get("message");

        return SUCCESS_FLAG.equals(message);
    }


}
