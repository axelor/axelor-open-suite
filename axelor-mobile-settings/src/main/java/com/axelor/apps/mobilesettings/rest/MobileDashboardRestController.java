package com.axelor.apps.mobilesettings.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardResponse;
import com.axelor.apps.mobilesettings.service.MobileDashboardResponseComputeService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/mobiledashboard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MobileDashboardRestController {
  @Operation(
      summary = "Get mobile dashboard",
      tags = {"Mobile Dashboard"})
  @Path("/{mobileDashboardId}")
  @GET
  @HttpExceptionHandler
  public Response getMobileDashboard(@PathParam("mobileDashboardId") Long mobileDashboardId)
      throws AxelorException, JSONException {
    new SecurityCheck().writeAccess(MobileChart.class).createAccess(MobileChart.class).check();
    MobileDashboard mobileDashboard =
        ObjectFinder.find(MobileDashboard.class, mobileDashboardId, ObjectFinder.NO_VERSION);

    Optional<MobileDashboardResponse> response =
        Beans.get(MobileDashboardResponseComputeService.class)
            .computeMobileDashboardResponse(mobileDashboard);

    if (response.isEmpty()) {
      return ResponseConstructor.build(
          Response.Status.FORBIDDEN, "You do not have access to this record");
    }

    return ResponseConstructor.build(
        Response.Status.OK, "Response of the query of the chart", response);
  }
}
