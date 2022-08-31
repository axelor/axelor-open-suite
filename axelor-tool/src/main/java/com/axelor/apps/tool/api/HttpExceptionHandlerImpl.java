package com.axelor.apps.tool.api;

import com.axelor.exception.service.TraceBackService;
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
      return invocation.proceed();
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
