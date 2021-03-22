package com.jrmf.interceptor;

import com.jrmf.domain.ChannelCustom;

public class UserThreadLocal {

  private static final ThreadLocal<Object> LOCALUSER = new ThreadLocal<>();

  public static void setLocalUser(Object user) {
    LOCALUSER.set(user);
  }

  public static Object getLocalUser() {
    return LOCALUSER.get();
  }

  public static void removeUser() {
    LOCALUSER.remove();
  }

}
