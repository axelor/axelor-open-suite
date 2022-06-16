package com.axelor.apps.tool.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResponseConstructor {

  public static Response build(
      Response.Status statusCode, String message, ResponseStructure object) {
    return Response.status(statusCode)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ResponseBody(statusCode, message, object))
        .build();
  }

  public static Response build(Response.Status statusCode, String message) {
    return Response.status(statusCode)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ResponseBody(statusCode, message))
        .build();
  }

  public static Response build(Response.Status statusCode, Object object) {
    return Response.status(statusCode).type(MediaType.APPLICATION_JSON).entity(object).build();
  }
}
