package com.axelor.apps.mobilesettings.rest;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.service.MobileChartResponseComputeService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/mobilechart")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MobileChartRestController {
  @Operation(
      summary = "Get mobile chart",
      tags = {"Mobile Chart"})
  @Path("/{mobileChartId}")
  @GET
  @HttpExceptionHandler
  public Response getMobileChart(@PathParam("mobileChartId") Long mobileChartId) {
    new SecurityCheck().writeAccess(MobileChart.class).createAccess(MobileChart.class).check();
    MobileChart mobileChart =
        ObjectFinder.find(MobileChart.class, mobileChartId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Response of the query of the chart",
        Beans.get(MobileChartResponseComputeService.class).computeMobileChartResponse(mobileChart));
  }
}
