package com.axelor.apps.base.module;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ControllerMethodInterceptor implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    ActionResponse response = getActionResponse(invocation);
    try {
      return invocation.proceed();
    } catch (Exception e) {
      if (response == null) {
        TraceBackService.trace(e);
      } else if (manageErrorException(invocation)) {
        TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      } else {
        TraceBackService.trace(response, e);
      }
    }
    return null;
  }

  protected boolean manageErrorException(MethodInvocation invocation) {
    Annotation[] annotations = invocation.getMethod().getDeclaredAnnotations();
    return Arrays.stream(annotations)
        .anyMatch(annotation -> ErrorException.class.equals(annotation.annotationType()));
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
