package com.jrmf.filter;

import static com.jrmf.utils.StringUtil.zipString;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.ApiRequestData;
import com.jrmf.persistence.YuncrUserAuthenticationDao;
import com.jrmf.utils.ThreadPoolUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author: YJY
 * @date: 2020/10/23 17:17
 * @description:
 */
@Component
public class YuncrInterceptor extends HandlerInterceptorAdapter {


  @Autowired
  YuncrUserAuthenticationDao yuncrUserAuthenticationDao;

  /**
   * 在请求处理之前进行调用（Controller方法调用之前） 基于URL实现的拦截器
   *
   * @param request
   * @param response
   * @param handler
   * @return
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String path = request.getServletPath();

    if(path.startsWith("/api/individual")){

      ThreadPoolUtils.getThread().execute(()->{

      String body = null;
      try {
        body = new String(readInputStream(request.getInputStream()), "utf-8");
        if(!StringUtils.isEmpty(body)) {
          JSONObject requestData = JSONObject.parseObject(body);
          String requestId = requestData.getString("third_serial_number");
          requestData.put("file","不存储");
          ApiRequestData data = new ApiRequestData();

          data.setRequestBody(requestData.toJSONString());
          data.setRequestId(requestId);
          data.setSource(1);
          yuncrUserAuthenticationDao.insertApiRequestData(data);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      });
    }

    return true;
  }

  private byte[] readInputStream(InputStream in) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] byteBuffer = new byte[256];
    while (true) {
      int readLen = in.read(byteBuffer);
      if (readLen < 0) {
        break;
      } else if (readLen == 0) {
        continue;
      } else {
        bytes.write(byteBuffer, 0, readLen);
      }
    }
    return bytes.toByteArray();
  }

}
