package com.axelor.apps.base.openapi;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import io.swagger.v3.core.util.Json;
import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AosOpenApiResource {
  @Context ServletConfig config;

  @Context Application application;

  @GET
  @Path("/openapi")
  public Response getOpenApi() throws ForbiddenException {
    if (!Boolean.parseBoolean(AppSettings.get().get("aos.swagger.enable"))) {
      throw new ForbiddenException(I18n.get(BaseExceptionMessage.SWAGGER_DISABLED));
    }

    return Response.status(Response.Status.OK)
        .entity(Json.pretty(Beans.get(AosSwagger.class).initSwagger()))
        .build();
  }
}
