package com.jrmf.payment.zjpay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class HttpData
{
  private List<NameValuePair> list;
  private String charset;

  public HttpData(List<NameValuePair> list, String charset)
  {
    this.list = list;
    this.charset = charset;
  }

  public String getData() throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer();

    int size = this.list.size();
    if (size != 0) {
      for (int i = 0; i < size; i++) {
        NameValuePair pair = (NameValuePair)this.list.get(i);
        sb.append(pair.getName()).append('=').append(URLEncoder.encode(pair.getValue(), this.charset)).append('&');
      }
      return sb.substring(0, sb.length() - 1);
    }
    return "";
  }
}