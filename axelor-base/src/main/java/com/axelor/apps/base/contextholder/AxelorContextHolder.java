package com.axelor.apps.base.contextholder;

import com.axelor.rpc.Context;

public class AxelorContextHolder {

  private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

  private AxelorContextHolder() {}

  public static void setContext(Context context) {
    if (context != null) {
      CONTEXT_THREAD_LOCAL.set(context);
    }
  }

  public static Context getContext() {
    return CONTEXT_THREAD_LOCAL.get();
  }

  public static void cleanContext() {
    CONTEXT_THREAD_LOCAL.remove();
  }
}
