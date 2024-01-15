/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
