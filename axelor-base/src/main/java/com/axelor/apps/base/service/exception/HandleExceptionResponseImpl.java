/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.exception;

import com.axelor.rpc.ActionResponse;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class HandleExceptionResponseImpl implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {

    if (invocation.getArguments() != null) {
      for (Object arg : invocation.getArguments()) {
        if (arg instanceof ActionResponse) {
          Object result = null;
          try {
            result = invocation.proceed();
          } catch (Exception e) {
            TraceBackService.trace((ActionResponse) arg, e);
          }
          return result;
        }
      }
      return invocation.proceed();
    } else {
      return invocation.proceed();
    }
  }
}
