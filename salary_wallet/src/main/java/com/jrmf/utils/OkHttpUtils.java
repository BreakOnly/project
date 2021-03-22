package com.jrmf.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: YJY
 * @date: 2020/12/4 15:34
 * @description:
 */
public class OkHttpUtils {

  private final static OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
      .connectTimeout(10L, TimeUnit.SECONDS)
      .readTimeout(10L, TimeUnit.SECONDS)
      .build();

  public static Response callHttp(Request request) throws IOException {
    Call call = OK_HTTP_CLIENT.newCall(request);
    return call.execute();
  }
}
