package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.util.HttpPostUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TransferDealStatusNoticeSender implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(TransferDealStatusNoticeSender.class);

	private static final String SUCCESS_FLAG = "SUCCESS";

    public static final String PROCESS = "process";

	private Map<String, Integer> levelDelayTable ;

	private JmsTemplate providerJmsTemplate;

	private Destination noticeTopicDestination;

	public TransferDealStatusNoticeSender(int senderThreadCount, JmsTemplate providerJmsTemplate,
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
			TransferDealStatusNotice notice = (TransferDealStatusNotice) noticeMessage.getObject();
			ThreadUtil.pdfThreadPool.execute(new Runnable() {

				@Override
				public void run() {
                    MDC.put(PROCESS, processId);

					try {
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

	private boolean sendAndReceiveReceipt(TransferDealStatusNotice notice) {

		if (notice.isBatchNotice()) {
			return sendMultipartAndReceiveReceipt(notice.getNotifyUrl(), notice.getNoticeData());
		} else {
			return sendJsonAndReceiveReceipt(notice.getNotifyUrl(), notice.getNoticeData());
		}
	}

	private boolean sendMultipartAndReceiveReceipt(String notifyUrl, Map<String, Object> noticeData) {

		logger.debug("try to notify url[{}] with multipart batch[{}]", notifyUrl,
				noticeData.get(APIDefinitionConstants.CFN_BATCH_NO));

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		CloseableHttpResponse httpResponse = null;
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000)
				.build();
		HttpPost httpPost = new HttpPost(notifyUrl);
		httpPost.setConfig(requestConfig);
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

		for (Entry<String, Object> entry : noticeData.entrySet()) {
			Object value = entry.getValue();
			if (byte[].class.equals(value.getClass())) {
				multipartEntityBuilder.addBinaryBody(entry.getKey(), (byte[]) value, ContentType.DEFAULT_BINARY,
						"batchFile.gzip");
			} else {
				multipartEntityBuilder.addTextBody(entry.getKey(), (String) value);
			}
		}

		HttpEntity httpEntity = multipartEntityBuilder.build();
		httpPost.setEntity(httpEntity);

		try {
			httpResponse = httpClient.execute(httpPost);
			HttpEntity responseEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
				StringBuffer buffer = new StringBuffer();
				String str = "";
				while ((str = reader.readLine()) != null) {
					buffer.append(str);
				}
				return SUCCESS_FLAG.equals(buffer.toString());
			}
		} catch (Exception e) {
			logger.error("error occured in notifying", e);
		} finally {
			try {
				httpClient.close();
				if (httpResponse != null) {
					httpResponse.close();
				}
			} catch (Exception e) {
				logger.error("error occured in close http client", e);
			}
		}
		return false;
	}

	private boolean sendJsonAndReceiveReceipt(String notifyUrl, Map<String, Object> noticeData) {

        HashMap<String, Object> post = HttpPostUtil.httpPost(notifyUrl, noticeData);
        String message = (String)post.get("message");

        return SUCCESS_FLAG.equals(message);
    }


}
