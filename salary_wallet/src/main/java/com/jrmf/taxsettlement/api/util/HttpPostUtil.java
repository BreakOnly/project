package com.jrmf.taxsettlement.api.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-05-22 9:56
 * @desc jdk  http  post  请求
 **/
public class HttpPostUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpPostUtil.class);

    public static HashMap<String, Object> httpPost(String notifyUrl, Map<String, Object> noticeData) {
        logger.debug("try to notify url[{}] with json data[{}]", notifyUrl, noticeData);
        HttpURLConnection conn = null;
        HashMap<String, Object> result = new HashMap<>(4);
        int responseCode ;
        try {
            conn = (HttpURLConnection) new URL(notifyUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();

            String json = JSON.toJSONString(noticeData);
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();
            responseCode = conn.getResponseCode();
            InputStream in;
            if(responseCode == HttpStatus.SC_OK){
                in = conn.getInputStream();
            }else{
                in = conn.getErrorStream();
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] byteBuffer = new byte[16];
            int dataLen ;
            while ((dataLen = in.read(byteBuffer)) > -1) {
                bytes.write(byteBuffer, 0, dataLen);
            }

            result.put("message",new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            logger.info("http请求返回结果[{}]",result);

        } catch (Exception e) {
            logger.error("error occured in notifyingurl[{}]",notifyUrl, e);
            responseCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            result.put("message",e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        result.put("code",responseCode);
        return result;
    }
}
