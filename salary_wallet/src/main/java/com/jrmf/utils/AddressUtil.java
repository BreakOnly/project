package com.jrmf.utils;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class AddressUtil {

    private static final String pcOnlineApiUrl = "http://whois.pconline.com.cn/jsAlert.jsp";

    /**
     * @param ip ip地址
     */
    public static String getAddresses(String ip) {
        HttpURLConnection conn ;
        try {
            conn = (HttpURLConnection) new URL(pcOnlineApiUrl+"?ip="+ip).openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int retLen ;
            while ((retLen = in.read(buffer)) > -1) {
                bytes.write(buffer, 0, retLen);
            }
            String retStr = new String(bytes.toByteArray(), "gb2312").trim();
            if(retStr.startsWith("alert")){
                retStr = retStr.substring(7,retStr.length()-3).trim();
                int index = retStr.indexOf("区");
                if(index>0){
                    retStr = retStr.substring(0,index+1);
                }else{
                    index = retStr.indexOf("市");
                    if(index>0){
                        retStr = retStr.substring(0,index+1);
                    }
                }
            }
            return retStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param content
     *            请求的参数 格式为：name=xxx&pwd=xxx
     * @param encodingString
     *            服务器端请求编码。如GBK,UTF-8等
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getAddresses(String content, String encodingString)
            throws UnsupportedEncodingException {
        // 这里调用淘宝API
        String urlStr = "http://ip.taobao.com/service/getIpInfo.php";
        // 从http://whois.pconline.com.cn取得IP所在的省市区信息
        String returnStr = getResult(urlStr, "ip="+content, encodingString);
        if (returnStr != null) {
            // 处理返回的省市区信息
            returnStr = decodeUnicode(returnStr);
            String[] temp = returnStr.split(",");
            if(temp.length<3){
                return "0";//无效IP，局域网测试
            }
            return returnStr;
        }
        return null;
    }
    /**
     * @param urlStr
     *            请求的地址
     * @param content
     *            请求的参数 格式为：name=xxx&pwd=xxx
     * @param encoding
     *            服务器端请求编码。如GBK,UTF-8等
     * @return
     */
    private static String getResult(String urlStr, String content, String encoding) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();// 新建连接实例
            connection.setConnectTimeout(2000);// 设置连接超时时间，单位毫秒
            connection.setReadTimeout(2000);// 设置读取数据超时时间，单位毫秒
            connection.setDoOutput(true);// 是否打开输出流 true|false
            connection.setDoInput(true);// 是否打开输入流true|false
            connection.setRequestMethod("POST");// 提交方法POST|GET
            connection.setUseCaches(false);// 是否缓存true|false
            connection.connect();// 打开连接端口
            DataOutputStream out = new DataOutputStream(connection
                    .getOutputStream());// 打开输出流往对端服务器写数据
            out.writeBytes(content);// 写数据,也就是提交你的表单 name=xxx&pwd=xxx
            out.flush();// 刷新
            out.close();// 关闭输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), encoding));// 往对端写完数据对端服务器返回数据
            // ,以BufferedReader流来读取
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();// 关闭连接
            }
        }
        return null;
    }

    /**
     * unicode 转换成 中文
     *
     * @author fanhui 2007-3-15
     * @param theString
     * @return
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed      encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

	/**
	 * 获取本机ip
	 * @return
	 */
	public static String getLocalIP() {
		String ip =null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		return ip;
	}

    // 测试
    public static void main(String[] args) {
        AddressUtil addressUtils = new AddressUtil();
//         测试ip 219.136.134.157 中国=华南=广东省=广州市=越秀区=电信
        String ip = "219.136.134.157";
        String address = "";
        address = getAddresses("119.2.6.213");
        System.out.println(address);
        JSONObject jsStr = JSONObject.parseObject(address);
        JSONObject data = JSONObject.parseObject(jsStr.getString("data"));
        String country = data.getString("country");
        String region = data.getString("region");
        String city = data.getString("city");
        String isp = data.getString("isp");
        System.out.println(">>>>>>>>>>>>>>>>>"+country+region+city+isp);
//         输出结果为：广东省,广州市,越秀区
    }

    public static String  getIp2(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * @param ip ip地址
     */
    public static String getAddresses2(String ip) {
        HttpURLConnection conn ;
        try {
            conn = (HttpURLConnection) new URL(pcOnlineApiUrl+"?ip="+ip).openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int retLen ;
            while ((retLen = in.read(buffer)) > -1) {
                bytes.write(buffer, 0, retLen);
            }
            String retStr = new String(bytes.toByteArray(), "gb2312").trim();
            if(retStr.startsWith("alert")){
                retStr = retStr.substring(7,retStr.length()-3).trim();
                int index = retStr.indexOf("区");
                if(index>0){
                    retStr = retStr.substring(0,index+1);
                }else{
                    index = retStr.indexOf("市");
                    if(index>0){
                        retStr = retStr.substring(0,index+1);
                    }
                }
            }
            return retStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
