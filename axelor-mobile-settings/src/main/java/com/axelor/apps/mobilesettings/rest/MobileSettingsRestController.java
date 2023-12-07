package com.axelor.apps.mobilesettings.rest;

import com.axelor.apps.mobilesettings.service.MobileSettingsResponseComputeService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppMobileSettings;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/mobilesettings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MobileSettingsRestController {
  @Operation(
      summary = "Get mobile settings",
      tags = {"Mobile Settings"})
  @GET
  @HttpExceptionHandler
  public Response getMobileSettings() {
    new SecurityCheck()
        .writeAccess(AppMobileSettings.class)
        .createAccess(AppMobileSettings.class)
        .check();

    return ResponseConstructor.build(
        Response.Status.OK,
        "Response of the query for settings",
        Beans.get(MobileSettingsResponseComputeService.class).computeMobileSettingsResponse());
  }
}
