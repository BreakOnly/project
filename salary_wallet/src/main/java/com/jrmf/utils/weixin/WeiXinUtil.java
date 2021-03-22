package com.jrmf.utils.weixin;


import com.jrmf.utils.weixin.service.TokenThread;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信端辅助类
 */
public class WeiXinUtil {

    private static Logger log = LoggerFactory.getLogger(WeiXinUtil.class);

    /*
     * 微信公众号的配置
     */
    public static final String APPID = "wx0ccdcfc8d7ac273a";
    public static final String APPSECRET = "096af5e138da4a31ef264b0d460aceed";


    /**
     * get请求接口返回json数据
     *
     * @param url
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static JSONObject doGetJson(String url) throws ClientProtocolException, IOException {

        JSONObject jsonObject = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //通过HttpGet方式提交
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        //判断返回结果
        if (entity != null) {
            String result = EntityUtils.toString(entity, "UTF-8");
            jsonObject = JSONObject.fromObject(result);
        }
        httpGet.releaseConnection();
        return jsonObject;
    }

    /**
     * post请求接口返回json数据
     *
     * @param url         请求地址
     * @param json        请求参数
     * @param contentType 请求类型
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static JSONObject doPostJson(String url, String json, String contentType) throws ClientProtocolException, IOException {

        JSONObject jsonObject = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //通过HttpPost方式提交
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-type", contentType);

        if (json != null && !"".equals(json)) {
            //有些接口必须使用json格式传递，不能使用List<BasicNameValuePair>，奇葩
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            httpPost.setEntity(requestEntity);
        }



        /* 有些接口必须使用json格式传递，该方式传递参数会返回错误

        //设置参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();

        Iterator iterator = map.entrySet().iterator();
        //传入参数循环赋值
        while(iterator.hasNext()){
            Map.Entry<String,String> elem = (Map.Entry<String, String>) iterator.next();
            list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
        }

        if(list.size() > 0){
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
            httpPost.setEntity(entity);
        }
        */


        CloseableHttpResponse response = httpclient.execute(httpPost);

        HttpEntity entity = response.getEntity();
        //判断返回结果
        if (entity != null) {
            String result = EntityUtils.toString(entity, "UTF-8");
            System.out.print(result);

            jsonObject = JSONObject.fromObject(result);
        }
        httpPost.releaseConnection();
        return jsonObject;
    }


    /**
     * 获取accesstoken
     *
     * @return
     * @throws IOException
     */
    public static AccessToken getAccessToken() throws IOException {
//        https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
        //使用corpid和secret换取一个操作凭据，在设置——权限管理中获取
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + WeiXinUtil.APPID
                + "&secret=" + WeiXinUtil.APPSECRET;

        log.info("获取access_token请求地址:{}", url);

        JSONObject jsonObject = WeiXinUtil.doGetJson(url);

        log.info("获取access_token请求结果:{}", jsonObject);

        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jsonObject.getString("access_token"));
        accessToken.setExpiresIn(jsonObject.getInt("expires_in"));
        return accessToken;

    }

    /**
     * 获取jsapi ticket
     *
     * @return
     * @throws IOException
     */
    public static JsapiTicket getJsapiTicket(String accessToken) throws IOException {

        String apiTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
        String requestUrl = apiTicketUrl.replace("ACCESS_TOKEN", accessToken);

        log.info("获取jsapi ticket请求地址:{}", apiTicketUrl);

        JSONObject jsonObject = WeiXinUtil.doGetJson(requestUrl);

        log.info("获取jsapi ticket请求结果:{}", jsonObject);

        JsapiTicket jsapiTicket = new JsapiTicket();
        jsapiTicket.setTicket(jsonObject.getString("ticket"));
        jsapiTicket.setExpiresIn(jsonObject.getInt("expires_in"));
        return jsapiTicket;

    }


    /**
     * 返回string,下载对账单不返回map，只能全部返回再做解析
     *
     * @param url
     * @param xml
     * @param contentYype
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws DocumentException
     */
    public static String doPostXml(String url, String xml, String contentYype) throws ClientProtocolException, IOException, DocumentException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        //通过HttpPost方式提交
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-type", contentYype);

        if (xml != null && !"".equals(xml)) {
            //有些接口必须使用json格式传递，不能使用List<BasicNameValuePair>，奇葩
            StringEntity requestEntity = new StringEntity(xml, "utf-8");
            httpPost.setEntity(requestEntity);
        }

        CloseableHttpResponse response = httpclient.execute(httpPost);

        HttpEntity entity = response.getEntity();
        String result = "";
        //判断返回结果
        if (entity != null) {
            result = EntityUtils.toString(entity, "UTF-8");
        }

        return result;
    }


    //字节数组转换为十六进制字符串
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * 页面分享conf
     */
    public static Map<String, Object> getAppWxConfig(String requestUrl, String backTitle, String backDesc, String backImgUrl) {

        Map<String, Object> ret = new HashMap<>();

        String jsapi_ticket = "";

        if (null != TokenThread.jsapiTicket) {
            jsapi_ticket = TokenThread.jsapiTicket.getTicket();
        }

        String timestamp = Long.toString(System.currentTimeMillis() / 1000); // 必填，生成签名的时间戳
        String nonceStr = UUID.randomUUID().toString(); // 必填，生成签名的随机串
        // 注意这里参数名必须全部小写，且必须有序
        String signature = "";
        String sign = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url=" + requestUrl;
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(sign.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
        }

        ret.put("backTitle", backTitle);
        ret.put("backLink", requestUrl);
        ret.put("backImgUrl", backImgUrl);
        ret.put("backDesc", backDesc);
        ret.put("appId", WeiXinUtil.APPID);
        ret.put("timestamp", timestamp);
        ret.put("nonceStr", nonceStr);
        ret.put("signature", signature);
//        ret.put("sign", sign);//测试
        return ret;
    }


}