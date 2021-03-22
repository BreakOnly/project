//package com.jrmf.utils.weixin.service;
//
//import com.jrmf.utils.threadpool.ThreadUtil;
//import com.jrmf.utils.weixin.WeiXinUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//
//
///**
// * 容器启动时调用获取access_token的线程
// */
//@Component
//public class InitServlet implements ApplicationRunner {
//
//    private static Logger log = LoggerFactory.getLogger(InitServlet.class);
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.info("weixin api APPID:{}", WeiXinUtil.APPID);
//        log.info("weixin api APPSECRET:{}", WeiXinUtil.APPSECRET);
//
//        // 未配置CORPID、CORPIDSECRET时给出提示
//        if ("".equals(WeiXinUtil.APPID) || "".equals(WeiXinUtil.APPSECRET)) {
//            log.error("APPID and APPSECRET configuration error, please check carefully.");
//        } else {
//            ThreadUtil.subAccountThreadPool.execute(new TokenThread());
//        }
//    }
//}
