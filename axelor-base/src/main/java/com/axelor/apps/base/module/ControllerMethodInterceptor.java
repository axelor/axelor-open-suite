package com.axelor.apps.base.module;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ControllerMethodInterceptor implements MethodInterceptor {

  public static final String EXCEPTION_ERROR_ANNOTATION_PACKAGE =
      "com.axelor.apps.base.service.exception.ErrorException";

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    ActionResponse response = getActionResponse(invocation);
    if (manageErrorException(invocation)) {
      try {
        invocation.proceed();
      } catch (Exception e) {
        TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      }
    } else {
      try {
        invocation.proceed();
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
    return null;
  }

  protected boolean manageErrorException(MethodInvocation invocation) {
    Annotation[] annotations = invocation.getMethod().getDeclaredAnnotations();
    return Arrays.stream(annotations)
        .anyMatch(
            annotation ->
                EXCEPTION_ERROR_ANNOTATION_PACKAGE.equals(annotation.annotationType().getName()));
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
