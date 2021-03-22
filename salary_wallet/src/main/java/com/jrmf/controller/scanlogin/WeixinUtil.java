package com.jrmf.controller.scanlogin;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONException;
import com.jrmf.utils.weixin.AccessToken;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.alibaba.fastjson.JSONObject;

/**
 * 公众平台通用接口工具类
 */
public class WeixinUtil {

	// 与接口配置信息中的Token要一致
	private static String token = "sadtytgasdwqewqsdcvcxsyukyuqqw";

    /**
     * 验证签名
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     */
    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = new String[] { token, timestamp, nonce };
        // 将token、timestamp、nonce三个参数进行字典序排序  
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
        }
        MessageDigest md = null;
        String tmpStr = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将三个参数字符串拼接成一个字符串进行sha1加密  
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        content = null;
        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信  
        return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray
     * @return
     */
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    /**
     * 将字节转换为十六进制字符串
     *
     * @param mByte
     * @return
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];

        String s = new String(tempArr);
        return s;
    }

 // 获取access_token的接口地址（GET） 限200（次/天）
    public final static String access_token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

    /**
     * 获取access_token
     *
     * @param appid 凭证
     * @param appsecret 密钥
     * @return
     */
    public static AccessToken getAccessToken(String appid, String appsecret) {
        AccessToken accessToken = null;

        String requestUrl = access_token_url.replace("APPID", appid).replace("APPSECRET", appsecret);
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        // 如果请求成功
        if (null != jsonObject) {
            try {
                accessToken = new AccessToken();
                accessToken.setToken(jsonObject.getString("access_token"));
//                accessToken.setExpiresIn(jsonObject.get("expires_in"));
            } catch (JSONException e) {
                accessToken = null;
                // 获取token失败
				e.printStackTrace();
            }
        }
        return accessToken;
    }


	public static Map<String,Object> getAccessTokens(String appid, String appsecret) {
		Map<String,Object> map = new HashMap<String,Object>();
		String requestUrl = access_token_url.replace("APPID", appid).replace(
				"APPSECRET", appsecret);
		map = httpRequest(requestUrl, "GET", null);
		return map;
	}

	public static Map<String,Object> getToken(String appid,String appsecret){
    	Map<String,Object> map = new HashMap<String,Object>();
    	String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRECT";
		String requestUrl = url.replace("ID", appid).replace(
				"SECRECT", appsecret);
		map = httpRequest(requestUrl, "GET", null);
		return map;
    }

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象  
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时  
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码  
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源  
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
           System.out.println("Weixin server connection timed out.");
        } catch (Exception e) {
        	 System.out.println("https request error:{}"+ e);
        }
        return jsonObject;
    }

 // 菜单创建（POST） 限100（次/天）
    public static String menu_create_url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    /**
     * 创建菜单 
     *
     * @param menu 菜单实例 
     * @param accessToken 有效的access_token 
     * @return 0表示成功，其他值表示失败 
     */  
   /* public static int createMenu(Menu menu, String accessToken) {  
        int result = 0;  
      
        // 拼装创建菜单的url  
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);  
        // 将菜单对象转换成json字符串  
        String jsonMenu = JSONObject.fromObject(menu).toString();  
        // 调用接口创建菜单  
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);  
      
        if (null != jsonObject) {  
            if (0 != jsonObject.getInt("errcode")) {  
                result = jsonObject.getInt("errcode");  
                log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));  
            }  
        }  
      
        return result;  
    }*/


    /**
     * 删除当前Menu
    * @Title: deleteMenu
    * @Description: 删除当前Menu
    * @param @return    设定文件
    * @return String    返回类型
    * @throws
     */
   /*public static String deleteMenu(String accessToken)
   {
       String action = "https://api.weixin.qq.com/cgi-bin/menu/delete? access_token="+accessToken;
       try {
          URL url = new URL(action);
          HttpURLConnection http = (HttpURLConnection)url.openConnection();    

          http.setRequestMethod("GET");        
          http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");    
          http.setDoOutput(true);        
          http.setDoInput(true);
          System.setProperty("sun.net.client.defaultConnectTimeout", "30000");//连接超时30秒
          System.setProperty("sun.net.client.defaultReadTimeout", "30000"); //读取超时30秒
          http.connect();
          OutputStream os= http.getOutputStream();    
          os.flush();
          os.close();

          InputStream is =http.getInputStream();
          int size =is.available();
          byte[] jsonBytes =new byte[size];
          is.read(jsonBytes);
          return "YES";
          } catch (MalformedURLException e) {
              e.printStackTrace();
          } catch (IOException e) {
        	  e.printStackTrace();
          }
       return "NO";   
   }*/
    /**
	 * 获取统一下单接口的签名
	 */
	@SuppressWarnings("rawtypes")
	public static String sign(SortedMap<String, String> params,String weixinKey){
		StringBuffer sb = new StringBuffer();
		Set es = params.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + weixinKey);
		String sign = MD5Util.MD5Encode(sb.toString(),"UTF-8").toUpperCase();
		return sign;
	}
	/**
	 * 将map转为xml
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String map2xml(Map map){
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("xml");
		if( null != map ){
			Set<String> sets = map.keySet();
			for(String set : sets) {
				Element element = root.addElement(set);
				parseMap(element,map.get(set));
			}
		}
        return document.asXML();
	}
	/**
	 * 递归解析map
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Element parseMap(Element element,Object object){
		if(object instanceof Map){//object为map
			Map map = (Map) object;
			Set<String> sets = map.keySet();
			for(String set : sets) {
				Element data = element.addElement(set);
				parseMap(data,map.get(set));
			}
		} else if(object instanceof List){
			List list = (List) object;
			for( int i = 0 ; i < list.size() ; i++){
				Map map = (Map) list.get(i);
				Set<String> sets = map.keySet();
				for(String set : sets) {
					Element data = element.addElement(set);
					parseMap(data,map.get(set));
				}
			}
		} else if(null != object){
			element.addCDATA(object.toString());
		}
		return element;
	}
	/**
	 * 发起https请求并获取结果  返回xml
	 */
	public static Map<String,Object> httpRequestXml(String requestUrl,
			String requestMethod, String outputStr) {
		StringBuffer buffer = new StringBuffer();
		Map<String,Object> map = new HashMap<String,Object>();
		try {

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url
					.openConnection();

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			try {
				System.out.println("微信返回报文：" + buffer.toString());
				Document document = DocumentHelper.parseText(buffer.toString());
				document.setXMLEncoding("UTF-8");
				map = XMLUtils.Dom2Map(document);
			} catch (Exception e){
				throw new DocumentException("XML转换异常",e.getCause());
			}
		} catch (ConnectException ce) {
			System.out.println("微信服务器连接超时.");
		} catch (Exception e) {
			System.out.println("https 请求错误:{}"+ e);
		}
		return map;
	}
	/**
	 * 解析微信发来的请求（XML）
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseXml(HttpServletRequest request){
		Map<String, String> map = new HashMap<String, String>();
        SAXReader reader = new SAXReader();
        try {
            InputStream ins = request.getInputStream();
            Document doc = reader.read(ins);
            Element root = doc.getRootElement();
            List<Element> list = root.elements();
            for (Element e : list) {
                map.put(e.getName(), e.getText());
            }
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}
	/**
	 * 获取jsapi_ticket
	 *
	 * @param accessToken
	 *            获取的全局access_token
	 * @return
	 */

	public static Map<String,Object> getJsApiTicket(String accessToken,String type) {
		Map<String,Object> map = new HashMap<String,Object>();
		String ticket_url_qiye = "";

		if("1".equals(type)){  // 企业号
			ticket_url_qiye = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token=ACCESS_TOKEN";
		}else{   //  公众号
			ticket_url_qiye = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
		}
		String requestUrl = ticket_url_qiye.replace("ACCESS_TOKEN", accessToken);
		map = httpRequest(requestUrl, "GET", null);
		return map;
	}
	/**
	 * 发送xml格式 获取对账单 返回 String
	 * @param requestUrl
	 * @param requestMethod
	 * @param outputStr
	 * @return
	 */
	@SuppressWarnings("unused")
	public static String httpRequestString(String requestUrl,
			String requestMethod, String outputStr) {
		StringBuffer buffer = new StringBuffer();
		Map<String,Object> map = new HashMap<String,Object>();
		String str = null;
		try {
			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url
					.openConnection();

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			str = buffer.toString();
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
		} catch (ConnectException ce) {
			System.out.println("微信服务器连接超时.");
		} catch (Exception e) {
			System.out.println("https 请求错误:{}"+e);
		}
		return str;
	}
	/**
	 *  对账单解析
	 * @param to_result
	 */
	public static void toString(String to_result) {
		String str = to_result;//获取对账报文
		String newStr = str.replaceAll(",", " "); // 去空格
		String[] tempStr = newStr.split("`"); // 数据分组
		String[] t = tempStr[0].split(" ");// 分组标题
		int k = 1; // 纪录数组下标
		int j = tempStr.length / 24; // 计算循环次数
		for (int i = 0; i < j; i++) {
			System.out.println(t[0] + ":" + tempStr[k]);
			System.out.println(t[1] + ":" + tempStr[k + 1]);
			System.out.println(t[2] + ":" + tempStr[k + 2]);
			System.out.println(t[3] + ":" + tempStr[k + 3]);
			System.out.println(t[4] + ":" + tempStr[k + 4]);
			System.out.println(t[5] + ":" + tempStr[k + 5]);
			System.out.println(t[6] + ":" + tempStr[k + 6]);
			System.out.println(t[7] + ":" + tempStr[k + 7]);
			System.out.println(t[8] + ":" + tempStr[k + 8]);
			System.out.println(t[9] + ":" + tempStr[k + 9]);
			System.out.println(t[10] + ":" + tempStr[k + 10]);
			System.out.println(t[11] + ":" + tempStr[k + 11]);
			System.out.println(t[12] + ":" + tempStr[k + 12]);
			System.out.println(t[13] + ":" + tempStr[k + 13]);
			System.out.println(t[14] + ":" + tempStr[k + 14]);
			System.out.println(t[15] + ":" + tempStr[k + 15]);
			System.out.println(t[16] + ":" + tempStr[k + 16]);
			System.out.println(t[17] + ":" + tempStr[k + 17]);
			System.out.println(t[18] + ":" + tempStr[k + 18]);
			System.out.println(t[19] + ":" + tempStr[k + 19]);
			System.out.println(t[20] + ":" + tempStr[k + 20]);
			System.out.println(t[21] + ":" + tempStr[k + 21]);
			System.out.println(t[22] + ":" + tempStr[k + 22]);
			System.out.println(t[23] + ":" + tempStr[k + 23]);
			System.out.println("---------");//摘取有用数据存入数据库
			k = k + 24;
		}
	}
	/**
	 *获取前一天日期
	 * @return
	 */
	public static String getYesterday(){
		Calendar calendar = Calendar.getInstance();//此时打印它获取的是系统当前时间
        calendar.add(Calendar.DATE, -1);    //得到前一天
		String  yestedayDate
		= new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
		return yestedayDate;
	}
	public static void toString1(String str){
		 String newStr = str.replaceAll(",", " "); // 去空格
	        String[] tempStr = newStr.split("`"); // 数据分组
	        String[] t = tempStr[0].split(" ");// 分组标题
	        int k = 1; // 纪录数组下标
	        int j = tempStr.length / t.length; // 计算循环次数
	        for (int i = 0; i < j; i++) {
	        	System.out.println(t[0] + ":" + tempStr[k]);
            	System.out.println(t[1] + ":" + tempStr[1 + k]);
                System.out.println(t[2] + ":" + tempStr[2 + k]);
                System.out.println(t[6] + ":" + tempStr[6 + k]);
                System.out.println(t[9] + ":" + tempStr[9 + k]);
                System.out.println(t[12] + ":" + tempStr[12 + k]);
                System.out.println(t[15] + ":" + tempStr[15 + k]);
                String[] s = tempStr[15 + k].split("\\\\");
                System.out.println("userId"+":"+s[0]);


	            System.out.println("---------");// 摘取有用数据存入数据库
	            k = k + t.length;
	        }
	}





}
