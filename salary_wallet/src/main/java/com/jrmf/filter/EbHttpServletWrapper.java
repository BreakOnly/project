package com.jrmf.filter;

import cn.emay.slf4j.Logger;
import cn.emay.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

/**
 * @author: YJY
 * @date: 2020/10/26 10:25
 * @description:
 */
public class EbHttpServletWrapper extends HttpServletRequestWrapper {

  private Logger logger = LoggerFactory.getLogger(EbHttpServletWrapper.class);
  private String encoding = "UTF-8";
  private byte[] requestBodyIniBytes;

  public EbHttpServletWrapper(HttpServletRequest request) throws IOException {
    super(request);
    ServletInputStream stream = request.getInputStream();
    String requestBody = StreamUtils.copyToString(stream, Charset.forName(encoding));
    requestBodyIniBytes = requestBody.getBytes(encoding);

  }



  @Override
  public ServletInputStream getInputStream() {
    final ByteArrayInputStream in;
    in = new ByteArrayInputStream(requestBodyIniBytes);

    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {

      }

      @Override
      public int read() throws IOException {

        return in.read();
      }
    };
  }

}
