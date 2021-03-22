package com.jrmf.utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * @author 种路路
 * @create 2019-01-23 11:10
 * @desc 邮件工具类
 **/
public class EmailUtil {
    private static final String CHARSET = "gbk";
    private static final String DEFAULT_MIMETYPE = "text/plain";

    /**
     * 发送邮件
     *
     * @param url    发件人邮箱
     * @param password    发件人密码
     * @param host    Smtp服务器地址
     * @param receivers    收件人
     * @param subject      标题
     * @param mailContent  邮件内容
     * @param files 附件
     * @param mimetype     内容类型 默认为text/plain,如果要发送HTML内容,应设置为text/html
     */

    public static void send(String url, String password, String host, String[] receivers, String subject, String mailContent, File[] files, String mimetype) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        // 需要校验
        props.put("mail.smtp.auth", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(url, password);
            }
        });
        // 测试
        session.setDebug(false);
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(url, MimeUtility.encodeText("金融魔方", "GB2312", "B")));
        InternetAddress[] toAddress = new InternetAddress[receivers.length];
        for (int i = 0; i < receivers.length; i++) {
            toAddress[i] = new InternetAddress(receivers[i]);
        }
        // 收件人邮件
        mimeMessage.setRecipients(Message.RecipientType.TO, toAddress);
        mimeMessage.setSubject(subject, CHARSET);

        Multipart multipart = new MimeMultipart();
        // 正文
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(mailContent, (mimetype != null && !"".equals(mimetype) ? mimetype : DEFAULT_MIMETYPE) + ";charset=" + CHARSET);
        // 发件内容
        multipart.addBodyPart(body);
        // 附件
        if (files != null && files.length>0) {
            for (File file : files) {
                MimeBodyPart attache = new MimeBodyPart();
                attache.setDataHandler(new DataHandler(new FileDataSource(file)));
                attache.setFileName(MimeUtility.encodeText(file.getName(), CHARSET, null));
                multipart.addBodyPart(attache);
            }
        }
        mimeMessage.setContent(multipart);
        mimeMessage.setSentDate(new Date());
        Transport.send(mimeMessage);
    }

    public static void main(String[] args) {
        try {
            long start =System.currentTimeMillis();
            send("zstservice@jrmf360.com", "Jrmf#2019", "smtp.jrmf360.com", new String[]{"1150994758@qq.com"}, "签约协议下载", "测试协议", null, "text/html;charset=GB2312");
            long end =System.currentTimeMillis();
            System.out.println("邮件发送耗费时长："+(end-start)/1000+"s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
