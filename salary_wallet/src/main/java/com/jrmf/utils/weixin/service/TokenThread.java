package com.jrmf.utils.weixin.service;

import com.jrmf.utils.weixin.AccessToken;
import com.jrmf.utils.weixin.JsapiTicket;
import com.jrmf.utils.weixin.WeiXinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 定时获取微信access_token的线程(确保不会过期)
 */
public class TokenThread implements Runnable {

    private static Logger log = LoggerFactory.getLogger(TokenThread.class);
    public static AccessToken accessToken = null;
    public static JsapiTicket jsapiTicket = null;

    @Override
    public void run() {

        log.info("-----------开始执行获取access_token线程---------");

        while (true) {
            try {

                accessToken = WeiXinUtil.getAccessToken();
                if (null != accessToken) {
                    log.info("获取access_token成功，有效时长{}秒 token:{}", accessToken.getExpiresIn(), accessToken.getToken());

                    jsapiTicket = WeiXinUtil.getJsapiTicket(accessToken.getToken());
                    if (null != jsapiTicket) {
                        log.info("获取jsapi_ticket成功，有效时长{}秒 ticket:{}", jsapiTicket.getExpiresIn(), jsapiTicket.getTicket());
                    } else {
                        // 如果jsapi_ticket为null，60秒后再获取
                        Thread.sleep(60 * 1000);
                    }
                    // 休眠7000秒
                    Thread.sleep((accessToken.getExpiresIn() - 200) * 1000);
                } else {

                    log.info("获取access_token失败，稍后重试");
                    // 如果access_token为null，60秒后再获取
                    Thread.sleep(60 * 1000);
                }
            } catch (InterruptedException e) {
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
