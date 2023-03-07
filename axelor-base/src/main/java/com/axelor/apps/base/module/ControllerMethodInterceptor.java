package com.axelor.apps.base.module;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionResponse;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ControllerMethodInterceptor implements MethodInterceptor {
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    ActionResponse response = getActionResponse(invocation);
    try {
      invocation.proceed();
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    return null;
  }

  protected ActionResponse getActionResponse(MethodInvocation invocation) {
    Object[] arguments = invocation.getArguments();
    ActionResponse response = null;
    for (Object argument : arguments) {
      if (argument instanceof ActionResponse) {
        response = (ActionResponse) argument;
      }
    }
    return response;
  }
}
