package com.axelor.apps.portal.web.interceptor;

import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.auth.AuthSecurityException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.common.base.Throwables;
import com.stripe.exception.StripeException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalResponseInterceptor implements MethodInterceptor {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    logger.trace("Web Service: {}", invocation.getMethod());

    PortalRestResponse response = null;

    try {
      response = (PortalRestResponse) invocation.proceed();
    } catch (Exception e) {
      response = new PortalRestResponse();
      response = onException(e, response);
    }
    return response;
  }

  private PortalRestResponse onException(Throwable throwable, PortalRestResponse response) {
    final Throwable cause = throwable.getCause();
    final Throwable root = Throwables.getRootCause(throwable);
    for (Throwable ex : Arrays.asList(throwable, cause, root)) {
      if (ex instanceof AxelorException) {
        return onAxelorException((AxelorException) ex, response);
      }
      if (ex instanceof AuthSecurityException) {
        return onAuthSecurityException((AuthSecurityException) ex, response);
      }

      if (ex instanceof StripeException) {
        return onStripeException((StripeException) ex, response);
      }
    }
    logger.error("Error: {}", throwable.getMessage());
    response.setException(throwable);
    TraceBackService.trace(throwable);
    return response;
  }

  private PortalRestResponse onStripeException(StripeException ex, PortalRestResponse response) {
    logger.error("Stripe Error: {}", ex.getMessage());
    TraceBackService.trace(ex);
    response.setException(ex);
    return response;
  }

  private PortalRestResponse onAuthSecurityException(
      AuthSecurityException e, PortalRestResponse response) {
    logger.error("Access Error: {}", e.getMessage());
    response.setException(e);
    return response;
  }

  private PortalRestResponse onAxelorException(AxelorException ex, PortalRestResponse response) {
    logger.error("Error: {}", ex.getMessage());
    TraceBackService.trace(ex);
    response.setException(ex);
    return response;
  }
}
