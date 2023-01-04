/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.api;

import com.axelor.app.AppSettings;
import com.axelor.apps.tool.exception.ToolExceptionMessage;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class HttpExceptionHandlerImpl implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      if (Boolean.parseBoolean(AppSettings.get().get("aos.api.enable"))) {
        return invocation.proceed();
      } else {
        throw new ForbiddenException(I18n.get(ToolExceptionMessage.API_DISABLED));
      }
    } catch (BadRequestException e) {
      return ResponseConstructor.build(Response.Status.BAD_REQUEST, e.getMessage());
    } catch (ForbiddenException e) {
      return ResponseConstructor.build(Response.Status.FORBIDDEN, e.getMessage());
    } catch (NotFoundException e) {
      return ResponseConstructor.build(Response.Status.NOT_FOUND, e.getMessage());
    } catch (ClientErrorException e) {
      return ResponseConstructor.build(Response.Status.CONFLICT, e.getMessage());
    } catch (Exception e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
